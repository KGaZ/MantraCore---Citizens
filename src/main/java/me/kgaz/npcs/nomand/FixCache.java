package me.kgaz.npcs.nomand;

import lombok.Getter;
import lombok.Setter;
import me.kgaz.KNPC;
import me.kgaz.npcs.NPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter @Setter
public class FixCache {

    private final static int VIEW_DISTANCE = 120;

    private int npcId;
    private boolean spawned;

    public FixCache(int npcId, int distance) {
        this.npcId = npcId;
        this.spawned = distance <= VIEW_DISTANCE;
    }

    public void check(KNPC main, Player player) {

        NPC npc = main.getNpcRegistry().getNpc(this.npcId);
        if (npc == null || player.getWorld() != npc.getLocation().getWorld())
            return;

        int currentDistance = (int) Math.ceil(player.getLocation().distance(npc.getLocation()));

        if (this.spawned) {
            if (currentDistance > VIEW_DISTANCE)
                this.spawned = false;
        } else {
            if (currentDistance <= VIEW_DISTANCE) {
                this.spawned = true;
                npc.refresh(player);
            }
        }

    }

}
