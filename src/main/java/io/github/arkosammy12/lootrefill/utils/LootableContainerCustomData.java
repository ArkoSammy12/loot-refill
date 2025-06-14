package io.github.arkosammy12.lootrefill.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.arkosammy12.lootrefill.LootRefill;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class LootableContainerCustomData {

    public static final Codec<LootableContainerCustomData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryKey.createCodec(RegistryKeys.LOOT_TABLE).optionalFieldOf("saved_loot_table_id").forGetter(obj -> Optional.ofNullable(obj.getSavedLootTableId())),
            Codec.LONG.fieldOf("saved_loot_table_seed").forGetter(LootableContainerCustomData::getSavedLootTableSeed),
            Codec.LONG.fieldOf("global_refill_count").forGetter(LootableContainerCustomData::getGlobalRefillCount),
            Codec.LONG.fieldOf("global_max_refill_amount").forGetter(LootableContainerCustomData::getGlobalMaxRefillAmount),
            Codec.LONG.fieldOf("individual_refill_amount").forGetter(LootableContainerCustomData::getIndividualRefillAmount),
            Codec.LONG.fieldOf("last_refilled_time").forGetter(LootableContainerCustomData::getLastRefilledTime),
            Codec.BOOL.fieldOf("looted").forGetter(LootableContainerCustomData::isLooted),
            Codec.unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), Codec.LONG).fieldOf("player_refill_count").forGetter(LootableContainerCustomData::getPlayerRefillCount)
    ).apply(instance, (optionalSavedLootTableId, savedLootTableSeed, globalRefillCount, globalMaxRefillAmount, individualRefillAmount, lastRefilledTime, looted, playerRefillCount) ->
            new LootableContainerCustomData(optionalSavedLootTableId.orElse(null), savedLootTableSeed, globalRefillCount, globalMaxRefillAmount, individualRefillAmount, lastRefilledTime, looted, playerRefillCount)));

    public static final Identifier ATTACHMENT_IDENTIFIER = Identifier.of(LootRefill.MOD_ID, "lootable_container_custom_data");

    @Nullable
    private RegistryKey<LootTable> savedLootTableId;
    private long savedLootTableSeed = 0;
    private long globalRefillCount = 0;
    private long individualRefillAmount = -1;
    private long globalMaxRefillAmount = -1;
    private long lastRefilledTime = 0;
    private boolean looted = false;
    private final Map<UUID, Long> playerRefillCount = new HashMap<>();

    public LootableContainerCustomData(@Nullable RegistryKey<LootTable> savedLootTableId, long savedLootTableSeed, long globalRefillCount, long individualRefillAmount, long globalMaxRefillAmount, long lastRefilledTime, boolean looted, Map<UUID, Long> playerRefillCount) {
        this.savedLootTableId = savedLootTableId;
        this.savedLootTableSeed = savedLootTableSeed;
        this.globalRefillCount = globalRefillCount;
        this.individualRefillAmount = individualRefillAmount;
        this.globalMaxRefillAmount = globalMaxRefillAmount;
        this.lastRefilledTime = lastRefilledTime;
        this.looted = looted;
        this.playerRefillCount.putAll(playerRefillCount);
    }

    public LootableContainerCustomData() {

    }

    public void setSavedLootTableId(@Nullable RegistryKey<LootTable> savedLootTableId) {
        this.savedLootTableId = savedLootTableId;
    }

    @Nullable
    public RegistryKey<LootTable> getSavedLootTableId() {
        return this.savedLootTableId;
    }

    public void setSavedLootTableSeed(long savedLootTableSeed) {
        this.savedLootTableSeed = savedLootTableSeed;
    }

    public long getSavedLootTableSeed() {
        return this.savedLootTableSeed;
    }

    public void setGlobalRefillCount(long globalRefillCont) {
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

    public void incrementRefillCountForPlayer(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();
        for (Map.Entry<UUID, Long> entry : this.playerRefillCount.entrySet()) {
            UUID key = entry.getKey();
            if (key.equals(playerUuid)) {
                entry.setValue(entry.getValue() + 1);
                return;
            }
        }
        this.playerRefillCount.put(playerUuid, 1L);
    }

    public long getRefillCountForPlayer(ServerPlayerEntity player) {
        UUID playerUuid = player.getUuid();
        for (Map.Entry<UUID, Long> entry : this.playerRefillCount.entrySet()) {
            UUID key = entry.getKey();
            if (key.equals(playerUuid)) {
                return entry.getValue();
            }
        }
        return -1;
    }

    private Map<UUID, Long> getPlayerRefillCount() {
        return this.playerRefillCount;
    }

}
