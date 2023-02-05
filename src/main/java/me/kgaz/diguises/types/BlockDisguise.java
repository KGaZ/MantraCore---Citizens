package me.kgaz.diguises.types;

import me.kgaz.diguises.DisguiseData;
import org.bukkit.entity.LivingEntity;

public class BlockDisguise extends MaterialDependedDisguise {

    private boolean breakOnDeath;

    public BlockDisguise(LivingEntity entity) {
        super(entity, 70);
        this.itemId = 1;
        this.data = 0;
    }

    public BlockDisguise(LivingEntity entity, int itemId, byte data){
        super(entity, 70);
        this.itemId = itemId;
        this.data = data;
    }

    public BlockDisguise setBreakOnDeath(boolean set){
        breakOnDeath = set;
        return this;
    }

    public boolean breakOnDeath(){
        return breakOnDeath;
    }


}
