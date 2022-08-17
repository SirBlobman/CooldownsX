package com.github.sirblobman.cooldowns.dictionary;

import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;

public final class MaterialDictionary extends Dictionary<XMaterial> {
    public MaterialDictionary(CooldownPlugin plugin) {
        super(plugin, "dictionary/material.yml", XMaterial.class);
    }
}
