package me.kgaz.diguises.types;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.kgaz.diguises.DisguiseData;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public class PlayerDisguise extends DisguiseData {

    private String skinTexture;
    private String skinSignature;

    public PlayerDisguise(LivingEntity entity) {
        super(entity, 0);

        skinTexture = "";
        skinSignature = "";

    }

    public PlayerDisguise setSkinTexture(String texture) {
        this.skinTexture = texture;
        return this;
    }

    public PlayerDisguise setSignature(String signature) {
        this.skinSignature = signature;
        return this;
    }

    public PlayerDisguise setSkin(GameProfile skin) {

        this.skinTexture = skin.getProperties().get("textures").iterator().next().getValue();
        this.skinSignature = skin.getProperties().get("textures").iterator().next().getSignature();

        return this;

    }

    public PlayerDisguise copySkin(Player player) {

        GameProfile skin = ((CraftPlayer)player).getHandle().getProfile();

        this.skinTexture = skin.getProperties().get("textures").iterator().next().getValue();
        this.skinSignature = skin.getProperties().get("textures").iterator().next().getSignature();

        return this;

    }

    public EntityPlayer generateEntity() {

        Location location = entity.getLocation();
        String mobName = entity.getCustomName();

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer world = ((CraftWorld) location.getWorld()).getHandle();

        String[] names = new String[0];

        if(isLongName()) {

            names = new String[2];
            names[0] = mobName.substring(0, mobName.length()-16);
            names[1] = mobName.substring(mobName.length()-16);

        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), isLongName() ? names[1] : mobName);

        profile.getProperties().put("textures", new Property("textures", skinTexture, skinSignature));

        EntityPlayer player = new EntityPlayer(server, world, profile, new PlayerInteractManager(world));

        server.getPlayerList().players.remove(player);

        try {

            Field id = net.minecraft.server.v1_8_R3.Entity.class.getDeclaredField("id");

            id.setAccessible(true);
            id.setInt(player, entity.getEntityId());


        } catch(Exception exc) {

            exc.printStackTrace();
            return null;

        }

        player.setLocation(location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());

        return player;

    }

    public String getPrefix() {

        String mobName = entity.getCustomName();

        if(isLongName()) {

            return mobName.substring(0, mobName.length()-16);

        } else return null;

    }

    public String getEntityName() {

        String mobName = entity.getCustomName();

        if(isLongName()) {

            return mobName.substring(mobName.length()-16);

        } else return mobName;

    }

    public boolean isLongName() {

        return entity.getCustomName().length() > 16;

    }

}
