package jobshop.solvers;

import jobshop.Instance;
import jobshop.encodings.Task;

import java.util.Comparator;

public class EST_SRPTSolver extends EST_GreedySolver {
    @Override
    protected Comparator<Task> getSecondaryComparator(Instance instance) {
        return Comparators.getSRPTComparator(instance);
    }
}
