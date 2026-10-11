package de.heimbrodt.sten.blueMoon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

        return true;
    }
}
