package com.SirBlobman.cooldowns.object;

import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CooldownInfo {
    private long goldenAppleExpireTime;
    private long enderPearlExpireTime;
    private final UUID playerId;
    public CooldownInfo(Player player) {
        Objects.requireNonNull(player, "player must not be null!");
        this.playerId = player.getUniqueId();
        this.goldenAppleExpireTime = -1L;
        this.enderPearlExpireTime = -1L;
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(this.playerId);
    }
    
    public long getGoldenAppleExpireTime() {
        return this.goldenAppleExpireTime;
    }
    
    public void setGoldenAppleExpireTime(long expireTime) {
        this.goldenAppleExpireTime = expireTime;
    }
    
    public boolean isGoldenAppleTimerExpired() {
        long systemTime = System.currentTimeMillis();
        long expireTime = getGoldenAppleExpireTime();
        return (systemTime >= expireTime);
    }
    
    public boolean isGoldenAppleTimerUnset() {
        long expireTime = getGoldenAppleExpireTime();
        return (expireTime < 0);
    }
    
    public long getEnderPearlExpireTime() {
        return this.enderPearlExpireTime;
    }
    
    public void setEnderPearlExpireTime(long expireTime) {
        this.enderPearlExpireTime = expireTime;
    }

    public boolean isEnderPearlTimerExpired() {
        long systemTime = System.currentTimeMillis();
        long expireTime = getEnderPearlExpireTime();
        return (systemTime >= expireTime);
    }
    
    public boolean isEnderPearlTimerUnset() {
        long expireTime = getEnderPearlExpireTime();
        return (expireTime < 0);
    }
}