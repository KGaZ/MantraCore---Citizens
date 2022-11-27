package me.kgaz.users;

import me.kgaz.Citizens;
import me.kgaz.npcs.NPC;
import me.kgaz.util.PacketInListener;
import me.kgaz.util.PacketOutListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager implements Listener {

    private Map<String, User> users = new HashMap<>();
    private List<PacketInListener> packetInListenerList;
    private List<PacketOutListener> packetOutListenerList;
    public Citizens owner;

    public UserManager(Citizens owner) {

        this.owner = owner;

        packetInListenerList = new ArrayList<>();
        packetOutListenerList = new ArrayList<>();

        owner.registerListener(this);

    }

    @EventHandler
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
}
