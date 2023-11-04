package me.kgaz.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {

    public List<Block> sphere(final Location center, final int radius) {

        List<Block> sphere = new ArrayList<Block>();

        for (int Y = -radius; Y < radius; Y++)
            for (int X = -radius; X < radius; X++)
                for (int Z = -radius; Z < radius; Z++)
                    if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius) {
                        final Block block = center.getWorld().getBlockAt(X + center.getBlockX(), Y + center.getBlockY(), Z + center.getBlockZ());
                        sphere.add(block);
                    }

        return sphere;
    }

}
