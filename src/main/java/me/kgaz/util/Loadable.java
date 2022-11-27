package me.kgaz.util;

import me.kgaz.Citizens;

public interface Loadable {

    public default void onLoad(Citizens main) {}

    public default void onDisable(Citizens main) {}

}
