package uk.ac.warwick.dcs.sherlock.engine.executor.common;

import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Scheduler which does work in priority order
 */
public class PriorityWorkScheduler {

    private final PriorityBlockingQueue<PriorityWorkTask> priorityQueue;
    private final ForkJoinPool priorityWorkForkPool;
    private final ExecutorService priorityWorkScheduler;

    public PriorityWorkScheduler() {
        this(10);
    }

    public PriorityWorkScheduler(Integer queueSize) {
        this.priorityWorkForkPool = ForkJoinPool.commonPool();
        this.priorityWorkScheduler = Executors.newSingleThreadExecutor();

        this.priorityQueue = new PriorityBlockingQueue<>(queueSize, Comparator.comparing(PriorityWorkTask::getPriority));

        this.priorityWorkScheduler.execute(() -> {
            while (true) {
                try {
                    PriorityWorkTask nextTask = this.priorityQueue.take();

                    synchronized (nextTask) {
                        this.priorityWorkForkPool.execute(nextTask.getTopAction());
                        nextTask.getTopAction().join();
                        nextTask.notifyAll();
                    }

                } catch (InterruptedException e) {
                    break;
                }
            }
        });
    }

    public void scheduleJob(PriorityWorkTask work) {
        priorityQueue.add(work);
    }

    public void shutdown() {
        this.priorityWorkForkPool.shutdown();
        this.priorityWorkScheduler.shutdownNow();
    }

}
