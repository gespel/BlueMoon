package de.heimbrodt.sten.blueMoon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlueMoon extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        getCommand("home").setExecutor(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player p = (Player) sender;

        if (cmd.getName().equalsIgnoreCase("home")) {
            p.sendMessage("Teleporting you home now...");
            p.teleport(new Location(Bukkit.getWorld("world"), 61.245, 65, -149.25));
        }
        else if(cmd.getName().equalsIgnoreCase("spellcast")) {
            if (args.length != 1) {
                p.sendMessage("No spell name provided!");
            }
            else {
                if (args[0].equalsIgnoreCase("reparus")) {
                    PlayerInventory pi = p.getInventory();
                    ItemStack mainHandItem = pi.getItemInMainHand();
                    ItemMeta mainHandMeta = mainHandItem.getItemMeta();
                    if (mainHandMeta instanceof Damageable damageable) {
                        damageable.setDamage(0);
                        mainHandItem.setItemMeta(mainHandMeta);
                        p.getInventory().setItemInMainHand(mainHandItem);
                        p.sendMessage(ChatColor.DARK_PURPLE + "You cast the spell " + ChatColor.GOLD + "Reparus" + ChatColor.RESET);
                    }
                }
            }
        }

        return true;
    }
}
