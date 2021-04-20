# CooldownsX
A plugin that adds configurable cooldowns to items.

# Placeholders
CooldownsX supports PlaceholderAPI and MVdWPlaceholderAPI
Surround the placeholders with %% or {} respectively.
| Placeholder | Description | Example |
| --- | --- | --- |
| cooldownsx_time_left_material | The amount of seconds left as an integer. | cooldownsx_time_left_GOLDEN_APPLE |
| cooldownsx_time_left_decimal_material | The amount of seconds left as a decimal with 2 places. | cooldownsx_time_left_decimal_ENDER_PEARL |
| cooldownsx_undying_time_left | The amount of seconds left before using another totem of undying as an integer. | cooldownsx_undying_time_left |
| cooldownsx_undying_time_left_decimal | The amount of seconds left before using another totem of undying as a decimal with 2 places. | cooldownsx_undying_time_left |

# Maven Information
CooldownsX has a useful API that is hosted on my own repository. To use the api, add the following values to your pom.xml file.
```xml
<repositories>
    <!-- SirBlobman Public Repository -->
    <repository>
        <id>sirblobman-public</id>
        <url>https://nexus.sirblobman.xyz/repository/public/</url>
    </repository>
</repositories>

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

# API Usage
To use the API you should make sure that CooldownsX is enabled on the server first.  
The main things you need to know are how to get the plugin instance and how to get data for a player:
```java
public CooldownPlugin getCooldownsX() {
    return JavaPlugin.getPlugin(CooldownPlugin.class);
}

public CooldownData getData(Player player) {
    CooldownPlugin plugin = getCooldownsX();
    CooldownManager cooldownManager = plugin.getCooldownManager();
    return cooldownManager.getData(player);
}
```

You can check the expiration time of a specific item for a player:
```java
public long getCooldownExpireMillis(Player player, XMaterial material) {
    CooldownData cooldownData = getData(player);
    return cooldownData.getCooldownExpireTime(material);
}
```
You can also set a custom cooldown using your own plugin:
```java
public void setCooldown(Player player, XMaterial material, CooldownSettings cooldownSettings) {
    CooldownPlugin plugin = getCooldownsX();
    CooldownData cooldownData = getData(player);
    return cooldownData.setCooldown(plugin, material, cooldownSettings);
}
```
