package me.kgaz.events;

import me.kgaz.MantraLibs;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Spin {

    private MantraLibs main;
    public List<Location> spinners;

    public Spin(MantraLibs main) {
        this.spinners = new ArrayList<>();
        this.main = main;
    }

    public void spin(final Block block, Integer interval, final Boolean randomOff, final int spins) {
        if (!this.spinners.contains(block.getLocation())) {
            this.spinners.add(block.getLocation());
            final BlockFace[] rotations = {
                    BlockFace.EAST, BlockFace.EAST_SOUTH_EAST, BlockFace.SOUTH_EAST,
                    BlockFace.SOUTH_SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_WEST,
                    BlockFace.WEST_SOUTH_WEST, BlockFace.WEST, BlockFace.WEST_NORTH_WEST,
                    BlockFace.NORTH_WEST,
                    BlockFace.NORTH_NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST, BlockFace.NORTH_EAST,
                    BlockFace.EAST_NORTH_EAST };
            int b = 0;
            int speed = interval.intValue();
            final Skull skull = (Skull)block.getState();
            byte b1;
            int i;
            BlockFace[] arrayOfBlockFace1;
            for (i = (arrayOfBlockFace1 = rotations).length, b1 = 0; b1 < i; ) {
                BlockFace face = arrayOfBlockFace1[b1];
                if (face == skull.getRotation())
                    break;
                b++;
                b1++;
            }
            int cz = b;
            new BukkitRunnable() {

                int i;
                int h;

                public void run() {
                    skull.setRotation(rotations[this.i]);
                    skull.update(true);
                    this.i++;
                    if (this.h + 1 == spins && randomOff.booleanValue())
                        if ((new Random()).nextInt(100) < 20) {
                            cancel();
                            Spin.this.spinners.remove(block.getLocation());
                            return;
                        }
                    if (this.i == rotations.length) {
                        this.h++;
                        this.i = 0;
                        if (this.h == spins) {
                            cancel();
                            Spin.this.spinners.remove(block.getLocation());
                            return;
                        }
                    }
                }
            }.runTaskTimer(this.main, 0L, speed);
        }
    }

}
