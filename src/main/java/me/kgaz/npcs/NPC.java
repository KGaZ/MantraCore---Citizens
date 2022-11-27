package me.kgaz.npcs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelHandlerContext;
import me.kgaz.Citizens;
import me.kgaz.tasks.TickTask;
import me.kgaz.tasks.Tickable;
import me.kgaz.util.PacketInListener;
import me.kgaz.util.PacketOutListener;
import me.kgaz.util.ParticleEffect;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;

public class NPC implements Listener, Tickable, PacketInListener, PacketOutListener {

    private static int TEAM_ID = 0;

    private Location location;
    private String name;
    private boolean visibleByDefault;
    private boolean lookAtPlayers;
    private List<Player> seenBy;

    private boolean spawned;
    private EntityPlayer entity;
    private String texture;
    private String signature;

    private boolean longName;
    private String[] names;

    private String secondLine;
    private EntityArmorStand secondLineEntity;

    private Citizens main;

    public NPC(Citizens main, Location loc, String name) {

        this.main = main;

        location = loc;
        this.name = name;
        visibleByDefault = true;
        lookAtPlayers = true;
        seenBy = new ArrayList<>(Bukkit.getOnlinePlayers());

        spawned = false;
        texture = null;
        signature = null;

        longName = name.length() > 16;

        secondLine = null;

        main.getUserManager().registerPacketInListener(this);
        main.getUserManager().registerPacketOutListener(this);
        main.registerListener(this);

    }

    public void setSecondLine(String line) {

        if(spawned) return;

        secondLine = line;

    }

    public void removeSecondLine() {

        if(spawned) return;

        secondLine = null;

    }

    public void remove() {

        despawn();
        main.getUserManager().unregisterPacketInListener(this);
        main.getUserManager().unregisterPacketOutListener(this);
        HandlerList.unregisterAll(this);

    }

