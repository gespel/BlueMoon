package de.heimbrodt.sten.blueMoon;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public final class BlueMoon extends JavaPlugin implements CommandExecutor, Listener {
    HashSet<BlueMoonPlayer> bluemoon_players;
    ArrayList<BlueMoonVillager> bluemoon_villagers;
    private NamespacedKey halloKey;

    @Override
    public void onEnable() {
        halloKey = new NamespacedKey(this, "hallo_villager");
        bluemoon_players = new HashSet<BlueMoonPlayer>();
        bluemoon_villagers = new ArrayList<BlueMoonVillager>();
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

    BlueMoonVillager getBlueMoonVillagerByVillager(Villager v) {
        for (BlueMoonVillager bv : this.bluemoon_villagers) {
            if (bv.getVillager() == v) {
                return bv;
            }
        }
        return null;
    }

    private void spawnHalloVillager(Player p) {
        Location loc = p.getLocation();

        // Der Consumer läuft, bevor die Entity in die Welt kommt → kein "Flackern"
        Villager villager = loc.getWorld().spawn(loc, Villager.class, v -> {
            v.setCustomName("§eBibliothekar");
            v.setCustomNameVisible(true);
            v.setProfession(Villager.Profession.LIBRARIAN);
            v.setAI(false);
            v.setInvulnerable(false);
            v.setSilent(false);
            v.setPersistent(true);
            v.setRemoveWhenFarAway(false);

            v.getPersistentDataContainer().set(halloKey, PersistentDataType.BYTE, (byte) 1);
        });

        bluemoon_villagers.add(new BlueMoonVillager(villager, BlueMoonVillagerType.LIBRARIAN));
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
        else if(cmd.getName().equalsIgnoreCase("bibliothekar")) {
            spawnHalloVillager(p);
        }
        else if(cmd.getName().equalsIgnoreCase("linasdorf")) {
            p.sendMessage("Willkommen zurück in Linas Dorf!");
            p.teleport(new Location(Bukkit.getWorld("world"), -253, 64, -709));
        }
        return true;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player p = event.getPlayer();
        String msg = PlainTextComponentSerializer.plainText().serialize(event.message());

        for (BlueMoonVillager bv : this.bluemoon_villagers) {
            if (bv.getVillager().getLocation().distance(p.getLocation()) < 10) {
                LLMRequester llmr = new LLMRequester();
                llmr.talkToPlayer(getBlueMoonPlayerByPlayer(p), bv, msg).thenAccept(text -> Bukkit.getScheduler().runTask(this, () -> {
                    p.sendMessage("§a[Bibliothekar] §f" + text);
                    bv.addMemory(p.getName(), msg, text);
                }));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVillagerClick(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        if (!villager.getPersistentDataContainer().has(halloKey, PersistentDataType.BYTE)) return;

        BlueMoonVillager bv = getBlueMoonVillagerByVillager(villager);

        Player p = event.getPlayer();
        event.setCancelled(true);
        LLMRequester llmr = new LLMRequester();
        llmr.talkToPlayer(getBlueMoonPlayerByPlayer(p), bv, "*spricht dich an*").thenAccept(text -> Bukkit.getScheduler().runTask(this, () -> p.sendMessage("§a[Bibliothekar] §f" + text)));

    }
}
