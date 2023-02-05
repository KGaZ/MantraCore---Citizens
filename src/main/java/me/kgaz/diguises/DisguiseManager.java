package me.kgaz.diguises;

import io.netty.channel.ChannelHandlerContext;
import me.kgaz.Citizens;
import me.kgaz.diguises.types.ItemDisguise;
import me.kgaz.diguises.types.MaterialDependedDisguise;
import me.kgaz.diguises.types.PlayerDisguise;
import me.kgaz.util.PacketOutListener;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DisguiseManager implements PacketOutListener {

    private ConcurrentHashMap<Integer, DisguiseData> disguises;
    private Citizens main;

    public DisguiseManager(Citizens main) {

        this.main = main;
        disguises = new ConcurrentHashMap<>();
        main.getUserManager().registerPacketOutListener(this);

    }

    public void removeDisguise(int entityId) {

        if(!disguises.containsKey(entityId)) return;

        DisguiseData current = disguises.get(entityId);

        disguises.remove(entityId);

        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityId);

        Bukkit.getOnlinePlayers().forEach(player -> {

            if(player.getEntityId() != entityId) ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);

        });

        PacketPlayOutSpawnEntityLiving respawn = new PacketPlayOutSpawnEntityLiving(current.getEntity());

        new BukkitRunnable() {

            public void run() {

                Bukkit.getOnlinePlayers().forEach(player -> {

                    if(player.getEntityId() != entityId) ((CraftPlayer)player).getHandle().playerConnection.sendPacket(respawn);

                });

            }

        }.runTaskLater(main, 3);

    }

    public void disguiseEntity(int entityId, DisguiseData data) {

        disguises.put(entityId, data);

        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(entityId);

        Bukkit.getOnlinePlayers().forEach(player -> {

            if(player.getEntityId() != entityId) ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);

        });

        PacketPlayOutSpawnEntityLiving respawn = new PacketPlayOutSpawnEntityLiving(data.getEntity());

        new BukkitRunnable() {

            public void run() {

                Bukkit.getOnlinePlayers().forEach(player -> {

                    if(player.getEntityId() != entityId) ((CraftPlayer)player).getHandle().playerConnection.sendPacket(respawn);

                });

            }

        }.runTaskLater(main, 3);


    }

    @Override
    public boolean handlePacket(ChannelHandlerContext context, PlayerConnection connection, Packet packet, Player target) {

        if(packet instanceof PacketPlayOutEntityStatus) {

            PacketPlayOutEntityStatus status = (PacketPlayOutEntityStatus) packet;

            byte stat;
            int entityID;

            try {

                Field a = PacketPlayOutEntityStatus.class.getDeclaredField("a");
                a.setAccessible(true);
                entityID = a.getInt(status);

                Field b = PacketPlayOutEntityStatus.class.getDeclaredField("b");

                b.setAccessible(true);
                stat = b.getByte(status);


            } catch (NoSuchFieldException | IllegalAccessException e) {

                e.printStackTrace();
                return false;

            }

            if(disguises.containsKey(entityID)) {

                if(stat == 3) {

                    Packet p = new PacketPlayOutEntityDestroy(entityID);

                    Bukkit.getScheduler().runTaskLater(main, new Runnable() {

                        @Override
                        public void run() {

                            connection.sendPacket(p);

                        }

                    }, 30);


                    return true;

                }

            }

        }

        if(packet instanceof PacketPlayOutSpawnEntityLiving) {

            PacketPlayOutSpawnEntityLiving p = (PacketPlayOutSpawnEntityLiving) packet;

            int entityId;

            try {

                Field a = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("a");
                a.setAccessible(true);
                entityId = a.getInt(p);
                a.setAccessible(false);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }

            if(disguises.containsKey(entityId)) {

                DisguiseData data = disguises.get(entityId);

                if(data instanceof PlayerDisguise) {

                    PlayerDisguise disguise = (PlayerDisguise) data;

                    EntityPlayer createdEntity = disguise.generateEntity();

                    PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(createdEntity);
                    PacketPlayOutPlayerInfo infoSpawnPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, createdEntity);
                    PacketPlayOutPlayerInfo infoRemovePacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, createdEntity);

                    DataWatcher watcher = createdEntity.getDataWatcher();

                    watcher.watch(10, (byte) 127);

                    PacketPlayOutEntityMetadata skinPacket = new PacketPlayOutEntityMetadata(createdEntity.getId(), watcher, true);

                    connection.sendPacket(infoSpawnPacket);
                    connection.sendPacket(spawnPacket);
                    connection.sendPacket(skinPacket);

                    Bukkit.getScheduler().runTaskLater(main, new Runnable() {

                        @Override
                        public void run() {

                            connection.sendPacket(infoRemovePacket);

                        }

                    }, 1);
                    
                    if(disguise.isLongName()) {
                        
                        me.kgaz.npcs.NPC.TEAM_ID++;

                        ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard)Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), (me.kgaz.npcs.NPC.TEAM_ID+"d"));
                        team.setPrefix(disguise.getPrefix());

                        connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
                        ArrayList<String> playerToAdd = new ArrayList<>(Collections.singletonList(disguise.getEntityName()));
                        connection.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));
                        
                    }

                    return true;

                }

                if(data instanceof MaterialDependedDisguise) {

                    MaterialDependedDisguise disguise = (MaterialDependedDisguise) data;

                    PacketPlayOutSpawnEntity newPacket = new PacketPlayOutSpawnEntity(disguise.getEntity(), disguise.getDisguiseID(), disguise.getDisguiseItemID() + (disguise.getDisguiseByteData() << 12));

                    connection.sendPacket(newPacket);

                    if(disguise instanceof ItemDisguise) {

                        ItemDisguise itemDisguise = (ItemDisguise) disguise;

                        PacketPlayOutEntityMetadata dataPacket = new PacketPlayOutEntityMetadata(disguise.getEntity().getId(), itemDisguise.generateEntity().getDataWatcher(), true);
                        connection.sendPacket(dataPacket);

                    }

                    return true;
                }

                try {

                    Field a = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("b");
                    a.setAccessible(true);

                    if(a.getInt(packet) == data.getDisguiseID()) {

                        context.write(packet);
                        return true;

                    }

                    a.setInt(packet, data.getDisguiseID());

                    context.write(packet);
                    return true;

                } catch(Exception exc) {

                    exc.printStackTrace();

                }

            }

        }

        return false;
    }
}
