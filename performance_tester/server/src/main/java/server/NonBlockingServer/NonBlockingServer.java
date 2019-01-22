package server.NonBlockingServer;

import com.google.protobuf.InvalidProtocolBufferException;
import protobuf.IntArray;
import server.ServerBase;
import statistics.StatisticsPerClient;
import statistics.StatisticsPerIteration;
import statistics.TimeStamp;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static sorting.BubbleSort.sort;

public class NonBlockingServer extends ServerBase {
    private final ExecutorService pool;
    private final ServerSocketChannel nonBlockingServerSocket;
    private final Queue<SocketChannelAndAttachingDataRead> sharedQueueRead;
    private final Queue<SocketChannelAndAttachingDataWrite> sharedQueueWrite;
    private final Selector selectorReader;
    private final Selector selectorWriter;

    public NonBlockingServer(InetAddress inetAddress, short port, int threadPoolSize) throws IOException {
        super(inetAddress, port);
        nonBlockingServerSocket = ServerSocketChannel.open().bind(new InetSocketAddress(port));
        pool = Executors.newFixedThreadPool(threadPoolSize);
        selectorReader = Selector.open();
        selectorWriter = Selector.open();
        sharedQueueRead = new ConcurrentLinkedQueue<>();
        sharedQueueWrite = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {

        new Thread(new Reader(selectorReader, selectorWriter, pool, sharedQueueRead, sharedQueueWrite)).start();
        new Thread(new Writer(selectorWriter, sharedQueueWrite)).start();

        while (!Thread.interrupted()) {
            try {
                SocketChannel newSocketChannel = nonBlockingServerSocket.accept();
                short clientID = (short) newSocketChannel.socket().getPort();

                newSocketChannel.configureBlocking(false);

                sharedQueueRead.add(
                    new SocketChannelAndAttachingDataRead(
                        newSocketChannel,
                        new AttachingDataRead(
                            new StatisticsPerClient(clientID), // локальная статистика
                            statisticsPerIteration))           // глобальная статистика
                );

                selectorReader.wakeup();
            } catch (IOException ignored) {
                break;
            }
        }
    }

    @Override
    public void interrupt() throws IOException {
        nonBlockingServerSocket.close();
        selectorReader.close();
        selectorWriter.close();
        pool.shutdown();
    }
}

class Reader implements Runnable {
    private final Selector selectorReader;
    private final Selector selectorWriter;
    private final Queue<SocketChannelAndAttachingDataRead> sharedQueueRead;
    private final Queue<SocketChannelAndAttachingDataWrite> sharedQueueWrite;
    private final ExecutorService pool;

    Reader(Selector selectorReader, Selector selectorWriter, ExecutorService pool,
           Queue<SocketChannelAndAttachingDataRead> sharedQueueRead,
           Queue<SocketChannelAndAttachingDataWrite> sharedQueueWrite) {
        this.selectorReader = selectorReader;
        this.selectorWriter = selectorWriter;
        this.pool = pool;
        this.sharedQueueRead = sharedQueueRead;
        this.sharedQueueWrite = sharedQueueWrite;
    }

