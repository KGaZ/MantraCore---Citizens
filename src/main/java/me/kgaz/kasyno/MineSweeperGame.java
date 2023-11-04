package me.kgaz.kasyno;

import me.kgaz.MantraLibs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

public class MineSweeperGame implements Listener {

    public static int POSSIBLE_WINNING = 5_000_000;

    private int mines;
    private int klikniete;
    private int winning;
    private int nextWin;
    private int buyIn;
    private Player player;
    private int[] grid;

    private boolean ended = false;

    private MantraLibs libs;

    public MineSweeperGame(int mines, int buyIn, Player player, MantraLibs libs) {

        this.mines = mines;
        this.buyIn = buyIn;
        this.player = player;

        klikniete = 0;
        winning = 0;
        nextWin = calculateNextWin();

        this.libs = libs;

        this.grid = new int[54];

        for (int i = 0; i < 54; i++) grid[i] = 0;

        libs.registerListener(this);

        startGui();

    }

    private int calculateNextWin() {

        return (int) (((float) (buyIn + winning)) * ((((float) mines) / (((float) ((28f - mines - klikniete)*4.5f)))) + (((float) klikniete) / 800f)));

    }


    public void startGui() {

        Inventory inv = Bukkit.createInventory(null, 54, "§8Saper | §b" + buyIn + "§3$ §8| (+ §a0§2$§8)");

        {

            ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§4");
            is.setItemMeta(im);
            for (int i = 0; i < 54; i++) inv.setItem(i, is);

        }

        {

            ItemStack is = new ItemStack(Material.QUARTZ_BLOCK);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§eKliknij, aby odslonic pole.");
            is.setItemMeta(im);

            for (int i = 0; i < 4; i++) for (int j = (i * 9) + 10; j < (i * 9) + 17; j++) inv.setItem(j, is);

        }

        {

            ItemStack is = new ItemStack(Material.EMERALD_BLOCK);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§7Wyplac wygrana §8(§a0§2$§8)");
            is.setItemMeta(im);
            inv.setItem(4, is);

        }

        player.openInventory(inv);

    }

    private Material WIN_MAT[] = {Material.EMERALD};
    private Material LOSE_MAT[] = {Material.TNT};

    private Material getRandomWinMaterial() {

        return WIN_MAT[new Random().nextInt(WIN_MAT.length)];

    }

    private Material getRandomLoseMaterial() {

        return LOSE_MAT[new Random().nextInt(LOSE_MAT.length)];

    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (e.getInventory() != null) {

            if (e.getInventory().getTitle() != null) {

                if (e.getInventory().getTitle().startsWith("§8Saper")) {

                    if (e.getWhoClicked() == player) {

                        e.setCancelled(true);
                        e.setResult(Event.Result.DENY);

                        if(ended) return;

                        if (e.getSlot() == 4) {


                            return;

                        }

                        if (e.getSlot() >= 10 && e.getSlot() <= 43 && e.getSlot() % 9 != 0 && e.getSlot() % 9 != 8) {

                            if (grid[e.getSlot()] == 0) {

                                grid[e.getSlot()] = 1;

                                float chance = ((float) mines) / (28f - (float) klikniete);

                                if (nextWin > MineSweeperGame.POSSIBLE_WINNING) chance = 1f;
                                else if (nextWin > MineSweeperGame.POSSIBLE_WINNING * 0.75f) chance *= 1.33f;
                                else if (nextWin < MineSweeperGame.POSSIBLE_WINNING * 0.075f) chance *= 0.66f;

                                Random random = new Random();

                                if (chance > random.nextFloat()) {

                                    ended = true;

                                    ItemStack is = new ItemStack(getRandomLoseMaterial());
                                    ItemMeta im = is.getItemMeta();
                                    im.setDisplayName("§c- §4" + (winning+buyIn));
                                    is.setItemMeta(im);
                                    e.getClickedInventory().setItem(e.getSlot(), is);

                                    Inventory inv = Bukkit.createInventory(null, 54, "§8Saper | (- §c" + (winning+buyIn) + "§4$§8)");
                                    inv.setContents(e.getClickedInventory().getContents());

                                    winning = 0;
                                    int tempMines = mines-1;
                                    int pola = 0;
                                    float lol = 0.1f;

                                    while(tempMines > 0) {

                                        for(int i = 0; i < 54; i++) {

                                            if(inv.getItem(i).getType() == Material.QUARTZ_BLOCK) {

                                                if(lol > random.nextFloat()) {

                                                    ItemStack tempIs = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
                                                    ItemMeta tempIm = tempIs.getItemMeta();
                                                    tempIm.setDisplayName("§4Bomba");
                                                    tempIs.setItemMeta(tempIm);
                                                    inv.setItem(i, tempIs);
                                                    tempMines--;

                                                    if(tempMines == 0) break;

                                                } else lol+=0.1f;

                                            }

                                        }

                                    }

                                    for(int i = 0; i < 54; i++) {

                                        if(inv.getItem(i).getType() == Material.QUARTZ_BLOCK) {

                                            ItemStack tempIs = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
                                            ItemMeta tempIm = tempIs.getItemMeta();
                                            tempIm.setDisplayName("§2Bezpiecznie");
                                            tempIs.setItemMeta(tempIm);
                                            inv.setItem(i, tempIs);

                                        }

                                    }

                                    e.getWhoClicked().openInventory(inv);

                                    ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.EXPLODE, 2f, 2*new Random().nextFloat());

                                } else {

                                    winning += nextWin;
                                    klikniete++;

                                    ItemStack is = new ItemStack(getRandomWinMaterial());
                                    ItemMeta im = is.getItemMeta();
                                    im.setDisplayName("§a+ §2" + nextWin);
                                    is.setItemMeta(im);
                                    e.getClickedInventory().setItem(e.getSlot(), is);

                                    Inventory inv = Bukkit.createInventory(null, 54, "§8Saper | §b" + buyIn + "§3$ §8| (+ §a" + winning + "§2$§8)");
                                    inv.setContents(e.getClickedInventory().getContents());
                                    e.getWhoClicked().openInventory(inv);

                                    ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), Sound.ORB_PICKUP, 2f, 2*new Random().nextFloat());

                                    nextWin = calculateNextWin();

                                }

                            }

                        }

                    }

                }

            }

        }

    }

}
