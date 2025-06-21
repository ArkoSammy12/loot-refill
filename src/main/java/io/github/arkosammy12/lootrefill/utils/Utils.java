package io.github.arkosammy12.lootrefill.utils;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
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
import net.minecraft.command.argument.*;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
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

import java.util.Optional;

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

            LiteralCommandNode<ServerCommandSource> setLootTableIdNode = CommandManager
                    .literal("setLootTableId")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, RegistryEntry<LootTable>> addLootTableArgumentNode = CommandManager
                    .argument("lootTable", RegistryEntryArgumentType.lootTable(registryAccess))
                    .executes(ctx -> {
                        RegistryEntry<LootTable> lootTableId = RegistryEntryArgumentType.getLootTable(ctx, "lootTable");
                        RegistryKey<LootTable> lootTableKey = lootTableId.getKey().orElseThrow();
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();

                        MinecraftServer server = ctx.getSource().getWorld().getServer();
                        Optional<RegistryEntry.Reference<LootTable>> optionalLootTableReference = server.getReloadableRegistries().createRegistryLookup().getOptionalEntry(lootTableKey);
                        if (optionalLootTableReference.isEmpty()) {
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
                        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$getCustomData().setSavedLootTableId(lootTableKey);
                        player.sendMessage(Text.literal(String.format("Successfully set the loot table id %s for the loot container block at %s!", lootTableId, blockHitResult.getBlockPos().toShortString())).formatted(Formatting.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })
                    .build();

            ArgumentCommandNode<ServerCommandSource, PosArgument> lootTablePositionArgumentNode = CommandManager
                    .argument("position", BlockPosArgumentType.blockPos())
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, Identifier> lootTableWorldArgumentNode = CommandManager
                    .argument("world", DimensionArgumentType.dimension())
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .executes(ctx -> {
                        RegistryEntry<LootTable> lootTableId = RegistryEntryArgumentType.getLootTable(ctx, "lootTable");
                        RegistryKey<LootTable> lootTableKey = lootTableId.getKey().orElseThrow();
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "world");
                        MinecraftServer server = world.getServer();
                        Optional<RegistryEntry.Reference<LootTable>> optionalLootTableReference = server.getReloadableRegistries().createRegistryLookup().getOptionalEntry(lootTableKey);
                        if (optionalLootTableReference.isEmpty()) {
                            sendMessageToPlayer(player, Text.literal(String.format("The loot table id %s does not exist!", lootTableId)).formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        BlockPos blockPos = BlockPosArgumentType.getBlockPos(ctx, "position");
                        BlockEntity blockEntity = world.getBlockEntity(blockPos);
                        if (!(blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
                            sendMessageToPlayer(player, Text.literal(String.format("The block at %s is not a loot container!", blockPos.toShortString())).formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$getCustomData().setSavedLootTableId(lootTableKey);
                        sendMessageToPlayer(player, Text.literal(String.format("Successfully set the loot table id %s for the loot container block at %s!", lootTableId, blockPos.toShortString())).formatted(Formatting.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })
                    .build();

            LiteralCommandNode<ServerCommandSource> setMaxRefillAmountNode = CommandManager
                    .literal("setMaxRefillAmount")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, Long> setMaxRefillAmountArgumentNode = CommandManager
                    .argument("maxRefillAmount", LongArgumentType.longArg(-1))
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
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
                        long individualMaxRefillAmount = LongArgumentType.getLong(ctx, "maxRefillAmount");
                        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$getCustomData().setIndividualRefillAmount(individualMaxRefillAmount);
                        player.sendMessage(Text.literal(String.format("Successfully set the max refill amount %s for the loot container block at %s!", individualMaxRefillAmount, blockHitResult.getBlockPos().toShortString())).formatted(Formatting.GREEN));
                        return Command.SINGLE_SUCCESS;

                    })
                    .build();

            ArgumentCommandNode<ServerCommandSource, PosArgument> maxRefillAmountPositionArgumentNode = CommandManager
                    .argument("position", BlockPosArgumentType.blockPos())
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .build();

            ArgumentCommandNode<ServerCommandSource, Identifier> maxRefillAmountWorldArgumentNode = CommandManager
                    .argument("world", DimensionArgumentType.dimension())
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                    .executes(ctx -> {
                        ServerPlayerEntity player = ctx.getSource().getPlayer();
                        ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "world");
                        BlockPos blockPos = BlockPosArgumentType.getBlockPos(ctx, "position");
                        BlockEntity blockEntity = world.getBlockEntity(blockPos);
                        if (!(blockEntity instanceof LootableContainerBlockEntity lootableContainerBlockEntity)) {
                            sendMessageToPlayer(player, Text.literal(String.format("The block at %s is not a loot container!", blockPos.toShortString())).formatted(Formatting.RED));
                            return Command.SINGLE_SUCCESS;
                        }
                        long individualMaxRefillAmount = LongArgumentType.getLong(ctx, "maxRefillAmount");
                        ((LootableContainerBlockEntityDuck) lootableContainerBlockEntity).lootrefill$getCustomData().setIndividualRefillAmount(individualMaxRefillAmount);
                        sendMessageToPlayer(player, Text.literal(String.format("Successfully set the max refill amount %s for the loot container block at %s!", individualMaxRefillAmount, blockPos.toShortString())).formatted(Formatting.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })
                    .build();

            lootRefillNode.addChild(setLootTableIdNode);
            lootRefillNode.addChild(setMaxRefillAmountNode);

            setLootTableIdNode.addChild(addLootTableArgumentNode);
            addLootTableArgumentNode.addChild(lootTablePositionArgumentNode);
            lootTablePositionArgumentNode.addChild(lootTableWorldArgumentNode);

            setMaxRefillAmountNode.addChild(setMaxRefillAmountArgumentNode);
            setMaxRefillAmountArgumentNode.addChild(maxRefillAmountPositionArgumentNode);
            maxRefillAmountPositionArgumentNode.addChild(maxRefillAmountWorldArgumentNode);

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
