package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class GreedySolver implements Solver {
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

    @Override
    public Result solve(Instance instance, long deadline) {
        ResourceOrder sol = new ResourceOrder(instance);

        //a matrix containing boolean indicating if a task has been treated or not
        boolean[][] treatedTasksMatrix = new boolean[instance.numJobs][instance.numTasks];
        //initialization with 0
        for(int j=0; j<instance.numJobs; j++) {
            Arrays.fill(treatedTasksMatrix[j], false);
        }

        //a list containing the realizable tasks
        ArrayList<Task> realizableTasks = new ArrayList<Task>(instance.numTasks);
        //initialize the realizable tasks set with the first tasks of the jobs
        for(int j=0; j<instance.numJobs; j++) {
            realizableTasks.add(new Task(j, 0));
        }

        //a list containing the remaining tasks
        ArrayList<Task> remainingTasks = new ArrayList<Task>(instance.numJobs * instance.numTasks);
        //initialize the remaining tasks set with the others tasks
        for(int i=1; i<instance.numTasks; i++) {
            for(int j=0; j<instance.numJobs; j++) {
                remainingTasks.add(new Task(j, i));
            }
        }

        while (realizableTasks.size() > 0) {
            //display
            for(int j=0; j<instance.numJobs; j++) {
                for(int i=0; i<instance.numTasks; i++) {
                    if(treatedTasksMatrix[j][i]) {
                        System.out.println("("+i+","+j+") ");
                    }
                }
            }

            // display zone
            System.out.println("\n");
            for(Task currentRealizableTask : realizableTasks) {
                System.out.println("("+currentRealizableTask.task+","+currentRealizableTask.job+") ");
            }
            System.out.println("\n");
            for(Task currentRemainingTask : remainingTasks) {
                System.out.println("("+currentRemainingTask.task+","+currentRemainingTask.job+") ");
            }
            System.out.println("\n\n");
            // end display zone


            Task chosenRealizableTask = chooseTask(instance, realizableTasks);
            int chosenRealizableTaskMachine = instance.machine(chosenRealizableTask.job, chosenRealizableTask.task);

            //add the chosen task to resource order
            sol.addTaskToResourceQueue(chosenRealizableTaskMachine, chosenRealizableTask.task, chosenRealizableTask.job);
            //remove the chosen task from the realizable tasks set
            realizableTasks.remove(chosenRealizableTask);
            //mark the task as treated in the treated tasks matrix
            treatedTasksMatrix[chosenRealizableTask.job][chosenRealizableTask.task] = true;

            for(Task currentTask : remainingTasks) {
                //if the previous task has been treated, then add the task to the realizable tasks set and remove it from the remaining tasks set
                if(treatedTasksMatrix[currentTask.job][currentTask.task-1]) {
                    realizableTasks.add(currentTask);
                    remainingTasks.remove(currentTask);
                }
            }
        }

        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }
}
