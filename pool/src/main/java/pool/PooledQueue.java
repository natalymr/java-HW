package pool;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

public class PooledQueue<T> {
    private final Queue<T> data;


    PooledQueue() {
        data = new ArrayDeque<>();
    }


    public synchronized T get() throws InterruptedException, NoSuchElementException {
        while (data.isEmpty()) {
            wait();
        }

        return data.remove();
    }


    public synchronized void add(T object) {
        data.add(object);

        notify();
    }
}
