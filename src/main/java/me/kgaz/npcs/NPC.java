package me.kgaz.npcs;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelHandlerContext;
import me.kgaz.Citizens;
import me.kgaz.tasks.TickTask;
import me.kgaz.tasks.Tickable;
import me.kgaz.util.PacketInListener;
import me.kgaz.util.PacketOutListener;
import me.kgaz.util.ParticleEffect;
import me.kgaz.util.Removeable;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Score;
import org.bukkit.util.Vector;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

public class NPC implements Listener, Tickable, PacketInListener, PacketOutListener, Removeable {

    public static int TEAM_ID = 0;
    private final int id;

    private boolean removed;
    private boolean shouldSave;

    private Location location;
    private String name;
    private boolean visibleByDefault;
    private boolean lookAtPlayers;
    private List<Player> seenBy;
    private VisibilityMask mask;

    private boolean spawned;
    private EntityLiving entity;
    private String texture;
    private String signature;

    private boolean longName;
    private String[] names;

    private String secondLine;
    private CustomSecondLine modifier;
    private EntityArmorStand secondLineEntity;

    private Citizens main;
    private DisguiseType disguise;

    public NPC(int id, YamlConfiguration yml, Citizens main) {

        String path = "npcs."+id;

        this.id = id;
        this.main = main;

        location = new Location(Bukkit.getWorld(yml.getString(path+".loc.world")), yml.getDouble(path+".loc.x"), yml.getDouble(path+".loc.y"), yml.getDouble(path+".loc.z"));
        this.name = yml.getString(path+".name");
        this.visibleByDefault = yml.getBoolean(path+".visibleByDefault");
        lookAtPlayers = yml.getBoolean(path+".lookAtPlayers");
        seenBy = new ArrayList<>( visibleByDefault ? Bukkit.getOnlinePlayers() : new ArrayList<>());
        mask = null;

        spawned = false;
        texture = yml.getString(path+".skin.texture");
        signature = yml.getString(path+".skin.signature");
        disguise = DisguiseType.valueOf(yml.getString(path+".disguise"));

        removed = false;

        longName = name.length() > 16;

        secondLine = yml.getString(path+".secondLine");
        modifier = null;

        main.getUserManager().registerPacketInListener(this);
        main.getUserManager().registerPacketOutListener(this);
        main.registerListener(this);

        shouldSave = true;

    }

    public NPC(int id, Citizens main, Location loc, String name) {

        this.id = id;
        this.main = main;

        location = loc;
        this.name = name;
        visibleByDefault = true;
        lookAtPlayers = true;
        seenBy = new ArrayList<>(Bukkit.getOnlinePlayers());
        mask = null;

        spawned = false;
        texture = null;
        signature = null;

        removed = false;

        longName = name.length() > 16;

        secondLine = null;
        modifier = null;
        disguise = DisguiseType.PLAYER;

        main.getUserManager().registerPacketInListener(this);
        main.getUserManager().registerPacketOutListener(this);
        main.registerListener(this);

        shouldSave = false;

    }

    public void setName(String name) {

        this.name = name;
        longName = name.length() > 16;

    }

    public int getCitizenId() {

        return id;

    }

    public void setVisibilityMask(VisibilityMask mask) {

        this.mask = mask;

    }

    public void removeVisibilityMask() {

        this.mask = null;

    }

    public void setVisibleByDefault(boolean bool) {

        this.visibleByDefault = bool;

    }

    public void setCustomLineModifier(CustomSecondLine csm) {

        this.modifier = csm;

    }

    public void setDisguise(DisguiseType type) {

        if(spawned) return;

        if(removed) return;

        this.disguise = type;

    }

    public void removeCustomLineModifier() {

        if(removed) return;

        this.modifier = null;

        if(secondLine != null) setSecondLine(secondLine);

    }

