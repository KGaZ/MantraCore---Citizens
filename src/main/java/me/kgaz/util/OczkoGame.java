package me.kgaz.util;

import me.kgaz.MantraLibs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import pl.nomand.mantracore.common.MantraCore;
import pl.nomand.mantracore.items.PrototypeItem;

import java.util.Arrays;
import java.util.Random;

public class OczkoGame implements Listener {

    private int moneyBet;
    private int wplnBet;
    private Player[] player = new Player[2];
    private Boolean turn;
    private Oczko owner;
    private BukkitRunnable runnable;

    private boolean[] cardsLeft = new boolean[12];
    private boolean[] pas = new boolean[2];

    private int[][] cards = {{0,0,0,0,0}, {0,0,0,0,0}};

    private int timeLeft;

    public OczkoGame() {}

    public OczkoGame(int moneyBet, int wplnBet, Player p1, Player p2, Oczko owner) {

        super();
        player[0] = p1;
        player[1] = p2;
        this.moneyBet = moneyBet;
        this.wplnBet = wplnBet;
        this.turn = null;
        this.timeLeft = 5;
        this.owner = owner;
        pas[0] = false;
        pas[1] = false;

        MantraCore.getInstance().getUserManager().getUser(p1).removeMoney(moneyBet);
        MantraCore.getInstance().getUserManager().getUser(p2).removeMoney(moneyBet);

        MantraLibs.getInstance().registerListener(this);

        runnable = new BukkitRunnable() {

            public void run() {

                tick();

            }

        };

        runnable.runTaskTimer(MantraLibs.getInstance(), 20, 20);

        start();

    }

    private int rollCard() {

        boolean left = false;

        for(int i = 0; i < 12; i++) {

            if(cardsLeft[i]) left = true;

        }

        if(!left) return 0;

        int roll = new Random().nextInt(11)+1;

        while(!cardsLeft[roll]) roll = new Random().nextInt(11)+1;

        cardsLeft[roll] = false;

        return roll;

    }

    private void start() {

        for(int i = 0; i < 12; i++) {

            cardsLeft[i] = true;

        }

        for(int h = 0; h < 2; h++){

            Inventory inv = Bukkit.createInventory(null, 54, "Gra w Oczko");

            {
                ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName("§4");
                is.setItemMeta(im);

                for(int i = 0; i < inv.getSize(); i++) inv.setItem(i, PrototypeItem.getDummy(is));
            }

            inv.setItem(18, PrototypeItem.getDummy(generateMoneyBet()));

            player[h].openInventory(inv);

        }

        this.updateCards();
        this.updateTurn();



    }

    private void updateCards() {

        for(int h = 0; h < 2; h++) {

            int playerId  = h;
            int enemyId = 1 - h;

            Inventory inv = player[playerId].getOpenInventory().getTopInventory();

            inv.setItem(1, PrototypeItem.getDummy(this.generateSumEnemyCard(enemyId)));

            int slot =  3;

            for(int i = 0; i < 5; i++) {

                if(i == 0) {
                    if(cards[enemyId][0] != 0) inv.setItem(slot, PrototypeItem.getDummy(this.generateEnemyBlankCard()));
                    else inv.setItem(slot, PrototypeItem.getDummy(this.generateBlank()));
                }
                else inv.setItem(slot, PrototypeItem.getDummy(this.generateEnemyCard(cards[enemyId][i])));

                slot++;
            }

            inv.setItem(46, PrototypeItem.getDummy(this.generateSumCard(playerId)));

            slot = 48;

            for(int i = 0; i < 5; i++) {

                inv.setItem(slot, PrototypeItem.getDummy(this.generateCard(cards[playerId][i])));

                slot++;
            }

        }

    }

    private int getTurnPlayerID(boolean bool) {
        return bool ? 1 : 0;
    }

