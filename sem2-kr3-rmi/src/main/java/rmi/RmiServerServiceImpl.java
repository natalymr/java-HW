package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class RmiServerServiceImpl implements RmiServerService{

    private Map<Object, Object> instances;

    public RmiServerServiceImpl() {
        instances = new HashMap<>();
    }

    @Override
    public void launch(short port) {
        System.out.println("l");

        ExecutorService executor = Executors.newCachedThreadPool();

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("serv");
            while (true) {
                System.out.println("while");
                try(Socket client = serverSocket.accept()) {
                    System.out.println("serv: connection");
                    ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                    ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                    switch (input.readByte()) {
                        case 1:
                            System.out.println("#1");

                            // input
                            String clazzName = input.readUTF();
                            String methodName = input.readUTF();

                            Class<?> clazz = Class.forName(clazzName);
                            Method method = clazz.getMethod(methodName);
                            int paramsCount = input.readInt();
                            Object[] params = new Object[paramsCount];
                            for (int i = 0; i < paramsCount; i++) {
                                params[i] = input.readObject();
                            }

                            // execute
                            Future<Object> future1 = executor.submit(
                                    new executeInvokeStatic<>(
                                            clazz,
                                            method,
                                            params
                                    )
                            );
                            Object result = future1.get();

                            // output
                            output.writeObject(result);

                            break;
                        case 2:
                            System.out.println("#2");

                            // input
                            Object clientThis = input.readObject();
                            Class<?> clazz2 = clientThis.getClass();
                            String consName = input.readUTF();

                            int paramsCount2 = input.readInt();
                            Object[] params2 = new Object[paramsCount2];
                            Class<?>[] paramsTypes = new Class[paramsCount2];
                            for (int i = 0; i < paramsCount2; i++) {
                                params2[i] = input.readObject();
                                paramsTypes[i] = params2[i].getClass();
                            }

                            Constructor<?> constructor = (Constructor) clazz2.getConstructor(paramsTypes);

                            // execute
                            Future<Object> future2 = executor.submit(
                                    new executeInit<Object>(
                                            constructor,
                                            params2
                                    )

                            );
                            Object result2 = future2.get();
                            instances.put(clientThis, result2);

                            // output
                            //output.writeObject(result2);

                            break;
                        case 3:
                            System.out.println("#3");

                            System.out.println("do not implement");
                            break;
                    }
                } catch (ClassNotFoundException | NoSuchMethodException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }

    }
}

class executeInvokeStatic<V> implements Callable<V> {

    Class<?> clazz;
    Method method;
    V[] params;

    executeInvokeStatic(Class<?> clazz, Method method, V... params) {
        this.clazz = clazz;
        this.method = method;
        this.params = params;
    }

    @Override
    public V call() throws Exception {
        Object object;
        try {
            object = (Object) clazz.newInstance();
            return (V) method.invoke(object, params);

        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return (V) new Object();
    }
}

class executeInit<V> implements Callable<V> {

    Constructor constructor;
    V[] params;

    executeInit(Constructor constructor, V... params) {
        this.constructor = constructor;
        this.params = params;
    }

    @Override
    public V call() throws Exception {
        return (V) constructor.newInstance(params);
    }
}