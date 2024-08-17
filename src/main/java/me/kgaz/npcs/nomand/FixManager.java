package me.kgaz.npcs.nomand;

import me.kgaz.KNPC;
import me.kgaz.npcs.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FixManager implements Listener {

    private final KNPC main;

    private final HashMap<Player, HashSet<FixCache>> cache = new HashMap<>();

    public FixManager(KNPC main) {
        this.main = main;
        main.registerListener(this);
        registerTimerTask();
    }

    public void registerTimerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {

                for(Player player : cache.keySet())
                    for(FixCache fixCache : cache.get(player))
                        fixCache.check(main, player);

            }
        }.runTaskTimer(main, 100, 100);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        cache.put(player, new HashSet<>());
        refresh(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cache.remove(event.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        refresh(event.getPlayer());
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.UNKNOWN || event.getTo().getWorld() != event.getFrom().getWorld())
            return;

        refresh(event.getPlayer());
    }

    // Usuwa biezace npc z pamieci dla gracza i wczytuje nowe npc z aktualnego swiata.
    private void refresh(Player player) {
        if (!cache.containsKey(player))
            return;

        Location location = player.getLocation();

        cache.get(player).clear();

        for(NPC npc : main.getNpcRegistry().getNpcs())
            if (npc.getLocation().getWorld() == location.getWorld())
                cache.get(player).add(new FixCache(npc.getCitizenId(), (int) Math.ceil(npc.getLocation().distance(location))));
    }

}
