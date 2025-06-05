package io.github.arkosammy12.lootrefill;

import com.mojang.serialization.Codec;
import io.github.arkosammy12.lootrefill.utils.ConfigUtils;
import io.github.arkosammy12.lootrefill.utils.Utils;
import io.github.arkosammy12.monkeyconfig.base.ConfigManager;
import io.github.arkosammy12.monkeyconfig.builders.ConfigManagerBuilderKt;
import io.github.arkosammy12.monkeyutils.registrars.DefaultConfigRegistrar;
import io.github.arkosammy12.monkeyutils.settings.CommandBooleanSetting;
import io.github.arkosammy12.monkeyutils.settings.CommandNumberSetting;
import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LootRefill implements ModInitializer {

    public static final String MOD_ID = "lootrefill";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final AttachmentType<RegistryKey<LootTable>> SAVED_LOOT_TABLE_KEY = AttachmentRegistry.createPersistent(Identifier.of(MOD_ID, "container_saved_loot_table_key"), RegistryKey.createCodec(RegistryKeys.LOOT_TABLE));
    public static final AttachmentType<Long> SAVED_LOOT_TABLE_SEED = AttachmentRegistry.create(Identifier.of(MOD_ID, "container_saved_loot_table_seed"), (builder) -> {
        builder.persistent(Codec.LONG);
        builder.initializer(() -> 0L);
    });
    public static final AttachmentType<Long> REFILL_COUNT = AttachmentRegistry.create(Identifier.of(MOD_ID, "container_refill_cont"), (builder) -> {
        builder.persistent(Codec.LONG);
        builder.initializer(() -> 0L);
    });
    public static final AttachmentType<Long> MAX_REFILLS = AttachmentRegistry.create(Identifier.of(MOD_ID, "container_max_refills"), (builder) -> {
        builder.persistent(Codec.LONG);
        builder.initializer(() -> -1L);
    });
    public static final AttachmentType<Long> LAST_SAVED_TIME = AttachmentRegistry.createPersistent(Identifier.of(MOD_ID, "container_last_saved_time"), Codec.LONG);
    public static final AttachmentType<Boolean> LOOTED = AttachmentRegistry.create(Identifier.of(MOD_ID, "container_looted"), (builder) -> {
        builder.persistent(Codec.BOOL);
        builder.initializer(() -> false);
    });
    public static final ConfigManager CONFIG_MANAGER = ConfigManagerBuilderKt.tomlConfigManager("lootrefill", FabricLoader.getInstance().getConfigDir().resolve("lootrefill.toml"), (manager) -> {
        manager.setLogger(LOGGER);
        ConfigUtils.TIME_UNTIL_REFILLS = manager.numberSetting("time_until_refill", 1_800L,(timeUntilRefill) -> {
            timeUntilRefill.setComment("(Default = 1800) The time in seconds between each refill.");
            timeUntilRefill.setImplementation(CommandNumberSetting::new);
            return Unit.INSTANCE;
        });

        ConfigUtils.MAX_REFILLS = manager.numberSetting("max_refills", 1L, (maxRefills) -> {
            maxRefills.setComment("(Default = -1) The max amount of refills allowed per container. -1 is the minimum value and enables unlimited refills. 0 disables loot refills.");
            maxRefills.setMinValue(-1L);
            maxRefills.setImplementation(CommandNumberSetting::new);
            return Unit.INSTANCE;
        });

        ConfigUtils.REFILL_ONLY_WHEN_EMPTY = manager.booleanSetting("refill_only_when_empty", true, (refillOnlyWhenEmpty) -> {

            // TODO: Clarify what happens if disabled
            refillOnlyWhenEmpty.setComment("(Default = true) If enabled, a container's timer will count down once all items have been removed, and allow refilling once done.");
            refillOnlyWhenEmpty.setImplementation(CommandBooleanSetting::new);
            return Unit.INSTANCE;
        });

        ConfigUtils.PROTECT_LOOT_CONTAINERS = manager.booleanSetting("protect_loot_containers", false, (protectLootContainers) -> {
            protectLootContainers.setComment("(Default = false) If enabled, loot containers will be unable to be broken by players directly. They can still be broken by indirect means such as explosions.");
            protectLootContainers.setImplementation(CommandBooleanSetting::new);
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
