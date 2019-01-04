package testRmi;

import org.junit.Assert;
import org.junit.Test;

import java.net.UnknownHostException;

class Tested {
    private final int forTests;

    Tested() {
        System.out.println("No Args in Constructor");
        forTests = 42;
    }

    Tested(Integer forTests) {
        System.out.println("Integer");
        this.forTests = forTests;
    }

    Tested(int forTests) {
        System.out.println("int");
        this.forTests = forTests;
    }

    void add(Integer other) {
        int result = forTests + other;
        System.out.println("result of add is " + result);
    }

    void methodWithExceptions() {
        throw new IllegalArgumentException("I was thrown in Tested class. This is OK.");
    }
}


public class RmiServerServiceImplTest {


    @Test
    public void static_test() throws InterruptedException, UnknownHostException, NoSuchMethodException {
        new Thread(() -> {
            new RmiServerServiceImpl().launch((short) 1027);
        }).start();

        Thread.sleep(1000);

        RmiClientServiceImpl rmiClientService = new RmiClientServiceImpl("localhost", (short) 1027);

        Object hello = rmiClientService.invokeStatic(
                RmiServerServiceImplTest.class,
                RmiServerServiceImplTest.class.getDeclaredMethod("invoke", String.class, String.class),
                "Hello", "World"
        );

        Assert.assertEquals("HelloWorld", hello);
    }

    @Test
    public void init_test() throws InterruptedException, NoSuchMethodException {
        new Thread(() -> {
            new RmiServerServiceImpl().launch((short) 1028);
        }).start();

        Thread.sleep(1000);

        RmiClientServiceImpl rmiClientService = new RmiClientServiceImpl("localhost", (short) 1028);

        Tested tested = new Tested();

        rmiClientService.init(
                tested,
                tested.getClass().getDeclaredConstructor()
        );

    }

    @Test
    public void init_const_with_int_test() throws InterruptedException, NoSuchMethodException {
        new Thread(() -> {
            new RmiServerServiceImpl().launch((short) 1029);
        }).start();

        Thread.sleep(1000);

        RmiClientServiceImpl rmiClientService = new RmiClientServiceImpl("localhost", (short) 1029);

        Tested tested = new Tested(10);

        rmiClientService.init(
                tested,
                tested.getClass().getDeclaredConstructor(int.class),
                10
        );
    }

    @Test
    public void init_const_with_Integer_test() throws InterruptedException, NoSuchMethodException {
        new Thread(() -> {
            new RmiServerServiceImpl().launch((short) 1030);
        }).start();

        Thread.sleep(1000);

        RmiClientServiceImpl rmiClientService = new RmiClientServiceImpl("localhost", (short) 1030);

        Tested tested = new Tested(new Integer(10));

        rmiClientService.init(
                tested,
                tested.getClass().getDeclaredConstructor(Integer.class),
                10
        );
    }

    @Test
    public void invoke_on_with_different_instances() throws InterruptedException, NoSuchMethodException {
        new Thread(() -> {
            new RmiServerServiceImpl().launch((short) 1031);
        }).start();

        Thread.sleep(1000);

        RmiClientServiceImpl rmiClientService = new RmiClientServiceImpl("localhost", (short) 1031);

        // first instance
        Tested tested = new Tested(new Integer(10));
        rmiClientService.init(
                tested,
                tested.getClass().getDeclaredConstructor(Integer.class),
                10
        );
        rmiClientService.invokeOn(
                tested,
                tested.getClass().getDeclaredMethod("add", Integer.class),
                20
        );

        // second instance
        Tested testedS = new Tested(new Integer(5));
        rmiClientService.init(
                testedS,
                testedS.getClass().getDeclaredConstructor(Integer.class),
                5
        );
        rmiClientService.invokeOn(
                testedS,
                testedS.getClass().getDeclaredMethod("add", Integer.class),
                40
        );
    }

    @Test
    public void invoke_on_method_with_exception() throws InterruptedException, NoSuchMethodException {
        new Thread(() -> {
            new RmiServerServiceImpl().launch((short) 1032);
        }).start();

        Thread.sleep(1000);

        RmiClientServiceImpl rmiClientService = new RmiClientServiceImpl("localhost", (short) 1032);

        // first instance
        Tested tested = new Tested();
        rmiClientService.init(
                tested,
                tested.getClass().getDeclaredConstructor()
        );

        rmiClientService.invokeOn(
                tested,
                tested.getClass().getDeclaredMethod("methodWithExceptions")
        );
    }


    public static String invoke(String s, String ss) {
        return s + ss;
    }
}