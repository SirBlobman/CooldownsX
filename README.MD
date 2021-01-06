# CooldownsX
A plugin that adds configurable cooldowns to items.

# Maven Information
CooldownsX has a useful API that is hosted on CodeMC
```xml
<repositories>
    <!-- CodeMC Public Repository -->
    <repository>
        <id>codemc-public</id>
        <url>https://repo.codemc.io/repository/maven-public/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.sirblobman.plugin</groupId>
        <artifactId>cooldowns</artifactId>
        <version>3.0.0-SNAPSHOT</version>
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
public long getCooldownExpireMillis(Player player, Material material) {
    CooldownData cooldownData = getData(player);
    return cooldownData.getCooldownExpireTime(material);
}
```
You can also set a custom cooldown using your own plugin:
```java
public void setCooldown(Player player, Material material, long expireMillis) {
    CooldownPlugin plugin = getCooldownsX();
    CooldownData cooldownData = getData(player);
    return cooldownData.setCooldown(plugin, material, expireMillis);
}
```
