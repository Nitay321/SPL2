package scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class TiredThreadTest {

    private TiredThread worker;
    private final int WORKER_ID = 1;
    private final double FATIGUE_FACTOR = 1.0;

    @BeforeEach
    void setUp() {
        worker = new TiredThread(WORKER_ID, FATIGUE_FACTOR);
        worker.start();
    }

    @AfterEach
    void tearDown() {
        if (worker.isAlive()) {
            worker.shutdown();
            try {
                worker.join(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Test
    void testInitialization() {
        assertEquals(WORKER_ID, worker.getWorkerId());
        assertEquals(0, worker.getTimeUsed());
        assertEquals(0, worker.getFatigue());
    }

    @Test
    void testTaskExecution() throws InterruptedException {
        Object monitor = new Object();
        AtomicBoolean isDone = new AtomicBoolean(false);

        worker.newTask(() -> {
            synchronized (monitor) {
                isDone.set(true);
                monitor.notifyAll();
            }
        });

        synchronized (monitor) {
            while (!isDone.get()) {
                monitor.wait(2000);
            }
        }

        assertTrue(isDone.get());
    }

    @Test
    void testMetricsUpdate() throws InterruptedException {
        long sleepTime = 50;
        Object monitor = new Object();
        AtomicBoolean isDone = new AtomicBoolean(false);

        worker.newTask(() -> {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                synchronized (monitor) {
                    isDone.set(true);
                    monitor.notifyAll();
                }
            }
        });

        synchronized (monitor) {
            while (!isDone.get()) {
                monitor.wait(2000);
            }
        }

        Thread.sleep(10);

        assertTrue(worker.getTimeUsed() >= sleepTime * 1_000_000);
        assertTrue(worker.getFatigue() > 0);
    }

    @Test
    void testBusyStateAndQueueCapacity() throws InterruptedException {
        Object monitor = new Object();
        
        worker.newTask(() -> {
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread.sleep(50);
        assertTrue(worker.isBusy());

        worker.newTask(() -> {});

        assertThrows(IllegalStateException.class, () -> worker.newTask(() -> {}));

        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    @Test
    void testShutdown() throws InterruptedException {
        worker.shutdown();
        worker.join(2000);
        assertFalse(worker.isAlive());
    }
}