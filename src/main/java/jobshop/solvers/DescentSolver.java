package jobshop.solvers;

import jobshop.*;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTaskIndexInResourceQueue, int lastTaskIndexInResourceQueue) {
            this.machine = machine;
            this.firstTask = firstTaskIndexInResourceQueue;
            this.lastTask = lastTaskIndexInResourceQueue;
        }

        Block(ResourceOrder order, int machine, int taskCount, Task firstTask) {
            int firstTaskIndexInResourceQueue = order.tasksOrderPerMachine[machine].indexOf(firstTask);
            int lastTaskIndexInResourceQueue = firstTaskIndexInResourceQueue + taskCount - 1;
            this.machine = machine;
            this.firstTask = firstTaskIndexInResourceQueue;
            this.lastTask = lastTaskIndexInResourceQueue;
        }
    }

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            Task task1 = order.tasksOrderPerMachine[this.machine].get(this.t1);
            Task task2 = order.tasksOrderPerMachine[this.machine].get(this.t2);
            order.tasksOrderPerMachine[this.machine].set(this.t1, task2);
            order.tasksOrderPerMachine[this.machine].set(this.t2, task1);
        }
    }

    boolean earliestStartTimeMode;
    boolean crescentOrder;
    boolean remainingProcessingTimeMode;

    /**
     * 
     * @param earliestStartTimeMode This argument specifies the greedy solver used. To learn more, go to GreedySolver constructor documentation
     * @param remainingProcessingTimeMode Idem
     * @param crescentOrder Idem
     */
    public DescentSolver(boolean earliestStartTimeMode, boolean remainingProcessingTimeMode, boolean crescentOrder) {
        this.earliestStartTimeMode = earliestStartTimeMode;
        this.crescentOrder = crescentOrder;
        this.remainingProcessingTimeMode = remainingProcessingTimeMode;
    }

    @Override
    public Result solve(Instance instance, long deadline) {

        Solver solver = new GreedySolver(this.earliestStartTimeMode, this.remainingProcessingTimeMode, this.crescentOrder);

        ResourceOrder bestSolution = null;
        int bestMakespan = Integer.MAX_VALUE;

        ResourceOrder bestNeighborSolution = new ResourceOrder(instance);
        bestNeighborSolution.fromSchedule(solver.solve(instance, deadline).schedule);
        int bestNeighborMakeSpan = bestNeighborSolution.toSchedule().makespan();

        while(bestNeighborMakeSpan < bestMakespan) {
            bestSolution = bestNeighborSolution;
            bestMakespan = bestNeighborMakeSpan;

            List<Block> blocks = blocksOfCriticalPath(bestSolution);
            for(Block currentBlock : blocks) {
                List<Swap> blockNeighbors = neighbors(currentBlock);
                for(Swap currentSwap : blockNeighbors) {
                    ResourceOrder currentNeighborSolution = bestSolution.copy();
                    currentSwap.applyOn(currentNeighborSolution);
                    int currentNeighborMakeSpan = currentNeighborSolution.toSchedule().makespan();
                    if(currentNeighborMakeSpan < bestNeighborMakeSpan) {
                        bestNeighborSolution = currentNeighborSolution;
                        bestNeighborMakeSpan = currentNeighborMakeSpan;
                    }
                }
            }
        }

        return new Result(instance, bestSolution.toSchedule(), Result.ExitCause.Blocked);
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
        List<Task> criticalPath = order.toSchedule().criticalPath();
        List<Block> blocks = new ArrayList<>(criticalPath.size() / 2);

        int currentBlockResource = -1;
        int currentBlockTaskCount = 0;
        Task currentBlockFirstTask = null;

        for(Task currentTask : criticalPath) {
            int currentTaskResource = order.instance.machine(currentTask);
            if(currentTaskResource != currentBlockResource) {
                if(currentBlockTaskCount > 1) {
                    blocks.add(new Block(order, currentBlockResource, currentBlockTaskCount, currentBlockFirstTask));
                }
                currentBlockResource = currentTaskResource;
                currentBlockTaskCount = 1;
                currentBlockFirstTask = currentTask;
            } else {
                currentBlockTaskCount++;
            }
        }
        if(currentBlockTaskCount > 1) {
            blocks.add(new Block(order, currentBlockResource, currentBlockTaskCount, currentBlockFirstTask));
        }

        return blocks;
    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        int blockSize = block.lastTask - block.firstTask + 1;
        List<Swap> swapList;
        if(blockSize == 2) {
            swapList = new ArrayList<Swap>(1);
            swapList.add(new Swap(block.machine, block.firstTask, block.lastTask));
        } else {
            swapList = new ArrayList<Swap>(2);
            swapList.add(new Swap(block.machine, block.firstTask, block.firstTask+1));
            swapList.add(new Swap(block.machine, block.lastTask-1, block.lastTask));
        }
        return swapList;
    }

}
