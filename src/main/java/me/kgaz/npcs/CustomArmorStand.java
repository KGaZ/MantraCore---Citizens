package me.kgaz.npcs;

import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

public class CustomArmorStand extends EntityArmorStand {

    public CustomArmorStand(World world) {

        super(world);
        world.removeEntity(this);

    }

    public CustomArmorStand(World world, double x1, double x2, double x3) {

        super(world,x1,x2,x3);
        world.removeEntity(this);

    }

    public CustomArmorStand(Location location) {

        super(((CraftWorld)location.getWorld()).getHandle());

        this.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

    }

}
