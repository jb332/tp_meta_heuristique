package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.*;

public class GreedySolver implements Solver {

    boolean earliestStartTimeMode;
    boolean crescentOrder;
    boolean remainingProcessingTimeMode;

    private static int getProcessingTime(Task task, Instance instance) {
        return instance.duration(task.job, task.task);
    }

    private static int getRemainingProcessingTime(Task task, Instance instance) {
        int remainingJobTime = 0;
        for (int i=task.task; i<instance.numTasks; i++) {
            remainingJobTime += instance.duration(task.job, i);
        }
        return remainingJobTime;
    }

    private Comparator<Task> getProcessingTimeComparator(Instance instance) {
        Comparator<Task> crescentCmp = Comparator.comparingInt((Task o) -> {
            if(remainingProcessingTimeMode) {
                return getRemainingProcessingTime(o, instance);
            } else {
                return getProcessingTime(o, instance);
            }
        });

        if(crescentOrder) {
            return crescentCmp;
        } else {
            return crescentCmp.reversed();
        }
    }

    //i hope java handles closures, and that the pointer "times" is going to be copied in the lambda function
    private Comparator<Task> getComparator(ResourceOrder sol, int[][] times) {
        if(earliestStartTimeMode) {
            return (Task o1, Task o2) -> {
                int startDateCmp = Comparator.comparingInt((Task o) -> times[o.job][o.task]).compare(o1, o2);
                if(startDateCmp != 0) {
                    return startDateCmp;
                } else {
                    return getProcessingTimeComparator(sol.instance).compare(o1, o2);
                }
            };
        } else {
            return getProcessingTimeComparator(sol.instance);
        }
    }

    private static void setEarliestTaskStartDate(Task task, ResourceOrder sol, int[][] startTimes) {
        int taskRes = sol.instance.machine(task);
        Task previousTaskResource = null;
        if(sol.tasksOrderPerMachine[taskRes].size() > 0) {
            previousTaskResource = sol.tasksOrderPerMachine[taskRes].get(sol.tasksOrderPerMachine[taskRes].size() - 1);
        }
        Task previousTaskJob = task.getPreviousJobTask();

        if (previousTaskResource == null) {
            if (previousTaskJob == null) {
                startTimes[task.job][task.task] = 0;
            } else if (startTimes[previousTaskJob.job][previousTaskJob.task] != -1) {
                startTimes[task.job][task.task] = startTimes[previousTaskJob.job][previousTaskJob.task] + sol.instance.duration(previousTaskJob);
            }
        } else if (startTimes[previousTaskResource.job][previousTaskResource.task] != -1) {
            if (previousTaskJob == null) {
                startTimes[task.job][task.task] = startTimes[previousTaskResource.job][previousTaskResource.task] + sol.instance.duration(previousTaskResource);
            } else if (startTimes[previousTaskJob.job][previousTaskJob.task] != -1) {
                startTimes[task.job][task.task] = Integer.max(
                        startTimes[previousTaskJob.job][previousTaskJob.task] + sol.instance.duration(previousTaskJob),
                        startTimes[previousTaskResource.job][previousTaskResource.task] + sol.instance.duration(previousTaskResource)
                );
            }
        }
    }

    /**
     *
     * @param earliestStartTimeMode if true then realizable tasks are ordered by earliest start time, and then by processing time or remaining processing time depending on the "remainingProcessingTimeMode" parameter and following the order determined by the "crescentOrder" parameter
     * @param remainingProcessingTimeMode if true then the remaining processing time for the job is calculated instead of the processing time
     * @param crescentOrder if true then the processing times or remaining processing times are ordered crescently, otherwise decrescently
     */
    public GreedySolver(boolean earliestStartTimeMode, boolean remainingProcessingTimeMode, boolean crescentOrder) {
        this.earliestStartTimeMode = earliestStartTimeMode;
        this.crescentOrder = crescentOrder;
        this.remainingProcessingTimeMode = remainingProcessingTimeMode;
    }

    @Override
    public Result solve(Instance instance, long deadline) {
        ResourceOrder sol = new ResourceOrder(instance);

        int[][] startTimes = null;
        if(earliestStartTimeMode) {
            startTimes = new int[instance.numJobs][instance.numTasks];
            for (int j = 0; j < instance.numJobs; j++) {
                Arrays.fill(startTimes[j], -1);
            }
        }

        //a list (heap actually) containing the realizable tasks
        Comparator<Task> comparator = this.getComparator(sol, startTimes);
        PriorityQueue<Task> realizableTasks = new PriorityQueue<Task>(instance.numJobs, comparator);

        //initialize the realizable tasks set with the first tasks of the jobs
        for(int j=0; j<instance.numJobs; j++) {
            Task task = new Task(j, 0);
            if(earliestStartTimeMode) {
                setEarliestTaskStartDate(task, sol, startTimes);
            }
            realizableTasks.add(task);
        }

        while (!realizableTasks.isEmpty()) {

            Task chosenRealizableTask = realizableTasks.poll();

            int chosenRealizableTaskMachine = instance.machine(chosenRealizableTask);

            //add the chosen task to resource order
            sol.addTaskToResourceQueue(chosenRealizableTaskMachine, chosenRealizableTask.task, chosenRealizableTask.job);

            if(earliestStartTimeMode) {
                //update the earliest start time of the realizable tasks after treating the chosen task
                ArrayList<Task> tasksToUpdate = new ArrayList<Task>(instance.numJobs);
                Iterator<Task> iter = realizableTasks.iterator();
                while(iter.hasNext()) {
                    Task currentTask = iter.next();
                    //update is necesseray only if a realizable task uses the same resource used by the chosen task
                    if(instance.machine(currentTask) == instance.machine(chosenRealizableTask)) {
                        setEarliestTaskStartDate(currentTask, sol, startTimes);
                        //remove the task having a new earliest start time
                        iter.remove();
                        //save the task in a list to re-add it later
                        tasksToUpdate.add(currentTask);
                    }
                }
                //re-add removed tasks to update them
                for(Task currentTask : tasksToUpdate) {
                    realizableTasks.add(currentTask);
                }
            }

            //add the chosen task successor to the realizable tasks heap if it exists
            if(chosenRealizableTask.task+1 < instance.numTasks) {
                Task newRealizableTask = new Task(chosenRealizableTask.job, chosenRealizableTask.task + 1);
                if(earliestStartTimeMode) {
                    setEarliestTaskStartDate(newRealizableTask, sol, startTimes);

                }
                realizableTasks.add(newRealizableTask);
            }
        }

        Schedule returnedSched;
        if(earliestStartTimeMode) {
            returnedSched = new Schedule(instance, startTimes);
            assert returnedSched.equals(sol.toSchedule());
        } else {
            returnedSched = sol.toSchedule();
        }
        return new Result(instance, returnedSched, Result.ExitCause.Blocked);
    }
}
