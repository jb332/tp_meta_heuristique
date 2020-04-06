package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Task;

import java.util.Comparator;

public class LPTSolver extends GreedySolver {

    @Override
    protected Comparator<Task> useComparator(Instance instance) {
        return Comparator.comparingInt((Task o) -> instance.duration(o.job, o.task));
    }

    /*
    @Override
    protected Task chooseTask(Instance instance, ArrayList<Task> realizableTasks) {
        Task shortestRealizableTask = null;
        int shortestRealizableTaskDuration = Integer.MAX_VALUE;

        for (Task currentRealizableTask : realizableTasks) {
            int currentRealizableTaskDuration = instance.duration(currentRealizableTask.job, currentRealizableTask.task);
            if(currentRealizableTaskDuration < shortestRealizableTaskDuration) {
                shortestRealizableTask = currentRealizableTask;
                shortestRealizableTaskDuration = currentRealizableTaskDuration;
            }
        }

        return shortestRealizableTask;
    }
    */
}
