package me.kgaz.debug;

import me.kgaz.MantraLibs;
import me.kgaz.betterDisguises.DisguiseData;
import me.kgaz.kasyno.*;
import me.kgaz.kasyno.poker.Card;
import me.kgaz.kasyno.poker.Hand;
import me.kgaz.npcs.CustomSecondLine;
import me.kgaz.npcs.DisguiseType;
import me.kgaz.npcs.NPC;
import me.kgaz.npcs.SkinFetcher;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityZombie;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;
import pl.nomand.mantracore.common.MantraCore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Debug implements CommandExecutor {

    public static Debug INSTANCE;

    private Map<String, ActionRun> Debugs;

    public static Animation TEST_ANIM = null;
    public static boolean startAnimation = false;

    public Debug(MantraLibs main) {

        INSTANCE = this;

        Debugs = new HashMap<>();

        BukkitRunnable runnable = new BukkitRunnable() {

            @Override
            public void run() {

                if(!startAnimation) return;

                TEST_ANIM.tick();

            }

        };

        registerAction("startStop", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) throws IOException {

                startAnimation = !startAnimation;

            }

        });


        registerAction("initAnim", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) throws IOException {


                TEST_ANIM = new Animation(arguments[0]);
                TEST_ANIM.tick();
                runnable.runTaskTimer(main, 2, 2);

            }

        });

        registerAction("testMapXD", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) throws IOException {

                executor.sendMessage(TEST_ANIM.getCurrentAnimation().getHeight()+"");
                executor.sendMessage(TEST_ANIM.getCurrentAnimation().getWidth()+"");

                for(int y = 0; y <= TEST_ANIM.getCurrentAnimation().getHeight(); y+=128) {

                    for(int x = 0; x <= TEST_ANIM.getCurrentAnimation().getWidth(); x+=128) {

                        MapView view = Bukkit.createMap(executor.getWorld());

                        new ArrayList<>(view.getRenderers()).forEach(view::removeRenderer);
                        view.addRenderer(new ShrekWatcher(TEST_ANIM, -x, -y));

                        ItemStack is = new ItemStack(Material.MAP, 1, view.getId());

                        ItemMeta im = is.getItemMeta();
                        im.setDisplayName((x/128)+" - "+(y/128));
                        is.setItemMeta(im);

                        executor.getInventory().addItem(is);

                    }

                }

            }

        });

        registerAction("compareCards", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                Card card1 = Card.valueOf(arguments[0].toUpperCase());
                Card card2 = Card.valueOf(arguments[1].toUpperCase());

                if(card1.getValue() > card2.getValue()) executor.sendMessage(card1.name());
                else if(card2.getValue() > card1.getValue()) executor.sendMessage(card2.name());
                else executor.sendMessage("Remis!");

            }

        });

        registerAction("checkCards", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                Hand gracz1 = new Hand();
                Hand gracz2 = new Hand();
                Hand wspolna = new Hand();

                Location loc1 = MantraCore.getInstance().getLocationManager().getLocation("gracz1hand");
                Location loc2 = MantraCore.getInstance().getLocationManager().getLocation("gracz2hand");
                Location locWspolna = MantraCore.getInstance().getLocationManager().getLocation("wspolnahand");

                {
                    Block block = loc1.getBlock();
                    Chest chest = (Chest) block.getState();

                    for(ItemStack is : chest.getBlockInventory().getContents()) {

                        if(is != null) if(is.getType() == Material.MAP) {

                            ItemMeta im = is.getItemMeta();
                            gracz1.addCard(Card.valueOf(im.getDisplayName().toUpperCase()));

                        }

                    }
                }
                {
                    Block block = loc2.getBlock();
                    Chest chest = (Chest) block.getState();

                    for(ItemStack is : chest.getBlockInventory().getContents()) {

                        if(is != null) if(is.getType() == Material.MAP) {

                            ItemMeta im = is.getItemMeta();
                            gracz2.addCard(Card.valueOf(im.getDisplayName().toUpperCase()));

                        }

                    }
                }
                {
                    Block block = locWspolna.getBlock();
                    Chest chest = (Chest) block.getState();

                    for(ItemStack is : chest.getBlockInventory().getContents()) {

                        if(is != null) if(is.getType() == Material.MAP) {

                            ItemMeta im = is.getItemMeta();
                            wspolna.addCard(Card.valueOf(im.getDisplayName().toUpperCase()));

                        }

                    }
                }

                String cards1 = "Gracz 1 posiada karty: ";
                String cards2 = "Gracz 1 posiada karty: ";

                for(Card card : gracz1.getCards()) cards1+=card.name()+" (§c"+card.getNumber().getValue()+"§f) ";

                for(Card card : gracz2.getCards()) cards2+=card.name()+" (§c"+card.getNumber().getValue()+"§f) ";

                for(Card card : wspolna.getCards()) {
                    cards1+=card.name()+" (§c"+card.getNumber().getValue()+"§f) ";
                    cards2+=card.name()+" (§c"+card.getNumber().getValue()+"§f) ";
                }

                gracz1.addWspolne(wspolna.getCards());
                gracz2.addWspolne(wspolna.getCards());

                executor.sendMessage(cards1);
                executor.sendMessage("Gracz 1 points: "+gracz1.evaluate());
                executor.sendMessage(cards2);
                executor.sendMessage("Gracz 2 points: "+gracz2.evaluate());


            }

        });

        registerAction("cardValueCheck", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                for(Card.Value val : Card.Value.values()) {

                    executor.sendMessage(val.name()+ " ("+val.getValue()+"): "+(Card.Value.fromNumber(val.getValue()) == val)+" ("+Card.Value.fromNumber(val.getValue())+")");

                }

            }

        });

        registerAction("getAllCards", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                for (Card value : Card.values()) {

                    executor.getWorld().dropItem(executor.getLocation(), main.getCardInitiator().getCardView(value));

                }

            }

        });

        registerAction("getCard", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                executor.getInventory().addItem(main.getCardInitiator().getCardView(Card.valueOf(arguments[0].toUpperCase())));

            }

        });

        registerAction("testMap", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) throws IOException {

                MapView view = Bukkit.createMap(executor.getWorld());

                new ArrayList<>(view.getRenderers()).forEach(view::removeRenderer);
                view.addRenderer(new BekaRenderer(arguments));

                ItemStack is = new ItemStack(Material.MAP, 1, view.getId());
                executor.getInventory().addItem(is);

            }

        });

        registerAction("testdisguises", new ActionRun() {
            @Override
            public void run(Player executor, String[] arguments) {

                String nick = arguments[0];

                StringBuilder name = new StringBuilder();

                for(int i = 1; i < arguments.length; i++) {

                    name.append(arguments[i]).append(i == arguments.length - 1 ? "" : " ");

                }

                EntityZombie zombie = new EntityZombie(((CraftWorld)executor.getLocation().getWorld()).getHandle());
                zombie.setLocation(executor.getLocation().getX(), executor.getLocation().getY(), executor.getLocation().getZ(), executor.getLocation().getYaw(), executor.getLocation().getPitch());

                zombie.setEquipment(0, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.DIAMOND_SWORD)));
                zombie.setEquipment(1, CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.DIAMOND_BOOTS)));
                zombie.setEquipment(3, CraftItemStack.asNMSCopy(new ItemStack(Material.CHAINMAIL_CHESTPLATE)));

                executor.sendMessage("Stage 1");

                SkinFetcher fetcher = new SkinFetcher(main, nick, new SkinFetcher.FetchResult() {

                    @Override
                    public void skinFetched(String texture, String signature) {

                        main.getNewDisguises().disguise(zombie.getBukkitEntity(), new DisguiseData(name.toString(), texture, signature, zombie));
                        zombie.getWorld().addEntity(zombie);
                        executor.sendMessage("Stage 2");
                    }

                });

            }
        });

        registerAction("testSweeper", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                new MineSweeperGame(Integer.parseInt(arguments[0]), Integer.parseInt(arguments[1]), Bukkit.getPlayer(arguments[2]), main);

            }

        });

        registerAction("testSkill", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                if(arguments.length == 1) executor.sendTitle(Title.builder().title("§e§lAutoEvent").subtitle("§8(§f15§8) §7Oblicz! §6/ae §a83+543").fadeIn(20).stay(50).fadeOut(20).build());
                else executor.sendTitle(Title.builder().title("§e§lAutoEvent").subtitle("§8(§f15§8) §7Przepisz kod! §6/ae §aawdhiuawd").fadeIn(20).stay(50).fadeOut(20).build());

            }

        });
        registerAction("testItem", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                ItemStack is = executor.getItemInHand();
                TextComponent comp = new TextComponent(TextComponent.fromLegacyText("[ITEM]"));
                comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent(CraftItemStack.asNMSCopy(is).save(new NBTTagCompound()).toString())}));
                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(comp));

            }

        });

        registerAction("newNpc", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                if(arguments.length == 0) return;
                String name = ChatColor.translateAlternateColorCodes('&', StringUtils.join(arguments, " "));
                NPC npc = main.getNpcRegistry().createNewNPC(name, executor.getLocation());
                npc.saveOnDisable();
                npc.spawn();
                executor.sendMessage("Utworzono nowego NPC o ID "+npc.getCitizenId());

            }

        });

        registerAction("spawnNpc", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                NPC npc = new NPC(0, main, executor.getLocation(), "§bRafal Aniszewski");
                npc.setSkin(
                        "ewogICJ0aW1lc3RhbXAiIDogMTY2OTU5MDg1NDE5MywKICAicHJvZmlsZUlkIiA6ICI5MWQ4Mzk4ZjFkNjQ0ZGFjYWQwNDBjNDdlZjEwMTI2NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ4TjBNQU5EeCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80NDJlOGMyNjRmZjlmMjg5YTk1MzAxNDEzZTZhMWMzYjNkOGQ0MDNjZmNlNjEwOThjZmM5OGYyZjE4ZDgzODIwIgogICAgfQogIH0KfQ==",
                        "QCiTeC8mmYgxtMDMLoxsQH12dmYeLzzCDP7uRpjOCgmoFOtMlaF1TObX6CbT3KfYTU8PFQY7WcUfxXO3W6hAU/kL/gMblpB3tGVGy/NXez+CEwKHPGMPpul6SMXeaoF/S1s/qEJhVdlu0n0rXv91buYNdvrdbgp5x5jdLkVry0UR999jTjusCCtIj35o3HcnZQkTEaxF2B2KVqoDrYbUK/GIFm25B1oY/RUxKjoVyvAdvSPc37F/LLeUhuylFk98NNzzY7QjhsGloNL8K9yz3BKuGyc1s3HzEi6wi8hESy/1OPmqNW9U99o0MVPR9r3j4Rp+N+K56xkKAD61xVGyJw6auxFCNSjcLqeh2pDUdcvBHISUFcUCUw5j07BZPxf9k9ML3CRcfnDCU+BlwxZ0Twbh93DsfdpvoP5urgkVFL0SW/ACeaGU0Z7Z+FfDpFaw8RQCnfQSXumsFiYqA0AL2d+0THYXT2uTNe0UriD81b0tSmu6VU6+/TzspgkfiO95Vdb1OgNF2hb0VX0ZxpMGCb2sL3bcZ8ZYXkgRUGcylm9NVlmDEGMCcv4QWYBoRdoj14rwL08n6wxwPQqYG1wBOvkewm7+T/+6mXi9v2p3lulOpwfUPB3m1MyYj8JroxutfRcP+YvLUdqpe/AZdeltfEsyrW3X9em885hSQt5Ag9I=");
                npc.setSecondLine("§e* §fWitaj, <player> §e*");

                npc.setCustomLineModifier(new CustomSecondLine() {
                    @Override
                    public String onSendingSecondLine(Player player, String line) {
                        return line.replace("<player>", player.getName());
                    }
                });
                npc.setDisguise(DisguiseType.PIG);
                npc.spawn();

                new BukkitRunnable() {

                    int i = 0;

                    public void run() {

                        i++;

                        if(npc.isRemoved()) {
                            this.cancel();
                            return;
                        }

                        npc.playArmAnimation();

                        if(i % 2 == 0) {
                            npc.setSecondLine("§6* §eWitaj <player>! §6*");
                        } else npc.setSecondLine("§e* §fWitaj <player>! §e*");

                    }

                }.runTaskTimer(main, 10, 10);

                new BukkitRunnable() {

                    public void run() {

                        npc.despawn(true);

                        npc.remove();

                    }

                }.runTaskLater(main, 200);

            }

        });

        registerAction("list", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                String ret = "";

                for(String s : Debugs.keySet()) {
                    ret+=", "+s;
                }

                executor.sendMessage("Lista komend: "+ret.replaceFirst(", ", ""));

            }

        });

    }

    public void registerAction(String command, ActionRun run) {
        Debugs.put(command.toLowerCase(), run);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender.hasPermission("admin.Debug")) {

            if(cmd.getName().equalsIgnoreCase("Action")) {

                if(args.length > 0) {

                    if(!(sender instanceof Player)) {
                        sender.sendMessage("Komenda tylko dla graczy!");
                        return false;
                    }

                    Player player = (Player) sender;

                    String Debug = args[0].toLowerCase();

                    ActionRun run = Debugs.get(Debug);

                    String[] arguments = new String[args.length-1];

                    int i = -2;

                    for(String string : args) {
                        i++;
                        if(i==-1) continue;

                        arguments[i] = ChatColor.translateAlternateColorCodes('&', args[i+1]);

                    }

                    if(run != null) {

                        try {
                            run.run(player, arguments);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }

                } else sender.sendMessage("Poprawnie: /Action <action> [args]");

            }

        } else sender.sendMessage("Nie masz do tego uprawnien!");

        return false;

    }

}