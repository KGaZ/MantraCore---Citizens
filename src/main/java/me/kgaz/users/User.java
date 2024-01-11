package me.kgaz.users;

import io.netty.channel.*;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.kgaz.npcs.NPC;
import me.kgaz.tasks.Tickable;
import me.kgaz.util.PacketInListener;
import me.kgaz.util.PacketOutListener;
import me.kgaz.util.Removeable;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.*;

public class User {

    public static Map<UUID, Channel> channels = new HashMap<UUID, Channel>();

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

                }

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