    @Override
    public void run() {
        while (selectorReader.isOpen()) {
            try {
                while (!sharedQueueRead.isEmpty()) {
                    SocketChannelAndAttachingDataRead channelsAndAttachment = sharedQueueRead.poll();

                    channelsAndAttachment.getSocketChannel().register(
                        selectorReader,
                        SelectionKey.OP_READ,
                        channelsAndAttachment.getAttachingDataRead()
                    );
                }

                int toRead = selectorReader.select();
                if (toRead == 0) {
                    continue;
                }

                Iterator<SelectionKey> selectionKeyIterator = selectorReader.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();

                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    AttachingDataRead attachingDataRead = (AttachingDataRead) selectionKey.attachment();

                    // установим время начала обработки, если еще до этого не установили
                    TimeStamp currentTimeStamp;
                    if (attachingDataRead.getCurrentTimeStamp() == null) {
                        currentTimeStamp = new TimeStamp();
                        attachingDataRead.setCurrentTimeStamp(currentTimeStamp);
                    } else {
                        currentTimeStamp = attachingDataRead.getCurrentTimeStamp();
                    }

                    if (currentTimeStamp.getStartRequest() == 0) {
                        currentTimeStamp.setStartRequest(System.currentTimeMillis());
                    }

                    int bytesRead;

                    try {
                        if (attachingDataRead.isSizeReady()) {
                            // тогда считываем массив
                            ByteBuffer arrayBuffer = attachingDataRead.getByteBufferForArray();
                            bytesRead = socketChannel.read(arrayBuffer);

                            if (arrayBuffer.position() == arrayBuffer.limit()) {
                                List<Integer> array = attachingDataRead.setArrayReady();
                                List<Integer> sortedArray = new ArrayList<>(array);


                                pool.execute(() -> {
                                    // не забудем записать время
                                    currentTimeStamp.setStartSort(System.currentTimeMillis());
                                    sort(sortedArray);
                                    currentTimeStamp.setEndSort(System.currentTimeMillis());

                                    sharedQueueWrite.add(
                                        new SocketChannelAndAttachingDataWrite(
                                            socketChannel,
                                            new AttachingDataWrite(
                                                sortedArray,
                                                currentTimeStamp,
                                                attachingDataRead.getLocalStatistics(),
                                                attachingDataRead.getGlobalStatistic())
                                        )
                                    );

                                    selectorWriter.wakeup();
                                });

                            }
                        } else {
                            // считываем размер массива
                            ByteBuffer sizeBuffer = attachingDataRead.getByteBufferForSize();
                            bytesRead = socketChannel.read(sizeBuffer);

                            if (sizeBuffer.position() == sizeBuffer.limit()) {
                                attachingDataRead.setSizeReady();
                            }
                        }
                    } catch (IOException e) {
                        selectionKey.cancel();
                        selectionKeyIterator.remove();
                        // значит, больше запросов от клиента не будет

                        attachingDataRead
                            .getGlobalStatistic()
                            .addNewStatisticsPerClient(
                                attachingDataRead.getLocalStatistics()
                            );
                        continue;
                    }

                    if (bytesRead == -1) {
                        // значит, больше запросов от клиента не будет
                        attachingDataRead
                            .getGlobalStatistic()
                            .addNewStatisticsPerClient(
                                attachingDataRead.getLocalStatistics()
                            );
                        selectionKey.cancel();
                    }

                    selectionKeyIterator.remove();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class Writer implements Runnable {

    private final Selector selectorWriter;
    private final Queue<SocketChannelAndAttachingDataWrite> sharedQueueWrite;

    Writer(Selector selectorWriter, Queue<SocketChannelAndAttachingDataWrite> sharedQueueWrite) {
        this.selectorWriter = selectorWriter;
        this.sharedQueueWrite = sharedQueueWrite;
    }

    @Override
    public void run() {
        while (selectorWriter.isOpen()) {
            try {
                while (!sharedQueueWrite.isEmpty()) {
                    SocketChannelAndAttachingDataWrite channelsAndAttachment = sharedQueueWrite.poll();

                    channelsAndAttachment.getSocketChannel().register(
                        selectorWriter,
                        SelectionKey.OP_WRITE,
                        channelsAndAttachment.getAttachingDataWrite()
                    );
                }

                int toRead = selectorWriter.select();
                if (toRead == 0) {
                    continue;
                }

                Iterator<SelectionKey> selectionKeyIterator = selectorWriter.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();

                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    AttachingDataWrite attachingDataWrite = (AttachingDataWrite) selectionKey.attachment();

                    try {
                        if (attachingDataWrite.isSizeSend()) {
                            // тогда отправляем массив
                            ByteBuffer arrayBuffer = attachingDataWrite.getByteBufferForArray();
                            socketChannel.write(arrayBuffer);

                            if (arrayBuffer.position() == arrayBuffer.limit()) {
                                // запишем время окончания запроса
                                TimeStamp currentTimeStamp = attachingDataWrite.getCurrentTimeStamp();
                                currentTimeStamp.setEndRequest(System.currentTimeMillis());
                                attachingDataWrite.getLocalStatistics().addNewStamp(currentTimeStamp);

                                selectionKey.cancel();
                            }
                        } else {
                            // отправляем размер массива
                            ByteBuffer sizeBuffer = attachingDataWrite.getByteBufferForSize();
                            socketChannel.write(sizeBuffer);

                            if (sizeBuffer.position() == sizeBuffer.limit()) {
                                attachingDataWrite.setSizeIsSend();
                            }
                        }
                    } catch (IOException e) {
                        selectionKey.cancel();
                    }

                    selectionKeyIterator.remove();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class AttachingDataRead {
    private final StatisticsPerClient localStatistics;
    private final StatisticsPerIteration globalStatistics;
    private TimeStamp currentTimeStamp;

    private final ByteBuffer arraySize;
    private ByteBuffer array;
    private boolean isSizeReady;

    AttachingDataRead(StatisticsPerClient localStatistics, StatisticsPerIteration globalStatistics) {
        this.localStatistics = localStatistics;
        this.globalStatistics = globalStatistics;

        isSizeReady = false;
        arraySize = ByteBuffer.allocate(4);
    }

    StatisticsPerClient getLocalStatistics() {
        return localStatistics;
    }

    StatisticsPerIteration getGlobalStatistic() {
        return globalStatistics;
    }

    TimeStamp getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    void  setCurrentTimeStamp(TimeStamp timeStamp) {
        currentTimeStamp = timeStamp;
    }

    ByteBuffer getByteBufferForSize() {
        return arraySize;
    }

    boolean isSizeReady() {
        return isSizeReady;
    }

    void setSizeReady() {
        isSizeReady = true;

        arraySize.flip();
        int size = arraySize.getInt();
        array = ByteBuffer.allocate(size);
    }

    ByteBuffer getByteBufferForArray() {
        return array;
    }

    List<Integer> setArrayReady() throws InvalidProtocolBufferException {

        // get an answer in protobuf mode
        array.flip();
        byte[] bytes = new byte[array.remaining()];
        array.get(bytes);
        IntArray result = IntArray.parseFrom(bytes);

        reset();

        return result.getArrayList();
    }

    private void reset() {
        arraySize.clear();
        isSizeReady = false;

        array.clear();
        array = null;
    }
}

class SocketChannelAndAttachingDataRead {
    private final SocketChannel socketChannel;
    private final AttachingDataRead attachingDataRead;

    SocketChannelAndAttachingDataRead(SocketChannel socketChannel, AttachingDataRead attachingDataRead) {
        this.socketChannel = socketChannel;
        this.attachingDataRead = attachingDataRead;
    }

    SocketChannel getSocketChannel() {
        return socketChannel;
    }

    AttachingDataRead getAttachingDataRead() {
        return attachingDataRead;
    }
}

class AttachingDataWrite {
    private final StatisticsPerClient localStatistics;
    private final StatisticsPerIteration globalStatistics;
    private TimeStamp currentTimeStamp;

    private final List<Integer> sortedArray;
    private final ByteBuffer arraySize = ByteBuffer.allocate(4);
    private final ByteBuffer array;
    private boolean isSizeSend = false;

    AttachingDataWrite(List<Integer> sortedArray,
                       TimeStamp currentTimeStamp, StatisticsPerClient localStatistics, StatisticsPerIteration globalStatistic) {
        this.localStatistics = localStatistics;
        this.globalStatistics = globalStatistic;
        this.currentTimeStamp = currentTimeStamp;
        this.sortedArray = sortedArray;

        // convert array in protobuf mode
        IntArray protobufArray = IntArray.newBuilder().addAllArray(sortedArray).build();

        arraySize.putInt(protobufArray.getSerializedSize());
        arraySize.flip();

        // allocate needed bytes
        array = ByteBuffer.allocate(protobufArray.getSerializedSize());
        // write to ByteBuffer
        array.put(protobufArray.toByteArray());
        array.flip();
    }

    StatisticsPerClient getLocalStatistics() {
        return localStatistics;
    }

    TimeStamp getCurrentTimeStamp() {
        return currentTimeStamp;
    }

    boolean isSizeSend() {
        return isSizeSend;
    }

    ByteBuffer getByteBufferForArray() {
        return array;
    }

    ByteBuffer getByteBufferForSize() {
        return arraySize;
    }

    void setSizeIsSend() {
        isSizeSend = true;
    }
}

class SocketChannelAndAttachingDataWrite {
    private final SocketChannel socketChannel;
    private final AttachingDataWrite attachingDataWrite;

    SocketChannelAndAttachingDataWrite(SocketChannel socketChannel, AttachingDataWrite attachingDataWrite) {
        this.socketChannel = socketChannel;
        this.attachingDataWrite = attachingDataWrite;
    }

    SocketChannel getSocketChannel() {
        return socketChannel;
    }

    AttachingDataWrite getAttachingDataWrite() {
        return attachingDataWrite;
    }
}