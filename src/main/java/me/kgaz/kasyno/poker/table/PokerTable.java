package me.kgaz.kasyno.poker.table;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import me.kgaz.MantraLibs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;
import pl.nomand.mantracore.common.MantraCore;

public class PokerTable implements Listener {

    private String id;

    private Hologram mainHolo;
    private Hologram sit1, sit2;
    private Hologram stats1, stats2;
    private TextLine playerCount;

    private ArmorStand sitting1 = null, sitting2 = null;
    private Player player1 = null, player2 = null;
    private boolean betting = false;

    private PokerGame poker = null;

    public PokerTable(MantraLibs libs, String id) {

        libs.registerListener(this);

        this.id = id;

        mainHolo = HologramsAPI.createHologram(libs, MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"main"));

        mainHolo.appendTextLine("Gra w Pokera §4§lBETA");
        playerCount = mainHolo.appendTextLine("§c0§4/§c2");

        sit1 = HologramsAPI.createHologram(libs, MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"sit1"));
        TextLine line1 = sit1.appendTextLine("Kliknij, aby usiasc");
        line1.setTouchHandler(new TouchHandler() {

            @Override
            public void onTouch(Player player) {

                sitPlayer1(player);

            }

        });


        sit2 = HologramsAPI.createHologram(libs, MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"sit2"));
        TextLine line2 = sit2.appendTextLine("Kliknij, aby usiasc");

        line2.setTouchHandler(new TouchHandler() {

            @Override
            public void onTouch(Player player) {

                sitPlayer2(player);

            }

        });

        stats1 = HologramsAPI.createHologram(libs, MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"stats1").clone().add(0, 1, 0));
        stats1.appendTextLine("Bank: 0");
        stats1.appendTextLine("Obecny Zaklad: 0");
        stats1.getVisibilityManager().setVisibleByDefault(false);
        Bukkit.getOnlinePlayers().forEach(pl -> stats1.getVisibilityManager().hideTo(pl));

        stats2 = HologramsAPI.createHologram(libs, MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"stats2").clone().add(0, 1, 0));
        stats2.appendTextLine("Bank: 0");
        stats2.appendTextLine("Obecny Zaklad: 0");
        stats2.getVisibilityManager().setVisibleByDefault(false);
        Bukkit.getOnlinePlayers().forEach(pl -> stats2.getVisibilityManager().hideTo(pl));

    }

    public boolean checkInventory(Player player) {

        for(int i = 0; i < 9; i++) {

            if(!(player.getInventory().getItem(i) == null || player.getInventory().getItem(i).getType() == Material.AIR)) {

                return false;

            }

        }

        return true;

    }

    public void sitPlayer1(Player player) {

        if(player == player1 || player == player2) return;

        if(!checkInventory(player)) {

            player.sendMessage("§cAby podjac sie gry w pokera, nalezy miec pusty glowny pasek w inventory.");
            return;

        }

        if(player1 == null) {

            player1 = player;
            sit(player, sit1.getLocation());
            player1.sendMessage("§7Zajales miejsce w stoliku do pokera.");

            update();

        }

    }

    public void sitPlayer2(Player player) {

        if(player == player1 || player == player2) return;

        if(!checkInventory(player)) {

            player.sendMessage("§cAby podjac sie gry w pokera, nalezy miec pusty glowny pasek w inventory.");
            return;

        }

        if(player2 == null) {

            player2 = player;
            sit(player, sit2.getLocation());
            player2.sendMessage("§7Zajales miejsce w stoliku do pokera.");

            update();

        }

    }

    public void update() {

        if(poker != null) {

            poker = null;

            if(player1 == null) {

                player2.teleport(MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"leave2"));
                quit(player2);

            } else {

                player1.teleport(MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"leave1"));
                quit(player1);

            }

            stats1.getVisibilityManager().setVisibleByDefault(false);
            Bukkit.getOnlinePlayers().forEach(player -> stats1.getVisibilityManager().hideTo(player));

            stats2.getVisibilityManager().setVisibleByDefault(false);
            Bukkit.getOnlinePlayers().forEach(player -> stats2.getVisibilityManager().hideTo(player));

        }

        int count = 0;

        if(player1 == null) {

            sit1.getVisibilityManager().setVisibleByDefault(true);
            Bukkit.getOnlinePlayers().forEach(player -> sit1.getVisibilityManager().showTo(player));

        } else {

            count++;
            sit1.getVisibilityManager().setVisibleByDefault(false);
            Bukkit.getOnlinePlayers().forEach(player -> sit1.getVisibilityManager().hideTo(player));

        }

        if(player2 == null) {

            sit2.getVisibilityManager().setVisibleByDefault(true);
            Bukkit.getOnlinePlayers().forEach(player -> sit2.getVisibilityManager().showTo(player));

        } else {

            count++;
            sit2.getVisibilityManager().setVisibleByDefault(false);
            Bukkit.getOnlinePlayers().forEach(player -> sit2.getVisibilityManager().hideTo(player));

        }

        playerCount.setText("§c"+count+"§4/§c2");

        if(count == 2) {

            if(MantraCore.getInstance().getUserManager().getUser(player1).getMoney() < 10000 || MantraCore.getInstance().getUserManager().getUser(player2).getMoney() < 10000) {

                player1.sendMessage("§cJeden z was nie ma wymaganych §410000§c aby rozpoczac gre! §4§l(BETA)");
                player2.sendMessage("§cJeden z was nie ma wymaganych §410000§c aby rozpoczac gre! §4§l(BETA)");

                player2.teleport(MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"leave2"));
                quit(player2);

                player1.teleport(MantraCore.getInstance().getLocationManager().getLocation("poker"+id+"leave1"));
                quit(player1);

                return;

            }

            playerCount.setText("§cGra w toku!");
            stats1.getVisibilityManager().setVisibleByDefault(true);
            Bukkit.getOnlinePlayers().forEach(player -> stats1.getVisibilityManager().showTo(player));

            stats2.getVisibilityManager().setVisibleByDefault(true);
            Bukkit.getOnlinePlayers().forEach(player -> stats2.getVisibilityManager().showTo(player));

            poker = new PokerGame(player1, 10000, player2, 10000);
        }


    }

    @EventHandler
    public void onExit(EntityDismountEvent e) {

        if(e.getEntity() instanceof Player) quit((Player) e.getEntity());

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {

        quit(e.getPlayer());

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if(e.getWhoClicked() == player1 || e.getWhoClicked() == player2) {

            if(e.getClickedInventory() instanceof PlayerInventory) {

                if((e.getSlot() >= 0 && e.getSlot() <= 8) || e.getClick().isShiftClick()) {

                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);
                    e.getWhoClicked().sendMessage("§cAby podjac sie gry w pokera, nalezy miec pusty glowny pasek w inventory.");

                }

            }

        }

    }

    public void quit(Player player) {

        if(player == player1 || player == player2) {

            if(poker != null) {

                if(player1 == player) poker.surrenderPlayer1();
                if(player2 == player) poker.surrenderPlayer2();

            }

            if(player1 == player) player1 = null;
            if(player2 == player) player2 = null;

            update();

        }
        
    }

    public void showBets() {

        if(player1 != null && player2 != null && !betting) {

            betting = true;
            Inventory bettingInv = Bukkit.createInventory(null, 9, "siema");
            player1.openInventory(bettingInv);
            player2.openInventory(bettingInv);

        }

    }



    private ArmorStand sit(Player player, Location loc) {

        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().subtract(0, 2.3, 0), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setPassenger(player);

        new BukkitRunnable() {

            public void run() {

                if(stand.getPassenger() == null) {
                    stand.remove();
                    this.cancel();
                }

            }

        }.runTaskTimer(MantraLibs.getInstance(), 20, 20);

        return stand;

    }

}
