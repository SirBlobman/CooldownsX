package com.github.sirblobman.cooldowns.api.configuration;

/**
 * Cooldown type enum
 *
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
     * Gaining a potion effect.
     * Includes drinking and splash potions for 1.8.8-1.12.2.
     * Uses EntityPotionEffectEvent in 1.13.2+ to detect when potions are added to a player.
     * Requires a potion effect type.
     */
    POTION,

    /**
     * Throwing a potion.
     * Works for splash and lingering potions.
     * Requires a potion effect type.
     */
    POTION_THROW,

    /**
     * Using an item to resurrect (currently only Totem of Undying)
     */
    UNDYING
}
