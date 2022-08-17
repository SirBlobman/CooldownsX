package com.github.sirblobman.cooldowns.object;

public enum CombatMode {
    /**
     * Can only be triggered when the player is in combat
     */
    TRUE,

    /**
     * Can only be triggered when the player is NOT in combat
     */
    FALSE,

    /**
     * Combat status is not checked.
     */
    IGNORE,

    /**
     * Use a separate timer when a player is in combat.
     */
    DIFFERENT
}
