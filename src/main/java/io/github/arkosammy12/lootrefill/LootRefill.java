package io.github.arkosammy12.lootrefill;

import io.github.arkosammy12.lootrefill.utils.ConfigUtils;
import io.github.arkosammy12.lootrefill.utils.Utils;
import io.github.arkosammy12.monkeyconfig.base.ConfigManager;
import io.github.arkosammy12.monkeyconfig.builders.ConfigManagerBuilderKt;
import io.github.arkosammy12.monkeyutils.registrars.DefaultConfigRegistrar;
import io.github.arkosammy12.monkeyutils.settings.CommandBooleanSetting;
import io.github.arkosammy12.monkeyutils.settings.CommandNumberSetting;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LootRefill implements ModInitializer {

    public static final String MOD_ID = "lootrefill";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ConfigManager CONFIG_MANAGER = ConfigManagerBuilderKt.tomlConfigManager("lootrefill", FabricLoader.getInstance().getConfigDir().resolve("lootrefill.toml"), (manager) -> {
        manager.setLogger(LOGGER);
        manager.section("preferences", preferences -> {
            ConfigUtils.TIME_UNTIL_REFILLS = preferences.numberSetting("time_until_refill", 1_800L,(timeUntilRefill) -> {
                timeUntilRefill.setComment("(Default = 1800) The time in seconds between each refill. Note that a refill only occurs whenever a loot container is opened.");
                timeUntilRefill.setImplementation(CommandNumberSetting::new);
                return Unit.INSTANCE;
            });
            ConfigUtils.MAX_REFILLS = preferences.numberSetting("max_refills", 1L, (maxRefills) -> {
                maxRefills.setComment("(Default = -1) The max amount of refills allowed per container. -1 is the minimum value and enables unlimited refills. 0 disables loot refills. For a given container, this value can be overridden by setting the individual max refill amount for that container.");
                maxRefills.setMinValue(-1L);
                maxRefills.setImplementation(CommandNumberSetting::new);
                return Unit.INSTANCE;
            });
            ConfigUtils.PER_PLAYER_REFILL_COUNTS = preferences.booleanSetting("per_player_refill_counts", false, perPlayerRefillCounts -> {
                perPlayerRefillCounts.setComment("(Default = false) If enabled, a container will only refill if the amount of times a player has refilled a container is lower than the max refill setting value.");
                perPlayerRefillCounts.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            ConfigUtils.REFILL_ONLY_WHEN_EMPTY = preferences.booleanSetting("refill_only_when_empty", true, (refillOnlyWhenEmpty) -> {
                refillOnlyWhenEmpty.setComment("(Default = true) Prevents refilling containers when they are not fully empty.");
                refillOnlyWhenEmpty.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            ConfigUtils.PROTECT_LOOT_CONTAINERS = preferences.booleanSetting("protect_loot_containers", false, (protectLootContainers) -> {
                protectLootContainers.setComment("(Default = false) If enabled, containers with a set loot table id and a refill count lower than the max refill setting value, or if the max refill amount is set to unlimited, will be unbreakable by players. They can still be broken by indirect means such as explosions.");
                protectLootContainers.setImplementation(CommandBooleanSetting::new);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
        return Unit.INSTANCE;
    });

    @Override
    public void onInitialize() {
        DefaultConfigRegistrar.INSTANCE.registerConfigManager(CONFIG_MANAGER);
        Utils.registerEvents();
        Utils.registerCommands();
    }

}
