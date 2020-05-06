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

    /** A matrix ordering (task, job) couples for each machine number. */
    public final ArrayList<Task>[] tasksOrderPerMachine;

    /**
     * Blank constructor
     * @param instance
     */
    public ResourceOrder(Instance instance) {
        super(instance);

        tasksOrderPerMachine = new ArrayList[instance.numMachines];
        for (int r = 0; r < instance.numMachines; r++) {
            tasksOrderPerMachine[r] = new ArrayList<Task>(instance.numJobs * instance.numTasks);
        }
    }

    /**
     * Constructor from schedule
     * @param schedule
     */
    public ResourceOrder(Schedule schedule) {
        this(schedule.pb);
        this.fromSchedule(schedule);
    }

    /**
     * Copy constructor
     * @param order
     */
    public ResourceOrder(ResourceOrder order) {
        this(order.instance);
        for (int r = 0; r < instance.numMachines; r++) {
            this.tasksOrderPerMachine[r] = (ArrayList<Task>) order.tasksOrderPerMachine[r].clone();
        }
    }

    public ResourceOrder copy() {
        return new ResourceOrder(this);
    }

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

    @Override
    public boolean equals(Object o) {
        ResourceOrder other = (ResourceOrder) o;
        boolean equals = this.instance.equals(other.instance);
        for(int r = 0; r < instance.numMachines && equals; r++) {
            Iterator<Task> iter = this.tasksOrderPerMachine[r].iterator();
            Iterator<Task> iterOther = other.tasksOrderPerMachine[r].iterator();
            while(iter.hasNext() && equals) {
                equals = iter.next().equals(iterOther.next());
            }
        }
        return equals;
    }
}
