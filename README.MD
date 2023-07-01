# CooldownsX

A plugin that adds customizable cool down timers to items.
Using this plugin, you can ensure that your server is balanced and that players
use items strategically instead of spamming them. 

## Requirements

- Java 8 required, Java 17 recommended.
- [Spigot](https://spigotmc.org/), [Paper](https://papermc.io/downloads/paper), or [Folia](https://papermc.io/software/folia) 1.8.8 - 1.20.1.
- [BlueSlimeCore](https://jenkins.sirblobman.xyz/job/SirBlobman/job/BlueSlimeCore/) 2.9.3 or higher.

## Placeholders

CooldownsX adds placeholders to plugins that support PlaceholderAPI.
Review the table below for placeholder information:

| Placeholder                           | Description                                                   | Example Output |
|---------------------------------------|---------------------------------------------------------------|----------------|
| %cooldownsx_time_left_`<id>`%         | The amount of seconds left for a specific cooldown. (integer) | 5              |
| %cooldownsx_time_left_decimal_`<id>`% | The amount of seconds left for a specific cooldown. (decimal) | 5.2            |

`<id>`: The configuration identifier. 

## API Information

CooldownsX has a useful API that is hosted on my own repository.
To use the api, add the following values to your `pom.xml` file:

<details>
<summary>Maven Repository</summary>

```xml
<repositories>
    <!-- SirBlobman Public Repository -->
    <repository>
        <id>sirblobman-public</id>
        <url>https://nexus.sirblobman.xyz/public/</url>
    </repository>
</repositories>
```
</details>
<details>
<summary>Maven Dependency</summary>

```xml
<dependencies>
    <!-- CooldownsX -->
    <dependency>
        <groupId>com.github.sirblobman.plugin.cooldowns</groupId>
        <artifactId>cooldowns-api</artifactId>
        <version>5.1.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```
</details>

## API Usage

To use the API you should make sure that CooldownsX is enabled on the server first.  
The main things you need to know are how to get the plugin instance and how to get data for a player:

<details>
<summary>Example Code</summary>

```java
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.github.sirblobman.plugin.cooldown.CooldownsX;
import com.github.sirblobman.plugin.cooldown.Cooldown;
import com.github.sirblobman.plugin.cooldown.PlayerCooldown;
import com.github.sirblobman.plugin.cooldown.PlayerCooldownManager;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public final class CooldownHelper {
    public @NotNull CooldownsX getCooldownsX() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        Plugin plugin = pluginManager.getPlugin("CooldownsX");
        return (CooldownsX) plugin;
    }

    public @NotNull PlayerCooldown getData(@NotNull Player player) {
        CooldownsX plugin = getCooldownsX();
        PlayerCooldownManager manager = plugin.getCooldownManager();
        return manager.getData(player);
    }
    
    public @Nullable Cooldown getCooldownSettings(@NotNull String id) {
        CooldownsX plugin = getCooldownsX();
        PlayerCooldownManager manager = plugin.getCooldownManager();
        return manager.getCooldownSettings(id);
    }

    /*
     *  You can check the expiration time of a specific cooldown for a player:
     */
    public long getCooldownExpireMillis(@NotNull Player player, @NotNull String id) {
        Cooldown cooldown = getCooldownSettings(id);
        if (cooldown == null) {
            return 0L;
        }

        PlayerCooldown data = getData(player);
        return data.getCooldownExpireTime(cooldown);
    }
}
```
</details>
