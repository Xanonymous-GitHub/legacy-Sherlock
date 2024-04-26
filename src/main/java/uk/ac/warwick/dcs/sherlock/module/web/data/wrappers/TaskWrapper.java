package uk.ac.warwick.dcs.sherlock.module.web.data.wrappers;

import uk.ac.warwick.dcs.sherlock.api.annotation.AdjustableParameterObj;
import uk.ac.warwick.dcs.sherlock.api.component.ITask;
import uk.ac.warwick.dcs.sherlock.api.registry.SherlockRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The wrapper that manages the task
 */
public class TaskWrapper {
    /**
     * The task entity
     */
    private final ITask task;

    /**
     * The detector wrapper
     */
    private final EngineDetectorWrapper detectorWrapper;

    /**
     * Initialise the wrapper using the task supplied
     *
     * @param task the task to manage
     */
    public TaskWrapper(ITask task) {
        this.task = task;
        this.detectorWrapper = new EngineDetectorWrapper(this.task.getDetector());
    }

    /**
     * Get the display name of the detector
     *
     * @return the name
     */
    public String getDisplayName() {
        return this.detectorWrapper.getDisplayName();
    }

    /**
     * Get the list of parameters as a string
     *
     * @return the string result
     */
    public String getParameterString() {
        StringBuilder result = new StringBuilder();

        List<AdjustableParameterObj> parameters = SherlockRegistry.getDetectorAdjustableParameters(task.getDetector());
        List<AdjustableParameterObj> postprocessing = SherlockRegistry.getPostProcessorAdjustableParametersFromDetector(task.getDetector());

        if (task.getParameterMapping() != null) {
            for (Map.Entry<String, Float> entry : task.getParameterMapping().entrySet()) {
                List<AdjustableParameterObj> para = new ArrayList<>();
                List<AdjustableParameterObj> post = new ArrayList<>();
                if (parameters != null && postprocessing != null) {
                    para = parameters.stream().filter(p -> (p.getReference()).equals(entry.getKey())).toList();
                    post = postprocessing.stream().filter(p -> (p.getReference()).equals(entry.getKey())).toList();
                }

                if (para.size() == 1) {
                    result.append(para.getFirst().getDisplayName()).append(" = ").append(entry.getValue()).append("<br /><br />");
                } else if (post.size() == 1) {
                    result.append("Post: ").append(post.getFirst().getDisplayName()).append(" = ").append(entry.getValue()).append("<br /><br />");
                } else {
                    result.append(entry.getKey()).append("=").append(entry.getValue()).append("<br /><br />");
                }
            }
        }

        if (result.length() < 12)
            return "None";

        return result.substring(0, result.length() - 12);
    }
}
