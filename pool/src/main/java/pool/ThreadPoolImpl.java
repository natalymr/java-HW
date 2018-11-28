package pool;

import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {
    private final PooledQueue<Runnable> queue;
    private final Thread[] threads;


    ThreadPoolImpl(int numOfThreads) {
        queue = new PooledQueue<>();
        threads = new Thread[numOfThreads];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                try {
                    while (true) {
                        Runnable task = queue.get();
                        task.run();
                    }
                } catch (InterruptedException ignored) {}
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
    }

    @Override
    public void shutdown() {
        for (Thread each : threads) {
            each.interrupt();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {}
        }
    }

    private static class PooledRunnable implements Runnable {

        private final LightFutureImpl future;

        PooledRunnable(LightFutureImpl future) {
            this.future = future;
        }

        @Override
        public void run() {
            future.compute();
        }

    }

    @Override
    public <T> LightFuture<T> submit(Supplier<T> supplier) {
        LightFutureImpl<T> future = new LightFutureImpl<>(supplier, this);

        queue.add(new PooledRunnable(future));

        return future;
    }

    <T> void addToQueue(LightFutureImpl<T> task) {
        queue.add(new PooledRunnable(task));
    }
}
