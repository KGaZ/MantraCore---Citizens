package me.kgaz.diguises.types;

import me.kgaz.diguises.DisguiseData;
import me.kgaz.util.ParticleEffect;
import net.minecraft.server.v1_8_R3.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class MaterialDependedDisguise extends DisguiseData {

    protected int itemId;
    protected byte data;
    protected boolean breakOnDeath;

    protected MaterialDependedDisguise(LivingEntity entity, int entityId) {
        super(entity, entityId);
        breakOnDeath = false;
    }

    public MaterialDependedDisguise setItemId(int itemId){
        this.itemId = itemId;
        return this;
    }

    public MaterialDependedDisguise setData(byte data){
        this.data = data;
        return this;
    }

    public MaterialDependedDisguise setDisguiseData(MaterialData data){
        this.itemId = data.getItemTypeId();
        this.data = data.getData();
        return this;
    }

    public MaterialDependedDisguise setBreakOnDeath(boolean set){
        breakOnDeath = set;
        return this;
    }

    public MaterialDependedDisguise setDisguiseData(ItemStack is) {
        return setDisguiseData(is.getData());
    }

    public int getDisguiseItemID(){
        return itemId;
    }

    public byte getDisguiseByteData(){
        return data;
    }

    @Override
    public void onDie(Entity entity) {

        ParticleEffect.BLOCK_CRACK.sendData(Bukkit.getOnlinePlayers(), entity.getLocation().getX(), entity.getLocation().getY()+0.5, entity.getLocation().getZ(), 0.3, 0.3, 0.3, 0.2, 128, 20, itemId, data);

    }

    public ItemStack getItemStack(){
        return new ItemStack(Material.getMaterial(itemId), 1, data);
    }


}
