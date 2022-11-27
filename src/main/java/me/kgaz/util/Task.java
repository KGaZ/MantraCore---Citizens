package me.kgaz.util;

public interface Task {

    public void run();

    public default boolean isActive() {
        return true;
    }

}
