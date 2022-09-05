package com.github.sirblobman.cooldowns.api.dictionary;

public interface IDictionary<E extends Enum<E>> {
    String get(E key);

    void set(E key, String value);

    void saveConfiguration();

    void reloadConfiguration();
}
