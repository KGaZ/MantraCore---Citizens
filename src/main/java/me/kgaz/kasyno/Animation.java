package me.kgaz.kasyno;

import me.kgaz.MantraLibs;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Animation {

    private BufferedImage currentAnimation;
    private File movie;
    private ImageInputStream in;
    private ImageReader reader;
    private int lastTick = 0;
    private int cooldown = 0;


    public Animation(String path) {

        movie = new File(MantraLibs.getInstance().getDataFolder(), "maps/movies/"+path);
        try {

            reader = ImageIO.getImageReadersBySuffix("GIF").next();
            in = ImageIO.createImageInputStream(movie);
            reader.setInput(in);

        } catch(Exception exc) {exc.printStackTrace();}

    }

    public void tick() {

        try {

            if(lastTick >= reader.getNumImages(true)) lastTick = 0;

            currentAnimation = reader.read(lastTick);

            lastTick++;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public BufferedImage getCurrentAnimation() {

        return currentAnimation;

    }

}
