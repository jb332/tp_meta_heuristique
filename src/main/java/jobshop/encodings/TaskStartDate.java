package jobshop.encodings;

public class TaskStartDate {
    protected Task task;
    protected int startDate;

    protected TaskStartDate(int job, int task, int startDate) {
        this.task = new Task(job, task);
        this.startDate = startDate;
    }
}
