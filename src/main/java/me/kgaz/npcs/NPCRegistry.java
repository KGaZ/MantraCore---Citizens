package me.kgaz.npcs;

import me.kgaz.MantraLibs;
import me.kgaz.util.Task;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NPCRegistry implements Task {

    private Map<Integer, NPC> registry;
    private MantraLibs main;
    private int lastId;

    public NPCRegistry(MantraLibs main) {

        this.main = main;

        registry = new HashMap<>();

        main.registerTaskOnDisable(this);

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

        if(registry.get(id) == null) return new DummyNPC(0, null, null, "");

        return registry.get(id);

    }

    public void onDisable() {

        getNpcs().forEach(NPC::deleteArmorStand);

    }

}
