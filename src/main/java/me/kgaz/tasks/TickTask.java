package me.kgaz.tasks;

public class TickTask {

    Tickable task;
    int tick;

    public TickTask(Tickable task) {

        this.task = task;

        tick = 0;

        if(task.getPeriod() <= 0) {

            throw new IllegalArgumentException("Tried to create Tickable listener with period <= 0!");

        }

    }

    public void tick(){

        if(task.isCancelled()) return;

        tick++;

        if(tick >= task.getPeriod()) {

            task.run();
            tick = 0;

        }

    }

    public Tickable getTask() {
        return task;
    }

}
