package me.kgaz.chat;

import me.kgaz.Citizens;
import me.kgaz.util.ParticleEffect;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class ChatManager implements Listener, CommandExecutor {

    private Citizens main;
    private int itemId;

    public ChatManager(Citizens main){

        this.main = main;

        main.registerListener(this);

        main.getCommand("showItemFromChat").setExecutor(this);
        main.getCommand("showHeartzFromChat").setExecutor(this);

    }



    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {

        if(e.isCancelled()) return;

        e.setCancelled(true);

        TextComponent message = new TextComponent(TextComponent.fromLegacyText(""));
        TextComponent space = new TextComponent(TextComponent.fromLegacyText(" "));

        { /* LEVEL */
            TextComponent level = new TextComponent(TextComponent.fromLegacyText("§8[§2LvL §a0§8]"));
            level.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(
                    "§7Posiadany Exp: §a0§8/§22.4kkk\n" +
                            "§7Postep Procentowy§8: §a0§2%")));

            message.addExtra(level);
            message.addExtra(space);

        }

        {

            String nick = e.getPlayer().getName();
            // TODO: actually add some rangs

            if(nick.equalsIgnoreCase("kgaz") || nick.equalsIgnoreCase("xn0mandx")) {

                TextComponent ranga = new TextComponent(TextComponent.fromLegacyText("§4§lDEV"));
                ranga.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(nick.equalsIgnoreCase("kgaz") ? "§7Wez do mnie nie pisz, zajety jestem." : "§7A dawaj rundke w Age of Empires IV")));
                ranga.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/administracja"));

                message.addExtra(ranga);
                message.addExtra(space);

            }

        }

        { /* NICK */

            TextComponent nick = new TextComponent(TextComponent.fromLegacyText((e.getPlayer().getName().equalsIgnoreCase("kgaz") || e.getPlayer().getName().equalsIgnoreCase("xN0MANDx") ? "§c" : "§9")+e.getPlayer().getName()));
            nick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§dMozna by tu dac jakis opis co\n§dGracz moze sobie sam ustawic czy cos.")));
            nick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg "+e.getPlayer().getName()+" "));

            message.addExtra(nick);
            message.addExtra(space);

        }

        message.addExtra(new TextComponent(TextComponent.fromLegacyText("§8» §e")));

        String lastColor = "§f";
        StringBuilder lastComponent = new StringBuilder("§f");

        char[] chars = ChatColor.translateAlternateColorCodes('&', e.getMessage()).toCharArray();

        for(int i = 0; i < ChatColor.translateAlternateColorCodes('&', e.getMessage()).length(); i++) {

            char c = chars[i];

            if(c == '@') {

                String nick = "";

                for(int n = i+1; n < chars.length; n++) {

                    i = n;

                    char next = chars[n];

                    if(Character.isLetter(next) || next == '_') {

                        nick+=next;

                    } else break;

                }

                message.addExtra(new TextComponent(TextComponent.fromLegacyText(lastComponent.toString())));

                {

                    Player target = Bukkit.getPlayer(nick);

                    if(target == null) {

                        TextComponent ping = new TextComponent("§8@§7"+nick);
                        ping.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§7Nie odnaleziono podanego gracza.")));

                        e.getPlayer().sendTitle(Title.builder().title("").subtitle("§8Nie odnaleziono podanego gracza!").fadeIn(10).fadeOut(10).stay(20).build());

                        message.addExtra(ping);

                    } else {

                        TextComponent ping = new TextComponent("§3@§b"+target.getName());
                        ping.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§fPong")));

                        target.playSound(target.getLocation(), Sound.ANVIL_LAND, 5, 1);

                        target.sendTitle(Title.builder().title("§bZostales Zaczepiony").subtitle("§3Przez §f"+e.getPlayer().getName()+"§3!").stay(30).fadeIn(5).fadeOut(20).build());

                        message.addExtra(ping);

                    }

                }

                lastComponent = new StringBuilder(lastColor + " ");

                continue;

            }

            if(c == '§') {

                if(i + 1 < chars.length) {

                    char next = chars[i + 1];

                    if(Character.isDigit(next)) {

                        lastColor = "§"+next;

                    } else {

                        lastColor+= "§"+next;

                    }

                    i++;

                    lastComponent.append(lastColor);
                    continue;

                }

            }

            if(c == '<') {

                if(i + 1 < chars.length) {

                    if(chars[i+1] == '3') {

                        i++;

                        message.addExtra(new TextComponent(TextComponent.fromLegacyText(lastComponent.toString())));

                        { /* Creating emote component */

                            TextComponent heart = new TextComponent("§4❤");
                            heart.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/showHeartzFromChat"));
                            message.addExtra(heart);

                        }

                        lastComponent = new StringBuilder(lastColor);

                        continue;

                    }

                }

            }
            if(c == '[') {

                if(i + 5 < chars.length) {

                    StringBuilder text = new StringBuilder();

                    for(int p = 0; p < 6; p++) {

                        text.append(chars[i + p]);

                    }

                    if(text.toString().equalsIgnoreCase("[item]")) {

                        TextComponent next;

                        { /* Creating emote component */

                            ItemStack is = e.getPlayer().getInventory().getItemInHand();

                            if(is == null || is.getType() == Material.AIR) {

                                lastComponent.append(c);

                                continue;

                            }

                            if(!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) {

                                TextComponent item = new TextComponent("§8[§7Brak Nazwy§8]");

                                item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,new BaseComponent[]{new TextComponent(CraftItemStack.asNMSCopy(is).save(new NBTTagCompound()).toString())}));

                                itemId++;

                                items.put(itemId, is);

                                item.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/showItemFromChat "+itemId));

                                next = item;

                            } else {

                                TextComponent item = new TextComponent("§8[§7"+is.getItemMeta().getDisplayName()+"§8]");

                                item.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM,new BaseComponent[]{new TextComponent(CraftItemStack.asNMSCopy(is).save(new NBTTagCompound()).toString())}));

                                itemId++;

                                items.put(itemId, is);

                                item.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/showItemFromChat "+itemId));

                                next = item;

                            }

                        }

                        i+=5;

                        message.addExtra(new TextComponent(TextComponent.fromLegacyText(lastComponent.toString())));

                        message.addExtra(next);

                        lastComponent = new StringBuilder(lastColor);

                        continue;

                    }

                }

            }

            lastComponent.append(c);

        }

        message.addExtra(new TextComponent(TextComponent.fromLegacyText(lastComponent.toString())));

        Bukkit.getOnlinePlayers().forEach(player -> {

            player.spigot().sendMessage(message);

        });

    }

    private List<CommandSender> block = new ArrayList<>();
    private Map<Integer, ItemStack> items = new HashMap<>();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(cmd.getName().equalsIgnoreCase("showItemFromChat")) {

            if(args.length > 0) {

                int id = 0;

                try {

                    id = Integer.parseInt(args[0]);

                }catch(Exception exc) {

                    return true;

                }

                if(items.containsKey(id)) {

                    Inventory inv = Bukkit.createInventory(null, 27, "§8Podglad Itemu");

                    {

                        ItemStack is = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
                        ItemMeta im = is.getItemMeta();
                        im.setDisplayName("§4");
                        is.setItemMeta(im);

                        for(int i = 0; i < 27; i++) inv.setItem(i, is);

                    }

                    inv.setItem(13, items.get(id));

                    ((Player)sender).openInventory(inv);

                }

            }

        }

        if(cmd.getName().equalsIgnoreCase("showHeartzFromChat")) {

            if(block.contains(sender)) return true;

            else {

                block.add(sender);

                if(sender instanceof Player) {

                    Player player = (Player) sender;

                    ParticleEffect.HEART.send(Bukkit.getOnlinePlayers(), player.getEyeLocation(), 0.4, 0.4, 0.4, 0.2, 32, 16);
                    player.playSound(player.getLocation(), Sound.WOLF_WHINE, 1, 1);

                }

                new BukkitRunnable() {

                    public void run() {

                        block.remove(sender);

                    }

                }.runTaskLater(main, 80);

            }

        }

        return true;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if(e.getClickedInventory() != null) {

            if(e.getClickedInventory().getTitle() != null) {

                if(e.getClickedInventory().getTitle().equalsIgnoreCase("§8Podglad Itemu")) {

                    e.setCancelled(true);
                    e.setResult(Event.Result.DENY);

                }

            }

        }

    }

}
