# CooldownsX

A plugin that adds configurable cooldowns to items.

# Placeholders

CooldownsX adds placeholders to plugins that support PlaceholderAPI.
Review the table below for placeholder information:

| Placeholder                               | Description                                                   | Example Output |
|-------------------------------------------|---------------------------------------------------------------|----------------|
| %cooldownsx_time_left_&lt;id&gt;%         | The amount of seconds left for a specific cooldown. (integer) | 5              |
| %cooldownsx_time_left_decimal_&lt;id&gt;% | The amount of seconds left for a specific cooldown. (decimal) | 5.2            |

# API Information

CooldownsX has a useful API that is hosted on my own repository.
To use the api, add the following values to your `pom.xml` file:

```xml

<repositories>
    <!-- SirBlobman Public Repository -->
    <repository>
        <id>sirblobman-public</id>
        <url>https://nexus.sirblobman.xyz/public/</url>
    </repository>
</repositories>
```

```xml

<dependencies>
    <!-- CooldownsX -->
    <dependency>
        <groupId>com.github.sirblobman.plugin.cooldowns</groupId>
        <artifactId>cooldowns-api</artifactId>
        <version>5.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## API Usage

To use the API you should make sure that CooldownsX is enabled on the server first.  
The main things you need to know are how to get the plugin instance and how to get data for a player:

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.github.sirblobman.cooldowns.api.ICooldownsX;
import com.github.sirblobman.cooldowns.api.configuration.ICooldownSettings;
import com.github.sirblobman.cooldowns.api.data.ICooldownData;
import com.github.sirblobman.cooldowns.api.manager.ICooldownManager;

import org.jetbrains.annotations.Nullable;

public final class CooldownHelper {
    public ICooldownsX getCooldownsX() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = pluginManager.getPlugin("CooldownsX");
        return (ICooldownsX) plugin;
    }

    public ICooldownData getData(Player player) {
        ICooldownsX plugin = getCooldownsX();
        ICooldownManager cooldownManager = plugin.getCooldownManager();
        return cooldownManager.getData(player);
    }

    @Nullable
    public ICooldownSettings getCooldownSettings(String id) {
        ICooldownsX plugin = getCooldownsX();
        ICooldownManager cooldownManager = plugin.getCooldownManager();
        return cooldownManager.getCooldownSettings(id);
    }

    /*
     *  You can check the expiration time of a specific cooldown for a player:
     */
    public long getCooldownExpireMillis(Player player, String id) {
        ICooldownSettings cooldownSettings = getCooldownSettings(id);
        if (cooldownSettings == null) {
            return 0L;
        }

        ICooldownData cooldownData = getData(player);
        return cooldownData.getCooldownExpireTime(cooldownSettings);
    }
}
```
