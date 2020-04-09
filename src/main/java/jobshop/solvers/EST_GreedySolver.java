package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Comparator;

public abstract class EST_GreedySolver extends GreedySolver {

    protected abstract Comparator<Task> getSecondaryComparator(Instance instance);

    @Override
    protected Comparator<Task> getComparator(Instance instance, ResourceOrder sol) {
        return (Task o1, Task o2) -> {
            int startDateCmp = Comparator.comparingInt((Task o) -> sol.computeTaskStartDate(o)).compare(o1, o2);
            if(startDateCmp != 0) {
                return startDateCmp;
            } else {
                return getSecondaryComparator(instance).compare(o1, o2);
            }
        };
    }
}
