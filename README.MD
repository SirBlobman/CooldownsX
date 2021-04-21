# CooldownsX
A plugin that adds configurable cooldowns to items.

# Placeholders
CooldownsX supports PlaceholderAPI and MVdWPlaceholderAPI. Review the table below for placeholder information:  
<table>
    <thead>
        <tr>
            <th>Placeholder</th>
            <th>Description</th>
            <th>MVdWPlaceholderAPI Example</th>
            <th>PlaceholderAPI Example</th>
            <th>Example Output</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>cooldownsx_time_left_&lt;material&gt;</td>
            <td>The amount of seconds left as an integer.</td>
            <td>{cooldownsx_time_left_GOLDEN_APPLE}</td>
            <td>%cooldownsx_time_left_GOLDEN_APPLE%</td>
            <td>5</td>
        </tr>
        <tr>
            <td>cooldownsx_time_left_decimal_&lt;material&gt;</td>
            <td>The amount of seconds left as a decimal with two places.</td>
            <td>{cooldownsx_time_left_decimal_GOLDEN_APPLE}</td>
            <td>%cooldownsx_time_left_decimal_GOLDEN_APPLE%</td>
            <td>5.24</td>
        </tr>
        <tr>
            <td>cooldownsx_undying_time_left</td>
            <td>The amount of seconds left before using a totem of undying as an integer.</td>
            <td>{cooldownsx_undying_time_left}</td>
            <td>%cooldownsx_undying_time_left%</td>
            <td>7</td>
        </tr>
        <tr>
            <td>cooldownsx_undying_time_left_decimal</td>
            <td>The amount of seconds left before using a totem of undying as an decimal with two places.</td>
            <td>{cooldownsx_undying_time_left_decimal}</td>
            <td>%cooldownsx_undying_time_left_decimal%</td>
            <td>7.62</td>
        </tr>
    </tbody>
</table>

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
