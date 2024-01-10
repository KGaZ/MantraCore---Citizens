package me.kgaz.users;

import me.kgaz.MantraLibs;
import me.kgaz.kasyno.poker.table.PokerTable;
import me.kgaz.tab.tablist.Tablist;
import me.kgaz.tasks.Tickable;
import me.kgaz.util.Oczko;
import me.kgaz.util.PacketInListener;
import me.kgaz.util.PacketOutListener;
import me.kgaz.util.Removeable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager implements Listener, Tickable {

    private Map<String, User> users = new HashMap<>();
    private List<PacketInListener> packetInListenerList;
    private List<PacketOutListener> packetOutListenerList;
    public MantraLibs owner;

    public UserManager(MantraLibs owner) {

        this.owner = owner;

        packetInListenerList = new ArrayList<>();
        packetOutListenerList = new ArrayList<>();

        owner.registerListener(this);
        owner.getGlobalTaskManager().registerTask(this, false);

    }

    private Map<Player, Tablist> tablists = new HashMap<>();

    private Oczko oczko1 = null, oczko2= null;
    private PokerTable table1;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        User newUser = new User(e.getPlayer(), this);
        users.put(e.getPlayer().getName().toLowerCase(), newUser);

        Tablist tab = owner.getTab().newTableTabList(e.getPlayer());
        tablists.put(e.getPlayer(), tab);

        if(oczko1 == null) {
            oczko1 = new Oczko("1");
            oczko1.onLoad(owner);
        }

        if(oczko2 == null) {
            oczko2 = new Oczko("2");
            oczko2.onLoad(owner);
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        users.get(e.getPlayer().getName().toLowerCase()).onQuit(e);
        users.remove(e.getPlayer().getName().toLowerCase());

        tablists.remove(e.getPlayer());

    }

    public Tablist getTablist(Player player) {

        return tablists.get(player);

    }

    public User getUser(String name) {

        return users.get(name.toLowerCase());

    }

    public User getUser(Player player) {

        return users.containsKey(player.getName().toLowerCase()) ? users.get(player.getName().toLowerCase()) : new User(player, this);

    }

    public List<PacketInListener> getPacketInListenerList() {
        return packetInListenerList;
    }

    public List<PacketOutListener> getPacketOutListenerList() {
        return packetOutListenerList;
    }

    public void registerPacketInListener(PacketInListener listener) {

        packetInListenerList.add(listener);

    }

    public void registerPacketOutListener(PacketOutListener listener) {

        packetOutListenerList.add(listener);

    }

    public void unregisterPacketOutListener(PacketOutListener npc) {

        packetOutListenerList.remove(npc);

    }

    public void unregisterPacketInListener(PacketInListener npc) {

        packetInListenerList.remove(npc);

    }

    @Override
    public void run() {

        packetInListenerList.removeIf(packetInListener -> {
            return (packetInListener instanceof Removeable && !((Removeable) packetInListener).isActive());
        });

        packetOutListenerList.removeIf(packetInListener -> {
            return (packetInListener instanceof Removeable && !((Removeable) packetInListener).isActive());
        });

    }

    @Override
    public int getPeriod() {
        return 20;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public void disable() {

        users.keySet().forEach(key -> users.get(key).disable());

    }

}
