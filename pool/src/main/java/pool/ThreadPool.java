package pool;

import java.util.function.Supplier;

interface ThreadPool {

    <T> LightFuture<T> submit(Supplier<T> supplier);

    void shutdown();
}
