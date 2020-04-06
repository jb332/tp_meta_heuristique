package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Task;

import java.util.Comparator;

public class SPTSolver extends GreedySolver {

    @Override
    protected Comparator<Task> useComparator(Instance instance) {
        return Comparator.comparingInt((Task o) -> instance.duration(o.job, o.task)).reversed();
    }

    /*
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
    */
}
