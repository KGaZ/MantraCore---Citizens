package me.kgaz.util;

import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.entity.Player;

public interface PacketInListener {

    /*
        Return whether the packet should be cancelled
     */

    public boolean handlePacket(Packet packet, Player target);

}