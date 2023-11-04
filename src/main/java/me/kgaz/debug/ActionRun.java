package me.kgaz.debug;

import org.bukkit.entity.Player;

import java.io.IOException;

public interface ActionRun {

    public void run(Player executor, String[] arguments) throws IOException;

}
