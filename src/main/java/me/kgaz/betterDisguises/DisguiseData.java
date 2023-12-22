package me.kgaz.betterDisguises;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.kgaz.MantraLibs;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.scoreboard.CraftScoreboard;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DisguiseData {

    private static int TEAM_ID = 0;

    private String[] name;
    private String texture;
    private String signature;

    private Entity replacement;
    private EntityEquipment eq;
    private EntityPlayer entity;

    private List<Packet> packets;

    public DisguiseData(String name, String texture, String signature, Entity replacement) {
        int size = (int) Math.ceil(((float) name.length())/16f);
        this.name = new String[size];
        for(int i = 0; i < size; i++) {
            this.name[i] = name.substring(i*16, Math.min((i + 1) * 16, name.length()));
        }

        this.texture = texture;
        this.signature = signature;
        this.replacement = replacement;

    }

    public void sendSpawnPacket(Player player) {

        Bukkit.getScheduler().runTaskAsynchronously(MantraLibs.MAIN, new Runnable() {

            @Override
            public void run() {

                eq = ((LivingEntity)replacement.getBukkitEntity()).getEquipment();
                Location location = replacement.getBukkitEntity().getLocation().clone();

                MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
                WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

                GameProfile profile = new GameProfile(UUID.randomUUID(), name.length > 1 ? name[1] : name[0]);

                profile.getProperties().put("textures", new Property("textures", texture, signature));

                try {
                    entity = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));
                } catch(Exception exc) {return;}

                try {

                    Field id = Entity.class.getDeclaredField("id");

                    id.setAccessible(true);
                    id.setInt(entity, replacement.getId());


                } catch(Exception exc) {

                    exc.printStackTrace();

                }

                entity.setLocation(location.getX(),
                        location.getY(),
                        location.getZ(),
                        location.getYaw(),
                        location.getPitch());

                entity.setPosition(location.getX(),
                        location.getY(),
                        location.getZ());

                entity.getBukkitEntity().getEquipment().setHelmet(eq.getHelmet().clone());
                entity.getBukkitEntity().getEquipment().setChestplate(eq.getChestplate().clone());
                entity.getBukkitEntity().getEquipment().setLeggings(eq.getLeggings().clone());
                entity.getBukkitEntity().getEquipment().setBoots(eq.getBoots().clone());
                entity.getBukkitEntity().getEquipment().setItemInHand(eq.getItemInHand());

                List<Packet> eq = new ArrayList<>();
                for(int i = 0; i < 5; i++) if(replacement.getEquipment()[i] != null) eq.add(new PacketPlayOutEntityEquipment(entity.getId(), i, replacement.getEquipment()[i]));

                packets = new ArrayList<>();

                PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(entity);

                PacketPlayOutPlayerInfo infoSpawnPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entity);

                DataWatcher watcher = entity.getDataWatcher();

                watcher.watch(10, (byte) 127);

                //packets.add(infoSpawnPacket);
                //packets.add(spawn);
                //packets.add(skinPacket);

                PlayerConnection conn = ((CraftPlayer)player).getHandle().playerConnection;

                //try {packets.forEach(conn::sendPacket);} catch(Exception exc) {}

                conn.sendPacket(infoSpawnPacket);
                conn.sendPacket(spawn);
                //conn.sendPacket(skinPacket);

                if(name.length > 1) {

                    TEAM_ID++;

                    ScoreboardTeam team = new ScoreboardTeam(((CraftScoreboard)Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), (TEAM_ID+"k"));
                    if(name.length == 3) {
                        team.setPrefix(name[0]);
                        team.setSuffix(name[2]);
                    } else {
                        team.setPrefix(name[0]);
                    }

                    conn.sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
                    ArrayList<String> playerToAdd = new ArrayList<>(Collections.singletonList(name[1]));
                    conn.sendPacket(new PacketPlayOutScoreboardTeam(team, playerToAdd, 3));

                }

                new BukkitRunnable() {

                    public void run (){

                        try{eq.forEach(conn::sendPacket);} catch(Exception exc) {}

                    }

                }.runTaskLater(MantraLibs.MAIN, 3);

            }
        });

    }


}
