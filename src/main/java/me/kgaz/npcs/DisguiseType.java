package me.kgaz.npcs;

import net.minecraft.server.v1_8_R3.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public enum DisguiseType {

    PLAYER(EntityPlayer.class, 0.1),
    ZOMBIE(EntityZombie.class, 0.25),
    ZOMBIE_PIGMAN(EntityPigZombie.class, 0.25),
    BABY_ZOMBIE_PIGMAN(EntityPigZombie.class, -0.7, true),
    BABY_ZOMBIE(EntityZombie.class, -0.7, true),
    SKELETON(EntitySkeleton.class, 0.1),
    CREEPER(EntityCreeper.class, 0.1),
    SPIDER(EntitySpider.class, -0.8),
    COW(EntityCow.class, -0.4),
    BABY_COW(EntityCow.class, -0.7, true),
    PIG(EntityPig.class, -0.8),
    BABY_PIG(EntityPig.class, -0.7, true);

    private Class<? extends Entity> clazz;
    private double yModifier;
    private boolean baby;

    private DisguiseType(Class<? extends Entity> clazz, double yModifier){
        this.clazz = clazz;
        this.baby = false;
        this.yModifier = yModifier;
    }

    private DisguiseType(Class<? extends Entity> clazz, double yModifier, boolean baby){
        this.clazz = clazz;
        this.baby = baby;
        this.yModifier = yModifier;
    }

    public EntityLiving createEntity(World world) {

        try {

            Constructor constructor = clazz.getConstructor(World.class);
            EntityLiving el = (EntityLiving) constructor.newInstance(world);

            if(baby) try {

                Method m = clazz.getMethod("setBaby", boolean.class);
                m.setAccessible(true);
                m.invoke(el, true);

            } catch(Exception exc) {



            }

            return el;

        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;

    }

    public double getYModifier() {

        return yModifier;

    }
}
