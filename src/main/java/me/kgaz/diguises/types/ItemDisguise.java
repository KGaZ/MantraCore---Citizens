package me.kgaz.diguises.types;

import me.kgaz.util.ParticleEffect;
import net.minecraft.server.v1_8_R3.EntityItem;
import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class ItemDisguise extends MaterialDependedDisguise implements NoHitBoxDisguise {

    public ItemDisguise(LivingEntity entity) {
        super(entity, 2);
    }

    @Override
    public void onDie(Entity entity) {

        if(!breakOnDeath) return;

        Location loc = entity.getLocation().add(0, 0.1, 0);

        if(!Material.getMaterial(itemId).isBlock())

            ParticleEffect.ITEM_CRACK.sendData(Bukkit.getOnlinePlayers(), loc, 0.2  , 0.2, 0.2, 0.2, 64, 20, itemId, data);

        else ParticleEffect.BLOCK_CRACK.sendData(Bukkit.getOnlinePlayers(), loc, 0.2  , 0.2, 0.2, 0.2, 64, 20, itemId, data);

    }

    public EntityItem generateEntity(){

        EntityItem item = new EntityItem(((CraftWorld)entity.getWorld()).getHandle(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), CraftItemStack.asNMSCopy(super.getItemStack()));

        try {

            Field id = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("id");

            id.setAccessible(true);
            id.setInt(item, entityId);


        } catch(Exception exc) {

            exc.printStackTrace();
            return null;

        }

        return item;

    }

    @Override
    public float getRadius() {
        return 2f;
    }
    @Override
    public Location getCenterLocation(Location location) {return location.add(0, 0.12, 0);}

}
