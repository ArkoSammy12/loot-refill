package io.github.arkosammy12.lootrefill.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.arkosammy12.lootrefill.LootRefill;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LootableContainerCustomData {

    public static final Codec<LootableContainerCustomData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).fieldOf("saved_loot_table_registry_key").forGetter(LootableContainerCustomData::getSavedLootTableKey),
            Codec.LONG.fieldOf("saved_loot_table_seed").forGetter(LootableContainerCustomData::getSavedLootTableSeed),
            Codec.LONG.fieldOf("global_refill_count").forGetter(LootableContainerCustomData::getGlobalRefillCount),
            Codec.LONG.fieldOf("global_max_refill_amount").forGetter(LootableContainerCustomData::getGlobalMaxRefillAmount),
            Codec.LONG.fieldOf("individual_refill_amount").forGetter(LootableContainerCustomData::getIndividualRefillAmount),
            Codec.LONG.fieldOf("last_refilled_time").forGetter(LootableContainerCustomData::getLastRefilledTime),
            Codec.BOOL.fieldOf("looted").forGetter(LootableContainerCustomData::isLooted),
            Codec.unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), Codec.LONG).fieldOf("player_refill_count").forGetter(LootableContainerCustomData::getPlayerRefillCount)
    ).apply(instance, LootableContainerCustomData::new));

    public static final AttachmentType<LootableContainerCustomData> ATTACHMENT = AttachmentRegistry.create(Identifier.of(LootRefill.MOD_ID, "lootable_container_custom_data"), builder -> {
        builder.persistent(CODEC);
        builder.initializer(LootableContainerCustomData::new);
    });

    private RegistryKey<LootTable> savedLootTableKey;
    private long savedLootTableSeed;
    private long globalRefillCount ;
    private long individualRefillAmount;
    private long globalMaxRefillAmount;
    private long lastRefilledTime;
    private boolean looted = false;
    private final Map<UUID, Long> playerRefillCount;

    public LootableContainerCustomData(RegistryKey<LootTable> savedLootTableKey, long savedLootTableSeed, long globalRefillCount, long individualRefillAmount, long globalMaxRefillAmount, long lastRefilledTime, boolean looted, Map<UUID, Long> playerRefillCount) {
        this.savedLootTableKey = savedLootTableKey;
        this.savedLootTableSeed = savedLootTableSeed;
        this.globalRefillCount = globalRefillCount;
        this.individualRefillAmount = individualRefillAmount;
        this.globalMaxRefillAmount = globalMaxRefillAmount;
        this.looted = looted;
        this.playerRefillCount = playerRefillCount;
    }

    public LootableContainerCustomData() {
        this.savedLootTableSeed = 0;
        this.globalRefillCount = 0;
        this.globalMaxRefillAmount = -1;
        this.individualRefillAmount = -1;
        this.playerRefillCount = new HashMap<>();
    }

    public void setSavedLootTableKey(RegistryKey<LootTable> savedLootTableKey) {
        this.savedLootTableKey = savedLootTableKey;
    }

    public RegistryKey<LootTable> getSavedLootTableKey() {
        return this.savedLootTableKey;
    }

    public void setSavedLootTableSeed(long savedLootTableSeed) {
        this.savedLootTableSeed = savedLootTableSeed;
    }

    public long getSavedLootTableSeed() {
        return this.savedLootTableSeed;
    }

    public void setGlobalRefillCont(long globalRefillCont) {
        this.globalRefillCount = globalRefillCont;
    }

    public long getGlobalRefillCount() {
        return this.globalRefillCount;
    }

    public void setIndividualRefillAmount(long individualRefillAmount) {
        this.individualRefillAmount = individualRefillAmount;
    }

    public long getIndividualRefillAmount() {
        return this.individualRefillAmount;
    }

    public void setGlobalMaxRefillAmount(long globalMaxRefillAmount) {
        this.globalMaxRefillAmount = globalMaxRefillAmount;
    }

    public long getGlobalMaxRefillAmount() {
        return this.globalMaxRefillAmount;
    }

    public void setLastRefilledTime(long lastRefilledTime) {
        this.lastRefilledTime = lastRefilledTime;
    }

    public long getLastRefilledTime() {
        return this.lastRefilledTime;
    }

    public void setLooted(boolean looted) {
        this.looted = looted;
    }

    public boolean isLooted() {
        return this.looted;
    }

    public void incrementRefillCountForPlayer(UUID uuid) {
        for (Map.Entry<UUID, Long> entry : this.playerRefillCount.entrySet()) {
            UUID key = entry.getKey();
            if (key.equals(uuid)) {
                entry.setValue(entry.getValue() + 1);
                return;
            }
        }
        this.playerRefillCount.put(uuid, 1L);
    }

    public long getRefillCountForPlayer(UUID uuid) {
        for (Map.Entry<UUID, Long> entry : this.playerRefillCount.entrySet()) {
            UUID key = entry.getKey();
            if (key.equals(uuid)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    private Map<UUID, Long> getPlayerRefillCount() {
        return this.playerRefillCount;
    }

}
