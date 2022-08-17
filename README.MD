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
        <url>https://nexus.sirblobman.xyz/repository/public/</url>
    </repository>
</repositories>
```

```xml
<dependencies>
    <!-- CooldownsX -->
    <dependency>
        <groupId>com.github.sirblobman.plugin</groupId>
        <artifactId>cooldowns</artifactId>
        <version>4.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

## API Usage
To use the API you should make sure that CooldownsX is enabled on the server first.  
The main things you need to know are how to get the plugin instance and how to get data for a player:

```java
import org.jetbrains.annotations.Nullable;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import com.github.sirblobman.cooldowns.CooldownPlugin;
import com.github.sirblobman.cooldowns.object.CooldownData;
import com.github.sirblobman.cooldowns.configuration.CooldownSettings;
import com.github.sirblobman.cooldowns.manager.CooldownManager;

public final class CooldownHelper {
    public CooldownPlugin getCooldownsX() {
        return JavaPlugin.getPlugin(CooldownPlugin.class);
    }

    public CooldownData getData(Player player) {
        CooldownPlugin plugin = getCooldownsX();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        return cooldownManager.getData(player);
    }

    @Nullable
    public CooldownSettings getCooldownSettings(String id) {
        CooldownPlugin plugin = getCooldownsX();
        CooldownManager cooldownManager = plugin.getCooldownManager();
        return cooldownManager.getCooldownSettings(id);
    }
    
    /*
     *  You can check the expiration time of a specific cooldown for a player:
     */
    public long getCooldownExpireMillis(Player player, String id) {
        CooldownSettings cooldownSettings = getCooldownSettings(id);
        if(cooldownSettings == null) {
            return 0L;
        }
        
        CooldownData cooldownData = getData(player);
        return cooldownData.getCooldownExpireTime(cooldownSettings);
    }
}
```
