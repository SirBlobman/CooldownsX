package com.github.sirblobman.cooldowns.dictionary;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.api.shaded.xseries.XPotion;

public final class PotionDictionary extends Dictionary<XPotion> {
    public PotionDictionary(@NotNull CooldownsX plugin) {
        super(plugin, "dictionary/potion.yml", XPotion.class);
    }
}
