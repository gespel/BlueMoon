package de.heimbrodt.sten.blueMoon;

import org.bukkit.entity.Player;

public class BlueMoonPlayer {
    private Player p;
    private float mana;
    private float max_mana;
    private SpellCaster spellCaster;

    public BlueMoonPlayer(Player p) {
        this.p = p;
        mana = 0;
        max_mana = 100;
        this.spellCaster = new SpellCaster(this);
    }

    public Player getPlayer() {
        return this.p;
    }

    public float getMana() {
        return this.mana;
    }

    public void drainMana(float amount) {
        this.mana -= amount;
    }

    public void cast(String spellName) {
        this.spellCaster.castSpell(spellName);
    }

    public void tickMana() {
        if (this.mana < this.max_mana) {
            this.mana += 1;
        }
    }
}
