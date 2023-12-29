package me.kgaz.util;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import de.tr7zw.nbtapi.NBTItem;
import me.kgaz.MantraLibs;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;
import pl.nomand.mantracore.common.MantraCore;
import pl.nomand.mantracore.items.PrototypeItem;

import java.util.Arrays;

public class Oczko implements Loadable, Listener {

    private Hologram main;
    private TextLine mainTextLine;
    private Hologram sit1;
    private Hologram sit2;

    private Player player1;
    private Player player2;

    private boolean inPlay;

    private boolean accept1;
    private boolean accept2;

    private String id;


    public Oczko(String id) {

        this.id = id;

        sit1 = null;
        sit2 = null;

        player1 = null;
        player2 = null;

        inPlay = false;

        accept1 = false;
        accept2 = false;

    }

    @Override
    public void onLoad(MantraLibs libs) {

        libs.registerListener(this);
        
        main = HologramsAPI.createHologram(libs, MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-main"));

        main.appendTextLine("Gra w Oczko");

        mainTextLine = main.appendTextLine("§a0§2/§a2");

        sit1 = HologramsAPI.createHologram(libs, MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-sit1"));

        {

            TextLine line = sit1.appendTextLine("Kliknij aby usiasc.");

            line.setTouchHandler(new TouchHandler() {

                @Override
                public void onTouch(Player player) {

                    if(player.getPassenger() != null || player.isInsideVehicle()) return;

                    player1 = player;

                    sit(player, MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-sit1"));

                    if(player2 != null) {

                        beginGame();

                    } else {

                        mainTextLine.setText("§a1§2/§a2");

                    }

                    sit1.getVisibilityManager().setVisibleByDefault(false);

                    Bukkit.getOnlinePlayers().forEach(pl -> sit1.getVisibilityManager().hideTo(pl));

                }


            });

        }

        sit2 = HologramsAPI.createHologram(libs, MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-sit2"));

        {
            TextLine line = sit2.appendTextLine("Kliknij aby usiasc.");

            line.setTouchHandler(new TouchHandler() {

                @Override
                public void onTouch(Player player) {

                    if(player.getPassenger() != null || player.isInsideVehicle()) return;

                    player2 = player;

                    sit(player, MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-sit2"));

                    if(player1 != null) {

                        beginGame();

                    } else {

                        mainTextLine.setText("§a1§2/§a2");

                    }

                    sit2.getVisibilityManager().setVisibleByDefault(false);

                    Bukkit.getOnlinePlayers().forEach(pl -> sit2.getVisibilityManager().hideTo(pl));

                }


            });
        }

    }

    @EventHandler
    public void vehicleExit(EntityDismountEvent e) {

        if(player1 == e.getEntity()) {

            player1 = null;

            if(inPlay) {

                surrenderPlayer1();

            } else updateHolos();

        }

        if(player2 == e.getEntity()) {

            player2 = null;

            if(inPlay) {

                surrenderPlayer2();

            } else updateHolos();

        }

    }

    private void updateHolos() {

        int i = 2;

        if(player1 == null) {

            sit1.getVisibilityManager().setVisibleByDefault(true);
            Bukkit.getOnlinePlayers().forEach(pl -> sit1.getVisibilityManager().showTo(pl));
            i--;

        }
        if(player2 == null) {

            sit2.getVisibilityManager().setVisibleByDefault(true);
            Bukkit.getOnlinePlayers().forEach(pl -> sit2.getVisibilityManager().showTo(pl));
            i--;

        }
        mainTextLine.setText("§a"+i+"§2/§a2");


    }

    public void stopGame() {

        gra = null;

        inPlay = false;
        if(player1.getVehicle() != null)
            player1.getVehicle().remove();
        player1.teleport(MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-leave1"));
        if(player2.getVehicle() != null)
            player2.getVehicle().remove();
        player2.teleport(MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-leave2"));

        try {
            player1.closeInventory();

            player2.closeInventory();
        }catch(Exception exc) {}

        updateHolos();

        player1 = null;
        player2 = null;

        inPlay = false;

        accept1 = false;
        accept2 = false;

    }

    public void surrenderPlayer1() {
        if(!inPlay) return;

        accept1 = false;

        inPlay = false;
        if(player1.getVehicle() != null) player1.getVehicle().remove();
        player1.teleport(MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-leave1"));
        player1 = null;

        player2.closeInventory();

        updateHolos();
    }

    public void surrenderPlayer2() {

        if(!inPlay) return;
        inPlay = false;
        accept2 = false;
        if(player2.getVehicle() != null) player2.getVehicle().remove();
        player2.teleport(MantraCore.getInstance().getLocationManager().getLocation("oczko-table"+id+"-leave2"));
        player2 = null;

        player1.closeInventory();

        updateHolos();
    }

    public void beginGame() {
        inPlay = true;
        mainTextLine.setText("§a2§2/§a2");

        Player[] player = {player1, player2};

        Inventory inv = Bukkit.createInventory(null, 36, "Ustawienie Zakladu");

        {
            ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§4");
            is.setItemMeta(im);

            ItemStack is2 = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
            is2.setItemMeta(im);

            for(int i = 0; i < inv.getSize(); i++) {

                if(i % 2 != 0) inv.setItem(i, PrototypeItem.getDummy(is));
                else inv.setItem(i, PrototypeItem.getDummy(is2));

            }
        }

        {

            ItemStack is = new ItemStack(Material.DOUBLE_PLANT);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§7Zaklad Pieniezny§8: §e0 §6Monet");
            im.setLore(Arrays.asList("§7Administracja nie odpowiada za przegrane","§7pieniadze. Jezeli cie rozlaczy podczas gry §4§l§nprzegrasz!","§cGrasz na wlasna odpowiedzialnosc!"));
            is.setItemMeta(im);

            inv.setItem(9, PrototypeItem.getDummy(is));

        }

        int[] madd = {1,100,1000,10000,100000};

        int slot = 11;

        for(int i : madd) {

            ItemStack is = new ItemStack(Material.IRON_FENCE);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§6"+i);
            im.setLore(Arrays.asList(
                    "§8§ §2§lLPM §8§ §7Podnies zaklad pieniezny o §a"+i+"§8.",
                    "§8§ §4§lPPM §8§ §7Obniz zaklad pieniezny o §c"+i+"§8.",
                    "§7Kliknij z §6SHIFT§eem§7 aby wykonac akcje §ex§65§8."
            ));

            is.setItemMeta(im);

            NBTItem item = new NBTItem(is);

            item.setInteger("MONEY.BET.ADD", i);

            inv.setItem(slot, PrototypeItem.getDummy(item.getItem()));

            slot++;
        }

        {
            ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§7Gotowosc §b"+player1.getName()+"§8: §f§lOCZEKUJE...");
            im.setLore(Arrays.asList(
                    "§7Grasz na wlasne ryzyko!"
            ));
            is.setItemMeta(im);
            inv.setItem(21, PrototypeItem.getDummy(is));
        }
        {
            ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§7Gotowosc §b"+player2.getName()+"§8: §f§lOCZEKUJE...");
            im.setLore(Arrays.asList(
                    "§7Grasz na wlasne ryzyko!"
            ));
            is.setItemMeta(im);
            inv.setItem(23, PrototypeItem.getDummy(is));
        }

        for(Player p : player) p.openInventory(inv);
    }

    private OczkoGame gra = null;

    private void startGame() {

        int moneyBet, wplnBet;

        {

            String name = ChatColor.stripColor(player1.getOpenInventory().getTopInventory().getItem(9).getItemMeta().getDisplayName());

            name = name.replace("Zaklad Pieniezny: ", "").replace(" Monet", "");

            moneyBet = Integer.parseInt(name);

        }

        //to avoid close event
        gra = new OczkoGame();

        player1.closeInventory();
        player2.closeInventory();

        if(MantraCore.getInstance().getUserManager().getUser(player1).getMoney() >= moneyBet && MantraCore.getInstance().getUserManager().getUser(player2).getMoney()  >= moneyBet)

            gra = new OczkoGame(moneyBet, 0, player1, player2, this);

        else {

            player1.sendMessage("§7Jeden z was nie ma na tyle pieniedzy!");
            player2.sendMessage("§7Jeden z was nie ma na tyle pieniedzy!");

            this.stopGame();

        }

    }

    private void checkAccepts() {

        if(accept1 && accept2) {

            startGame();

        } else {

            if(accept1) {

                ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName("§7Gotowosc §b"+player1.getName()+"§8: §2§lZAAKCEPTOWANE");
                im.setLore(Arrays.asList(
                        "§7Grasz na wlasne ryzyko!"
                ));
                is.setItemMeta(im);
                player1.getOpenInventory().getTopInventory().setItem(21, PrototypeItem.getDummy(is));

            } else {

                ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName("§7Gotowosc §b"+player1.getName()+"§8: §f§lOCZEKUJE...");
                im.setLore(Arrays.asList(
                        "§7Grasz na wlasne ryzyko!"
                ));
                is.setItemMeta(im);
                player1.getOpenInventory().getTopInventory().setItem(21, PrototypeItem.getDummy(is));

            }

            if(accept2) {

                ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName("§7Gotowosc §b"+player2.getName()+"§8: §2§lZAAKCEPTOWANE");
                im.setLore(Arrays.asList(
                        "§7Grasz na wlasne ryzyko!"
                ));
                is.setItemMeta(im);
                player1.getOpenInventory().getTopInventory().setItem(23, PrototypeItem.getDummy(is));

            } else {

                ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName("§7Gotowosc §b"+player2.getName()+"§8: §f§lOCZEKUJE...");
                im.setLore(Arrays.asList(
                        "§7Grasz na wlasne ryzyko!"
                ));
                is.setItemMeta(im);
                player1.getOpenInventory().getTopInventory().setItem(23, PrototypeItem.getDummy(is));

            }

        }

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if(gra != null) return;

        if(e.getClickedInventory() == null) return;
        if(e.getClickedInventory().getTitle() == null) return;

        if(e.getWhoClicked() == player1 || e.getWhoClicked() == player2 && e.getClickedInventory().getTitle().equals("Ustawienie Zakladu")) {

            e.setCancelled(true);

            Player pl = (Player) e.getWhoClicked();

            if(e.getSlot() == 23 && e.getWhoClicked() == player2) {

                pl.playSound(pl.getLocation(), Sound.NOTE_BASS, 1, 1);

                accept2 = true;
                checkAccepts();

            }

            if(e.getSlot() == 21 && e.getWhoClicked() == player1) {

                pl.playSound(pl.getLocation(), Sound.NOTE_BASS, 1, 1);

                accept1 = true;
                checkAccepts();

            }

            if(e.getSlot() > 10 && e.getSlot() < 16) {

                if(e.getCurrentItem() == null) return;
                if(e.getCurrentItem().getType() == Material.AIR) return;

                NBTItem item = new NBTItem(e.getCurrentItem());

                if(player1 == null) {
                    surrenderPlayer1();
                    return;
                }
                if(player2 == null) {
                    surrenderPlayer2();
                    return;
                }

                player1.playSound(pl.getLocation(), Sound.NOTE_BASS, 1, 1);
                player2.playSound(pl.getLocation(), Sound.NOTE_BASS, 1, 1);

                int amount = item.getInteger("MONEY.BET.ADD");

                if(e.getClick() == ClickType.SHIFT_LEFT) {

                    amount*=5;

                }
                if(e.getClick() == ClickType.SHIFT_RIGHT) {

                    amount*=-5;

                }
                if(e.getClick() == ClickType.RIGHT) {

                    amount*=-1;

                }

                String name = ChatColor.stripColor(e.getClickedInventory().getItem(9).getItemMeta().getDisplayName());

                name = name.replace("Zaklad Pieniezny: ", "").replace(" Monet", "");

                int newAmount = amount + Integer.parseInt(name);

                if(MantraCore.getInstance().getUserManager().getUser(player1).getMoney() < newAmount && MantraCore.getInstance().getUserManager().getUser(player2).getMoney() < newAmount) {

                    ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.VILLAGER_DEATH, 1, 0.5f);
                    e.getWhoClicked().sendMessage("§7Jeden z was nie ma tyle monet!");
                    return;

                }

                if(newAmount < 0) newAmount = 0;

                ItemMeta im = e.getClickedInventory().getItem(9).getItemMeta();
                im.setDisplayName("§7Zaklad Pieniezny§8: §e"+newAmount+" §6Monet");
                e.getClickedInventory().getItem(9).setItemMeta(im);

                accept1 = false;
                accept2 = false;
                checkAccepts();

            }

        }

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {

        if(gra != null) return;

        if(e.getPlayer() == player1 || e.getPlayer() == player2) {

            if(e.getPlayer() == player1) {

                surrenderPlayer1();

            }

            if(e.getPlayer() == player2) {

                surrenderPlayer2();

            }

        }

    }

    private void sit(Player player, Location loc) {

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

    }

}
