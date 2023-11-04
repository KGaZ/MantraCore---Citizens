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

public class CustomRenderer extends MapRenderer {

    private File file;
    private BufferedImage img;
    private boolean drawn = false;

    public CustomRenderer(String path) {

        file = new File(MantraLibs.getInstance().getDataFolder(), "maps/"+path);

        try {

            img = ImageIO.read(file);

        } catch (IOException e) {

            System.out.println("Cannot load "+file.getAbsolutePath());

        }


    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {

        if(drawn) return;

        mapCanvas.drawImage(0, 0, img);

        drawn = true;

    }

}
