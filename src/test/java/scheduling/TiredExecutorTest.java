package scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TiredExecutorTest {

    private TiredExecutor executor;
    private final int NUM_THREADS = 4;

    @BeforeEach
    void setUp() {
        executor = new TiredExecutor(NUM_THREADS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Test
    void testSubmitAllBlocksUntilCompletion() {
        int numTasks = 20;
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < numTasks; i++) {
            tasks.add(counter::incrementAndGet);
        }

        executor.submitAll(tasks);

        assertEquals(numTasks, counter.get());
    }

    @Test
    void testConcurrencyAndFairness() {
        int numTasks = 100;
        AtomicInteger counter = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < numTasks; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(1); 
                } catch (InterruptedException ignored) {}
                counter.incrementAndGet();
            });
        }

        executor.submitAll(tasks);

        assertEquals(numTasks, counter.get());
    }

    @Test
    void testWorkerReport() {
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {});
        executor.submitAll(tasks);

        String report = executor.getWorkerReport();
        assertNotNull(report);
        assertTrue(report.contains("Worker"));
        assertTrue(report.contains("id"));
        assertTrue(report.contains("fatige"));
    }
}