    public void setSecondLine(String line) {

        if(removed) return;

        if(spawned) {

            if(secondLine == null) {

                secondLine = line;

                secondLineEntity = new EntityArmorStand(((CraftWorld)location.getWorld()).getHandle(), location.getX(), location.getY()+disguise.getYModifier(), location.getZ());
                secondLineEntity.setGravity(false);
                secondLineEntity.setInvisible(true);
                secondLineEntity.setCustomName(secondLine);
                secondLineEntity.setCustomNameVisible(true);

                seenBy.forEach(player -> ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(secondLineEntity)));

            } else secondLine = line;

            if(line == null) {

                removeSecondLine();

                return;
            }

            if(modifier != null) {

                for(Player player : seenBy) {

                    DataWatcher watcher = new DataWatcher(secondLineEntity);

                    watcher.a(2, modifier.onSendingSecondLine(player, secondLine));

                    PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(secondLineEntity.getId(), watcher, true);

                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);

                }

            } else {

                secondLineEntity.setCustomName(line);

                PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(secondLineEntity.getId(), secondLineEntity.getDataWatcher(), true);

                seenBy.forEach(player -> ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet));

            }

            return;

        }

        secondLine = line;

    }

    public void removeSecondLine() {

        if(removed) return;

        secondLine = null;

        if(spawned) {

            seenBy.forEach(player -> ((CraftPlayer)player).getHandle().playerConnection.sendPacket(getArmorStandDestroyPacket()));

        }

    }

    public void remove() {

        if(removed) return;

        despawn();
        shouldSave = false;
        HandlerList.unregisterAll(this);

        removed = true;

    }

    @Override
    public void run() {

        if(lookAtPlayers) {

            for(Player player : seenBy) {

                if(player.getWorld() == location.getWorld()) {

                    Vector difference = player.getLocation().subtract(entity.getBukkitEntity().getLocation()).toVector().normalize();
                    if(player.getLocation().distanceSquared(location) <= 25) {

                        byte yaw = (byte) MathHelper.d((Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2) * 256.0F) / 360.0F);

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
    public boolean isCancelled() {
        return removed;
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

        if(removed) return;

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

        if(removed) return;

        this.texture = texture;
        this.signature = signature;

    }

    private void initEntity() {

        if(removed) return;

        if(spawned) return;

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        if(longName) {

            names = new String[2];
            names[0] = name.substring(0, name.length()-16);
            names[1] = name.substring(name.length()-16, name.length());

        }

        if(disguise == DisguiseType.PLAYER) {

            GameProfile profile = new GameProfile(UUID.randomUUID(), longName ? names[1] : name);

            if(texture != null && signature != null) profile.getProperties().put("textures", new Property("textures", texture, signature));

            entity = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));

        } else {

            entity = disguise.createEntity(world);

            entity.setCustomName(name);
            entity.setCustomNameVisible(true);

        }

        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
        entity.setPosition(location.getX(), location.getY(), location.getZ());

        if(secondLine != null) {

            secondLineEntity = new EntityArmorStand(world, location.getX(), location.getY()+disguise.getYModifier(), location.getZ());
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

        if(removed) return;

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

        if(removed) return;

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

    private List<Player> cooldown = new ArrayList<>();

    private void sendSpawnPackets(Player player) {

        if(player.getWorld() != location.getWorld()) return;

        if(removed) return;

        if(cooldown.contains(player)) return;

        cooldown.add(player);

        new BukkitRunnable() {
            public void run() {
                cooldown.remove(player);
            }
        }.runTaskLater(main, 20);

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        if(disguise == DisguiseType.PLAYER) {

            PacketPlayOutNamedEntitySpawn packet = getSpawnPacket();
            PacketPlayOutPlayerInfo packetInfo = getSpawnInfoPacket();
            PacketPlayOutEntityMetadata metadata = getMetaDataPacket();

            if(lookAtPlayers) {

                Vector difference = player.getLocation().subtract(entity.getBukkitEntity().getLocation()).toVector().normalize();

                if (player.getLocation().distanceSquared(location) <= 25) {

                    float yaw = MathHelper.d((Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2)));

                    // Calculate the Pitch for the NPC
                    Vector height = entity.getBukkitEntity().getLocation().subtract(player.getLocation()).toVector().normalize();

                    entity.pitch = (float) MathHelper.d((Math.toDegrees(Math.atan(height.getY()))));
                    entity.yaw = yaw;

                    connection.sendPacket(packetInfo);
                    connection.sendPacket(packet);
                    connection.sendPacket(metadata);
                    connection.sendPacket(new PacketPlayOutRespawn(entity.getId(), EnumDifficulty.HARD, WorldType.CUSTOMIZED, WorldSettings.EnumGamemode.CREATIVE));

                } else {

                    connection.sendPacket(packetInfo);
                    connection.sendPacket(packet);
                    connection.sendPacket(metadata);
                    connection.sendPacket(new PacketPlayOutRespawn(entity.getId(), EnumDifficulty.HARD, WorldType.CUSTOMIZED, WorldSettings.EnumGamemode.CREATIVE));

                }

            } else {

                connection.sendPacket(packetInfo);
                connection.sendPacket(packet);
                connection.sendPacket(metadata);
                connection.sendPacket(new PacketPlayOutRespawn(entity.getId(), EnumDifficulty.HARD, WorldType.CUSTOMIZED, WorldSettings.EnumGamemode.CREATIVE));

            }

            if(longName) {

                TEAM_ID++;

                ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard)Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), ChatColor.stripColor(TEAM_ID+"r"));
                team.setPrefix(names[0]);

                connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
                ArrayList<String> playerToAdd = new ArrayList<>(Collections.singletonList(names[1]));
                connection.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));

            }

        } else {

            PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving((EntityLiving) entity);

            if(lookAtPlayers) {

                Vector difference = player.getLocation().subtract(entity.getBukkitEntity().getLocation()).toVector().normalize();

                if (player.getLocation().distanceSquared(location) <= 25) {

                    float yaw = MathHelper.d((Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2)));

                    // Calculate the Pitch for the NPC
                    Vector height = entity.getBukkitEntity().getLocation().subtract(player.getLocation()).toVector().normalize();

                    entity.pitch = (float) MathHelper.d((Math.toDegrees(Math.atan(height.getY()))));
                    entity.yaw = yaw;

                }

                connection.sendPacket(spawnPacket);

               // ((EntityLiving)entity).setCustomNameVisible(true);

                //connection.sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), ((EntityLiving)entity).getDataWatcher(), true));

            }

        }

        if(secondLine != null) {

            connection.sendPacket(new PacketPlayOutSpawnEntityLiving(secondLineEntity));

            if(modifier != null) setSecondLine(secondLine);

        }

    }

    private PacketPlayOutEntity.PacketPlayOutEntityLook getRotationPacket(float yaw, float pitch) {

        return new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), (byte) ((yaw/256f) * 360f), (byte) ((pitch/256f) * 360f), true);

    }

    private PacketPlayOutEntityHeadRotation getHeadRotation(float yaw) {

        return new PacketPlayOutEntityHeadRotation(entity, (byte) ((yaw/256f) * 360f));

    }

    private PacketPlayOutNamedEntitySpawn getSpawnPacket() {

        return new PacketPlayOutNamedEntitySpawn((EntityPlayer) entity);

    }

    private PacketPlayOutPlayerInfo getSpawnInfoPacket() {

        return new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer) entity);

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

        return new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (EntityPlayer) entity);

    }

    private void removeTab(Player player) {

        if(disguise != DisguiseType.PLAYER) return;

        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(getRemoveInfoPacket());

    }

    private void sendDespawnData(Player player) {

        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(getDestroyPacket());

    }

    public void playArmAnimation() {

        if(removed) return;

        if(!spawned) return;

        seenBy.forEach(player -> ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutAnimation(entity, 0)));

    }

    public int getEntityId() {

        if(removed) return 0;

        return spawned ? entity.getId() : 0;

    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {

        if(removed) return;

        if(e.getPlayer().getWorld() == location.getWorld() && e.getFrom() != location.getWorld() && seenBy.contains(e.getPlayer())) {
            sendSpawnPackets(e.getPlayer());
            queRemovePacket(e.getPlayer());
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        if(removed) return;

        if(visibleByDefault) {

            if(mask != null) {

                if(!mask.shouldSee(e.getPlayer())) return;

            }

            seenBy.add(e.getPlayer());

            if(e.getPlayer().getWorld() == location.getWorld()) {

                 sendSpawnPackets(e.getPlayer());
                 queRemovePacket(e.getPlayer());

            }

        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        if(removed) return;

        seenBy.remove(e.getPlayer());

    }

    private void queRemovePacket(Player player) {

        if(removed) return;

        new BukkitRunnable() {

            public void run() {

                removeTab(player);

            }

        }.runTaskLater(main, 1);

    }

    public boolean isRemoved() {

        return removed;

    }

    public boolean canBeSeenBy(Player player) {

        if(removed) return false;

        return seenBy.contains(player);

    }

    public void hideFrom(Player player) {

        if(removed) return;

        if(seenBy.contains(player)) {

            seenBy.remove(player);

            sendDespawnData(player);

            if(secondLine != null) ((CraftPlayer)player).getHandle().playerConnection.sendPacket(getArmorStandDestroyPacket());

        }

    }

    public void showTo(Player player) {

        if(removed) return;

        if(!seenBy.contains(player)) {

            seenBy.add(player);

            sendSpawnPackets(player);

        }

    }

    public void saveOnDisable() {

        if(removed) return;

        shouldSave = true;

    }

    void save(YamlConfiguration yml) {

        if(removed) return;

        String path = "npcs."+id;

        yml.set(path+".loc.world", location.getWorld().getName());
        yml.set(path+".loc.x", location.getX());
        yml.set(path+".loc.y", location.getY());
        yml.set(path+".loc.z", location.getZ());
        yml.set(path+".name", name);
        yml.set(path+".visibleByDefault", visibleByDefault);
        yml.set(path+".lookAtPlayers", lookAtPlayers);
        yml.set(path+".skin.texture", texture);
        yml.set(path+".skin.signature", signature);
        yml.set(path+".secondLine", secondLine);
        yml.set(path+".disguise", disguise.toString());

    }

    @Override
    public boolean isActive() {
        return !removed;
    }

    public String getName() {

        return name;

    }

    public void setLookAtPlayers(boolean lookAtPlayers) {
        this.lookAtPlayers = lookAtPlayers;
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isVisibleByDefault() {
        return visibleByDefault;
    }

    public boolean isLookAtPlayers() {
        return lookAtPlayers;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public EntityLiving getEntity() {
        return entity;
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }

    public String getSecondLine() {
        return secondLine;
    }

    public DisguiseType getDisguise() {
        return disguise;
    }

    public void setLocation(Location location) {

        boolean moveWorlds = this.location.getWorld() != location.getWorld();

        this.location = location;

        if(spawned) {

            if(moveWorlds) {

                despawn();
                initEntity();
                spawn();

            } else {

                entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
                entity.setPosition(location.getX(), location.getY(), location.getZ());

                seenBy.forEach(player -> ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(entity)));

                if(secondLine != null) {

                    secondLineEntity.setLocation(location.getX(), location.getY() + disguise.getYModifier(), location.getZ(), location.getPitch(), location.getYaw());
                    secondLineEntity.setPosition(location.getX(), location.getY() + disguise.getYModifier(), location.getZ());

                    seenBy.forEach(player -> ((CraftPlayer)player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityTeleport(secondLineEntity)));

                }

            }

        }

    }
}
