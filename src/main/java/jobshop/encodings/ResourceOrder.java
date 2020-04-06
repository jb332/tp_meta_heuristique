package jobshop.encodings;

import jobshop.Encoding;
import jobshop.Instance;
import jobshop.Schedule;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void addTaskToResourceQueue(int resource, int task, int job) {
        tasksOrderPerMachine[resource].add(new Task(job, task));
    }

    private Task getPreviousTaskJob(Task task) {
        return task.getPreviousJobTask();
    }

    private Task getPreviousTaskResource(Task task) {
        for(int r = 0; r < instance.numMachines; r++) {
            for(int i = 0; i < tasksOrderPerMachine[r].size(); i++) {
                if(tasksOrderPerMachine[r].get(i).equals(task)) {
                    if(i > 0) {
                        return tasksOrderPerMachine[r].get(i-1);
                    } else {
                        return null;
                    }
                }
            }
        }
        System.err.println("Task not found in the order matrix");
        System.exit(-1);
        return null; //never reached
    }

    private int getDuration(Task task) {
        return instance.duration(task.job, task.task);
    }

    private int computeTaskStartDate(Task task) throws Exception {
        Task previousTaskResource = getPreviousTaskResource(task);
        Task previousTaskJob = getPreviousTaskJob(task);

        if(previousTaskResource == null) {
            if(previousTaskJob == null) {
                return 0;
            } else {
                return getDuration(previousTaskJob) + computeTaskStartDate(previousTaskJob);
            }
        } else {
            if( previousTaskJob == null) {
                return getDuration(previousTaskResource) + computeTaskStartDate(previousTaskResource);
            } else {
                return Integer.max(getDuration(previousTaskResource) + computeTaskStartDate(previousTaskResource), getDuration(previousTaskJob) + computeTaskStartDate(previousTaskJob));
            }
        }
    }

    //version pas opti avec redondance
    public Schedule toSchedule2() throws Exception {
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

    //bonne version
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
                        startTimes[currentTask.job][currentTask.task] = startTimes[previousTaskJob.job][previousTaskJob.task] + getDuration(previousTaskJob);
                    }
                } else if(startTimes[previousTaskResource.job][previousTaskResource.task] != -1){
                    if(previousTaskJob == null) {
                        iter.remove();
                        startTimes[currentTask.job][currentTask.task] = startTimes[previousTaskResource.job][previousTaskResource.task] + getDuration(previousTaskResource);
                    } else if(startTimes[previousTaskJob.job][previousTaskJob.task] != -1){
                        iter.remove();
                        startTimes[currentTask.job][currentTask.task] = Integer.max(
                                startTimes[previousTaskJob.job][previousTaskJob.task] + getDuration(previousTaskJob),
                                startTimes[previousTaskResource.job][previousTaskResource.task] + getDuration(previousTaskResource)
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
/*
    public void fromSchedule(Schedule schedule) {
        ArrayList<Task> queue = new ArrayList<Task>(instance.numJobs * instance.numTasks);
        for(int i=0; i<instance.numTasks; i++) {
            for(int j=0; j<instance.numJobs; j++) {
                queue.add(new Task(j, i));
            }
        }

        Iterator<Task> iter = queue.iterator();
        while(queue.size() > 0) {
            if(iter.hasNext()) {
                Task currentTask = iter.next();
            } else {
                //reset the iterator to keep looping until there is no more element in the queue
                iter = queue.iterator();
            }
        }



        for(int i=0; i<instance.numTasks; i++) {
            for(int j=0; j<instance.numJobs; j++) {
                int currentStartTime = schedule.startTime(j, i);
                //this task has no predecessor in the job
                if(i==0) {
                    //so if its start date is different from zero it means it has a resource predecessor
                    if(currentStartTime != 0) {

                    }
                } else {

                }
            }
        }


    }
 */
}
