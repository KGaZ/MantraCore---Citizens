package me.kgaz.tab.tablist;

import org.bukkit.entity.Player;

/**
 * The highest level of a tab list.
 */
public interface Tab {
    Player getPlayer();

    /**
     * Enables the tab list, starts any necessary listeners/schedules.
     * @return The tab list.
     */
    Tab enable();

    /**
     * Disables the tab list: stops existing listeners/schedules.
     * @return The tab list.
     */
    Tab disable();
}
