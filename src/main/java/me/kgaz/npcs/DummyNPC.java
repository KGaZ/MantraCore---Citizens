package me.kgaz.npcs;

import me.kgaz.KNPC;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class DummyNPC extends NPC {


    public DummyNPC(int id, YamlConfiguration yml, KNPC main) {
        super(id, yml, main);
    }

    public DummyNPC(int id, KNPC main, Location loc, String name) {
        super(id, main, loc, name);
    }

    @Override
    public void setCustomLineModifier(CustomSecondLine csm) {

    }
}
