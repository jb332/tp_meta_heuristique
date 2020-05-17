package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.List;

public class TabooSolver implements Solver {

    int tabooDuration;
    int maxIter;
    boolean earliestStartTimeMode;
    boolean crescentOrder;
    boolean remainingProcessingTimeMode;

    /**
     *
     * @param earliestStartTimeMode This argument specifies the greedy solver used. To learn more, go to GreedySolver constructor documentation
     * @param remainingProcessingTimeMode Idem
     * @param crescentOrder Idem
     */
    public TabooSolver(int tabooDuration, int maxIter, boolean earliestStartTimeMode, boolean remainingProcessingTimeMode, boolean crescentOrder) {
        this.tabooDuration = tabooDuration;
        this.maxIter = maxIter;
        this.earliestStartTimeMode = earliestStartTimeMode;
        this.crescentOrder = crescentOrder;
        this.remainingProcessingTimeMode = remainingProcessingTimeMode;
    }

    private static boolean isTaboo(int[][] tabooSolutions, DescentSolver.Swap swap, ResourceOrder solution, int k) {
        List<Task> tasks = swap.getTasksToSwap(solution);
        return k <
                tabooSolutions
                        [tasks.get(0).job * solution.instance.numTasks + tasks.get(0).task]
                        [tasks.get(1).job * solution.instance.numTasks + tasks.get(1).task];
    }

    private void setTaboo(int[][] tabooSolutions, DescentSolver.Swap swap, ResourceOrder solution, int k) {
        List<Task> tasks = swap.getTasksToSwap(solution);
        tabooSolutions
                [tasks.get(1).job * solution.instance.numTasks + tasks.get(1).task]
                [tasks.get(0).job * solution.instance.numTasks + tasks.get(0).task]
                = tabooDuration + k;
    }

    @Override
    public Result solve(Instance instance, long deadline) {
        Solver solver = new GreedySolver(this.earliestStartTimeMode, this.remainingProcessingTimeMode, this.crescentOrder);

        ResourceOrder bestSolution = new ResourceOrder(solver.solve(instance, -1).schedule);
        int bestMakespan = bestSolution.toSchedule().makespan();

        ResourceOrder currentSolution = bestSolution;

        //implicitly filled with zeros
        int[][] tabooSolutions = new int[instance.numJobs * instance.numTasks][instance.numJobs * instance.numTasks];

        int k = 0;

        while(k < maxIter && System.currentTimeMillis() < deadline) {
            k++;

            DescentSolver.Swap bestNeighborSwap = null;
            ResourceOrder bestNeighborSolution = null;
            int bestNeighborMakespan = Integer.MAX_VALUE;

            List<DescentSolver.Block> blocks = DescentSolver.blocksOfCriticalPath(currentSolution);
            for (DescentSolver.Block currentBlock : blocks) {
                List<DescentSolver.Swap> blockNeighbors = DescentSolver.neighbors(currentBlock);
                for (DescentSolver.Swap currentSwap : blockNeighbors) {
                    ResourceOrder currentNeighborSolution = currentSolution.copy();
                    currentSwap.applyOn(currentNeighborSolution);
                    int currentNeighborMakespan = currentNeighborSolution.toSchedule().makespan();
                    if (
                            currentNeighborMakespan < bestNeighborMakespan &&
                            (currentNeighborMakespan < bestMakespan || !isTaboo(tabooSolutions, currentSwap, currentSolution, k))
                    ) {
                        bestNeighborSwap = currentSwap;
                        bestNeighborSolution = currentNeighborSolution;
                        bestNeighborMakespan = currentNeighborMakespan;
                    }
                }
            }

            if(bestNeighborSolution != null) {
                setTaboo(tabooSolutions, bestNeighborSwap, currentSolution, k);

                currentSolution = bestNeighborSolution;

                if (bestNeighborMakespan < bestMakespan) {
                    bestSolution = bestNeighborSolution;
                    bestMakespan = bestNeighborMakespan;
                }
            }
        }

        Result.ExitCause exitCause = Result.ExitCause.Blocked;
        if(System.currentTimeMillis() >= deadline) {
            exitCause = Result.ExitCause.Timeout;
        }
        return new Result(instance, bestSolution.toSchedule(), exitCause);
    }
}
