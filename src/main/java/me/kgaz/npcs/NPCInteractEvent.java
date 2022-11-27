package me.kgaz.npcs;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NPCInteractEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private NPC npc;
    private boolean rightClick;

    public NPCInteractEvent(Player player, NPC npc, boolean rightClick) {

        this.player = player;
        this.npc = npc;
        this.rightClick = rightClick;

    }

    public Player getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public NPC getNpc() {
        return npc;
    }

    public boolean isRightClick() {
        return rightClick;
    }

}
