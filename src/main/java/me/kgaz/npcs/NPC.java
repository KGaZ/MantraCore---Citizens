package me.kgaz.npcs;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelHandlerContext;
import me.kgaz.MantraLibs;
import me.kgaz.tasks.Tickable;
import me.kgaz.util.PacketInListener;
import me.kgaz.util.PacketOutListener;
import me.kgaz.util.ParticleEffect;
import me.kgaz.util.Removeable;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NPC implements Listener, Tickable, PacketInListener, PacketOutListener, Removeable {

    public static int TEAM_ID = 0;
    private final int id;

    private boolean removed;
    private boolean shouldSave;

    private Location location;
    private String name;
    private boolean visibleByDefault;
    private boolean lookAtPlayers;
    private final List<Player> seenBy;
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

    private final MantraLibs main;
    private DisguiseType disguise;

    private ArmorStand holder;
    private org.bukkit.inventory.ItemStack[] items;
    public boolean editing;

    public NPC(int id, YamlConfiguration yml, MantraLibs main) {

        String path = "npcs."+id;

        this.id = id;
        this.main = main;
        this.items = new org.bukkit.inventory.ItemStack[5];
        editing = false;

        location = new Location(Bukkit.getWorld(yml.getString(path+".loc.world")), yml.getDouble(path+".loc.x"), yml.getDouble(path+".loc.y"), yml.getDouble(path+".loc.z"), (float) yml.getDouble(path+".loc.yaw"), (float) yml.getDouble(path+".loc.pitch"));
        this.name = yml.getString(path+".name");
        this.visibleByDefault = yml.getBoolean(path+".visibleByDefault");
        lookAtPlayers = yml.getBoolean(path+".lookAtPlayers");
        seenBy = new ArrayList<>( visibleByDefault ? Bukkit.getOnlinePlayers() : new ArrayList<>());
        mask = null;

        spawned = false;
        texture = yml.getString(path+".skin.texture");
        signature = yml.getString(path+".skin.signature");
        disguise = DisguiseType.valueOf(yml.getString(path+".disguise"));

        for(int i = 0; i < 5; i++) items[i] = yml.getItemStack(path+".eq."+i, null);

        removed = false;

        longName = name.length() > 16 || name.startsWith("§1") || name.startsWith("§2") || name.startsWith("§3") || name.startsWith("§4");

        secondLine = yml.getString(path+".secondLine");
        modifier = null;

        main.getUserManager().registerPacketInListener(this);
        main.getUserManager().registerPacketOutListener(this);
        main.registerListener(this);

        shouldSave = true;

        holder = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 0, 0), EntityType.ARMOR_STAND);
        holder.setGravity(false);
        holder.setVisible(false);

    }

    public NPC(int id, MantraLibs main, Location loc, String name) {

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

        longName = name.length() > 16 || name.startsWith("§1") || name.startsWith("§2") || name.startsWith("§3") || name.startsWith("§4");

        secondLine = null;
        modifier = null;
        disguise = DisguiseType.PLAYER;

        main.getUserManager().registerPacketInListener(this);
        main.getUserManager().registerPacketOutListener(this);
        main.registerListener(this);

        shouldSave = false;

        holder = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 0, 0), EntityType.ARMOR_STAND);
        holder.setGravity(false);
        holder.setVisible(false);

    }

    public void setName(String name) {

        this.name = name;
        longName = name.length() > 16 || name.startsWith("§1") || name.startsWith("§2") || name.startsWith("§3") || name.startsWith("§4");

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

    public void refreshSecondLine(Player player) {

        DataWatcher watcher = new DataWatcher(secondLineEntity);

        watcher.a(2, modifier.onSendingSecondLine(player, secondLine));

        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(secondLineEntity.getId(), watcher, true);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

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

                    ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);


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

    private final int i = 0;

    @Override
    public void run() {

        if(lookAtPlayers) {

            for(Player player : seenBy) {

                if(player.getWorld() == location.getWorld()) {

                    Vector difference;

                    try {

                        difference = player.getLocation().subtract(entity.getBukkitEntity().getLocation()).toVector().normalize();

                    }catch(Exception exc) {

                        continue;

                    }

                    if(player.getLocation().distanceSquared(location) <= 60) {

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

        if (packet instanceof PacketPlayOutSpawnEntity) {

            PacketPlayOutSpawnEntity data = (PacketPlayOutSpawnEntity) packet;

            int id;

            try {

                Field a = PacketPlayOutSpawnEntity.class.getDeclaredField("a");
                a.setAccessible(true);
                id = a.getInt(data);

            } catch (NoSuchFieldException | IllegalAccessException e) {

                e.printStackTrace();
                return true;

            }

            if(spawned) {

                if(id == NPC.this.holder.getEntityId()) {

                    if(seenBy.contains(target)) {

                        sendSpawnPackets(target);
                        queRemovePacket(target);

                    }

                    return true;

                }

            }

        }

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

                if(target.getWorld() != location.getWorld()) return false;
                if(target.getLocation().distanceSquared(location) > 26) return false;

                boolean rightClick = true;

                if(action == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) rightClick = false;
                else return false;

                NPCInteractEvent event = new NPCInteractEvent(target, this, rightClick);

                if(editing && target.hasPermission("npc.admin.edit")) {

                    org.bukkit.inventory.ItemStack is = target.getInventory().getItemInHand();

                    if(is == null || is.getType() == Material.AIR) {

                        new BukkitRunnable() {

                            public void run() {

                                for(int i = 0; i < 5; i++) {

                                    if(items[i] != null) if(items[i].getType() != Material.AIR) {

                                        location.getWorld().dropItem(location, items[i]);
                                        target.playSound(location, Sound.CHICKEN_EGG_POP, 1, 1);

                                        PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(entity.getId(), i, null);
                                        Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld() == location.getWorld()).forEach(player -> {((CraftPlayer)player).getHandle().playerConnection.sendPacket(eq);});


                                        items[i] = null;

                                    }

                                }

                            }

                        }.runTask(main);

                    } else {

                        org.bukkit.Material mat = is.getType();
                        int itemType = 0;
                        if(mat.toString().contains("HELMET") || mat.isBlock() || mat == Material.SKULL_ITEM) itemType = 4;
                        if(mat.toString().contains("CHESTPLATE")) itemType = 3;
                        if(mat.toString().contains("LEGGINGS")) itemType = 2;
                        if(mat.toString().contains("BOOTS")) itemType = 1;

                        items[itemType] = is;

                        PacketPlayOutEntityEquipment eq = new PacketPlayOutEntityEquipment(entity.getId(), itemType, CraftItemStack.asNMSCopy(items[itemType]));
                        Bukkit.getOnlinePlayers().stream().filter(player -> player.getWorld() == location.getWorld()).forEach(player -> {((CraftPlayer)player).getHandle().playerConnection.sendPacket(eq);});

                    }

                } else Bukkit.getScheduler().runTask(main, new Runnable() {

                    @Override
                    public void run() {

                        try {Bukkit.getPluginManager().callEvent(event);} catch(Exception exc) {}

                    }
                });

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

            if(name.length() > 16) {

                names = new String[2];
                names[0] = name.substring(0, name.length()-16);
                names[1] = name.substring(name.length()-16);

            } else {

                names = new String[2];
                names[0] = name.substring(0, 4);
                names[1] = name.substring(4);

            }

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

        entity.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
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
                    queRemovePacket(player);

                }

            }

            new BukkitRunnable() {

                public void run() {

                    seenBy.forEach(NPC.this::removeTab);

                }

            }.runTaskLater(main, 140);

        }

    }

    private final List<Player> cooldown = new ArrayList<>();

    private void sendSpawnPackets(Player player) {

        if (removed) return;

        sendDespawnData(player);

        sentData.remove(player);

        new BukkitRunnable() {

            public void run() {

                if (player.getWorld() != location.getWorld()) return;

                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

                if (disguise == DisguiseType.PLAYER) {

                    PacketPlayOutNamedEntitySpawn packet = getSpawnPacket();
                    PacketPlayOutPlayerInfo packetInfo = getSpawnInfoPacket();
                    PacketPlayOutEntityMetadata metadata = getMetaDataPacket();

                    if (lookAtPlayers) {

                        Vector difference = null;
                        boolean skip = false;

                        try {

                            difference = player.getLocation().subtract(entity.getBukkitEntity().getLocation()).toVector().normalize();

                        }catch(Exception exc) {

                            skip = true;

                        }

                        if(!skip) if (player.getLocation().distanceSquared(location) <= 60) {

                            float yaw = MathHelper.d((Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2)));

                            // Calculate the Pitch for the NPC
                            Vector height = entity.getBukkitEntity().getLocation().subtract(player.getLocation()).toVector().normalize();

                            entity.pitch = (float) MathHelper.d((Math.toDegrees(Math.atan(height.getY()))));
                            entity.yaw = yaw;

                            connection.sendPacket(packetInfo);
                            connection.sendPacket(packet);

                            connection.sendPacket(new PacketPlayOutEntityHeadRotation(entity, (byte) entity.yaw));
                            connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), (byte) yaw, (byte) entity.pitch, true));

                        } else {

                            connection.sendPacket(packetInfo);
                            connection.sendPacket(packet);

                            connection.sendPacket(new PacketPlayOutEntityHeadRotation(entity, (byte) (MathHelper.d((location.getYaw()) * 256.0F) / 360.0F)));
                            connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), (byte) ((MathHelper.d(location.getYaw()) * 256.0F) / 360.0F), (byte) ((MathHelper.d(location.getPitch()) * 256.0F) / 360.0F), true));


                        }

                    } else {

                        connection.sendPacket(packetInfo);
                        connection.sendPacket(packet);

                        connection.sendPacket(new PacketPlayOutEntityHeadRotation(entity, (byte) (MathHelper.d((location.getYaw()) * 256.0F) / 360.0F)));
                        connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), (byte) ((MathHelper.d(location.getYaw()) * 256.0F) / 360.0F), (byte) ((MathHelper.d(location.getPitch()) * 256.0F) / 360.0F), true));

                        connection.sendPacket(getRotationPacket(entity.yaw, entity.pitch));
                        connection.sendPacket(getHeadRotation(entity.yaw));

                    }



                    if (longName) {

                        TEAM_ID++;

                        ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), ChatColor.stripColor(TEAM_ID + "r"));
                        team.setPrefix(names[0]);
                        connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
                        ArrayList<String> playerToAdd = new ArrayList<>(Collections.singletonList(names[1]));
                        connection.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));

                    }

                } else {

                    PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(entity);

                    if (lookAtPlayers) {

                        Vector difference = player.getLocation().subtract(entity.getBukkitEntity().getLocation()).toVector().normalize();

                        if (player.getLocation().distanceSquared(location) <= 60) {

                            float yaw = MathHelper.d((Math.toDegrees(Math.atan2(difference.getZ(), difference.getX()) - Math.PI / 2)));

                            // Calculate the Pitch for the NPC
                            Vector height = entity.getBukkitEntity().getLocation().subtract(player.getLocation()).toVector().normalize();

                            entity.pitch = (float) MathHelper.d((Math.toDegrees(Math.atan(height.getY()))));
                            entity.yaw = yaw;

                        }

                        connection.sendPacket(spawnPacket);

                    }

                }

                List<Packet> eqp = new ArrayList<>();
                for(int i = 0; i < 5; i++) if(items[i] != null) eqp.add(new PacketPlayOutEntityEquipment(entity.getId(), i, CraftItemStack.asNMSCopy(items[i])));

                new BukkitRunnable() {

                    public void run (){

                        try{eqp.forEach(connection::sendPacket);} catch(Exception exc) {}

                    }

                }.runTaskLater(main, 3);

                if (secondLine != null) {

                    connection.sendPacket(new PacketPlayOutEntityDestroy(secondLineEntity.getId()));

                    new BukkitRunnable() {

                        public void run () {

                            connection.sendPacket(new PacketPlayOutSpawnEntityLiving(secondLineEntity));

                            if (modifier != null) setSecondLine(secondLine);

                        }

                    }.runTaskLater(main, 3);

                }

            }

        }.runTaskLater(main, 6);


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

        //((CraftPlayer)player).getHandle().playerConnection.sendPacket(getRemoveInfoPacket());

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

    private List<Player> sentData = new ArrayList<>();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {

        if(removed) return;

        sentData.remove(e.getPlayer());

        if(e.getPlayer().getWorld() == location.getWorld() && seenBy.contains(e.getPlayer())) {
            sendSpawnPackets(e.getPlayer());
            queRemovePacket(e.getPlayer());
        }

    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {

        if(removed) return;

        sentData.remove(e.getPlayer());

        if(e.getPlayer().getWorld() == location.getWorld() && seenBy.contains(e.getPlayer())) {
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

        }.runTaskLater(main, 140);

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
            queRemovePacket(player);

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
        yml.set(path+".loc.pitch", location.getPitch());
        yml.set(path+".loc.yaw", location.getYaw());
        yml.set(path+".name", name);
        yml.set(path+".visibleByDefault", visibleByDefault);
        yml.set(path+".lookAtPlayers", lookAtPlayers);
        yml.set(path+".skin.texture", texture);
        yml.set(path+".skin.signature", signature);
        yml.set(path+".secondLine", secondLine);
        yml.set(path+".disguise", disguise.toString());

        for(int i = 0; i < 5; i++) yml.set(path+".eq."+i, items[i]);

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

        holder.teleport(location.clone().add(0, 0, 0));

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

    public ArmorStand getHolder() {
        return holder;
    }

    public void deleteArmorStand() {

        holder.remove();

    }

    public void dspawn(Player player) {

        sendSpawnPackets(player);
        queRemovePacket(player);

    }

}
