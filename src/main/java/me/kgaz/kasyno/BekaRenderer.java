package me.kgaz.kasyno;

import me.kgaz.MantraLibs;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BekaRenderer extends MapRenderer {

    private BufferedImage[] images;
    private int animation = 0;
    private int cooldown = 0;

    public BekaRenderer(String... path) throws IOException {

        images = new BufferedImage[path.length];

        int i = 0;

        for(String s : path) {

            images[i] = ImageIO.read(new File(MantraLibs.getInstance().getDataFolder(), "maps/"+s));
            i++;

        }

    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {

        if(cooldown > 0) {

            cooldown--;
            return;

        }

        mapCanvas.drawImage(0, 0, images[animation]);

        animation++;
        if(animation == images.length) animation = 0;
        cooldown = 7;

    }

}
