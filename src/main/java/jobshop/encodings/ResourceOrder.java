package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public class ResourceOrder extends Encoding {

    /** A matrix ordering (task, job) couples by machine numbers. */
    public final ArrayList<Task>[] tasksOrderPerMachine;

    public ResourceOrder(Instance instance) {
        super(instance);

        tasksOrderPerMachine = new ArrayList[instance.numMachines];
        for (int r = 0; r < instance.numMachines; r++) {
            tasksOrderPerMachine[r] = new ArrayList<Task>(instance.numJobs * instance.numTasks);
        }
    }

    public ResourceOrder(ResourceOrder order) {
        this(order.instance);
        for (int r = 0; r < instance.numMachines; r++) {
            this.tasksOrderPerMachine[r] = (ArrayList<Task>) order.tasksOrderPerMachine[r].clone();
        }
    }

    public ResourceOrder copy() {
        return new ResourceOrder(this);
    }

    /*
    public boolean isTaskTreated(Task task) {
        boolean found = false;
        for (int r = 0 ; r < instance.numMachines && !found; r++) {
            Iterator<Task> iter = tasksOrderPerMachine[r].iterator();
            while(iter.hasNext() && !found) {
                Task currentTask = iter.next();
                if(task.equals(currentTask)) {
                    found = true;
                }
            }
        }
        return found;
    }
    */

    public void addTaskToResourceQueue(int resource, int task, int job) {
        tasksOrderPerMachine[resource].add(new Task(job, task));
    }

    private Task getPreviousTaskJob(Task task) {
        return task.getPreviousJobTask();
    }

    private Task getPreviousTaskResource(Task task) {
        int r = instance.machine(task.job, task.task);
        int i = tasksOrderPerMachine[r].indexOf(task);
        if(i > 0) {
            return tasksOrderPerMachine[r].get(i - 1);
        } else if(i == 0) {
            return null;
        } else {
            //if the task is not found, it means it is the first function call and task is a realizable task
            if(tasksOrderPerMachine[r].size() > 0) {
                return tasksOrderPerMachine[r].get(tasksOrderPerMachine[r].size() - 1);
            } else {
                return null;
            }
        }
    }

    /*
    public int computeTaskStartDate(Task task) {
        Task previousTaskResource = getPreviousTaskResource(task);
        Task previousTaskJob = getPreviousTaskJob(task);

        if(previousTaskResource == null) {
            if(previousTaskJob == null) {
                return 0;
            } else {
                return instance.duration(previousTaskJob.job, previousTaskJob.task) + computeTaskStartDate(previousTaskJob);
            }
        } else {
            if( previousTaskJob == null) {
                return instance.duration(previousTaskResource.job, previousTaskResource.task) + computeTaskStartDate(previousTaskResource);
            } else {
                return Integer.max(
                        instance.duration(previousTaskResource.job, previousTaskResource.task) + computeTaskStartDate(previousTaskResource),
                        instance.duration(previousTaskJob.job, previousTaskJob.task) + computeTaskStartDate(previousTaskJob)
                );
            }
        }
    }

    //version pas opti avec redondance
    public Schedule toSchedule2() {
        //long startTime = System.nanoTime();

        int[][] startTimes = new int[instance.numJobs][instance.numTasks];

        for(int j=0; j<instance.numJobs; j++) {
            for(int i=0; i<instance.numTasks; i++) {
                startTimes[j][i] = computeTaskStartDate(new Task(j, i));
            }
        }

        //System.out.println(System.nanoTime()-startTime);
        return new Schedule(instance, startTimes);
    }
    */

    @Override
    public Schedule toSchedule() {
        //long startTime = System.nanoTime();

        int[][] startTimes = new int[instance.numJobs][instance.numTasks];
        for(int j=0; j<instance.numJobs; j++) {
            Arrays.fill(startTimes[j], -1);
        }

        ArrayList<Task> queue = new ArrayList<Task>(instance.numJobs * instance.numTasks);
        for(int j=0; j<instance.numJobs; j++) {
            for(int i=0; i<instance.numTasks; i++) {
                queue.add(new Task(j, i));
            }
        }

        Iterator<Task> iter = queue.iterator();
        while(queue.size() > 0) {
            if(iter.hasNext()) {
                Task currentTask = iter.next();
                Task previousTaskResource = getPreviousTaskResource(currentTask);
                Task previousTaskJob = getPreviousTaskJob(currentTask);

                if(previousTaskResource == null) {
                    if(previousTaskJob == null) {
                        iter.remove();
                        startTimes[currentTask.job][currentTask.task] = 0;
                    } else if(startTimes[previousTaskJob.job][previousTaskJob.task] != -1) {
                        iter.remove();
                        startTimes[currentTask.job][currentTask.task] = startTimes[previousTaskJob.job][previousTaskJob.task] + instance.duration(previousTaskJob.job, previousTaskJob.task);
                    }
                } else if(startTimes[previousTaskResource.job][previousTaskResource.task] != -1){
                    if(previousTaskJob == null) {
                        iter.remove();
                        startTimes[currentTask.job][currentTask.task] = startTimes[previousTaskResource.job][previousTaskResource.task] + instance.duration(previousTaskResource.job, previousTaskResource.task);
                    } else if(startTimes[previousTaskJob.job][previousTaskJob.task] != -1){
                        iter.remove();
                        startTimes[currentTask.job][currentTask.task] = Integer.max(
                                startTimes[previousTaskJob.job][previousTaskJob.task] + instance.duration(previousTaskJob.job, previousTaskJob.task),
                                startTimes[previousTaskResource.job][previousTaskResource.task] + instance.duration(previousTaskResource.job, previousTaskResource.task)
                        );
                    }
                }
            } else {
                //reset the iterator to keep looping until there is no more element in the queue
                iter = queue.iterator();
            }
        }

        //System.out.println(System.nanoTime()-startTime);
        return new Schedule(instance, startTimes);
    }

    public void fromSchedule(Schedule schedule) {
        ArrayList<TaskStartDate> sortedSchedule = new ArrayList<TaskStartDate>(instance.numJobs*instance.numTasks);
        for(int j=0; j<instance.numJobs; j++) {
            for(int i=0; i<instance.numTasks; i++) {
                sortedSchedule.add(new TaskStartDate(j, i, schedule.startTime(j, i)));
            }
        }
        sortedSchedule.sort(Comparator.comparingInt((TaskStartDate o) -> o.startDate));
        for(TaskStartDate currentTaskStartDate : sortedSchedule) {
            Task currentTask = currentTaskStartDate.task;
            int currentTaskMachine = instance.machine(currentTask.job, currentTask.task);
            addTaskToResourceQueue(currentTaskMachine, currentTask.task, currentTask.job);
        }
    }

    public String toString() {
        StringBuilder strBuild = new StringBuilder();
        for(int r = 0; r < instance.numMachines; r++) {
            strBuild.append("M"+r+" : ");
            for(Task currentTask : tasksOrderPerMachine[r]) {
                strBuild.append("("+currentTask.job+","+currentTask.task+") ");
            }
            strBuild.append("\n");
        }
        strBuild.append("\n");
        return strBuild.toString();
    }
}
