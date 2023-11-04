package me.kgaz.kasyno;

import me.kgaz.debug.Debug;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class ShrekWatcher extends MapRenderer {

    private Animation anim;
    private int offsetX, offsetY;
    private BufferedImage image = null;

    public ShrekWatcher(Animation animation, int offsetX, int offsetY) {

        this.anim = animation;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {


        if(Debug.startAnimation) {

            if(image != null) {

                if(image == anim.getCurrentAnimation()) return;

            }

            mapCanvas.drawImage(offsetX, offsetY, anim.getCurrentAnimation());

        }

    }

}
