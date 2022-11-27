package me.kgaz.util;

import java.util.*;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import io.netty.util.internal.ThreadLocalRandom;

public enum ParticleEffect {

    FLAME(EnumParticle.FLAME),
    BARRIER(EnumParticle.BARRIER),
    BLOCK_CRACK(EnumParticle.BLOCK_CRACK, Feature.DATA),
    BLOCK_DUST(EnumParticle.BLOCK_DUST, Feature.DATA),
    CLOUD(EnumParticle.CLOUD),
    CRIT(EnumParticle.CRIT),
    CRIT_MAGIC(EnumParticle.CRIT_MAGIC),
    DRIP_LAVA(EnumParticle.DRIP_LAVA),
    DRIP_WATER(EnumParticle.DRIP_WATER),
    ENCHANTMENT_TABLE(EnumParticle.ENCHANTMENT_TABLE),
    EXPLOSION_HUGE(EnumParticle.EXPLOSION_HUGE),
    EXPLOSION_LARGE(EnumParticle.EXPLOSION_LARGE),
    EXPLOSION_NORMAL(EnumParticle.EXPLOSION_NORMAL),
    FIREWORKS_SPARK(EnumParticle.FIREWORKS_SPARK),
    FOOTSTEP(EnumParticle.FOOTSTEP),
    HEART(EnumParticle.HEART),
    ITEM_CRACK(EnumParticle.ITEM_CRACK, Feature.DATA),
    ITEM_TAKE(EnumParticle.ITEM_TAKE),
    LAVA(EnumParticle.LAVA),
    MOB_APPEARANCE(EnumParticle.MOB_APPEARANCE),
    NOTE(EnumParticle.NOTE, Feature.COLOR),
    PORTAL(EnumParticle.PORTAL),
    REDSTONE(EnumParticle.REDSTONE, Feature.COLOR),
    SLIME(EnumParticle.SLIME),
    SMOKE_LARGE(EnumParticle.SMOKE_LARGE),
    SMOKE_NORMAL(EnumParticle.SMOKE_NORMAL),
    SNOW_SHOVEL(EnumParticle.SNOW_SHOVEL),
    SNOWBALL(EnumParticle.SNOWBALL),
    SPELL(EnumParticle.SPELL),
    SPELL_INSTANT(EnumParticle.SPELL_INSTANT),
    SPELL_MOB(EnumParticle.SPELL_MOB, Feature.COLOR),
    SPELL_MOB_AMBIENT(EnumParticle.SPELL_MOB_AMBIENT),
    SPELL_WITCH(EnumParticle.SPELL_WITCH),
    SUSPENDED(EnumParticle.SUSPENDED),
    SUSPENDED_DEPTH(EnumParticle.SUSPENDED_DEPTH),
    TOWN_AURA(EnumParticle.TOWN_AURA),
    VILLAGER_ANGRY(EnumParticle.VILLAGER_ANGRY),
    VILLAGER_HAPPY(EnumParticle.VILLAGER_HAPPY),
    WATER_BUBBLE(EnumParticle.WATER_BUBBLE),
    WATER_DROP(EnumParticle.WATER_DROP),
    WATER_SPLASH(EnumParticle.WATER_SPLASH),
    WATER_WAKE(EnumParticle.WATER_WAKE);

    private EnumParticle particle;
    private EnumSet<Feature> features;

    private ParticleEffect(EnumParticle particle) {

        this.particle = particle;
        features = EnumSet.noneOf(Feature.class);

    }

    private ParticleEffect(EnumParticle particle, Feature... features) {

        this.particle = particle;
        this.features = EnumSet.noneOf(Feature.class);

        this.features.addAll(Arrays.asList(features));

    }

    public boolean hasFeature(Feature f) {

        return this.features.contains(f);

    }

