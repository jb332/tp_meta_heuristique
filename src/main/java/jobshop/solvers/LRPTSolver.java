package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Task;

import java.util.Comparator;

public class LRPTSolver extends GreedySolver {

    @Override
    protected Comparator<Task> useComparator(Instance instance) {
        return Comparator.comparingInt((Task o) -> {
            int remainingJobTime = 0;
            for (int i=o.task; i<instance.numTasks; i++) {
                remainingJobTime += instance.duration(o.job, i);
            }
            return remainingJobTime;
        });
    }

    /*
    @Override
    protected Task chooseTask(Instance instance, ArrayList<Task> realizableTasks) {
        Task chosenRealizableTask = null;
        int minRemainingJobTime = Integer.MAX_VALUE;

        for (Task currentRealizableTask : realizableTasks) {
            int remainingJobTime = 0;
            for (int i=currentRealizableTask.task; i<instance.numTasks; i++) {
                remainingJobTime += instance.duration(currentRealizableTask.job, i);
            }
            if(remainingJobTime < minRemainingJobTime) {
                chosenRealizableTask = currentRealizableTask;
                minRemainingJobTime = remainingJobTime;
            }
        }

        return chosenRealizableTask;
    }
    */
}
