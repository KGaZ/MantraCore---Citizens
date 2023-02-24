package me.kgaz.holograms;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import io.netty.channel.ChannelHandlerContext;
import me.kgaz.Citizens;
import me.kgaz.npcs.VisibilityMask;
import me.kgaz.util.PacketOutListener;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Hologram implements Listener, PacketOutListener {

    public static int LAST_ID = 0;

    private Citizens main;
    private Location beginLoc;
    private final int id;
    private List<HologramLine> lines;
    private VisibilityMask mask;
    private boolean spawned;

    public Hologram(Citizens main, Location loc, String... lines) {

        id = LAST_ID++;
        this.main = main;
        this.beginLoc = loc;
        this.lines = new ArrayList<>();
        this.spawned = false;

        for(String line : lines) {

            this.lines.add(new HologramLine(this, loc.clone().add(0, this.lines.size()*0.25d, 0), line));

        }

        main.registerListener(this);
      //  main.getUserManager().registerPacketOutListener(this);

    }

    public void addLine(String text) {

        HologramLine line = new HologramLine(this, beginLoc.clone().add(0, 0.25d*lines.size(), 0), text);
        lines.add(line);

        if(spawned) line.spawn();

    }

    public HologramLine getLine(int line) {

        if(line >= lines.size()) return null;

        return lines.get(line);

    }

    public void removeLine(int line) {

        if(line >= lines.size()) return;

        HologramLine l = lines.get(line);
        if(spawned) l.despawn();
        lines.remove(line);

    }

    public void spawn() {

        if(spawned) return;

        this.spawned = true;

        lines.forEach(HologramLine::spawn);

    }

    public void despawn() {

        if(!spawned) return;

        this.spawned = false;

        lines.forEach(HologramLine::despawn);

    }

    @Override
    public boolean handlePacket(ChannelHandlerContext context, PlayerConnection connection, Packet packet, Player target) {

        if(packet instanceof PacketPlayOutSpawnEntityLiving) {

            PacketPlayOutSpawnEntityLiving data = (PacketPlayOutSpawnEntityLiving) packet;

            int entityId;

            try {

                Field a = data.getClass().getDeclaredField("a");
                a.setAccessible(true);
                entityId = a.getInt(data);

            } catch (Exception exc) { return false; }

            for(HologramLine line : lines) {

                if(entityId == line.getEntityId()) {

                    if(mask != null) {

                        if(!mask.shouldSee(target)) {

                            return true;

                        }

                    }

                }

            }

        }

        return false;
    }

}
