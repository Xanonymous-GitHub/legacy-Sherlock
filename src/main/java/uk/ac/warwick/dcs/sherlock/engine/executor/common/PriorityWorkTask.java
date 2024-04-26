package uk.ac.warwick.dcs.sherlock.engine.executor.common;

import java.util.concurrent.ForkJoinTask;

/**
 * Work task wrapper object
 */
public class PriorityWorkTask {

    private final ForkJoinTask topAction;
    private final Priority priority;

    public PriorityWorkTask(ForkJoinTask topAction, Priority priority) {
        this.topAction = topAction;
        this.priority = priority;
    }

    Priority getPriority() {
        return priority;
    }

    ForkJoinTask getTopAction() {
        return topAction;
    }
}
