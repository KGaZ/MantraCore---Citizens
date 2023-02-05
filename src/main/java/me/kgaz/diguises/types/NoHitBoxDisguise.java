package me.kgaz.diguises.types;

import org.bukkit.Location;

public interface NoHitBoxDisguise {

    public float getRadius();
    public Location getCenterLocation(Location location);

}