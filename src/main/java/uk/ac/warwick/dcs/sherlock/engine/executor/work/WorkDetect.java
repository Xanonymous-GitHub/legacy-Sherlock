package uk.ac.warwick.dcs.sherlock.engine.executor.work;

import uk.ac.warwick.dcs.sherlock.api.model.detection.DetectorWorker;
import uk.ac.warwick.dcs.sherlock.api.model.postprocessing.AbstractModelTaskRawResult;
import uk.ac.warwick.dcs.sherlock.engine.executor.JobStatus;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * Recursive task to run detectors
 */
public class WorkDetect extends RecursiveTask<List<AbstractModelTaskRawResult>> {

    private final JobStatus status;

    private final List<DetectorWorker> workers;
    private final int threshold;
    private final int begin;
    private final int end;

    private List<AbstractModelTaskRawResult> result;

    public WorkDetect(JobStatus jobStatus, List<DetectorWorker> workers, int threshold) {
        this(jobStatus, workers, threshold, 0, workers.size());
        this.result = Collections.EMPTY_LIST;
    }

    private WorkDetect(JobStatus jobStatus, List<DetectorWorker> workers, int threshold, int begin, int end) {
        this.status = jobStatus;

        this.workers = workers;
        this.threshold = threshold;
        this.begin = begin;
        this.end = end;
        this.result = null;
    }

    private static AbstractModelTaskRawResult runWorker(DetectorWorker worker) {
        worker.execute();
        return worker.getRawResult();
    }

    public List<AbstractModelTaskRawResult> getResults() {
        return this.result;
    }

    @Override
    protected List<AbstractModelTaskRawResult> compute() {
        List<AbstractModelTaskRawResult> res;
        int size = this.end - this.begin;

        if (size > this.threshold) {
            int middle = this.begin + (size / 2);
            WorkDetect t1 = new WorkDetect(this.status, this.workers, this.threshold, this.begin, middle);
            t1.fork();
            WorkDetect t2 = new WorkDetect(this.status, this.workers, this.threshold, middle, this.end);

            res = t2.compute();
            res.addAll(t1.join());
        } else {
            res = new LinkedList<>();

            for (int i = this.begin; i < this.end; i++) {
                AbstractModelTaskRawResult raw = runWorker(this.workers.get(i));
                if (raw != null) {
                    res.add(raw);
                }
                this.status.incrementProgress();
            }
        }

        if (this.result != null) {
            this.result = res;
        }

        return res;
    }
}
