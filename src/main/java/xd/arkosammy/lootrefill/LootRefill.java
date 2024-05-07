package xd.arkosammy.lootrefill;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.command.argument.*;
import net.minecraft.loot.LootDataType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.LootCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xd.arkosammy.lootrefill.util.ducks.LootableContainerBlockEntityAccessor;

public class LootRefill implements ModInitializer {

	public static final String MOD_ID = "lootrefill";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final GameRules.Key<GameRules.IntRule> SECONDS_UNTIL_REFILL = GameRuleRegistry.register("secondsUntilLootRefill", GameRules.Category.MISC, GameRuleFactory.createIntRule(1_800));
	public static final GameRules.Key<GameRules.IntRule> MAX_REFILLS = GameRuleRegistry.register("maxAmountOfLootRefills", GameRules.Category.MISC, GameRuleFactory.createIntRule(-1, -1));
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
			return !((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$shouldBeProtected(world);
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

			//Root node
			LiteralCommandNode<ServerCommandSource> lootRefillNode = CommandManager
					.literal(MOD_ID)
					.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
					.build();

			// Add loot table id command node
			LiteralCommandNode<ServerCommandSource> addLootTableIdCommand = CommandManager
					.literal("add_loot_table_id")
					.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
					.build();

			ArgumentCommandNode<ServerCommandSource, Identifier> addLootTableIdArgumentNode = CommandManager
					.argument("loot_table_id", IdentifierArgumentType.identifier())
					.suggests(LootCommand.SUGGESTION_PROVIDER)
					.executes(context -> {
						Identifier lootTableId = IdentifierArgumentType.getIdentifier(context, "loot_table_id");
						ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

						MinecraftServer server = context.getSource().getWorld().getServer();
                        if(!server.getLootManager().getIds(LootDataType.LOOT_TABLES).contains(lootTableId)){
							player.sendMessage(Text.literal(String.format("The loot table id %s does not exist!", lootTableId)).formatted(Formatting.RED));
							return Command.SINGLE_SUCCESS;
						}
						HitResult hitResult = player.raycast(10, 1, false);
						if(!(hitResult instanceof BlockHitResult blockHitResult)) {
							player.sendMessage(Text.literal("You must be looking at a block to use this command!").formatted(Formatting.RED));
							return Command.SINGLE_SUCCESS;
						}
						BlockEntity blockEntity = player.getWorld().getBlockEntity(blockHitResult.getBlockPos());
						if(!(blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
							player.sendMessage(Text.literal("The block you are looking at is not a loot container!").formatted(Formatting.RED));
							return Command.SINGLE_SUCCESS;
						}
						((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$setCachedLootTableId(lootTableId);
						player.sendMessage(Text.literal(String.format("Successfully set the loot table id %s for the loot container block at %s!", lootTableId, blockHitResult.getBlockPos().toShortString())).formatted(Formatting.GREEN));
						return Command.SINGLE_SUCCESS;
					})
					.build();

			ArgumentCommandNode<ServerCommandSource, PosArgument> positionArgumentNode = CommandManager
					.argument("position", BlockPosArgumentType.blockPos())
					.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
					.build();

			ArgumentCommandNode<ServerCommandSource, Identifier> worldArgumentNode = CommandManager
					.argument("world", DimensionArgumentType.dimension())
					.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
					.executes(context -> {
						Identifier lootTableId = IdentifierArgumentType.getIdentifier(context, "loot_table_id");
						ServerPlayerEntity player = context.getSource().getPlayer();
						ServerWorld world = DimensionArgumentType.getDimensionArgument(context, "world");
						MinecraftServer server = world.getServer();
						if(!server.getLootManager().getIds(LootDataType.LOOT_TABLES).contains(lootTableId)){
							sendMessageToPlayer(player, Text.literal(String.format("The loot table id %s does not exist!", lootTableId)).formatted(Formatting.RED));
							return Command.SINGLE_SUCCESS;
						}
						BlockPos blockPos = BlockPosArgumentType.getBlockPos(context, "position");
						BlockEntity blockEntity = world.getBlockEntity(blockPos);
						if(!(blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
							sendMessageToPlayer(player, Text.literal(String.format("The block at %s is not a loot container!", blockPos.toShortString())).formatted(Formatting.RED));
							return Command.SINGLE_SUCCESS;
						}
						((LootableContainerBlockEntityAccessor) lootableContainerBlockEntity).lootrefill$setCachedLootTableId(lootTableId);
						sendMessageToPlayer(player, Text.literal(String.format("Successfully set the loot table id %s for the loot container block at %s!", lootTableId, blockPos.toShortString())).formatted(Formatting.GREEN));
						return Command.SINGLE_SUCCESS;
					})
					.build();

			dispatcher.getRoot().addChild(lootRefillNode);
			lootRefillNode.addChild(addLootTableIdCommand);
			addLootTableIdCommand.addChild(addLootTableIdArgumentNode);
			addLootTableIdArgumentNode.addChild(positionArgumentNode);
			positionArgumentNode.addChild(worldArgumentNode);

		});

	}

	public static long secondsToTicks(long seconds){
		return seconds * SharedConstants.TICKS_PER_SECOND;
	}

	private static void sendMessageToPlayer(@Nullable ServerPlayerEntity player, Text message){
		if(player == null){
			LOGGER.info(message.getString());
		} else {
			player.sendMessage(message, false);
		}
	}

}