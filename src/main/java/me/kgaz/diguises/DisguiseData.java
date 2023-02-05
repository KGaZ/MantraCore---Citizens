package me.kgaz.diguises;

import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class DisguiseData {

    protected int entityId;
    protected LivingEntity entity;

    public DisguiseData(LivingEntity entity, int entityId){

        this.entity = entity;
        this.entityId = entityId;

    }

    public int getDisguiseID(){
        return entityId;
    }

    public void onDie(Entity entity){}

    public EntityLiving getEntity() {

        return ((CraftLivingEntity)entity).getHandle();

    }
}
