package com.github.sirblobman.plugin.cooldown.api.configuration;

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
     * Uses EntityPotionEffectEvent to detect when potions are added to a player.
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
     * Only works on Spigot 1.11.2 or higher.
     */
    UNDYING,

    /**
     * Placing an entity, like end crystal, armor stand, or minecraft
     * Only works on Spigot 1.13.2 or higher.
     * Requires an entity type.
     */
    PLACE_ENTITY
}
