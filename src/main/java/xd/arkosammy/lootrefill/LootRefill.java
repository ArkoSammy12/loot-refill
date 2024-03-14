package xd.arkosammy.lootrefill;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LootRefill implements ModInitializer {

	public static final String MODID = "lootrefill";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final GameRules.Key<GameRules.IntRule> TIME_UNTIL_REFILL = GameRuleRegistry.register("timeInSecondsUntilLootRefill", GameRules.Category.MISC, GameRuleFactory.createIntRule(1800, 1));
	public static final GameRules.Key<GameRules.IntRule> MAX_REFILLS = GameRuleRegistry.register("maximumAmountOfLootRefills", GameRules.Category.MISC, GameRuleFactory.createIntRule(-1, -1));
	public static final GameRules.Key<GameRules.BooleanRule> REFILL_ONLY_WHEN_EMPTY = GameRuleRegistry.register("refillLootOnlyWhenEmpty", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));

	@Override
	public void onInitialize() {


	}
}