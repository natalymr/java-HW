package testRmi;

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

public class RmiServerServiceImpl implements RmiServerService {

    private Map<Integer, Object> instances; // client vs server

    RmiServerServiceImpl() {
        instances = new HashMap<>();
    }

    @Override
    public void launch(short port) {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                    switch (input.readByte()) {
                        case RmiClientServiceImpl.INVOKE_STATIC: {
                            System.out.println("#1");

                            // input
                            String clazzName = input.readUTF();
                            String methodName = input.readUTF();

                            Class<?> clazz = Class.forName(clazzName);

                            Object[] params = readParams(input);
                            Class<?>[] paramClasses = getParamsClasses(params);

                            Method method = clazz.getMethod(methodName, paramClasses);

                            // execute & output
                            executeMethod(output, null, method, params);

                            break;
                        }

                        case RmiClientServiceImpl.INIT: {
                            System.out.println("#2");

                            // input
                            int hashOfClientObject = input.readInt();
                            String clazzName = input.readUTF();
                            Class<?> clazz = Class.forName(clazzName);

                            Object[] params = readParams(input);
                            Class<?>[] paramClasses = getParamsClasses(params);

                            Constructor constructor = clazz.getDeclaredConstructor(paramClasses);

                            // execute & output
                            executeConstructor(output, hashOfClientObject, params, constructor);

                            break;
                        }

                        case RmiClientServiceImpl.INVOKE_ON: {

                            System.out.println("#3");

                            // input
                            int hashOfClientObject = input.readInt();
                            Object serverObject = instances.get(hashOfClientObject);

                            String clazzName = input.readUTF();
                            Class<?> clazz = Class.forName(clazzName);

                            String methodName = input.readUTF();

                            Object[] params = readParams(input);
                            Class<?>[] paramClasses = getParamsClasses(params);

                            Method method = clazz.getDeclaredMethod(methodName, paramClasses);

                            // execute & output
                            executeMethod(output, serverObject, method, params);

                            break;
                        }
                    }
                } catch (ClassNotFoundException | NoSuchMethodException
                        | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeConstructor(ObjectOutputStream output, int hashOfClientObject, Object[] params, Constructor constructor) throws InstantiationException, IllegalAccessException, IOException {
        try {
            // execute
            Object serverObject = constructor.newInstance(params);
            instances.put(hashOfClientObject, serverObject);
            // write to output
            output.writeBoolean(true);
        } catch (InvocationTargetException e) {
            // write to output
            output.writeBoolean(false);
            output.writeObject(e.getCause());
        }

        output.flush();
    }

    private void executeMethod(ObjectOutputStream output, Object serverObject, Method method, Object[] params) throws IllegalAccessException, IOException {
        try {
            // execute
            Object result = method.invoke(serverObject, params);
            // write to output
            output.writeBoolean(true);
            output.writeObject(result);
        } catch (InvocationTargetException e) {
            // write to output
            output.writeBoolean(false);
            output.writeObject(e.getCause());
        }
        output.flush();
    }

    private Class<?>[] getParamsClasses(Object[] params) {
        Class<?>[] paramClasses = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramClasses[i] = params[i].getClass();
        }

        return paramClasses;
    }

    private Object[] readParams(ObjectInputStream input) throws IOException, ClassNotFoundException {
        int paramsCount = input.readInt();
        Object[] params = new Object[paramsCount];
        for (int i = 0; i < paramsCount; i++) {
            params[i] = input.readObject();
        }
        return params;
    }
}