    private boolean hasSpaceForCard(int playerId) {

        boolean space = false;

        for(int i = 0; i < 5; i++) if(cards[playerId][i] == 0) {
            space = true;
            break;
        }

        return space;

    }

    private boolean end = false;

    private void tie() {

        MantraCore.getInstance().getUserManager().getUser(player[0]).addMoney(moneyBet);
        MantraCore.getInstance().getUserManager().getUser(player[1]).addMoney(moneyBet);

        for(int i = 0; i < 2; i++) {
            player[i].sendMessage("§7Remis§8!");
        }

        HandlerList.unregisterAll(this);

        runnable.cancel();

        owner.stopGame();
    }

    private void broadcast(String message) {
        for(int i = 0; i < 2; i++) player[i].sendMessage(message);
    }

    private void endGame() {

        end = true;

        int p1Score = 0;
        for(int i = 0; i < 5; i++) p1Score+=this.cards[0][i];

        int p2Score = 0;
        for(int i = 0; i < 5; i++) p2Score+=this.cards[1][i];

        this.broadcast("§8Analiza Gry");

        String nr1 = "";

        if(p1Score > 21) nr1 = "§4"+p1Score;
        if(p1Score == 21) nr1 = "§2"+p1Score;
        if(p1Score < 21) nr1 = "§6"+p1Score;

        String nr2 = "";

        if(p2Score > 21) nr2 = "§4"+p2Score;
        if(p2Score == 21) nr2 = "§2"+p2Score;
        if(p2Score < 21) nr2 = "§6"+p2Score;

        this.broadcast(nr1+"§8 - "+nr2);

        if(p1Score == p2Score) {

            this.broadcast("§7Oboje gracze maja tyle samo punktow.");

            tie();
            return;

        }

        if(p1Score > 21 && p2Score > 21) {

            if(p1Score < p2Score) {

                this.broadcast("§7Kazdy ma powyzej 21!");
                this.tie();
                return;

            } else {

                this.broadcast("§7Kazdy ma powyzej 21!");
                this.tie();
                return;

            }

        }
        if(p1Score > 21) {
            this.broadcast("§2"+player[0].getName()+"§7 wyszedl poza 21!");
            this.stopGame(player[1]);
            return;
        }
        if(p2Score > 21) {

            this.broadcast("§2"+player[1].getName()+"§7 wyszedl poza 21!");
            this.stopGame(player[0]);
            return;
        }

        if(p1Score > p2Score) {
            this.broadcast("§7Wiecej punktow ma §2"+player[0].getName()+"§8!");
            this.stopGame(player[0]);
            return;

        } else {

            this.broadcast("§7Wiecej punktow ma §2"+player[1].getName()+"§8!");
            this.stopGame(player[1]);
            return;

        }

    }

    private void switchTurn() {

        if(pas[1] && pas[0]) {
            endGame();
            return;
        }

        if(turn == null)  {

            for(int i = 0; i < 2; i++) for(int h = 0; h < 2; h++) cards[i][h] = rollCard();

            updateCards();

            turn = new Random().nextInt(2) == 0;


        } else {

            turn = !turn;

            if(pas[turn ? 1 : 0]) turn = !turn;

            if(pas[turn ? 1 : 0]) endGame();

        }

        timeLeft = 20;

    }

