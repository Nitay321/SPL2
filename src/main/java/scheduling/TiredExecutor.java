package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        // TODO
        workers = new TiredThread[numThreads];
        for(int i = 0; i<numThreads; i++){
            double fatigue_factor = 0.5 + Math.random();
            workers[i] = new TiredThread(i, fatigue_factor);
            idleMinHeap.add(workers[i]);

            workers[i].start();
        }
    }

    public void submit(Runnable task) {
        // TODO
       try{
            TiredThread worker = idleMinHeap.take();
            
            inFlight.incrementAndGet();

            
            Runnable wrapper = () -> {
                try{
                    task.run();
                }
                finally{
                    idleMinHeap.add(worker);
                    inFlight.decrementAndGet();

                    synchronized(inFlight) {
                        inFlight.notifyAll(); 
                    }
                }
            };
            try {
                worker.newTask(wrapper); 
            } 
            catch (Exception e) {
                inFlight.decrementAndGet();
                synchronized (inFlight) { inFlight.notifyAll(); }
                throw new RuntimeException("Worker rejected task", e);
            }
            
        }
        catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }

    }

    public void submitAll(Iterable<Runnable> tasks) {
        // TODO: submit tasks one by one and wait until all finish
        for(Runnable task: tasks){
            submit(task);         
        }
        synchronized(inFlight) {
            while (inFlight.get() > 0) {
                try {
                    inFlight.wait(); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        // TODO
        for(TiredThread worker: workers){
            worker.shutdown();
        }
        for(TiredThread worker: workers){
            worker.join();
        }
    }

    public synchronized String getWorkerReport() {
        // TODO: return readable statistics for each worker
        String ans = "";
        int i = 0;
        for(TiredThread worker: workers){
            i++;
            ans +=  "Worker " + i + ": id " + worker.getWorkerId() + ", fatige " +
             worker.getFatigue() + ", time used " + worker.getTimeUsed() + ", time idle " + worker.getTimeIdle() + "\n";
        }
        return ans;
    } 
}
