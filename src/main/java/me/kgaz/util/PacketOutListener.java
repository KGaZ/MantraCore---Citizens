package me.kgaz.util;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.entity.Player;

public interface PacketOutListener {

    /*
    Handles packet either for single player or for every player,
    depends on how you register it. If you somehow modified packet,
    you need to do context.write(packet) and return true,
    if you want to cancel packet float just return true,
    if you did not do anything with packet, just rode some things
    off of it, return false,
    sorry it is so complicated but fuck mojang.
    */

    public boolean handlePacket(ChannelHandlerContext context, PlayerConnection connection, Packet packet, Player target);

}