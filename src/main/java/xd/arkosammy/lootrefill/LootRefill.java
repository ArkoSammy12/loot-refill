package xd.arkosammy.lootrefill;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LootRefill implements ModInitializer {

	public static final String MODID = "lootrefill";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final GameRules.Key<GameRules.IntRule> SECONDS_UNTIL_REFILL = GameRuleRegistry.register("secondsUntilLootRefill", GameRules.Category.MISC, GameRuleFactory.createIntRule(1_800));
	public static final GameRules.Key<GameRules.IntRule> MAX_REFILLS = GameRuleRegistry.register("maximumAmountOfLootRefills", GameRules.Category.MISC, GameRuleFactory.createIntRule(-1, -1));
	public static final GameRules.Key<GameRules.BooleanRule> REFILL_ONLY_WHEN_EMPTY = GameRuleRegistry.register("refillLootOnlyWhenEmpty", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
	public static final GameRules.Key<GameRules.BooleanRule> PROTECT_LOOT_CONTAINERS = GameRuleRegistry.register("protectLootContainers", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

	@Override
	public void onInitialize() {

		PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
			if(world.isClient()) {
				return true;
			}
			if(!world.getGameRules().getBoolean(PROTECT_LOOT_CONTAINERS)){
				return true;
			}
			if(!state.hasBlockEntity()){
				return true;
			}
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if(!(blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
				return true;
			}
			return lootableContainerBlockEntity.getLootTableId() == null;
		});

	}

	public static long secondsToTicks(long seconds){
		return seconds * SharedConstants.TICKS_PER_SECOND;
	}

}