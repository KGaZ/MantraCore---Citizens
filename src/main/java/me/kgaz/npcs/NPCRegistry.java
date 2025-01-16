package me.kgaz.npcs;

import me.kgaz.KNPC;
import me.kgaz.util.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NPCRegistry implements Task {

    private Map<Integer, NPC> registry;
    private KNPC main;
    private int lastId;

    public NPCRegistry(KNPC main) {

        this.main = main;

        registry = new HashMap<>();

        main.registerTaskOnDisable(this);

        int count = 0;
        System.out.println("Rozpoczeto usuwanie starych armorstandow...");

        for(World world : Bukkit.getWorlds()) {

            for(Entity entity : world.getEntities()) {

                if(((CraftEntity)entity).getHandle() instanceof RemoveArmorStand) {

                    entity.remove();
                    count++;

                }

            }

        }

        System.out.println("Poprawnie usunieto "+count+" starych armorstandow!");

        loadFiles();

    }

    public NPC createNewNPC(String name, Location loc) {

        lastId++;

        NPC npc = new NPC(lastId, main, loc, name);

        registry.put(lastId, npc);

        return npc;

    }

    private void loadFiles() {

        File file = new File(main.getDataFolder(), "npcs.yml");
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);

        if(!yml.isSet("npcs")) return;

        for(String key : yml.getConfigurationSection("npcs").getKeys(false)) {

            lastId = Integer.parseInt(key);

            NPC npc = new NPC(lastId, yml, main);

            npc.spawn();

            registry.put(lastId, npc);

        }

    }

    @Override
    public void run() {

        File file = new File(main.getDataFolder(), "npcs.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        yml.set("npcs", null);

        for(Integer id : registry.keySet()) {

            NPC npc = registry.get(id);
            npc.save(yml);

        }

        try {
            yml.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Collection<NPC> getNpcs() {

        return registry.values();

    }

    public void removeNPC(int citizenId) {

        if(!registry.containsKey(citizenId)) return;

        registry.get(citizenId).remove();
        registry.remove(citizenId);

    }

    public NPC getNpc(int id) {

//        if(registry.get(id) == null) return new DummyNPC(0, null, null, "");

        return registry.get(id);

    }

    public void onDisable() {

        getNpcs().forEach(NPC::deleteArmorStand);

    }

}
