package me.kgaz.betterDisguises;

import io.netty.channel.ChannelHandlerContext;
import me.kgaz.MantraLibs;
import me.kgaz.util.PacketOutListener;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.DataWatcher.WatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisguisePacketManager implements PacketOutListener {

    private Map<Integer, DisguiseData> data = new HashMap<>();

    public void disguise(Entity entity, DisguiseData data) {

        this.data.put(entity.getEntityId(), data);

    }

    @Override
    public boolean handlePacket(ChannelHandlerContext context, PlayerConnection connection, Packet packet, Player target) {

        if(packet instanceof PacketPlayOutEntityMetadata) {

            PacketPlayOutEntityMetadata data = (PacketPlayOutEntityMetadata) packet;

            int entityid;
            try {

                Field id = PacketPlayOutEntityMetadata.class.getDeclaredField("a");
                id.setAccessible(true);
                entityid = id.getInt(data);

            } catch(Exception exc) {

                return false;

            }

            if(!this.data.containsKey(entityid)) {

                return false;

            }

            List<WatchableObject> list;

            try {

                Field lista = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
                lista.setAccessible(true);
                list = (List<WatchableObject>) lista.get(data);

            } catch(Exception exc) {

                return false;

            }

            List<WatchableObject> filtered = new ArrayList<>();

            int removed = 0;

            for(WatchableObject watchableObject : list) {

                int index;

                try {

                    Field id = WatchableObject.class.getDeclaredField("a");
                    id.setAccessible(true);
                    index = id.getInt(watchableObject);

                } catch (Exception e) {

                    removed++;
                    continue;

                }

                if((index > 0 && index <= 10) || index == 15 || (index >= 16 && index <= 18)) {

                    filtered.add(watchableObject);

                } else removed++;

            }

            try {

                Field lista = PacketPlayOutEntityMetadata.class.getDeclaredField("b");
                lista.setAccessible(true);
                lista.set(data, filtered);

            } catch(Exception exc) {

                return false;

            }

            if(removed != 0) {

                context.write(data);
                return true;

            }

        }

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

            if(this.data.containsKey(entityID)) {

                if(stat == 3) {

                    Packet p = new PacketPlayOutEntityDestroy(entityID);

                    Bukkit.getScheduler().runTaskLater(MantraLibs.MAIN, new Runnable() {

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

            PacketPlayOutSpawnEntityLiving data = (PacketPlayOutSpawnEntityLiving) packet;
            int entityid;
            try {

                Field id = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("a");
                id.setAccessible(true);
                entityid = id.getInt(data);

            } catch(Exception exc) {

                entityid = -1;

            }

            if(entityid == -1) return false;

            if(this.data.containsKey(entityid)) {

                this.data.get(entityid).sendSpawnPacket(target);

                return true;

            }

        }

        return false;
    }
}
