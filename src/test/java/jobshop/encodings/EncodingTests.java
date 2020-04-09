package jobshop.encodings;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.solvers.BasicSolver;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class EncodingTests {

    @Test
    public void testJobNumbers() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        /*
                    Task 1  Task 2  Task 3
            Job 1   M1 D3   M2 D3   M3 D2
            Job 2   M2 D2   M1 D2   M3 D4

            Job order : 1 2 2 1 1 2

                    T1  T2  T3
            Job 1   0   3   6
            Job 2   0   3   8
         */

        final int[][] times1 = {
                {0, 3, 6},
                {0, 3, 8}
        };
        Schedule schedMan = new Schedule(instance, times1);
        Schedule schedAuto = enc.toSchedule();
        // TODO: make it print something meaningful
        // by implementing the toString() method
        System.out.println(schedMan);
        System.out.println(schedAuto);
        assert schedAuto.equals(schedMan);
        assert schedMan.isValid();
        assert schedMan.makespan() == 12;



        // numéro de jobs : 1 1 2 2 1 2
        enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        /*
                    Task 1  Task 2  Task 3
            Job 1   M1 D3   M2 D3   M3 D2
            Job 2   M2 D2   M1 D2   M3 D4

            Job order : 1 1 2 2 1 2

                    T1  T2  T3
            Job 1   0   3   6
            Job 2   6   8   10
         */

        final int[][] times2 = {
                {0, 3, 6},
                {6, 8, 10}
        };
        schedMan = new Schedule(instance, times2);
        schedAuto = enc.toSchedule();
        System.out.println(schedMan);
        System.out.println(schedAuto);
        assert schedAuto.equals(schedMan);
        assert schedMan.isValid();
        assert schedMan.makespan() == 14;

        JobNumbers enc2 = new JobNumbers(instance);
        enc2.fromSchedule(schedAuto);
        Schedule newSchedAuto = enc2.toSchedule();

        System.out.println(enc);
        System.out.println(enc2);

        System.out.println(schedAuto);
        System.out.println(newSchedAuto);
    }

    @Test
    public void testResourceOrder() throws Exception {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // numéro de jobs : 1 2 2 1 1 2 (cf exercices)
        ResourceOrder enc = new ResourceOrder(instance);
        enc.addTaskToResourceQueue(0, 0, 0);
        enc.addTaskToResourceQueue(0, 1, 1);
        enc.addTaskToResourceQueue(1, 0, 1);
        enc.addTaskToResourceQueue(1, 1, 0);
        enc.addTaskToResourceQueue(2, 2, 0);
        enc.addTaskToResourceQueue(2, 2, 1);
        /*
                    Task 1  Task 2  Task 3
            Job 1   M1 D3   M2 D3   M3 D2
            Job 2   M2 D2   M1 D2   M3 D4

                 Step 1  Step 2
            R1   (1,1)   (2,2)
            R2   (1,2)   (2,1)
            R3   (3,1)   (3,2)

                    T1  T2  T3
            Job 1   0   3   6
            Job 2   0   3   8
         */

        final int[][] times = {
                {0, 3, 6},
                {0, 3, 8}
        };
        Schedule schedMan = new Schedule(instance, times);
        Schedule schedAuto = enc.toSchedule();
        Schedule schedAuto2 = enc.toSchedule2();

        // TODO: make it print something meaningful
        // by implementing the toString() method
        System.out.println(schedMan);
        System.out.println(schedAuto);
        System.out.println(schedAuto2);

        assert schedAuto.equals(schedMan);
        assert schedAuto2.equals(schedMan);
        assert schedMan.isValid();
        assert schedMan.makespan() == 12;

        ResourceOrder enc2 = new ResourceOrder(instance);
        enc2.fromSchedule(schedAuto);
        Schedule newSchedAuto = enc2.toSchedule();

        System.out.println(enc);
        System.out.println(enc2);

        System.out.println(schedAuto);
        System.out.println(newSchedAuto);

    }

    @Test
    public void testBasicSolver() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

        // build a solution that should be equal to the result of BasicSolver
        JobNumbers enc = new JobNumbers(instance);
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;
        enc.jobs[enc.nextToSet++] = 0;
        enc.jobs[enc.nextToSet++] = 1;

        Schedule sched = enc.toSchedule();
        assert sched.isValid();
        assert sched.makespan() == 12;

        Solver solver = new BasicSolver();
        Result result = solver.solve(instance, System.currentTimeMillis() + 10);

        assert result.schedule.isValid();
        assert result.schedule.makespan() == sched.makespan(); // should have the same makespan
    }
}
