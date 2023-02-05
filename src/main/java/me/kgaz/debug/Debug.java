package me.kgaz.debug;

import me.kgaz.Citizens;
import me.kgaz.diguises.DisguiseCreator;
import me.kgaz.npcs.*;
import me.kgaz.util.ParticleEffect;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Debug implements CommandExecutor {

    public static Debug INSTANCE;

    private Map<String, ActionRun> Debugs;

    public Debug(Citizens main) {

        INSTANCE = this;

        Debugs = new HashMap<>();

        registerAction("armorstand", new ActionRun() {
            @Override
            public void run(Player executor, String[] arguments) {

                ArmorStand stand = (ArmorStand) executor.getWorld().spawnEntity(executor.getLocation(), EntityType.ARMOR_STAND);
                stand.setGravity(false);
                stand.setVisible(false);
                stand.setCustomName("Siema");
                stand.setCustomNameVisible(true);

            }
        });

        registerAction("disguise", new ActionRun() {

            @Override
            public void run(Player executor, String[] arguments) {

                Zombie zombie = (Zombie) executor.getWorld().spawnEntity(executor.getLocation(), EntityType.ZOMBIE);

                zombie.setCustomName("Aniol Smierci Poziom 2");
                zombie.setCustomNameVisible(true);

                new BukkitRunnable() {

                    public void run() {

                        main.getDisguiseManager().disguiseEntity(zombie.getEntityId(), new DisguiseCreator(zombie, EntityType.SPIDER).generate());

                    }

                }.runTaskLater(main, 60);

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

                        arguments[i] = args[i+1];

                    }

                    if(run != null) {

                        run.run(player, arguments);

                    }

                } else sender.sendMessage("Poprawnie: /Action <action> [args]");

            }

        } else sender.sendMessage("Nie masz do tego uprawnien!");

        return false;

    }

}