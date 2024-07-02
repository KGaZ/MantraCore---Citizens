package me.kgaz.users;

import io.netty.channel.*;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.kgaz.npcs.NPC;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.*;

public class User {

    public static Map<UUID, Channel> channels = new HashMap<UUID, Channel>();

    private static int TEAM_ID = 534;

    private boolean valid;

    private boolean disabled;
    private UserManager manager;
    private Player owner;
    private String nickName;

    private boolean cancelled;

    public User(Player player, UserManager manager) {

        valid = player.isOnline();

        if(!valid) throw new IllegalArgumentException("Tried to create User from offline Player!");

        disabled = false;

        owner = player;
        this.manager = manager;
        nickName = player.getName();

        CraftPlayer craftPlayer = (CraftPlayer)player;
        Channel channel = craftPlayer.getHandle().playerConnection.networkManager.channel;
        channels.put(craftPlayer.getUniqueId(), channel);

        if(channel.pipeline().get("PacketInjector") != null) {
            return;
        }

        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<PacketPlayInUseEntity>() {

            public void decode(ChannelHandlerContext context, PacketPlayInUseEntity packet, List<Object> objects) {

                objects.add(packet);

                if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayInUseEntity")) {

                    int id = (int) getValue(packet, "a");

                    for(NPC npc : manager.getOwner().getNpcRegistry().getNpcs()) {

                        if(npc.getEntityId() == id) {

                            PacketPlayInUseEntity.EnumEntityUseAction action = (PacketPlayInUseEntity.EnumEntityUseAction) getValue(packet, "action");
                            npc.rightClick(player, action);

                        }

                    }

                }

            }

        });

        channel.pipeline().addBefore("packet_handler", player.getName(), new ChannelOutboundHandlerAdapter() {

            public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {

                if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayOutSpawnEntity")) {

                    int id = (int) getValue(packet, "a");

                    for(NPC npc : manager.getOwner().getNpcRegistry().getNpcs()) {

                        if(npc.getHandlerId() == id) {

                            npc.handlerSent(player);

                        }

                    }

                } //else if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayOutEntityMetadata")) {
//
//                    int entityId = (int) getValue(packet, "a");
//
//                    WorldCache cache = manager.getOwner().getMobManager().getCache().get(player.getWorld());
//
//                    if (!cache.getPlayerDisguisedMobs().containsKey(entityId)) {
//                        super.write(context, packet, promise);
//                         return;
//                    }
//
//                    List<DataWatcher.WatchableObject> list = (List<DataWatcher.WatchableObject>) getValue(packet, "b");
//                    List<DataWatcher.WatchableObject> filtered = new ArrayList<>();
//
//                    int removed = 0;
//
//                    for (DataWatcher.WatchableObject watchableObject : list) {
//
//                        int index;
//
//                        try {
//
//                            Field id = DataWatcher.WatchableObject.class.getDeclaredField("a");
//                            id.setAccessible(true);
//                            index = id.getInt(watchableObject);
//
//                        } catch (Exception e) {
//                            removed++;
//                            continue;
//                        }
//
//                        if ((index > 0 && index <= 10) || index == 15 || (index >= 16 && index <= 18)) {
//
//                            filtered.add(watchableObject);
//
//                        } else removed++;
//
//                    }
//
//                    try {
//
//                        Field lista = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
//                        lista.setAccessible(true);
//                        lista.set(packet, filtered);
//
//                    } catch (Exception exc) {}
//
//                } else if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayOutSpawnEntityLiving")) {
//
//                    int entityId = (int) getValue(packet, "a");
//
//                    WorldCache cache = manager.getOwner().getMobManager().getCache().get(player.getWorld());
//
//                    if (cache.getPlayerDisguisedMobs().containsKey(entityId)) {
//
//                        CachedMob mob = cache.getPlayerDisguisedMobs().get(entityId);
//
//                        LivingEntity replacement = mob.getLivingEntity();
//                        Location loc = replacement.getLocation().clone();
//                        EntityEquipment eq = replacement.getEquipment();
//
//                        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
//                        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();
//
//                        int size = (int) Math.ceil(((float) mob.getMob().getName().length()) / 16f);
//                        String[] name = new String[size];
//                        for (int i = 0; i < size; i++) {
//                            name[i] = mob.getMob().getName().substring(i * 16, Math.min((i + 1) * 16, mob.getMob().getName().length()));
//                        }
//
//                        GameProfile profile = new GameProfile(UUID.randomUUID(), name.length > 1 ? name[1] : name[0]);
//
//                        profile.getProperties().put("textures", new Property("textures", mob.getMob().getDisguiseData().getSkin().getTexture(), mob.getMob().getDisguiseData().getSkin().getSignature()));
//
//                        EntityPlayer entity = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
//
//                        try {
//
//                            Field id = Entity.class.getDeclaredField("id");
//
//                            id.setAccessible(true);
//                            id.setInt(entity, replacement.getEntityId());
//
//                        } catch (Exception exc) {
//                            exc.printStackTrace();
//                        }
//
//                        entity.setLocation(loc.getX(),
//                                loc.getY(),
//                                loc.getZ(),
//                                loc.getYaw(),
//                                loc.getPitch());
//
//                        entity.setPosition(loc.getX(),
//                                loc.getY(),
//                                loc.getZ());
//
//                        try {
//
//                            entity.getBukkitEntity().getEquipment().setHelmet(eq.getHelmet().clone());
//                            entity.getBukkitEntity().getEquipment().setChestplate(eq.getChestplate().clone());
//                            entity.getBukkitEntity().getEquipment().setLeggings(eq.getLeggings().clone());
//                            entity.getBukkitEntity().getEquipment().setBoots(eq.getBoots().clone());
//                            entity.getBukkitEntity().getEquipment().setItemInHand(eq.getItemInHand());
//
//                        } catch(Exception ignored) {
//                            Bukkit.getLogger().warning("Nie mozna bylo ubrac entity w itemy.");
//                        }
//
//                        Entity en_nms = ((CraftLivingEntity)replacement).getHandle();
//
//                        List<Packet> eqp = new ArrayList<>();
//                        for(int i = 0; i < 5; i++) if(en_nms.getEquipment()[i] != null) eqp.add(new PacketPlayOutEntityEquipment(entity.getId(), i, en_nms.getEquipment()[i]));
//
//                        PacketPlayOutPlayerInfo infoSpawnPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity);
//                        PacketPlayOutNamedEntitySpawn spawnPacket = new PacketPlayOutNamedEntitySpawn(entity);
//
//                        DataWatcher watcher = entity.getDataWatcher();
//
//                        watcher.watch(10, (byte) 127);
//
//                        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;
//
//                        connection.sendPacket(infoSpawnPacket);
//                        connection.sendPacket(spawnPacket);
//
//                        if (name.length > 1) {
//
//                            TEAM_ID++;
//
//                            ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), (TEAM_ID + "k"));
//                            if (name.length == 3) {
//                                team.setPrefix(name[0]);
//                                team.setSuffix(name[2]);
//                            } else {
//                                team.setPrefix(name[0]);
//                            }
//
//                            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
//                            ArrayList<String> playerToAdd = new ArrayList<>(Collections.singletonList(name[1]));
//                            connection.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));
//
//                        }
//
//                        new BukkitRunnable() {
//
//                            public void run (){
//                                try{eqp.forEach(connection::sendPacket);} catch(Exception ignored) {}
//                            }
//
//                        }.runTaskLater(MantraLibs.MAIN, 3);
//
//                        return;
//                    }
//
//
//                } else if (packet.getClass().getSimpleName().equalsIgnoreCase("PacketPlayOutEntityStatus")) {
//
//                    byte stat = (byte) getValue(packet, "b");
//                    int entityID = (int) getValue(packet, "a");
//
//                    WorldCache cache = manager.getOwner().getMobManager().getCache().get(player.getWorld());
//
//                    if (cache.getPlayerDisguisedMobs().containsKey(entityID)) {
//                        if (stat == 3) {
//
//                            Packet p = new PacketPlayOutEntityDestroy(entityID);
//
//                            Bukkit.getScheduler().runTaskLater(MantraLibs.MAIN, new Runnable() {
//                                @Override
//                                public void run() {
//                                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(p);
//                                }
//                            }, 30);
//
//                            return;
//                        }
//                    }
//
//
//                }

                super.write(context, packet, promise);

            }

        });

    }

    public void onQuit(PlayerQuitEvent e) {

       cancelled = true;

        try{

            Channel channel = ((CraftPlayer)e.getPlayer()).getHandle().playerConnection.networkManager.channel;

            try {

                channel.eventLoop().submit(() -> {

                    channel.pipeline().remove(e.getPlayer().getName());
                    channel.pipeline().remove("PacketInjector");
                    return null;

                });

            }catch(Exception exc) {

            }

            channels.remove(e.getPlayer().getUniqueId());

        }catch (Exception ex){

            ex.printStackTrace();
        }


    }
    public void disable() {

        disabled = true;

    }


    private Object getValue(Object instance, String name) {
        Object result = null;

        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            result = field.get(instance);
            field.setAccessible(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
