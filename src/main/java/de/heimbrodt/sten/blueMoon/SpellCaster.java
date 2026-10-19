package de.heimbrodt.sten.blueMoon;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class SpellCaster {
    BlueMoonPlayer bp;

    public SpellCaster(BlueMoonPlayer bp) {
        this.bp = bp;
    }

    boolean castSpell(String spellName) {
        if (spellName.equalsIgnoreCase("reparus")) {
            if (bp.getMana() > 60.0) {
                PlayerInventory pi = bp.getPlayer().getInventory();
                ItemStack mainHandItem = pi.getItemInMainHand();
                ItemMeta mainHandMeta = mainHandItem.getItemMeta();
                if (mainHandMeta instanceof Damageable damageable) {
                    damageable.setDamage(0);
                    mainHandItem.setItemMeta(mainHandMeta);
                    bp.getPlayer().getInventory().setItemInMainHand(mainHandItem);
                    bp.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "You cast the spell " + ChatColor.GOLD + "Reparus" + ChatColor.RESET);
                    bp.drainMana(60.0F);

                    SpellEffects.ring(bp.getPlayer());
                    return true;
                }
            }
            else {
                bp.getPlayer().sendMessage(ChatColor.RED + "Not enough mana!");
            }
        }
        return false;
    }
}
