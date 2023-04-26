package com.github.sirblobman.cooldowns.dictionary;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.api.shaded.xseries.XPotion;

public final class PotionDictionary extends Dictionary<XPotion> {
    public PotionDictionary(@NotNull ICooldownsX plugin) {
        super(plugin, "dictionary/potion.yml", XPotion.class);
    }
}
