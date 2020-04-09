package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Comparator;

public class LPTSolver extends GreedySolver {

    @Override
    protected Comparator<Task> getComparator(Instance instance, ResourceOrder sol) {
        return Comparators.getLPTComparator(instance);
    }
}
