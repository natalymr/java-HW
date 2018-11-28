package pool;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class LightFutureImpl<T> implements LightFuture<T> {

    private final ThreadPoolImpl pool;
    private final Supplier<T> supplier;

    private T result = null;
    private Throwable error;

    private volatile boolean isReady;
    private boolean computedSuccessfully;

    private List<LightFutureImpl> thenApplyLFI = new ArrayList<>();

    LightFutureImpl(Supplier<T> supplier, ThreadPoolImpl pool) {
        this.pool = pool;
        this.supplier = supplier;
        isReady = false;
        computedSuccessfully = false;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }


    @Override
    public synchronized T get() throws LightExecutionException, InterruptedException {
        while (!isReady) {
            wait();
        }

        if (computedSuccessfully) {
            return result;
        } else {
            throw new LightExecutionException(error);
        }
    }

    public void compute() {
        try {
            result = supplier.get();

            computedSuccessfully = true;

        } catch (RuntimeException e) {
            error = e;

            computedSuccessfully = false;
        }

        isReady = true; // volatile
        synchronized (this) {
            notify();
        }

        for (LightFutureImpl task : thenApplyLFI) {
            pool.addToQueue(task);
        }
    }

    @Override
    public synchronized <V> LightFuture<V> thenApply(Function<T, V> function) {

        if (isReady) {
            if (computedSuccessfully) {
                return pool.submit(() -> function.apply(result));
            } else {
                return pool.submit(() -> { throw new RuntimeException(error); });
            }
        } else {

            LightFutureImpl<V> newLFI = new LightFutureImpl<>(() -> function.apply(result), pool);
            thenApplyLFI.add(newLFI);

            return newLFI;
        }
    }
}
