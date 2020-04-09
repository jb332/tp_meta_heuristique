package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Task;

import java.util.Comparator;

public class Comparators {

    public static Comparator<Task> getLPTComparator(Instance instance) {
        return Comparator.comparingInt((Task o) -> instance.duration(o.job, o.task));
    }

    public static Comparator<Task> getSPTComparator(Instance instance) {
        return Comparator.comparingInt((Task o) -> instance.duration(o.job, o.task)).reversed();
    }

    public static Comparator<Task> getLRPTComparator(Instance instance) {
        return Comparator.comparingInt((Task o) -> {
            int remainingJobTime = 0;
            for (int i=o.task; i<instance.numTasks; i++) {
                remainingJobTime += instance.duration(o.job, i);
            }
            return remainingJobTime;
        });
    }

    public static Comparator<Task> getSRPTComparator(Instance instance) {
        return Comparator.comparingInt((Task o) -> {
            int remainingJobTime = 0;
            for (int i=o.task; i<instance.numTasks; i++) {
                remainingJobTime += instance.duration(o.job, i);
            }
            return remainingJobTime;
        }).reversed();
    }
}
