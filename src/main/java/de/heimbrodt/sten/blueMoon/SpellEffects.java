package de.heimbrodt.sten.blueMoon;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class SpellEffects {
    public SpellEffects() {

    }

    public static void ring(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.2f);

        double radius = 1.5;
        int points = 40;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            world.spawnParticle(Particle.DUST, loc.clone().add(x, 0.1, z), 1, 0, 0, 0, 0, dust);
        }

        world.playSound(loc, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1.2f);
    }

    public static void aura(Player player) {
        Location loc = player.getLocation().add(0, 1, 0); // Körpermitte
        World world = player.getWorld();

        // count, offsetX, offsetY, offsetZ, speed
        world.spawnParticle(Particle.WITCH, loc, 40, 0.5, 0.7, 0.5, 0.05);
        world.spawnParticle(Particle.END_ROD, loc, 15, 0.3, 0.5, 0.3, 0.05);

        world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
    }

}
