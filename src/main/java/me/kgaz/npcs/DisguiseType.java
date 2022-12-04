package me.kgaz.npcs;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public enum DisguiseType {

    PLAYER(EntityPlayer.class, 0.1),
    ZOMBIE(EntityZombie.class, 0.25),
    SKELETON(EntitySkeleton.class, 0.25),
    CREEPER(EntityCreeper.class, 0.25),
    SPIDER(EntitySpider.class, -0.8),
    PIG(EntityPig.class, -0.8);

    private Class<? extends Entity> clazz;
    private double yModifier;

    private DisguiseType(Class<? extends Entity> clazz, double yModifier){
        this.clazz = clazz;
        this.yModifier = yModifier;
    }

    public EntityLiving createEntity(World world) {

        try {
            Constructor constructor = clazz.getConstructor(World.class);
            return (EntityLiving) constructor.newInstance(world);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;

    }

    public double getYModifier() {
        return yModifier;
    }
}
