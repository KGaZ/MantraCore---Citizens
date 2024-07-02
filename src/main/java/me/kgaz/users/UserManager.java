package me.kgaz.users;

import me.kgaz.KNPC;
import me.kgaz.tasks.Tickable;
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
    public KNPC owner;

    public UserManager(KNPC owner) {

        this.owner = owner;

        packetInListenerList = new ArrayList<>();
        packetOutListenerList = new ArrayList<>();

        owner.registerListener(this);
        owner.getGlobalTaskManager().registerTask(this, false);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {

        User newUser = new User(e.getPlayer(), this);
        users.put(e.getPlayer().getName().toLowerCase(), newUser);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        users.get(e.getPlayer().getName().toLowerCase()).onQuit(e);
        users.remove(e.getPlayer().getName().toLowerCase());

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


    }

    public void registerPacketOutListener(PacketOutListener listener) {


    }

    public void unregisterPacketOutListener(PacketOutListener npc) {


    }

    public void unregisterPacketInListener(PacketInListener npc) {


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

    public KNPC getOwner() {

        return owner;

    }
}
