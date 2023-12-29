package me.kgaz.users;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageDecoder;
import me.kgaz.tasks.Tickable;
import me.kgaz.util.PacketInListener;
import me.kgaz.util.PacketOutListener;
import me.kgaz.util.Removeable;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class User implements Tickable {

    private boolean valid;

    private boolean disabled;
    private UserManager manager;
    private Player owner;
    private String nickName;

    private boolean cancelled;

    private List<PacketInListener> packetInListenerList;
    private List<PacketOutListener> packetOutListenerList;

    private ChannelPipeline pipeline;
    private MessageToMessageDecoder<Packet> in;
    private ChannelOutboundHandlerAdapter out;


    public User(Player player, UserManager manager) {

        valid = player.isOnline();

        if(!valid) throw new IllegalArgumentException("Tried to create User from offline Player!");

        disabled = false;

        owner = player;
        this.manager = manager;
        nickName = player.getName();

        packetInListenerList = new ArrayList<>();
        packetOutListenerList = new ArrayList<>();

        PlayerConnection connection = ((CraftPlayer)player).getHandle().playerConnection;

        pipeline = connection.networkManager.channel.pipeline();

        in = new MessageToMessageDecoder<Packet>() {

            public void decode(ChannelHandlerContext context, Packet packet, List<Object> objects) {

                if(disabled) {

                    objects.add(packet);
                    try {
                        context.write(objects);
                    } catch(Exception exc) {
                        return;
                    }

                }

                for(PacketInListener packetInlistener : manager.getPacketInListenerList()) {

                    try {

                        if (packetInlistener.handlePacket(packet, player)) {

                            return;

                        }

                    } catch(Exception exc) {

                        exc.printStackTrace();

                    }

                }

                for(PacketInListener packetInlistener : packetInListenerList) {

                    try {

                        if (packetInlistener.handlePacket(packet, player)) {

                            return;

                        }

                    } catch(Exception exc) {

                        exc.printStackTrace();

                    }

                }

                objects.add(packet);

                try {
                    context.write(objects);
                } catch(Exception exc) {
                    return;
                }

            }

        };

        try {

            pipeline.addAfter("decoder", "incoming_handler", in);

        } catch(Exception exc) {

            player.kickPlayer("Wystapil nieznany blad. Prosimy o relog oraz kontakt z administracja.");
            valid=false;
            return;

        }

        out = new ChannelOutboundHandlerAdapter() {

            @Override
            public void write(ChannelHandlerContext context, Object packet, ChannelPromise promise) throws Exception {

                if(disabled) {

                    super.write(context, packet, promise);
                    return;
                }

                for(PacketOutListener packetOutListener : manager.getPacketOutListenerList()) {

                    try {

                        if (packetOutListener.handlePacket(context, connection, (Packet) packet, player)) {

                            return;

                        }

                    } catch(Exception exc) {

                        exc.printStackTrace();

                    }

                }

                for(PacketOutListener packetOutListener : packetOutListenerList) {

                    try {

                        if (packetOutListener.handlePacket(context, connection, (Packet) packet, player)) {

                            return;

                        }

                    } catch(Exception exc) {

                        exc.printStackTrace();

                    }

                }

                try {

                    super.write(context, packet, promise);

                } catch(Exception exc) {

                    return;

                }

            }

        };

        pipeline.addBefore("packet_handler", "custom_handler", out);

    }

    public void onQuit(PlayerQuitEvent e) {

       cancelled = true;

    }

    public void registerPacketInListener(PacketInListener listener) {

        packetInListenerList.add(listener);

    }

    public void registerPacketOutListener(PacketOutListener listener) {

        packetOutListenerList.add(listener);

    }

    @Override
    public void run() {

        packetInListenerList.removeIf(packetInListener -> {
            return (packetInListener instanceof Removeable && !((Removeable) packetInListener).isActive());
        });

        packetOutListenerList.removeIf(packetInListener -> {
            return (packetInListener instanceof Removeable && !((Removeable) packetInListener).isActive());
        });

    }

    @Override
    public int getPeriod() {
        return 20;
    }

    public void disable() {

        disabled = true;

    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isValid(){
        return valid;
    }
}
