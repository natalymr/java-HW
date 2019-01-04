package testRmi;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;

public class RmiClientServiceImpl implements RmiClientService {
    private final short serverPort;
    private final String host;

    static final byte INVOKE_STATIC = 1;
    static final byte INIT          = 2;
    static final byte INVOKE_ON     = 3;

    RmiClientServiceImpl(String host, short serverPort) {
        this.host = host;
        this.serverPort = serverPort;
    }

    @Override
    public Object invokeStatic(Class<?> clazz, Method method, Object... params) {

        try(Socket socket = new Socket(host, serverPort)) {

            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            // send
            output.writeByte(INVOKE_STATIC);

            output.writeUTF(clazz.getName());

            output.writeUTF(method.getName());

            output.writeInt(params.length);
            for (Object param : params) {
                output.writeObject(param);
            }
            output.flush();

            // receive
            boolean noException = input.readBoolean();
            if (noException) {
                return input.readObject();
            } else {
                throw readAndWrapException(input);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init(Object localReceiverReference, Constructor constructor, Object... params) {
        try(Socket socket = new Socket(host, serverPort)) {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            // send
            output.writeByte(INIT);

            int hashOfLocalReceiverReference = System.identityHashCode(localReceiverReference);
            output.writeInt(hashOfLocalReceiverReference);

            output.writeUTF(localReceiverReference.getClass().getName());

            output.writeInt(params.length);
            for (Object param : params) {
                output.writeObject(param);
            }
            output.flush();

            // receive
            boolean noException = input.readBoolean();
            if (!noException) {
                throw readAndWrapException(input);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object invokeOn(Object localReceiverReference, Method method, Object... params) {
        try(Socket socket = new Socket(host, serverPort)) {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            // send
            output.writeByte(INVOKE_ON);

            int hashOfLocalReceiverReference = System.identityHashCode(localReceiverReference);
            output.writeInt(hashOfLocalReceiverReference);

            output.writeUTF(localReceiverReference.getClass().getName());

            output.writeUTF(method.getName());

            output.writeInt(params.length);
            for (Object param : params) {
                output.writeObject(param);
            }

            output.flush();

            // receive
            boolean noException = input.readBoolean();
            if (noException) {
                return input.readObject();
            } else {
                throw readAndWrapException(input);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private RuntimeException readAndWrapException(ObjectInputStream input) throws IOException, ClassNotFoundException {
        Exception exception = (Exception) input.readObject();
        if (exception instanceof RuntimeException) {
            return (RuntimeException) exception;
        } else {
            return new RuntimeException(exception);
        }
    }
}
