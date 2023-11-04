package me.kgaz.npcs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.kgaz.MantraLibs;
import org.bukkit.Bukkit;

import java.io.InputStreamReader;
import java.net.URL;

public class SkinFetcher {

    public SkinFetcher(MantraLibs main, String nick, FetchResult result) {

        Thread fetchThread = new Thread() {

            public void run() {

                String texture, signature;

                try {

                    URL url_0 = new URL("https://api.mojang.com/users/profiles/minecraft/" + nick);
                    InputStreamReader reader_0 = new InputStreamReader(url_0.openStream());
                    String uuid = new JsonParser().parse(reader_0).getAsJsonObject().get("id").getAsString();

                    URL url_1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                    InputStreamReader reader_1 = new InputStreamReader(url_1.openStream());
                    JsonObject textureProperty = new JsonParser().parse(reader_1).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
                    texture = textureProperty.get("value").getAsString();
                    signature = textureProperty.get("signature").getAsString();

                } catch(Exception exc) {

                    texture = ""; signature = "";

                }

                String finalTexture = texture;
                String finalSignature = signature;
                Bukkit.getScheduler().runTask(main, new Runnable() {
                    @Override
                    public void run() {
                        result.skinFetched(finalTexture, finalSignature);
                    }
                });

            }

        };

        fetchThread.start();

    }

    public interface FetchResult {

        public void skinFetched(String texture, String signature);

    }

}
