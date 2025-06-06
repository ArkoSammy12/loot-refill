package io.github.arkosammy12.lootrefill.utils;

import io.github.arkosammy12.monkeyconfig.util.ElementPath;

public final class ConfigUtils {

    private ConfigUtils() {
        throw new AssertionError();
    }

    public static ElementPath TIME_UNTIL_REFILLS;
    public static ElementPath MAX_REFILLS;
    public static ElementPath PER_PLAYER_REFILL_COUNTS;
    public static ElementPath REFILL_ONLY_WHEN_EMPTY;
    public static ElementPath PROTECT_LOOT_CONTAINERS;

}
