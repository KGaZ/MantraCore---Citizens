package me.kgaz.tasks;

import me.kgaz.Citizens;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class GlobalTaskManager implements Runnable {

    private Citizens main;
    private Set<TickTask> asyncTickTasks, syncTickTasks;

    public GlobalTaskManager(Citizens main) {

        asyncTickTasks = new HashSet<>();
        syncTickTasks = new HashSet<>();
        this.main = main;

        Bukkit.getScheduler().runTaskTimer(main, this, 1, 1);

    }

    @Override
    public void run() {

        asyncTickTasks.forEach(task -> {

            Bukkit.getScheduler().runTaskAsynchronously(main, task::tick);

        });

        syncTickTasks.forEach(TickTask::tick);

    }

    public void registerTask(Tickable tickable, boolean async) {

        if(async) asyncTickTasks.add(new TickTask(tickable));
        else syncTickTasks.add(new TickTask(tickable));

    }

    public void unregister(Tickable tickable, boolean async) {
        if(async) asyncTickTasks.removeIf(tickTask -> tickTask.getTask() == tickable);
        else syncTickTasks.removeIf(tickTask -> tickTask.getTask() == tickable);
    }
}