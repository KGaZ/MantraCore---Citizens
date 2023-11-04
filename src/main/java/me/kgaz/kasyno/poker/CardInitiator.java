package me.kgaz.kasyno.poker;

import me.kgaz.MantraLibs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CardInitiator {

    private Map<Card, Short> mapId = new HashMap<>();

    public CardInitiator(MantraLibs libs) {

        for(Card card : Card.values()) {

            MapView view = Bukkit.createMap(Bukkit.getWorlds().get(0));

            new ArrayList<>(view.getRenderers()).forEach(view::removeRenderer);
            view.addRenderer(card.getRenderer());

            mapId.put(card, view.getId());

        }

    }

    public ItemStack getCardView(Card card) {

        ItemStack is = new ItemStack(Material.MAP, 1, mapId.get(card));
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§4Karta do Pokera");
        is.setItemMeta(im);
        return is;

    }

}
