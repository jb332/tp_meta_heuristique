package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public abstract class FormerGreedySolver implements Solver {

    final static boolean debug = false;
    static Path file;

    static {
        if(debug) {
            String path = "/home/jb/Bureau/debug.txt";

            file = Paths.get(path);
            //delete the previous version of the file if it exists
            File formerFile = new File(path);
            formerFile.delete();
            //create a new file
            File file = new File(path);
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Class : GreedySolver\nError : Could not create \"debug.txt\" file");
            }
        }
    }

    private static void write(String text) {
        try {
            Files.write(file, text.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Class : GreedySolver\nError : Could not print to file");
        }
    }

    protected abstract Task chooseTask(Instance instance, ArrayList<Task> realizableTasks);

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


        int cpt;
        if(debug) {
            cpt = 1;
            write(this.getClass().getSimpleName()+" :\n\n");
        }
        while (realizableTasks.size() > 0) {
            if(debug) {
                write("Iteration " + cpt + " :\nTreated Tasks : ");
                for (int j = 0; j < instance.numJobs; j++) {
                    for (int i = 0; i < instance.numTasks; i++) {
                        if (treatedTasksMatrix[j][i]) {
                            write("(" + i + "," + j + ") ");
                        }
                    }
                }
                write("\nRealizable Tasks : ");
                for (Task currentRealizableTask : realizableTasks) {
                    write("(" + currentRealizableTask.task + "," + currentRealizableTask.job + ") ");
                }
                write("\nRemaining Tasks : ");
                for (Task currentRemainingTask : remainingTasks) {
                    write("(" + currentRemainingTask.task + "," + currentRemainingTask.job + ") ");
                }
                cpt++;
            }


            Task chosenRealizableTask = chooseTask(instance, realizableTasks);
            if(debug) {
                write("\nChosen Task : (" + chosenRealizableTask.task + "," + chosenRealizableTask.job + ")");
                write("\n\n");
            }

            int chosenRealizableTaskMachine = instance.machine(chosenRealizableTask.job, chosenRealizableTask.task);

            //add the chosen task to resource order
            sol.addTaskToResourceQueue(chosenRealizableTaskMachine, chosenRealizableTask.task, chosenRealizableTask.job);
            //remove the chosen task from the realizable tasks set
            realizableTasks.remove(chosenRealizableTask);
            //mark the task as treated in the treated tasks matrix
            treatedTasksMatrix[chosenRealizableTask.job][chosenRealizableTask.task] = true;

            Iterator<Task> iter = remainingTasks.iterator();
            while(iter.hasNext()) {
                Task currentTask = iter.next();
                //if the previous task has been treated, then add the task to the realizable tasks set and remove it from the remaining tasks set
                if(treatedTasksMatrix[currentTask.job][currentTask.task-1]) {
                    realizableTasks.add(currentTask);
                    iter.remove();
                }
            }
        }
        if(debug) {
            write("\n\n\n\n");
        }

        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }
}
