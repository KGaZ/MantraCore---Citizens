package me.kgaz.util;

import me.kgaz.KNPC;

public interface Loadable {

    public default void onLoad(KNPC main) {}

    public default void onDisable(KNPC main) {}

}
