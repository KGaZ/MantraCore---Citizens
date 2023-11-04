package me.kgaz.util;

import me.kgaz.MantraLibs;

public interface Loadable {

    public default void onLoad(MantraLibs main) {}

    public default void onDisable(MantraLibs main) {}

}
