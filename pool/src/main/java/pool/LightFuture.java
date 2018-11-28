package pool;

import java.util.function.Function;

public interface LightFuture<T> {

    /**
     *
     * @return возвращает true, если задача выполнена
     */
    boolean isReady();

    /**
     *
     * @return возвращает результат выполнения задачи
     * @throws LightExecutionException, InterruptedException
     */
    T get() throws LightExecutionException, InterruptedException;

    /**
     *
     * @param function  может быть применена к результату данной задачи X и возвращает новую задачу Y, принятую к исполнению
     * @return
     */
    //LightFuture<T> thenApply(Function<T, T> function) throws InterruptedException;
    <V> LightFuture<V> thenApply(Function<T, V> function) throws InterruptedException;
}
