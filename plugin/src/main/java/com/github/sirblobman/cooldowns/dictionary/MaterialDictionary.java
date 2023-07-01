package com.github.sirblobman.cooldowns.dictionary;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.cooldowns.api.CooldownsX;
import com.github.sirblobman.api.shaded.xseries.XMaterial;

public final class MaterialDictionary extends Dictionary<XMaterial> {
    public MaterialDictionary(@NotNull CooldownsX plugin) {
        super(plugin, "dictionary/material.yml", XMaterial.class);
    }
}
