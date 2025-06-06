package io.github.arkosammy12.lootrefill.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.arkosammy12.lootrefill.LootRefill;
import io.github.arkosammy12.lootrefill.ducks.LootableContainerBlockEntityDuck;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.SharedConstants;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
import org.apache.commons.lang3.function.Suppliers;
import org.jetbrains.annotations.Nullable;

public final class Utils {

    private Utils() {
        throw new AssertionError();
    }

    public static void registerEvents() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, entity) -> {
            if (world.isClient()) {
                return true;
            }
            if (!state.hasBlockEntity()) {
                return true;
            }
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
                return true;
            }
            return !((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$shouldBeProtected(world);
        });
    }

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandNode<ServerCommandSource> lootRefillNode = dispatcher.getRoot().getChild(LootRefill.MOD_ID) == null ? Suppliers.get(() -> {
                LiteralCommandNode<ServerCommandSource> node = CommandManager
                        .literal(LootRefill.MOD_ID)
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .build();
                dispatcher.getRoot().addChild(node);
                return node;
            }) : dispatcher.getRoot().getChild(LootRefill.MOD_ID);

            LiteralCommandNode<ServerCommandSource> addLootTableNode = CommandManager
                    .literal("addLootTable")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, Identifier> addLootTableArgumentNode = CommandManager
                    .argument("loot_table_registry_key", IdentifierArgumentType.identifier())
                    .suggests(LootCommand.SUGGESTION_PROVIDER)
                    .executes(context -> {
                        Identifier lootTableId = IdentifierArgumentType.getIdentifier(context, "loot_table_registry_key");
                        RegistryKey<LootTable> lootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableId);
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();

                        MinecraftServer server = context.getSource().getWorld().getServer();
                        if (!server.getReloadableRegistries().getIds(RegistryKeys.LOOT_TABLE).contains(lootTableId)){
                            player.sendMessage(Text.literal(String.format("The loot table id %s does not exist!", lootTableId)).formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        HitResult hitResult = player.raycast(10, 1, false);
                        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
                            player.sendMessage(Text.literal("You must be looking at a block to use this command!").formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        BlockEntity blockEntity = player.getWorld().getBlockEntity(blockHitResult.getBlockPos());
                        if (!(blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
                            player.sendMessage(Text.literal("The block you are looking at is not a loot container!").formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$setSavedLootTableKey(lootTableKey);
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
                        RegistryKey<LootTable> lootTableKey = RegistryKey.of(RegistryKeys.LOOT_TABLE, lootTableId);
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        ServerWorld world = DimensionArgumentType.getDimensionArgument(context, "world");
                        MinecraftServer server = world.getServer();
                        if (!server.getReloadableRegistries().getIds(RegistryKeys.LOOT_TABLE).contains(lootTableId)){
                            sendMessageToPlayer(player, Text.literal(String.format("The loot table id %s does not exist!", lootTableId)).formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        BlockPos blockPos = BlockPosArgumentType.getBlockPos(context, "position");
                        BlockEntity blockEntity = world.getBlockEntity(blockPos);
                        if (!(blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
                            sendMessageToPlayer(player, Text.literal(String.format("The block at %s is not a loot container!", blockPos.toShortString())).formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$setSavedLootTableKey(lootTableKey);
                        sendMessageToPlayer(player, Text.literal(String.format("Successfully set the loot table id %s for the loot container block at %s!", lootTableId, blockPos.toShortString())).formatted(Formatting.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })
                    .build();

            lootRefillNode.addChild(addLootTableNode);
            addLootTableNode.addChild(addLootTableArgumentNode);
            addLootTableArgumentNode.addChild(positionArgumentNode);
            positionArgumentNode.addChild(worldArgumentNode);

        });
    }

    public static long secondsToTicks(long seconds) {
        return seconds * SharedConstants.TICKS_PER_SECOND;
    }

    private static void sendMessageToPlayer(@Nullable ServerPlayerEntity player, Text message) {
        if (player == null) {
            LootRefill.LOGGER.info(message.getString());
        } else {
            player.sendMessage(message, false);
        }
    }

}
