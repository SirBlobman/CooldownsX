package com.github.sirblobman.cooldowns.placeholder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.sirblobman.api.language.Language;
import com.github.sirblobman.api.language.LanguageManager;
import com.github.sirblobman.api.xseries.XMaterial;
import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.manager.CooldownManager;
import com.github.sirblobman.cooldowns.object.CooldownData;

public final class PlaceholderHelper {
    public static long getTimeLeftMillis(Player player, XMaterial material) {
        CooldownPlugin plugin = getPlugin();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        CooldownData cooldownData = cooldownManager.getData(player);

        long cooldownExpireMillis = cooldownData.getCooldownExpireTime(material);
        long systemMillis = System.currentTimeMillis();
        long subtractMillis = (cooldownExpireMillis - systemMillis);
        return Math.max(subtractMillis, 0L);
    }

    public static String getTimeLeftSeconds(Player player, XMaterial material) {
        long timeLeftMillis = getTimeLeftMillis(player, material);
        long timeLeftSeconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMillis);
        return Long.toString(timeLeftSeconds);
    }

    public static String getTimeLeftSecondsDecimal(Player player, XMaterial material) {
        double timeLeftMillis = getTimeLeftMillis(player, material);
        double timeLeftSeconds = (timeLeftMillis / 1_000.0D);

        CooldownPlugin plugin = getPlugin();
        LanguageManager languageManager = plugin.getLanguageManager();
        Language language = languageManager.getLanguage(player);
        if (language == null) {
            DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.US);
            DecimalFormat decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
            return decimalFormat.format(timeLeftSeconds);
        }

        DecimalFormat decimalFormat = language.getDecimalFormat();
        return decimalFormat.format(timeLeftSeconds);
    }

    private static CooldownPlugin getPlugin() {
        return JavaPlugin.getPlugin(CooldownPlugin.class);
    }
}
