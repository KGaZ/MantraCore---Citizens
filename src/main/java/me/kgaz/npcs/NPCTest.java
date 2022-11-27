package me.kgaz.npcs;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCTest implements Listener {

    @EventHandler
    public void onClick(NPCInteractEvent e) {

        Bukkit.broadcastMessage(e.getNpc().getEntityId() +" zostal klikniety przez "+e.getPlayer().getName()+"! PPM: "+e.isRightClick());

    }

}
