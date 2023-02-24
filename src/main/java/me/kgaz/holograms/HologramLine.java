package me.kgaz.holograms;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class HologramLine {

    private final Hologram owner;
    private Location loc;
    private String name;
    private EntityArmorStand entity;
    private CustomLine custom;

    public HologramLine(Hologram holo, Location loc, String name) {

        this.owner = holo;
        this.loc = loc;
        this.name = name;
        this.entity = null;

    }

    public void setCustomLine(CustomLine line) {
        this.custom = line;
    }

    public void removeCustomLine(){
        this.custom = null;
    }

    public void spawn() {

        if(entity == null) {

            ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setCustomName(name);
            stand.setCustomNameVisible(true);
            stand.setVisible(false);

            this.entity = ((CraftArmorStand)stand).getHandle();

        }

    }

    public void rename(String name) {

        this.name = name;
        this.entity.getBukkitEntity().setCustomName(name);

    }

    public void sendSpawnData(Player player) {

        if(entity != null) {

            PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving(entity);

            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);

        }

    }

    public void sendDespawnData(Player player) {

        if(entity != null) {

            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entity.getId());

            ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);

        }

    }

    public void setLocation(Location loc) {

        this.loc = loc;
        despawn();
        spawn();

    }

    public void despawn() {

        if(entity != null) {

            entity.getWorld().removeEntity(entity);
            entity = null;

        }

    }

    int getEntityId(){

        return entity == null ? 0 : entity.getId();

    }

    boolean shouldModify(Player player) {

        if(custom == null) return false;

        return !custom.onSending(player, name).equals(name);

    }

}
