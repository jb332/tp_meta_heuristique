package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Task;

import java.util.ArrayList;

public class FormerSRPTSolver extends FormerGreedySolver {
    @Override
    protected Task chooseTask(Instance instance, ArrayList<Task> realizableTasks) {
        Task chosenRealizableTask = null;
        int maxRemainingJobTime = Integer.MIN_VALUE;

        for (Task currentRealizableTask : realizableTasks) {
            int remainingJobTime = 0;
            for (int i=currentRealizableTask.task; i<instance.numTasks; i++) {
                remainingJobTime += instance.duration(currentRealizableTask.job, i);
            }
            if(remainingJobTime > maxRemainingJobTime) {
                chosenRealizableTask = currentRealizableTask;
                maxRemainingJobTime = remainingJobTime;
            }
        }

        return chosenRealizableTask;
    }
}
