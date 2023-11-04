package me.kgaz.events;

import me.kgaz.MantraLibs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class AdminCommand implements CommandExecutor, Listener {

    private MantraLibs main;

    public AdminCommand(MantraLibs main) {

        main.getCommand("admin").setExecutor(this);
        main.registerListener(this);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(cmd.getName().equalsIgnoreCase("admin")) {

            if(sender.hasPermission("admin")) {

                Inventory inv = Bukkit.createInventory(null, 9, "Menu Administracyjne");

                {

                    ItemStack is = new ItemStack(Material.EMERALD);
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName("§2Rozdaj przedmiot losowemu graczowi");
                    im.setLore(Arrays.asList("§7Przedmiot z twojej reki trafi do", "§7losowego gracza na serwerze, ktory nie jest Toba"));
                    is.setItemMeta(im);
                    inv.setItem(0, is);

                }

                {

                    ItemStack is = new ItemStack(Material.DIAMOND);
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName("§9Rozdaj przedmiot kazdemu graczowi");
                    im.setLore(Arrays.asList("§7Przedmiot z twojej reki trafi do", "§7kazdego gracza na serwerze."));
                    is.setItemMeta(im);
                    inv.setItem(1, is);

                }

                Player player = (Player) sender;
                player.openInventory(inv);

            }

        }

        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if(e.getClickedInventory() == null) return;
        if(e.getClickedInventory().getTitle() == null) return;

        if(e.getClickedInventory().getTitle().equalsIgnoreCase("Menu Administracyjne")) {

            e.setCancelled(true);
            e.setResult(Event.Result.DENY);

            if(e.getSlot() == 0) {

                if(Bukkit.getOnlinePlayers().size() == 1) {

                    e.getWhoClicked().closeInventory();
                    ((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.VILLAGER_DEATH, 0.1f, 0.1f);
                    ((Player) e.getWhoClicked()).sendMessage("§cNie ma nikogo na serwerze, zjebie!");

                } else {

                    ((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.LEVEL_UP, 1.5f, 1f);

                    Player chosen = new ArrayList<>(Bukkit.getOnlinePlayers()).get(new Random().nextInt(Bukkit.getOnlinePlayers().size()));
                    while(chosen == e.getWhoClicked()) {

                        chosen = new ArrayList<>(Bukkit.getOnlinePlayers()).get(new Random().nextInt(Bukkit.getOnlinePlayers().size()));

                    }

                    Bukkit.broadcastMessage("§8 »§7 Administrator §2"+e.getWhoClicked().getName()+" §7rozdal przedmiot losowej osobie. Szczesciarzem, ktory go otrzymal jest §a"+chosen.getName()+"§8!");
                    chosen.getInventory().addItem(e.getWhoClicked().getItemInHand());
                    chosen.playSound(chosen.getLocation(), Sound.LEVEL_UP, 1f, 1f);

                }

            }

            if(e.getSlot() == 1) {

                if(Bukkit.getOnlinePlayers().size() == 1) {

                    e.getWhoClicked().closeInventory();
                    ((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.VILLAGER_DEATH, 0.1f, 0.1f);
                    ((Player) e.getWhoClicked()).sendMessage("§cNie ma nikogo na serwerze, zjebie!");

                } else {

                    ((Player)e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.LEVEL_UP, 1.5f, 1f);

                    for(Player chosen : Bukkit.getOnlinePlayers()) {

                        if(chosen == e.getWhoClicked()) continue;

                        chosen.getInventory().addItem(e.getWhoClicked().getItemInHand());
                        chosen.playSound(chosen.getLocation(), Sound.LEVEL_UP, 1f, 1f);

                    }

                    Bukkit.broadcastMessage("§8 »§7 Administrator §2"+e.getWhoClicked().getName()+" §7rozdal przedmiot kazdemu graczowi na serwerze");

                }

            }

        }

    }

}
