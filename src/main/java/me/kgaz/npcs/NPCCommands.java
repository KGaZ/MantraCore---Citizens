package me.kgaz.npcs;

import me.kgaz.Citizens;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NPCCommands implements CommandExecutor, Listener {

    private Map<Player, NPC> selected = new HashMap<>();
    private Citizens main;
    private String[] commands;

    public NPCCommands(Citizens main) {

        this.main = main;
        main.registerListener(this);

        commands = new String[] {
                "§6/§enpc help [strona] §8- §7Wszystkie komendy",
                "§6/§enpc list §8- §7Wyswietla wszystkie NPC'ty",
                "§6/§enpc info §8- §7Wyswietla informacje o NPC'cie",
                "§6/§enpc sel <id> §8- §7Wybor NPC.",
                "§6/§enpc remove §8- §7Usuwasz permamentnie NPC.",
                "§6/§enpc create §8- §7Tworzysz NPC i wybierasz go.",
                "§6/§enpc tp §8- §7Teleport do NPC",
                "§6/§enpc move §8- §7Teleport NPC do Ciebie",
                "§6/§enpc setLine [line] §8- §7Ustawia druga linijke NPC."
        };

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(cmd.getName().equalsIgnoreCase("npc") && sender instanceof Player) {

            if(sender.hasPermission("admin.citizens")) {

                if(args.length == 0) {

                    sendHelp(sender, 1);

                } else {

                    if(args[0].equalsIgnoreCase("help")) {

                        int page = 1;

                        try {

                            page = Integer.parseInt(args[1]);

                        } catch(Exception exc) {

                            page = 1;

                        }

                        sendHelp(sender, page);

                    }

                    if(args[0].equalsIgnoreCase("list")) {

                        Collection<NPC> npcs = main.getNpcRegistry().getNpcs();

                        sender.sendMessage("§8Zarejestrowane NPC'ty: §2"+npcs.size());

                        StringBuilder list = new StringBuilder();

                        for(NPC npc : npcs) {

                            list.append("§7, §2").append(npc.getCitizenId()).append(".§a").append(npc.getName());

                        }

                        sender.sendMessage(list.toString().replaceFirst(", ", ""));

                    }

                    Player player = (Player) sender;

                    if(args[0].equalsIgnoreCase("setLine")) {

                        NPC sel = selected.get(player);
                        if(sel == null) {

                            sender.sendMessage("§7Aby uzyc te komende nalezy wybrac NPC za pomoca patyka lub /npc sel!");
                            return false;

                        }

                        if(args.length > 1) {

                            StringBuilder line = new StringBuilder(args[1]);
                            for(int i = 2; i < args.length; i++) line.append(" ").append(args[1]);

                            sel.setSecondLine(ChatColor.translateAlternateColorCodes('&', line.toString()));
                            sender.sendMessage("§7Ustawiono druga linijke dla NPC!");

                        } else sender.sendMessage("§7Poprawne uzycie§8: §6/§enpc setLine [linijka]");

                    }

                    if(args[0].equalsIgnoreCase("skin")) {

                        if(args.length > 1) {

                            String nick = args[1];

                        } else sender.sendMessage("§7Poprawne uzycie§8: §6/§enpc skin <nick>");

                    }

                    if(args[0].equalsIgnoreCase("tphere") || args[0].equalsIgnoreCase("move")) {

                        NPC sel = selected.get(player);
                        if(sel == null) {

                            sender.sendMessage("§7Aby uzyc te komende nalezy wybrac NPC za pomoca patyka lub /npc sel!");
                            return false;

                        }

                        sel.setLocation(player.getLocation());

                        sender.sendMessage("§7Przeteleportowano do Ciebie NPC!");

                    }

                    if(args[0].equalsIgnoreCase("tp")) {

                        NPC sel = selected.get(player);
                        if(sel == null) {

                            sender.sendMessage("§7Aby uzyc te komende nalezy wybrac NPC za pomoca patyka lub /npc sel!");
                            return false;

                        }

                        player.teleport(sel.getLocation());

                        sender.sendMessage("§7Przeteleportowano do NPC!");

                    }

                    if(args[0].equalsIgnoreCase("sel")) {

                        int id = 0;

                        try {

                            id = Integer.parseInt(args[1]);

                        } catch(Exception exc) {

                            sender.sendMessage("§7Poprawne uzycie§8: §6/§enpc sel <id>");

                            return false;

                        }

                        for(NPC npc : main.getNpcRegistry().getNpcs()) {

                            if(npc.getCitizenId() == id) {

                                sender.sendMessage("§7Wybrano NPC o id §6"+id+"§8!");

                                selected.put(player, npc);

                                return false;

                            }

                        }

                        sender.sendMessage("§7Nie odnaleziono NPC o podanym id!");
                        return false;

                    }

                    if(args[0].equalsIgnoreCase("create")) {

                        if(args.length > 1) {

                            StringBuilder name = new StringBuilder(args[1]);
                            for(int i = 2; i < args.length; i++) name.append(" ").append(args[i]);

                            NPC nowy = main.getNpcRegistry().createNewNPC(ChatColor.translateAlternateColorCodes('&', name.toString()), player.getLocation());
                            nowy.spawn();
                            nowy.saveOnDisable();

                            selected.put(player, nowy);

                            sender.sendMessage("§7Stworzyles nowego NPC o id §9"+nowy.getCitizenId());

                        } else {

                            sender.sendMessage("§7Podaj nazwe NPC!");

                        }

                    }

                    if(args[0].equalsIgnoreCase("remove")) {

                        NPC sel = selected.get(player);
                        if(sel == null) {

                            sender.sendMessage("§7Aby uzyc te komende nalezy wybrac NPC za pomoca patyka lub /npc sel!");
                            return false;

                        }

                        main.getNpcRegistry().removeNPC(sel.getCitizenId());
                        selected.remove(player);
                        sender.sendMessage("§7Usunales NPC!");
                        return false;

                    }

                    if(args[0].equalsIgnoreCase("info")) {

                        NPC sel = selected.get(player);
                        if(sel == null) {

                            sender.sendMessage("§7Aby uzyc te komende nalezy wybrac NPC za pomoca patyka lub /npc sel!");
                            return false;

                        }

                        sender.sendMessage("§8Informacje o NPC");
                        sender.sendMessage("§7Citizen ID§8: §f"+sel.getCitizenId());
                        sender.sendMessage("§7Zapisywanie do pliku§8: "+((sel.shouldSave()) ? "§2Tak" : "§4Nie"));
                        sender.sendMessage("§7Lokalizacja§8: §6"+sel.getLocation().getBlockX()+"§e, §6"+sel.getLocation().getBlockY()+"§e, §6"+sel.getLocation().getBlockZ());
                        sender.sendMessage("§7Swiat§8: §e"+sel.getLocation().getWorld().getName());
                        sender.sendMessage("§7Widoczny dla kazdego§8: "+((sel.isVisibleByDefault()) ? "§2Tak" : "§4Nie"));
                        sender.sendMessage("§7Patrzenie na graczy§8: "+((sel.isLookAtPlayers()) ? "§2Tak" : "§4Nie"));
                        sender.sendMessage("§7Zespawnowany§8: "+((sel.isSpawned()) ? "§2Tak" : "§4Nie"));

                        TextComponent comp = new TextComponent(TextComponent.fromLegacyText("§7Informacje o Skinie §9[NAJEDZ]"));
                        comp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (TextComponent.fromLegacyText(
                                "§7Texture:" + "\n" +
                                        (sel.getTexture() != null ? "§a"+sel.getTexture() : "§cBrak") + "\n" +
                                "§7Signature" + "\n" +
                                        (sel.getSignature() != null ? "§a"+sel.getSignature() : "§cBrak")
                        ))));

                        //player.spigot().sendMessage(comp);

                        sender.sendMessage("§7Druga linijka§8: §f"+(sel.getSecondLine() != null ? sel.getSecondLine() : "§cBrak"));
                        sender.sendMessage("§7Disguise Type§8: §9"+sel.getDisguise().toString());

                    }

                }

            } else sender.sendMessage("§7Nie posiadasz uprawnien do tej komendy! §8(§2admin.citizens§8)");

        }

        return false;
    }

    public void sendHelp(CommandSender sender, int page) {

        int startPage = (page-1)*6;
        int endPage = Math.min(page * 6, commands.length);

        int pages = (int) Math.ceil((double) commands.length / 6d);

        sender.sendMessage("§8--------{ §6Komendy §8(§e"+page+"§8/§6" + pages + "§8) }--------");


        if(startPage >= commands.length) {

            startPage = 0;
            endPage = 6;

        }

        for(int i = startPage; i < endPage; i++) {

            sender.sendMessage(commands[i]);

        }

    }

}
