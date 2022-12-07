package me.kgaz.tasks;

public interface Tickable {

    void run();

    default int getPeriod(){
        return 1;
    }

    public boolean isCancelled();

}
