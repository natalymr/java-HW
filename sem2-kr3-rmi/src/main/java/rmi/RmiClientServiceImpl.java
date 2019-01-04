package rmi;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RmiClientServiceImpl implements RmiClientService {
    private Map<Object, Object> instanceClientsVSinstanceServer;
    private final short serverPort;
    private final InetAddress host;

    public RmiClientServiceImpl(InetAddress host, short serverPort) {
        this.host = host;
        this.serverPort = serverPort;
        instanceClientsVSinstanceServer = new HashMap<>();
    }

    @Override
    public Object invokeStatic(Class<?> clazz, Method method, Object... params) {

        System.out.println("inv client");
        try(Socket socket = new Socket(host.getHostAddress(), serverPort)) {

            System.out.println("connect");
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            System.out.println("client : write");
            output.writeByte(1);
            output.writeUTF(clazz.getName());
            output.writeUTF(method.getName());
            output.writeInt(params.length);
            for (Object param : params) {
                output.writeObject(param);
            }

            output.flush();
            System.out.println("client: end write");

            return input.readObject();


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void init(Object localReceiverReference, Constructor constructor, Object... params) {
        short serverPort = 80;
        try(Socket socket = new Socket("localhost", serverPort)) {
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());

            // send
            output.writeByte(2);
            output.writeObject(localReceiverReference);
            output.writeUTF(constructor.getName());
            output.writeInt(params.length);
            for (Object param : params) {
                output.writeObject(param);
            }

            // receive
            Object result = input.readObject();
            instanceClientsVSinstanceServer.put(localReceiverReference, result);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object invokeOn(Object localReceiverReference, Method method, Object... params) {
        return null;
    }
}