    private void updateTurn() {

        for(int i = 0; i < 2; i++) {

            int enemyId = 1 - i;

            Inventory inv = player[i].getOpenInventory().getTopInventory();

            if(pas[i]) {

                if(inv.getSize() < 10) continue;

                ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName("§cSpasowales. Nie mozesz dalej grac.");
                is.setItemMeta(im);



                inv.setItem(22, PrototypeItem.getDummy(is.clone()));
                inv.setItem(30, PrototypeItem.getDummy(is.clone()));
                inv.setItem(32, PrototypeItem.getDummy(is.clone()));

                continue;
            }

            if(pas[enemyId]) {

                ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 11);
                ItemMeta im = is.getItemMeta();
                im.setDisplayName("§9Twoj przeciwnik spasowal!");
                im.setLore(Arrays.asList("§7Twoja kolej sie zakonczy na pasie.","§7Mozesz dobrac tyle kart ile chcesz."));
                is.setItemMeta(im);

                inv.setItem(31, PrototypeItem.getDummy(is));

            }

            inv.setItem(22, PrototypeItem.getDummy(generateTurntIcon(i)));

            if(turn == null) {

                inv.setItem(30, PrototypeItem.getDummy(this.generateBlank()));
                inv.setItem(32, PrototypeItem.getDummy(this.generateBlank()));

            } else {

                if(turn) {

                    if(i == 1) {

                        int score = 0;
                        for(int b = 0; b < 5; b++) score+= cards[i][b];

                        if(this.hasSpaceForCard(i) && score < 21){

                            ItemStack is = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
                            ItemMeta im = is.getItemMeta();
                            im.setDisplayName("§2Dobierz Karte");
                            im.setLore(Arrays.asList("§7Po kliknieciu twoija tura sie zakonczy."));
                            is.setItemMeta(im);

                            inv.setItem(30, PrototypeItem.getDummy(is));

                        } else {

                            ItemStack is = new ItemStack(Material.BARRIER, 1, (short) 5);
                            ItemMeta im = is.getItemMeta();
                            im.setDisplayName("§cNie mozesz dobrac wiecej kart.");
                            if(!this.hasSpaceForCard(i)) im.setLore(Arrays.asList("§7Masz juz piec kart."));
                            if(score >= 21) im.setLore(Arrays.asList("§7Wartosc twoich kart wychodzi ponad 21."));
                            is.setItemMeta(im);

                            inv.setItem(30, PrototypeItem.getDummy(is));
                        }

                        {
                            ItemStack is = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
                            ItemMeta im = is.getItemMeta();
                            im.setDisplayName("§4Pas");
                            im.setLore(Arrays.asList("§7Po kliknieciu twoija tura sie zakonczy."));
                            is.setItemMeta(im);

                            inv.setItem(32, PrototypeItem.getDummy(is));
                        }

                    } else {

                        inv.setItem(30, PrototypeItem.getDummy(this.generateBlank()));
                        inv.setItem(32, PrototypeItem.getDummy(this.generateBlank()));

                    }

                }
                if(!turn) {

                    if(i == 0) {

                        if(this.hasSpaceForCard(i)){

                            ItemStack is = new ItemStack(Material.STAINED_CLAY, 1, (short) 5);
                            ItemMeta im = is.getItemMeta();
                            im.setDisplayName("§2Dobierz Karte");
                            im.setLore(Arrays.asList("§7Po kliknieciu twoija tura sie zakonczy."));
                            is.setItemMeta(im);

                            inv.setItem(30, PrototypeItem.getDummy(is));

                        } else {

                            ItemStack is = new ItemStack(Material.BARRIER, 1, (short) 5);
                            ItemMeta im = is.getItemMeta();
                            im.setDisplayName("§cNie mozesz dobrac wiecej kart.");
                            im.setLore(Arrays.asList("§7Masz juz piec kart."));
                            is.setItemMeta(im);

                            inv.setItem(30, PrototypeItem.getDummy(is));
                        }

                        {
                            ItemStack is = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
                            ItemMeta im = is.getItemMeta();
                            im.setDisplayName("§4Pas");
                            im.setLore(Arrays.asList("§7Po kliknieciu twoija tura sie zakonczy."));
                            is.setItemMeta(im);

                            inv.setItem(32, PrototypeItem.getDummy(is));
                        }

                    } else {

                        inv.setItem(30, PrototypeItem.getDummy(this.generateBlank()));
                        inv.setItem(32, PrototypeItem.getDummy(this.generateBlank()));

                    }

                }

            }

        }

    }

    private void tick() {

        if(end) return;

        if(this.turn == null) {

            for(int i = 0; i < 2; i++) player[i].playSound(player[i].getLocation(), Sound.ORB_PICKUP, 1, 1);

        } else if(timeLeft < 6) for(int i = 0; i < 2; i++) player[i].playSound(player[i].getLocation(), Sound.ORB_PICKUP, 1, 1);

        timeLeft--;

        if(timeLeft == 0) {

            if(turn != null) {

                pas[this.getTurnPlayerID(turn)] = true;
                for(int i = 0; i < 2; i++) {
                    player[i].playSound(player[i].getLocation(), Sound.VILLAGER_DEATH, 1, 0.5f);
                    player[i].sendMessage("§7Gracz §2"+player[this.getTurnPlayerID(turn)].getName()+"§7 spasowal z powodu koncu czasu!");
                }

            }

            switchTurn();

        }

        updateTurn();

    }

    private ItemStack generateTurntIcon(int playerId) {

        if(turn == null) {

            ItemStack is = new ItemStack(Material.ENDER_PEARL, timeLeft);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§7Gra rozpocznie sie za §2"+timeLeft);
            im.setLore(Arrays.asList("§7Za chwile rozpocznie sie gra!"));
            is.setItemMeta(im);
            return is;
        }

        if(turn) {

            ItemStack is = new ItemStack(playerId == 1 ? Material.EYE_OF_ENDER : Material.ENDER_PEARL, timeLeft);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§7Teraz kolej §2"+player[1].getName());
            if(!pas[0]) im.setLore(Arrays.asList("§7Pozostaly czas: §2"+timeLeft+"§as"));
            else im.setLore(Arrays.asList("§7Pozostaly czas: §2"+timeLeft+"§as", "§7Drugi gracz spasowal."));

            is.setItemMeta(im);
            return is;

        }

        if(!turn) {

            ItemStack is = new ItemStack(playerId == 0 ? Material.EYE_OF_ENDER : Material.ENDER_PEARL, timeLeft);
            ItemMeta im = is.getItemMeta();
            im.setDisplayName("§7Teraz kolej §2"+player[0].getName());
            if(!pas[1]) im.setLore(Arrays.asList("§7Pozostaly czas: §2"+timeLeft+"§as"));
            else im.setLore(Arrays.asList("§7Pozostaly czas: §2"+timeLeft+"§as", "§7Drugi gracz spasowal."));

            is.setItemMeta(im);
            return is;

        }

        return null;

    }

    private ItemStack generateBlank() {

        ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§4");
        is.setItemMeta(im);
        return is;

    }

    private ItemStack generateMoneyBet() {

        ItemStack is = new ItemStack(Material.DOUBLE_PLANT);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§7Zaklad Pieniezny§8: §6"+this.moneyBet+"§e Monet");
        is.setItemMeta(im);
        return is;

    }

    private ItemStack generateSumEnemyCard(int player) {

        int sum = 0;

        for(int i : cards[player]) sum+=i;

        sum-=cards[player][0];

        if(sum == 0) return generateBlank();

        ItemStack is = new ItemStack(Material.EMPTY_MAP, sum);

        ItemMeta im = is.getItemMeta();

        if(sum > 21) {

            im.setDisplayName("§f??? §7+ §c"+sum+"§4/§c21");

        } else if (sum == 21) {

            im.setDisplayName("§f??? §7+ §a"+sum+"§2/§a21");

        } else if (sum < 21) {

            im.setDisplayName("§f??? §7+ §e"+sum+"§6/§e21");

        }

        String lore = "§e???";

        boolean show = false;

        for(int i : cards[player]) {

            if(!show) {
                show = true;
                continue;
            }

            if(i != 0) lore+="§6 + §e"+i;
        }
        im.setLore(Arrays.asList(lore));

        is.setItemMeta(im);

        return is;

    }

    private ItemStack generateSumCard(int player) {

        int sum = 0;

        for(int i : cards[player]) sum+=i;

        if(sum == 0) return generateBlank();

        ItemStack is = new ItemStack(Material.PAPER, sum);

        ItemMeta im = is.getItemMeta();

        if(sum > 21) {

            im.setDisplayName("§c"+sum+"§4/§c21");

        } else if (sum == 21) {

            im.setDisplayName("§a"+sum+"§2/§a21");

        } else if (sum < 21) {

            im.setDisplayName("§e"+sum+"§6/§e21");

        }

        String lore = "";
        for(int i : cards[player]) {
            if(i!=0) lore+="§6 + §e"+i;
        }

        im.setLore(Arrays.asList(StringUtils.replaceOnce(lore, "§6 + ", "")));

        is.setItemMeta(im);

        return is;

    }

    private Player getOppositePlayer(Player pl) {

        if(pl == player[1]) return player[0];
        else return player[1];

    }

    private ItemStack generateEnemyBlankCard() {

        ItemStack is = new ItemStack(Material.EMPTY_MAP, 0);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§6???");
        is.setItemMeta(im);

        return is;

    }

    private ItemStack generateEnemyCard(int cardValue) {

        if(cardValue == 0) return generateBlank();

        ItemStack is = new ItemStack(Material.EMPTY_MAP, cardValue);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§6"+cardValue);
        is.setItemMeta(im);

        return is;

    }

    private ItemStack generateCard(int cardValue) {

        if(cardValue == 0) return generateBlank();

        ItemStack is = new ItemStack(Material.PAPER, cardValue);
        ItemMeta im = is.getItemMeta();
        im.setDisplayName("§6"+cardValue);
        is.setItemMeta(im);

        return is;

    }

    private void stopGame(Player winner) {

        MantraCore.getInstance().getUserManager().getUser(winner).addMoney(moneyBet * 2L);

        for(int i = 0; i < 2; i++) {
            player[i].sendMessage("§7Te partie wygrywa gracz §2"+winner.getName()+"§8!");
        }

        HandlerList.unregisterAll(this);

        runnable.cancel();

        owner.stopGame();

    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if(e.getClickedInventory() != null)

            if((e.getWhoClicked() == player[1] || e.getWhoClicked() == player[0]) && e.getClickedInventory().getTitle().equals("Gra w Oczko")) {

                e.setCancelled(true);
                e.setResult(Result.DENY);

                ItemStack is = e.getCurrentItem();

                if(is.getType() == Material.STAINED_CLAY) {

                    int id = 0;

                    if(e.getWhoClicked() == player[1]) id = 1;

                    if(e.getSlot() == 30) {

                        for(int i = 0; i < 5; i++) {

                            if(this.cards[id][i] == 0) {

                                cards[id][i] = this.rollCard();

                                break;
                            }

                        }

                        e.getClickedInventory().setItem(30, PrototypeItem.getDummy(this.generateBlank()));

                        switchTurn();
                        this.updateCards();

                    } else if (e.getSlot() == 32) {

                        pas[id] = true;
                        for(int i = 0; i < 2; i++) {
                            player[i].playSound(player[i].getLocation(), Sound.VILLAGER_DEATH, 1, 0.5f);
                            player[i].sendMessage("§7Gracz §2"+player[this.getTurnPlayerID(turn)].getName()+"§7 spasowal!");
                        }

                        e.getClickedInventory().setItem(32, PrototypeItem.getDummy(this.generateBlank()));

                        switchTurn();
                        this.updateTurn();

                    }

                }

            }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {

        if(e.getPlayer() == player[1] || e.getPlayer() == player[0]) {

            for(int i = 0; i < 2; i++) {
                player[i].sendMessage("§7Gracz "+e.getPlayer().getName()+"§7 wyszedl podczas gry§8!");
            }

            this.stopGame(getOppositePlayer((Player) e.getPlayer()));

        }

    }

}
