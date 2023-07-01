package com.github.sirblobman.plugin.cooldown.dictionary;

import org.jetbrains.annotations.NotNull;

import com.github.sirblobman.plugin.cooldown.api.CooldownsX;
import com.github.sirblobman.api.shaded.xseries.XMaterial;

public final class MaterialDictionary extends Dictionary<XMaterial> {
    public MaterialDictionary(@NotNull CooldownsX plugin) {
        super(plugin, "dictionary/material.yml", XMaterial.class);
    }
}
