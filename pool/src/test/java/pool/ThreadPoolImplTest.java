package pool;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.Assert.*;

public class ThreadPoolImplTest {

    private static final int nThreads = 4;
    private ThreadPoolImpl pool;

    @Before
    public void createPool() {
        pool = new ThreadPoolImpl(nThreads);
    }

    @After
    public void shutdownPool() {
        pool.shutdown();
    }

    @Test
    public void testNumberOfThreads() throws LightExecutionException, InterruptedException {

        Set<Long> threadsID = new HashSet<>();
        List<LightFuture<Long>> futures = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            futures.add(pool.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return Thread.currentThread().getId();
            }));
        }

        for (LightFuture<Long> future : futures) {
            threadsID.add(future.get());
        }

        assertEquals(nThreads, threadsID.size());
    }

    @Test
    public void testReturnedValue() throws LightExecutionException, InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        int delta = 1;
        int max = 100;

        List<LightFuture<Integer>> futures = new ArrayList<>();
        Set<Integer> results = new HashSet<>();

        for (int i = 0; i < max; i++) {
            futures.add(pool.submit(() -> counter.addAndGet(delta)));
        }

        for (int i = 0; i < max; i++) {
            int supplierResult = futures.get(i).get();

            results.add(supplierResult);
        }

        assertEquals(max, results.size());
    }

    @Test
    public void testExceptionsAreThrowed() {
        String errorMsg = "test";

        LightFuture exception = pool.submit(() -> {
            throw new RuntimeException(errorMsg);
        });

        try{
            exception.get();
        } catch (LightExecutionException e) {
            assertEquals("java.lang.RuntimeException: " + errorMsg,
                         e.getMessage());
        } catch (InterruptedException ignored) {}
    }

    @Test
    public void testIsReadyInOKCaseNotGet() {
        LightFuture<String> result = pool.submit(() -> "test");

        assertEquals(false, result.isReady());
    }

    @Test
    public void testIsReadyInOKCaseGet() {
        LightFuture<String> result = pool.submit(() -> "test");
        try {
            result.get();
        } catch (LightExecutionException | InterruptedException ignored) {}

        assertEquals(true, result.isReady());
    }

    @Test
    public void testIsReadyInEXCEPTIONCaseNotGet() {
        LightFuture result = pool.submit(() -> {
           throw new RuntimeException("test");
        });

        assertEquals(false, result.isReady());
    }

    @Test
    public void testIsReadyInEXCEPTIONCaseGet() {
        LightFuture result = pool.submit(() -> {
            throw new RuntimeException("test");
        });

        try {
            result.get();
        } catch (LightExecutionException | InterruptedException ignored) {}

        assertEquals(true, result.isReady());
    }

    @Test
    public void shutdown() {
        pool.shutdown();
    }
    
    @Test
    public void simpleThenApply() throws InterruptedException, LightExecutionException {
        LightFuture<String> task1 = pool.submit(() -> "test");
        Function<String, String> task2 = (string) -> string + " then Function";

        LightFuture<String> result1 = task1.thenApply(task2);
        LightFuture<String> result2 = result1.thenApply(task2);

        assertEquals("test", task1.get());
        assertEquals("test then Function", result1.get());
        assertEquals("test then Function then Function", result2.get());
    }
}