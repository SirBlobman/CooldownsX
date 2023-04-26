package com.github.sirblobman.cooldowns.dictionary;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.api.shaded.xseries.XMaterial;

public final class MaterialDictionary extends Dictionary<XMaterial> {
    public MaterialDictionary(@NotNull ICooldownsX plugin) {
        super(plugin, "dictionary/material.yml", XMaterial.class);
    }
}
