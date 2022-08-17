package com.github.sirblobman.cooldowns.dictionary;

import com.github.sirblobman.api.xseries.XPotion;
import com.github.sirblobman.cooldowns.CooldownPlugin;

public final class PotionDictionary extends Dictionary<XPotion> {
    public PotionDictionary(CooldownPlugin plugin) {
        super(plugin, "dictionary/potion.yml", XPotion.class);
    }
}
