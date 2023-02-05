package me.kgaz.diguises;

import me.kgaz.diguises.types.*;
import me.kgaz.npcs.DisguiseType;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class DisguiseCreator {

    private DisguiseData work;

    public DisguiseCreator(LivingEntity entity, EntityType type) {

        this(entity, type.getTypeId());

    }

    public DisguiseCreator(LivingEntity entity, int id) {

        if(id == 2) {

            work = new ItemDisguise(entity);

            return;

        }

        if(id == 70) {

            work = new BlockDisguise(entity);

            return;

        }

        if(id == 0) {

            work = new PlayerDisguise(entity);

            return;

        }

        work = new LivingEntityDisguise(entity, id);

    }

    public DisguiseCreator setSkin(Player player){

        if(work instanceof PlayerDisguise) {

            PlayerDisguise disg = (PlayerDisguise) work;

            disg.setSkin(((CraftPlayer)player).getHandle().getProfile());

        }

        return this;

    }

    public DisguiseCreator setSkin(String texture, String signature){

        if(work instanceof PlayerDisguise) {

            PlayerDisguise disg = (PlayerDisguise) work;

            disg.setSkinTexture(texture).setSignature(signature);

        }

        return this;

    }



    public DisguiseCreator setItemID(int itemID){

        if(work instanceof MaterialDependedDisguise){

            MaterialDependedDisguise disg = (MaterialDependedDisguise) work;

            disg.setItemId(itemID);

        }

        return this;

    }

    public DisguiseCreator setItemData(byte data){

        if(work instanceof MaterialDependedDisguise){

            MaterialDependedDisguise disg = (MaterialDependedDisguise) work;

            disg.setData(data);

        }

        return this;

    }

    public DisguiseCreator setItemData(MaterialData data){

        if(work instanceof MaterialDependedDisguise){

            MaterialDependedDisguise disg = (MaterialDependedDisguise) work;

            disg.setDisguiseData(data);

        }

        return this;

    }

    public DisguiseCreator setItemData(ItemStack data){

        if(work instanceof MaterialDependedDisguise){

            MaterialDependedDisguise disg = (MaterialDependedDisguise) work;

            disg.setDisguiseData(data);

        }

        return this;

    }

    public DisguiseCreator setBreakOnDeath(boolean shouldBreak){

        if(work instanceof MaterialDependedDisguise){

            MaterialDependedDisguise disguise = (MaterialDependedDisguise) work;

            disguise.setBreakOnDeath(shouldBreak);

        }

        return this;

    }

    public DisguiseData generate(){

        return work;

    }


}