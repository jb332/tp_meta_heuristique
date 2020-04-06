package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Task;

import java.util.ArrayList;

public class FormerSPTSolver extends FormerGreedySolver {

    @Override
    protected Task chooseTask(Instance instance, ArrayList<Task> realizableTasks) {
        Task longestRealizableTask = null;
        int longestRealizableTaskDuration = Integer.MIN_VALUE;

        for (Task currentRealizableTask : realizableTasks) {
            int currentRealizableTaskDuration = instance.duration(currentRealizableTask.job, currentRealizableTask.task);
            if(currentRealizableTaskDuration > longestRealizableTaskDuration) {
                longestRealizableTask = currentRealizableTask;
                longestRealizableTaskDuration = currentRealizableTaskDuration;
            }
        }

        return longestRealizableTask;
    }
}