    @Override
    public void run() {

        if(lookAtPlayers) {

            for(Player player : seenBy) {

                if(player.getWorld() == location.getWorld()) {

                    Vector difference = player.getLocation().subtract(entity.getBukkitEntity().getLocation()).toVector().normalize();
                    if(player.getLocation().distanceSquared(location) <= 25) {

                        byte yaw = (byte) MathHelper.d((Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2) * 256.0F) / 360.0F);

                        // Calculate the Pitch for the NPC
                        Vector height = entity.getBukkitEntity().getLocation().subtract(player.getLocation()).toVector().normalize();
                        byte pitch = (byte) MathHelper.d((Math.toDegrees(Math.atan(height.getY())) * 256.0F) / 360.0F);

                        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

                        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entity, yaw));
                        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), yaw, pitch, true));

                    }

                }

            }

        }

    }

    @Override
    public boolean handlePacket(ChannelHandlerContext context, PlayerConnection connection, Packet packet, Player target) {

        Bukkit.getScheduler().runTask(main, new Runnable() {

            @Override
            public void run() {

                if(packet instanceof PacketPlayOutMapChunkBulk) {

                    int[] xt = new int[0], yt = new int[0];

                    try {

                        Field a = PacketPlayOutMapChunkBulk.class.getDeclaredField("a");
                        a.setAccessible(true);
                        xt = (int[]) a.get(packet);

                        Field b = PacketPlayOutMapChunkBulk.class.getDeclaredField("b");
                        b.setAccessible(true);
                        yt = (int[]) b.get(packet);

                    } catch (NoSuchFieldException | IllegalAccessException e) {

                        e.printStackTrace();

                    }

                    for(int x : xt) {

                        for(int y : yt) {

                            if(location.getChunk().getX() == x && location.getChunk().getZ() == y) {

                                if(seenBy.contains(target)) {

                                    sendSpawnPackets(target);
                                    queRemovePacket(target);
                                    return;

                                }

                            }

                        }

                    }

                }

            }

        });

        return false;
    }

    @Override
    public boolean handlePacket(Packet packet, Player target) {

        if(packet instanceof PacketPlayInUseEntity) {

            PacketPlayInUseEntity data = (PacketPlayInUseEntity) packet;

            int id = -1;
            PacketPlayInUseEntity.EnumEntityUseAction action = null;

            try {

                Field i = PacketPlayInUseEntity.class.getDeclaredField("a");
                i.setAccessible(true);
                id = i.getInt(data);

                Field a = PacketPlayInUseEntity.class.getDeclaredField("action");
                a.setAccessible(true);
                action = (PacketPlayInUseEntity.EnumEntityUseAction) a.get(data);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            if(id == this.entity.getId()) {

                if(!seenBy.contains(target)) return false;

                if(target.getLocation().distanceSquared(location) > 26) return false;

                boolean rightClick = true;

                if(action == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) rightClick = false;
                else if(action == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) rightClick = true;
                else return false;

                NPCInteractEvent event = new NPCInteractEvent(target, this, rightClick);
                Bukkit.getPluginManager().callEvent(event);

            }

        }

        return false;
    }

    public void copySkin(Player player) {

        try {

            Property property = ((CraftPlayer)player).getProfile().getProperties().get("textures").iterator().next();
            this.texture = property.getValue();
            this.signature = property.getSignature();

        } catch(Exception exc) {

            texture = null;
            signature = null;

        }

    }

    public void setSkin(String texture, String signature) {

        this.texture = texture;
        this.signature = signature;

    }

    private void initEntity() {

        if(spawned) return;

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        if(longName) {

            names = new String[2];
            names[0] = name.substring(0, name.length()-16);
            names[1] = name.substring(name.length()-16, name.length());

        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), longName ? names[1] : name);

        if(texture != null && signature != null) profile.getProperties().put("textures", new Property("textures", texture, signature));

        entity = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
        entity.setPosition(location.getX(), location.getY(), location.getZ());

        if(secondLine != null) {

            secondLineEntity = new EntityArmorStand(world, location.getX(), location.getY()+0.1, location.getZ());
            secondLineEntity.setGravity(false);
            secondLineEntity.setInvisible(true);
            secondLineEntity.setCustomName(secondLine);
            secondLineEntity.setCustomNameVisible(true);

        }

    }

    public void despawn() {
        despawn(false);
    }

    public void despawn(boolean particles) {

        if(spawned) {

            main.getGlobalTaskManager().unregister(this, true);

            spawned = false;

            seenBy.forEach(NPC.this::sendDespawnData);

            if(secondLine != null) {

                for (Player player : seenBy) {

                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(getArmorStandDestroyPacket());

                }

            }

            if(particles) ParticleEffect.CLOUD.send(seenBy, location.clone().add(0,1,0), 0.2, 1, 0.2, 0.1, 64, 16);

        }

    }

    public void spawn() {

        if(!spawned) {

            initEntity();

            main.getGlobalTaskManager().registerTask(this, true);

            spawned = true;

            for (Player player : seenBy) {

                if(player.getWorld() == location.getWorld()) {

                    sendSpawnPackets(player);

                }

            }

            new BukkitRunnable() {

                public void run() {

                    seenBy.forEach(NPC.this::removeTab);

                }

            }.runTaskLater(main, 1);

        }

    }

    public void sendSpawnPackets(Player player) {

        PacketPlayOutNamedEntitySpawn packet = getSpawnPacket();
        PacketPlayOutPlayerInfo packetInfo = getSpawnInfoPacket();
        PacketPlayOutEntityMetadata metadata = getMetaDataPacket();

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        if(lookAtPlayers) {

            Vector difference = player.getLocation().subtract(entity.getBukkitEntity().getLocation()).toVector().normalize();

            if (player.getLocation().distanceSquared(location) <= 25) {

                float yaw = MathHelper.d((Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2)));

                // Calculate the Pitch for the NPC
                Vector height = entity.getBukkitEntity().getLocation().subtract(player.getLocation()).toVector().normalize();

                entity.pitch = (float) MathHelper.d((Math.toDegrees(Math.atan(height.getY()))));
                entity.yaw = yaw;

                connection.sendPacket(getSpawnInfoPacket());
                connection.sendPacket(getSpawnPacket());
                connection.sendPacket(getMetaDataPacket());

            } else {

                connection.sendPacket(packetInfo);
                connection.sendPacket(packet);
                connection.sendPacket(metadata);

            }

        } else {

            connection.sendPacket(packetInfo);
            connection.sendPacket(packet);
            connection.sendPacket(metadata);

        }

        if(longName) {

            TEAM_ID++;

            ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard)Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), ChatColor.stripColor(TEAM_ID+"r"));
            team.setPrefix(names[0]);

            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
            ArrayList<String> playerToAdd = new ArrayList<>(Collections.singletonList(names[1]));
            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));

        }

        if(secondLine != null) {

            connection.sendPacket(new PacketPlayOutSpawnEntityLiving(secondLineEntity));

        }

    }

    private PacketPlayOutEntity.PacketPlayOutEntityLook getRotationPacket(float yaw, float pitch) {

        return new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), (byte) ((yaw/256f) * 360f), (byte) ((pitch/256f) * 360f), true);

    }

    private PacketPlayOutEntityHeadRotation getHeadRotation(float yaw) {

        return new PacketPlayOutEntityHeadRotation(entity, (byte) ((yaw/256f) * 360f));

    }

    private PacketPlayOutNamedEntitySpawn getSpawnPacket() {

        return new PacketPlayOutNamedEntitySpawn(entity);

    }

    private PacketPlayOutPlayerInfo getSpawnInfoPacket() {

        return new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity);

    }

    private PacketPlayOutEntityDestroy getArmorStandDestroyPacket() {

        return new PacketPlayOutEntityDestroy(secondLineEntity.getId());

    }

    private PacketPlayOutEntityDestroy getDestroyPacket() {

        return new PacketPlayOutEntityDestroy(entity.getId());

    }

    private PacketPlayOutEntityMetadata getMetaDataPacket() {

        DataWatcher watcher = entity.getDataWatcher();

        watcher.watch(10, (byte) 127);

        return new PacketPlayOutEntityMetadata(entity.getId(), watcher, true);

    }

    private PacketPlayOutPlayerInfo getRemoveInfoPacket(){

        return new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entity);

    }

    public void removeTab(Player player) {

        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(getRemoveInfoPacket());

    }

    public void sendDespawnData(Player player) {

        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(getDestroyPacket());

    }

    public int getEntityId() {

        return spawned ? entity.getId() : 0;

    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {

        if(e.getPlayer().getWorld() == location.getWorld() && e.getFrom() != location.getWorld() && seenBy.contains(e.getPlayer())) {
            sendSpawnPackets(e.getPlayer());
            queRemovePacket(e.getPlayer());
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        if(visibleByDefault) seenBy.add(e.getPlayer());

        if(e.getPlayer().getWorld() == location.getWorld()) {
            this.sendSpawnPackets(e.getPlayer());
            queRemovePacket(e.getPlayer());
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        seenBy.remove(e.getPlayer());

    }

    public void queRemovePacket(Player player) {

        new BukkitRunnable() {

            public void run() {

                removeTab(player);

            }

        }.runTaskLater(main, 1);

    }
}
