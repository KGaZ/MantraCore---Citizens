package me.kgaz.tab;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import me.kgaz.tab.tablist.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class Tabbed implements Listener {
    private static Map<Plugin,Tabbed> instances = new HashMap<>();
    @Getter @Setter static Level logLevel = Level.WARNING;

    @Getter private final Plugin plugin;
    private final Map<Player, Tab> tabLists;

    public Tabbed(Plugin plugin) {
        this.plugin = plugin;
        this.tabLists = new HashMap<>();
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        instances.put(plugin, this);
    }

    public static void log(Level level, String message) {
        if (level.intValue() >= logLevel.intValue())
            System.out.println("[" + level.getName() + "] " + message);
    }

    /**
     * Gets an instance of Tabbed from a plugin.
     * @param plugin
     * @return
     */
    public static Tabbed getTabbed(Plugin plugin) {
        return instances.get(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        destroyTabList(event.getPlayer());
    }

    /**
     * Get the current tab list of the player.
     * @param player
     * @return The tab list, or null if it wasn't present.
     */
    public Tab getTabList(Player player) {
        return this.tabLists.get(player);
    }

    /**
     * Disables the tab list of a player.
     * @param player
     * @return The tab list removed (or null if it wasn't present).
     */
    public Tab destroyTabList(Player player) {
        Tab tab = getTabList(player);
        if (tab == null)
            return null;
        this.tabLists.remove(player);
        return tab.disable();
    }

    /**
     * Disables a tab list.
     * @param tab
     * @return The tab list removed.
     */
    public Tab destroyTabList(Tab tab) {
        return destroyTabList(tab.getPlayer());
    }

    /**
     * Creates a new TitledTabList with the given parameters.
     * @param player
     * @return
     */
    public TitledTab newTitledTabList(Player player) {
        return put(player, new TitledTab(player).enable());
    }

    /**
     * Creates a new DefaultTabList.
     * @param player
     * @return
     */
    public DefaultTab newDefaultTabList(Player player) {
        return put(player, new DefaultTab(this, player, -1).enable());
    }

    /**
     * Creates a new CustomTabList with the given parameters.
     * @param player
     * @return
     */
    public SimpleTab newSimpleTabList(Player player) {
        return newSimpleTabList(player, SimpleTab.MAXIMUM_ITEMS);
    }

    /**
     * Creates a new CustomTabList with the given parameters.
     * @param player
     * @param maxItems
     * @return
     */
    public SimpleTab newSimpleTabList(Player player, int maxItems) {
        return newSimpleTabList(player, maxItems, -1);
    }

    /**
     * Creates a new CustomTabList with the given parameters.
     * @param player
     * @param maxItems
     * @param minColumnWidth
     * @return
     */
    public SimpleTab newSimpleTabList(Player player, int maxItems, int minColumnWidth) {
        return newSimpleTabList(player, maxItems, minColumnWidth, -1);
    }

    /**
     * Creates a new CustomTabList with the given parameters.
     * @param player
     * @param maxItems
     * @param minColumnWidth
     * @param maxColumnWidth
     * @return
     */
    public SimpleTab newSimpleTabList(Player player, int maxItems, int minColumnWidth, int maxColumnWidth) {
        return put(player, new SimpleTab(this, player, maxItems, minColumnWidth, maxColumnWidth).enable());
    }

    /**
     * Creates a new TableTabList with the given parameters.
     * @param player
     * @return
     */
    public Tablist newTableTabList(Player player) {
        return newTableTabList(player, 4);
    }

    /**
     * Creates a new TableTabList with the given parameters.
     * @param player
     * @param columns
     * @return
     */
    public Tablist newTableTabList(Player player, int columns) {
        return newTableTabList(player, columns, -1);
    }

    /**
     * Creates a new TableTabList with the given parameters.
     * @param player
     * @param columns
     * @param minColumnWidth
     * @return
     */
    public Tablist newTableTabList(Player player, int columns, int minColumnWidth) {
        return newTableTabList(player, columns, minColumnWidth, -1);
    }

    /**
     * Creates a new TableTabList with the given parameters.
     * @param player
     * @param columns
     * @param minColumnWidth
     * @param maxColumnWidth
     * @return
     */
    public Tablist newTableTabList(Player player, int columns, int minColumnWidth, int maxColumnWidth) {
        return put(player, new Tablist(this, player, columns, minColumnWidth, maxColumnWidth).enable());
    }

    private <T extends Tab> T put(Player player, T tabList) {
        Preconditions.checkArgument(!this.tabLists.containsKey(player), "player '" + player.getName() + "' already has a tablist");
        this.tabLists.put(player, tabList);
        return tabList;
    }
}
