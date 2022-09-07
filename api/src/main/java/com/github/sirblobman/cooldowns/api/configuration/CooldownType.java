package com.github.sirblobman.cooldowns.api.configuration;

/**
 * Cooldown type enum
 * @author SirBlobman
 */
public enum CooldownType {
    /**
     * Right-clicking an item in the air or on a block.
     * Requires a material.
     */
    INTERACT_ITEM,

    /**
     * Right-clicking on a block.
     * Requires a material
     */
    INTERACT_BLOCK,

    /**
     * Eating or drinking an item.
     * Requires a material
     */
    CONSUME_ITEM,

    /**
     * Throwing or drinking a potion.
     * Works for potions, splash potions, and lingering potions.
     * Requires a potion effect type.
     */
    POTION,

    /**
     * Using an item to resurrect (currently only Totem of Undying)
     */
    UNDYING
}
