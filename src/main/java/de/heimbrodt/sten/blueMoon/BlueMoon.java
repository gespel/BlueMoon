package de.heimbrodt.sten.blueMoon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public final class BlueMoon extends JavaPlugin implements CommandExecutor, Listener {
    HashSet<BlueMoonPlayer> bluemoon_players;

    @Override
    public void onEnable() {
        bluemoon_players = new HashSet<BlueMoonPlayer>();
        getCommand("home").setExecutor(this);
        getCommand("spellcast").setExecutor(this);
        getCommand("mana").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (BlueMoonPlayer bp : bluemoon_players) {
                    bp.tickMana();
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        BlueMoonPlayer bp = new BlueMoonPlayer(p);
        bluemoon_players.add(bp);
        getLogger().info("Player " + p.getName() + " joined as a Mage!");
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        for (BlueMoonPlayer bp : bluemoon_players) {
            if (bp.getPlayer() == p) {
                bluemoon_players.remove(bp);
                getLogger().info("Player " + p.getName() + " left.");
                break;
            }
        }
    }

    BlueMoonPlayer getBlueMoonPlayerByPlayer(Player p) {
        for (BlueMoonPlayer bp : this.bluemoon_players) {
            if (bp.getPlayer() == p) {
                return bp;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player p = (Player) sender;
        BlueMoonPlayer bp = getBlueMoonPlayerByPlayer(p);

        if (cmd.getName().equalsIgnoreCase("home")) {
            p.sendMessage("Teleporting you home now...");
            p.teleport(new Location(Bukkit.getWorld("world"), 61.245, 65, -149.25));
        }
        else if(cmd.getName().equalsIgnoreCase("spellcast")) {
            if (args.length != 1) {
                p.sendMessage("No spell name provided!");
            }
            else {
                assert bp != null;
                bp.cast(args[0]);
            }
        }
        else if(cmd.getName().equalsIgnoreCase("mana")) {
            assert bp != null;
            p.sendMessage(ChatColor.BLUE + "" + bp.getMana());
        }
        return true;
    }
}