    public void send(Collection<? extends Player> players, float x, float y, float z, float offSetX, float offSetY, float offSetZ, float speed, int count, double viewRange) {

        double squareRanged = viewRange * viewRange;

        List<Player> left = new ArrayList<>();

        if(viewRange != 0) for(Player player : players) {

            if(player.getLocation().distance(new Location(player.getWorld(), x, y, z)) < squareRanged) {

                left.add(player);

            }

        }

        else left = new ArrayList<>(players);

        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(

                this.particle, true,
                x, y, z,
                offSetX, offSetY, offSetZ,
                speed,
                count
        );

        left.forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet));

    }

    public void send(Collection<? extends Player> players, double x, double y, double z, double offSetX, double offSetY, double offSetZ, double speed, int count, double viewRange) {
        this.send(players, (float) x, (float) y, (float) z, (float) offSetX, (float) offSetY, (float) offSetZ, (float) speed, (int) count, (double) viewRange);
    }

    public void send(Collection<? extends Player> players, Location loc, double offSetX, double offSetY, double offSetZ, double speed, int count, double viewRange) {
        this.send(players, loc.getX(), loc.getY(), loc.getZ(), offSetX, offSetY, offSetZ, speed, count, viewRange);
    }

    public void send(Collection<? extends Player> players, double x, double y, double z, double offSetX, double offSetY, double offSetZ, double speed, int count) {
        this.send(players, x, y, z, offSetX, offSetY, offSetZ, speed, count, 0);
    }

    public void send(Collection<? extends Player> players, Location loc, double offSetX, double offSetY, double offSetZ, double speed, int count) {
        this.send(players, loc.getX(), loc.getY(), loc.getZ(), offSetX, offSetY, offSetZ, speed, count, 0);
    }

    public void sendMovingParticle(Collection<? extends Player> players, double x, double y, double z, double velocityX, double velocityY, double velocityZ, double speed, double viewRange) {
        this.send(players, x, y, z, velocityX, velocityY, velocityZ, speed, 0, viewRange);
    }

    public void sendMovingParticle(Collection<? extends Player> players, double x, double y, double z, Vector velocity, double speed, double viewRange) {
        this.send(players, x, y, z, velocity.getX(), velocity.getY(), velocity.getZ(), speed, 0, viewRange);
    }

    public void sendMovingParticle(Collection<? extends Player> players, Location loc, double velocityX, double velocityY, double velocityZ, double speed, double viewRange) {
        this.send(players, loc.getX(), loc.getY(), loc.getZ(), velocityX, velocityY, velocityZ, speed, 0, viewRange);
    }

    public void sendMovingParticle(Collection<? extends Player> players, Location loc, Vector velocity, double speed, double viewRange) {
        this.send(players, loc.getX(), loc.getY(), loc.getZ(), velocity.getX(), velocity.getY(), velocity.getZ(), speed, 0, viewRange);
    }

    public void sendMovingParticle(Collection<? extends Player> players, double x, double y, double z, double velocityX, double velocityY, double velocityZ, double speed) {
        this.send(players, x, y, z, velocityX, velocityY, velocityZ, speed, 0, 0);
    }

    public void sendMovingParticle(Collection<? extends Player> players, double x, double y, double z, Vector velocity, double speed) {
        this.send(players, x, y, z, velocity.getX(), velocity.getY(), velocity.getZ(), speed, 0, 0);
    }

    public void sendMovingParticle(Collection<? extends Player> players, Location loc, double velocityX, double velocityY, double velocityZ, double speed) {
        this.send(players, loc.getX(), loc.getY(), loc.getZ(), velocityX, velocityY, velocityZ, speed, 0, 0);
    }

    public void sendMovingParticle(Collection<? extends Player> players, Location loc, Vector velocity, double speed) {
        this.send(players, loc.getX(), loc.getY(), loc.getZ(), velocity.getX(), velocity.getY(), velocity.getZ(), speed, 0, 0);
    }

    public void sendColor(Collection<? extends Player> players, float x, float y, float z, Color color, double viewRange) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, x, y, z, getColor(color.getRed()), getColor(color.getGreen()), getColor(color.getBlue()), 1, 0, viewRange);

    }

    public void sendColor(Collection<? extends Player> players, float x, float y, float z, int r, int g, int b, double viewRange) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, x, y, z, getColor(r), getColor(g), getColor(b), 1, 0, viewRange);

    }

    public void sendColor(Collection<? extends Player> players, float x, float y, float z, double offSetX, double offSetY, double offSetZ, int r, int g, int b, int amount, double viewRange) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        ThreadLocalRandom random = ThreadLocalRandom.current();

        float colorRed = getColor(r);
        float colorGreen = getColor(g);
        float colorBlue = getColor(b);

        for(int i = 0; i < amount; i++) {

            double newX = x + random.nextDouble(-offSetX, offSetX);
            double newY = y + random.nextDouble(-offSetY, offSetY);
            double newZ = z + random.nextDouble(-offSetZ, offSetZ);

            this.send(players, newX, newY, newZ, colorRed, colorGreen, colorBlue, 1, 0, viewRange);

        }

    }

    public void sendColor(Collection<? extends Player> players, float x, float y, float z, double offSetX, double offSetY, double offSetZ, Color color, int amount, double viewRange) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        float colorRed = getColor(color.getRed());
        float colorGreen = getColor(color.getGreen());
        float colorBlue = getColor(color.getBlue());

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for(int i = 0; i < amount; i++) {

            double newX = x + random.nextDouble(-offSetX, offSetX);
            double newY = y + random.nextDouble(-offSetY, offSetY);
            double newZ = z + random.nextDouble(-offSetZ, offSetZ);

            this.send(players, newX, newY, newZ,  colorRed, colorGreen, colorBlue, 1, 0, viewRange);

        }

    }

    public void sendColor(Collection<? extends Player> players, Location loc, Color color, double viewRange) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, loc.getX(), loc.getY(), loc.getZ(), getColor(color.getRed()), getColor(color.getGreen()), getColor(color.getBlue()), 1, 0, viewRange);

    }

    public void sendColor(Collection<? extends Player> players, Location loc, int r, int g, int b, double viewRange) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, loc.getX(), loc.getY(), loc.getZ(), getColor(r), getColor(g), getColor(b), 1, 0, viewRange);

    }

    public void sendColor(Collection<? extends Player> players, Location loc, double offSetX, double offSetY, double offSetZ, int r, int g, int b, int amount, double viewRange) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        ThreadLocalRandom random = ThreadLocalRandom.current();

        float colorRed = getColor(r);
        float colorGreen = getColor(g);
        float colorBlue = getColor(b);

        for(int i = 0; i < amount; i++) {

            double newX = loc.getX() + random.nextDouble(-offSetX, offSetX);
            double newY = loc.getY() + random.nextDouble(-offSetY, offSetY);
            double newZ = loc.getZ() + random.nextDouble(-offSetZ, offSetZ);

            this.send(players, newX, newY, newZ, colorRed, colorGreen, colorBlue, 1, 0, viewRange);

        }

    }

    public void sendColor(Collection<? extends Player> players, Location loc, double offSetX, double offSetY, double offSetZ, Color color, int amount, double viewRange) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        float colorRed = getColor(color.getRed());
        float colorGreen = getColor(color.getGreen());
        float colorBlue = getColor(color.getBlue());

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for(int i = 0; i < amount; i++) {

            double newX = loc.getX() + random.nextDouble(-offSetX, offSetX);
            double newY = loc.getY() + random.nextDouble(-offSetY, offSetY);
            double newZ = loc.getZ() + random.nextDouble(-offSetZ, offSetZ);

            this.send(players, newX, newY, newZ,  colorRed, colorGreen, colorBlue, 1, 0, viewRange);

        }

    }

    //NEW

    public void sendColor(Collection<? extends Player> players, float x, float y, float z, Color color) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, x, y, z, getColor(color.getRed()), getColor(color.getGreen()), getColor(color.getBlue()), 1, 0);

    }

    public void sendColor(Collection<? extends Player> players, float x, float y, float z, int r, int g, int b) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, x, y, z, getColor(r), getColor(g), getColor(b), 1, 0);

    }

    public void sendColor(Collection<? extends Player> players, float x, float y, float z, double offSetX, double offSetY, double offSetZ, int r, int g, int b, int amount) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        ThreadLocalRandom random = ThreadLocalRandom.current();

        float colorRed = getColor(r);
        float colorGreen = getColor(g);
        float colorBlue = getColor(b);

        for(int i = 0; i < amount; i++) {

            double newX = x + random.nextDouble(-offSetX, offSetX);
            double newY = y + random.nextDouble(-offSetY, offSetY);
            double newZ = z + random.nextDouble(-offSetZ, offSetZ);

            this.send(players, newX, newY, newZ, colorRed, colorGreen, colorBlue, 1, 0);

        }

    }

    public void sendColor(Collection<? extends Player> players, float x, float y, float z, double offSetX, double offSetY, double offSetZ, Color color, int amount) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        float colorRed = getColor(color.getRed());
        float colorGreen = getColor(color.getGreen());
        float colorBlue = getColor(color.getBlue());

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for(int i = 0; i < amount; i++) {

            double newX = x + random.nextDouble(-offSetX, offSetX);
            double newY = y + random.nextDouble(-offSetY, offSetY);
            double newZ = z + random.nextDouble(-offSetZ, offSetZ);

            this.send(players, newX, newY, newZ,  colorRed, colorGreen, colorBlue, 1, 0);

        }

    }

    public void sendColor(Collection<? extends Player> players, Location loc, Color color) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, loc.getX(), loc.getY(), loc.getZ(), getColor(color.getRed()), getColor(color.getGreen()), getColor(color.getBlue()), 1, 0);

    }

    public void sendColor(Collection<? extends Player> players, double x, double y, double z, Color color) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, x, y, z, getColor(color.getRed()), getColor(color.getGreen()), getColor(color.getBlue()), 1, 0);

    }

    public void sendColor(Collection<? extends Player> players, Location loc, int r, int g, int b) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        this.send(players, loc.getX(), loc.getY(), loc.getZ(), getColor(r), getColor(g), getColor(b), 1, 0);

    }

    public void sendColor(Collection<? extends Player> players, Location loc, double offSetX, double offSetY, double offSetZ, int r, int g, int b, int amount) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        ThreadLocalRandom random = ThreadLocalRandom.current();

        float colorRed = getColor(r);
        float colorGreen = getColor(g);
        float colorBlue = getColor(b);

        for(int i = 0; i < amount; i++) {

            double newX = loc.getX() + random.nextDouble(-offSetX, offSetX);
            double newY = loc.getY() + random.nextDouble(-offSetY, offSetY);
            double newZ = loc.getZ() + random.nextDouble(-offSetZ, offSetZ);

            this.send(players, newX, newY, newZ, colorRed, colorGreen, colorBlue, 1, 0);

        }

    }

    public void sendColor(Collection<? extends Player> players, Location loc, double offSetX, double offSetY, double offSetZ, Color color, int amount) {

        if(!this.hasFeature(Feature.COLOR)) throw new IllegalArgumentException("Color packet sent on non color Particle!");

        float colorRed = getColor(color.getRed());
        float colorGreen = getColor(color.getGreen());
        float colorBlue = getColor(color.getBlue());

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for(int i = 0; i < amount; i++) {

            double newX = loc.getX() + random.nextDouble(-offSetX, offSetX);
            double newY = loc.getY() + random.nextDouble(-offSetY, offSetY);
            double newZ = loc.getZ() + random.nextDouble(-offSetZ, offSetZ);

            this.send(players, newX, newY, newZ,  colorRed, colorGreen, colorBlue, 1, 0);

        }

    }

    //new int[] {
    //id,
    //id | data << 12 }

    public void sendData(Collection<? extends Player> players, double x, double y, double z, double offSetX, double offSetY, double offSetZ, double speed, int count, int viewRange, int itemId, byte data) {

        if(!this.hasFeature(Feature.DATA)) throw new IllegalArgumentException("Data packet sent on non data Particle!");

        double squareRanged = viewRange * viewRange;

        List<Player> left = new ArrayList<>();

        if(viewRange != 0) for(Player player : players) {

            if(player.getLocation().distance(new Location(player.getWorld(), x, y, z)) < squareRanged) {

                left.add(player);

            }

        }

        else left = new ArrayList<>(players);

        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(

                this.particle, true,
                (float) x,(float)  y,(float)  z,
                (float) offSetX, (float) offSetY, (float) offSetZ,
                (float) speed,
                count,
                new int[] {
                        itemId,
                        itemId | data << 12
                }
        );

        left.forEach(player -> ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet));

    }

    public void sendData(Collection<? extends Player> players, double x, double y, double z, double offSetX, double offSetY, double offSetZ, double speed, int count, int viewRange, ItemStack is) {

        this.sendData(players, x, y, z, offSetX, offSetY, offSetZ, speed, count, viewRange, is.getType().getId(), (byte) is.getTypeId());

    }

    public void sendData(Collection<? extends Player> players, Location location, double offSetX, double offSetY, double offSetZ, double speed, int count, int viewRange, int itemId, byte data) {

        this.sendData(players, location.getX(), location.getY(), location.getZ(), offSetX, offSetY, offSetZ, speed, count, viewRange, itemId, data);

    }

    public void sendData(Collection<? extends Player> players, Location loc, double offSetX, double offSetY, double offSetZ, double speed, int count, int viewRange, ItemStack is) {

        this.sendData(players, loc.getX(), loc.getY(), loc.getZ(), offSetX, offSetY, offSetZ, speed, count, viewRange, is.getType().getId(), (byte) is.getTypeId());

    }

    public void sendData(Collection<? extends Player> players, double x, double y, double z, double offSetX, double offSetY, double offSetZ, double speed, int count, ItemStack is) {

        this.sendData(players, x, y, z, offSetX, offSetY, offSetZ, speed, count, 0, is.getType().getId(), (byte) is.getTypeId());

    }

    public void sendData(Collection<? extends Player> players, Location location, double offSetX, double offSetY, double offSetZ, double speed, int count, int itemId, byte data) {

        this.sendData(players, location.getX(), location.getY(), location.getZ(), offSetX, offSetY, offSetZ, speed, count, 0, itemId, data);

    }

    public void sendData(Collection<? extends Player> players, Location loc, double offSetX, double offSetY, double offSetZ, double speed, int count, ItemStack is) {

        this.sendData(players, loc.getX(), loc.getY(), loc.getZ(), offSetX, offSetY, offSetZ, speed, count, 0, is.getType().getId(), (byte) is.getTypeId());

    }

    private float getColor(float value) {
        if (value <= 0) {
            value = -1;
        }
        return value / 255f;
    }

    public enum Feature {

        COLOR,
        DATA;

    }

}
