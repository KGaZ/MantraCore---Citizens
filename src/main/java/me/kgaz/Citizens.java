package me.kgaz;

import me.kgaz.debug.Debug;
import me.kgaz.npcs.NPCTest;
import me.kgaz.tasks.GlobalTaskManager;
import me.kgaz.tasks.Tickable;
import me.kgaz.users.UserManager;
import me.kgaz.util.Loadable;
import me.kgaz.util.Task;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Citizens extends JavaPlugin {

    private Listener[] listeners;
    private Loadable[] loadables;
    private List<Task> disableTasks;
    private GlobalTaskManager gtm;
    private UserManager manager;

    private void preEnable() {

        System.out.println("Initiating values...");

        manager = new UserManager(this);
        listeners = new Listener[] {new NPCTest()};
        loadables = new Loadable[] {};
        disableTasks = new ArrayList<>();
        gtm = new GlobalTaskManager(this);

        getCommand("action").setExecutor(new Debug(this));

        /*
        Test
         */


    }

    @Override
    public void onEnable() {

        System.out.println("Enabling Plugin...");

        preEnable();

        registerListeners();

        loadLoadables();

        System.out.println("Enabled Plugin Successfully!");

    }

    @Override
    public void onDisable() {

        System.out.println("Disabling Plugin...");

        disableLoadables();

        executeDisableTasks();

    }



    public void registerListener(Listener listener) {

        Bukkit.getPluginManager().registerEvents(listener, this);

    }

    public void registerTaskOnDisable(Task task) {

        disableTasks.add(task);

    }

    private void executeDisableTasks() {

        System.out.println("Executing Tasks...");

        /*
        disableTasks.stream().filter(Task::isActive).forEach(Task::run);
        */

    }

    private void disableLoadables() {

        System.out.println("Disabling Classes...");

        StringBuilder registered = new StringBuilder("§fDisabled: ");
        int failed = 0, success = 0;

        for(Loadable loadable : loadables) {

            try {

                loadable.onDisable(this);

                registered.append("§a").append(loadable.getClass().getSimpleName()).append("§2, ");
                ++success;

            } catch(Exception exc) {

                exc.printStackTrace();

                registered.append("§c").append(loadable.getClass().getSimpleName()).append("§4,");
                ++failed;

            }

        }

        Bukkit.getConsoleSender().sendMessage(registered.toString().substring(0, registered.length()-1)+"§f;");
        Bukkit.getConsoleSender().sendMessage("§fDisabled Total: "+(failed+success) +", Failed: "+failed+", Success: "+success);

    }

    private void loadLoadables() {

        System.out.println("Loading Classes...");

        StringBuilder registered = new StringBuilder("§fLoaded Classes: ");
        int failed = 0, success = 0;

        for(Loadable loadable : loadables) {

            try {

                loadable.onLoad(this);

                registered.append("§a").append(loadable.getClass().getSimpleName()).append("§2, ");
                ++success;

            } catch(Exception exc) {

                exc.printStackTrace();

                registered.append("§c").append(loadable.getClass().getSimpleName()).append("§4,");
                ++failed;

            }

        }

        Bukkit.getConsoleSender().sendMessage(registered.toString().substring(0, registered.length()-1)+"§f;");
        Bukkit.getConsoleSender().sendMessage("§fLoaded Total: "+(failed+success) +", Failed: "+failed+", Success: "+success);

    }

    private void registerListeners() {

        System.out.println("Registering listeners...");

        StringBuilder registered = new StringBuilder("§fRegistered Listeners: ");
        int failed = 0, success = 0;

        for(Listener listener : listeners) {

            try {

                Bukkit.getPluginManager().registerEvents(listener, this);

                registered.append("§a").append(listener.getClass().getSimpleName()).append("§2, ");
                ++success;

            } catch(Exception exc) {

                exc.printStackTrace();

                registered.append("§c").append(listener.getClass().getSimpleName()).append("§4,");
                ++failed;

            }

        }

        Bukkit.getConsoleSender().sendMessage(registered.toString().substring(0, registered.length()-1)+"§f;");
        Bukkit.getConsoleSender().sendMessage("§fRegistered Total: "+(failed+success) +", Failed: "+failed+", Success: "+success);

    }

    public GlobalTaskManager getGlobalTaskManager(){
        return gtm;
    }

    public UserManager getUserManager() {
        return manager;
    }

}
