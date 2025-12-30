package scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TiredExecutorTest {

    private TiredExecutor executor;
    private final int NUM_THREADS = 5;

    @BeforeEach
    void setExecutor() {
        executor = new TiredExecutor(NUM_THREADS);
    }

    @AfterEach
    void killExecutor() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Test
    void checkAllTasksRun() {
        int total_tasks = 20;
        AtomicInteger count = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < total_tasks; i++) {
            tasks.add(() -> count.incrementAndGet());
        }

        executor.submitAll(tasks);

        assertEquals(total_tasks, count.get());
    }

    @Test
    void checkConcurrency() {
        int total_tasks = 100;
        AtomicInteger count = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < total_tasks; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(10); 
                } catch (InterruptedException e) {
\                }
                count.incrementAndGet();
            });
        }

        executor.submitAll(tasks);

        assertEquals(total_tasks, count.get());
    }

    @Test
    void checkReport() {
        List<Runnable> tasks = new ArrayList<>();
        tasks.add(() -> {});
        executor.submitAll(tasks);

        String report = executor.getWorkerReport();
        assertNotNull(report);
        
        assertTrue(report.contains("Worker"));
        assertTrue(report.contains("id"));
        assertTrue(report.contains("fatigue")); 
        assertTrue(report.contains("time used")); 
        assertTrue(report.contains("time idle")); 
    }
}