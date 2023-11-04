package me.kgaz.events;

import me.kgaz.MantraLibs;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import pl.nomand.mantracore.common.MantraCore;

public class SpinLocations implements Listener {

    private Location loc = null;
    private Spin spin;

    public SpinLocations(MantraLibs libs) {

        libs.registerListener(this);
        this.spin = new Spin(libs);

    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {

        if(loc == null) loc = MantraCore.getInstance().getLocationManager().getLocation("globus");

        if(e.getClickedBlock() != null) {

            if(e.getClickedBlock().getLocation().getBlockX() == loc.getBlockX() && e.getClickedBlock().getLocation().getBlockY() == loc.getBlockY() && e.getClickedBlock().getLocation().getBlockZ() == loc.getBlockZ()) {

                spin.spin(e.getClickedBlock(), 2, true, 8);

            }

        }

    }

}
