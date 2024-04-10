package me.kgaz.npcs;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class RemoveArmorStand extends EntityArmorStand {

    public RemoveArmorStand(World world) {
        super(world);
    }

    public RemoveArmorStand(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    public RemoveArmorStand(Location loc) {

        super(((CraftWorld)loc.getWorld()).getHandle());
        setPosition(loc.getX(), loc.getY(), loc.getZ());
        setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

    }

}
