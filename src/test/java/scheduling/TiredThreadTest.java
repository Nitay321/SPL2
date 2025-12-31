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
    void setWorker() {
        worker = new TiredThread(WORKER_ID, FATIGUE_FACTOR);
        worker.start();
    }

    @AfterEach
    void killWorker() {
        if (worker.isAlive()) {
            worker.shutdown();
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
            }
        }
    }

    @Test
    void checkWorkerAttributes() {
        assertEquals(WORKER_ID, worker.getWorkerId());
        assertEquals(0, worker.getTimeUsed());
        assertEquals(0, worker.getFatigue());
    }

    @Test
    void checkTaskForWorker() throws InterruptedException {
        Object monitor = new Object();
        AtomicBoolean finished = new AtomicBoolean(false);

        worker.newTask(() -> {
            synchronized (monitor) {
                finished.set(true);
                monitor.notifyAll();
            }
        });

        synchronized (monitor) {
            while (!finished.get()) {
                monitor.wait(1000);
            }
        }

        assertTrue(finished.get());
    }

    @Test
    void checkTimeAndFatigue() throws InterruptedException {
        long sleep_time = 100;
        Object monitor = new Object();
        AtomicBoolean finished = new AtomicBoolean(false);

        worker.newTask(() -> {
            try {
                Thread.sleep(sleep_time);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                synchronized (monitor) {
                    finished.set(true);
                    monitor.notifyAll();
                }
            }
        });

        synchronized (monitor) {
            while (!finished.get()) {
                monitor.wait(1000);
            }
        }

        Thread.sleep(50);

        assertTrue(worker.getTimeUsed() >= sleep_time * 1_000_000);
        assertTrue(worker.getFatigue() > 0);
    }

    @Test
    void checkFullQueue() throws InterruptedException {
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
    void checkWorkerStops() throws InterruptedException {
        worker.shutdown();
        worker.join(1000);
        assertFalse(worker.isAlive());
    }
}