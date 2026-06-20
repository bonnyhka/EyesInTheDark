package com.darkmist.eyesinthedark.command;

import com.darkmist.eyesinthedark.DarkConfig;
import com.darkmist.eyesinthedark.DarknessEncounterManager;
import com.darkmist.eyesinthedark.EncounterMode;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class DarknessCommands {
    private static final DynamicCommandExceptionType INVALID_MODE = new DynamicCommandExceptionType(
            value -> Component.literal("Unknown apparition mode: " + value)
    );

    private DarknessCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("eyesinthedark")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("spawn")
                        .executes(ctx -> spawn(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException()), EncounterMode.NATURAL))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> spawn(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), EncounterMode.NATURAL))
                                .then(Commands.argument("mode", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(EncounterMode.COMMAND_NAMES, builder))
                                        .executes(ctx -> spawn(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), parseMode(ctx.getArgument("mode", String.class)))))))
                .then(Commands.literal("force")
                        .executes(ctx -> spawn(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException()), EncounterMode.STARE))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> spawn(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), EncounterMode.STARE))))
                .then(Commands.literal("blink")
                        .executes(ctx -> spawn(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException()), EncounterMode.BLINK))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> spawn(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets"), EncounterMode.BLINK))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clear(ctx.getSource(), List.of(ctx.getSource().getPlayerOrException())))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> clear(ctx.getSource(), EntityArgument.getPlayers(ctx, "targets")))))
                .then(Commands.literal("chance")
                        .executes(ctx -> showChance(ctx.getSource()))
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.20D, 1.0D))
                                .executes(ctx -> setChance(ctx.getSource(), DoubleArgumentType.getDouble(ctx, "value")))))
                .then(Commands.literal("enabled")
                        .executes(ctx -> showEnabled(ctx.getSource()))
                        .then(Commands.argument("value", BoolArgumentType.bool())
                                .executes(ctx -> setEnabled(ctx.getSource(), BoolArgumentType.getBool(ctx, "value")))))
                .then(Commands.literal("interval")
                        .executes(ctx -> showInterval(ctx.getSource()))
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(200, 24000))
                                .executes(ctx -> setInterval(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "ticks")))))
                .then(Commands.literal("cooldown")
                        .executes(ctx -> showCooldown(ctx.getSource()))
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(200, 72000))
                                .executes(ctx -> setCooldown(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "ticks")))))
                .then(Commands.literal("status")
                        .executes(ctx -> showStatus(ctx.getSource()))));
    }

    private static EncounterMode parseMode(String mode) throws CommandSyntaxException {
        return EncounterMode.fromCommand(mode).orElseThrow(() -> INVALID_MODE.create(mode));
    }

    private static int spawn(CommandSourceStack source, Collection<ServerPlayer> targets, EncounterMode mode) {
        int spawned = 0;
        for (ServerPlayer player : targets) {
            if (DarknessEncounterManager.spawnBehind(player, mode, true)) {
                spawned++;
            }
        }
        feedback(source, Component.literal("Eyes in the Dark: spawned behind " + spawned + " player(s) in " + mode.commandName() + " mode."));
        return spawned;
    }

    private static int clear(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            DarknessEncounterManager.clear(player);
        }
        feedback(source, Component.literal("Eyes in the Dark: cleared apparition for " + targets.size() + " player(s)."));
        return targets.size();
    }

    private static int showChance(CommandSourceStack source) {
        feedback(source, Component.literal("Eyes in the Dark spawn chance: " + Math.round(DarkConfig.NATURAL_SPAWN_CHANCE.get() * 100.0D) + "%"));
        return (int)Math.round(DarkConfig.NATURAL_SPAWN_CHANCE.get() * 100.0D);
    }

    private static int setChance(CommandSourceStack source, double value) {
        DarkConfig.NATURAL_SPAWN_CHANCE.set(value);
        feedback(source, Component.literal("Eyes in the Dark spawn chance set to " + Math.round(value * 100.0D) + "%"));
        return (int)Math.round(value * 100.0D);
    }

    private static int showEnabled(CommandSourceStack source) {
        feedback(source, Component.literal("Eyes in the Dark natural spawns: " + DarkConfig.NATURAL_SPAWNS.get()));
        return DarkConfig.NATURAL_SPAWNS.get() ? 1 : 0;
    }

    private static int setEnabled(CommandSourceStack source, boolean value) {
        DarkConfig.NATURAL_SPAWNS.set(value);
        feedback(source, Component.literal("Eyes in the Dark natural spawns set to " + value));
        return value ? 1 : 0;
    }

    private static int showInterval(CommandSourceStack source) {
        int ticks = DarkConfig.NATURAL_CHECK_INTERVAL_TICKS.getAsInt();
        feedback(source, Component.literal("Eyes in the Dark check interval: " + ticks + " ticks (" + formatSeconds(ticks) + " seconds)."));
        return ticks;
    }

    private static int setInterval(CommandSourceStack source, int ticks) {
        DarkConfig.NATURAL_CHECK_INTERVAL_TICKS.set(ticks);
        feedback(source, Component.literal("Eyes in the Dark check interval set to " + ticks + " ticks."));
        return ticks;
    }

    private static int showCooldown(CommandSourceStack source) {
        int ticks = DarkConfig.SERVER_COOLDOWN_TICKS.getAsInt();
        feedback(source, Component.literal("Eyes in the Dark cooldown: " + ticks + " ticks (" + formatSeconds(ticks) + " seconds)."));
        return ticks;
    }

    private static int setCooldown(CommandSourceStack source, int ticks) {
        DarkConfig.SERVER_COOLDOWN_TICKS.set(ticks);
        feedback(source, Component.literal("Eyes in the Dark cooldown set to " + ticks + " ticks."));
        return ticks;
    }

    private static int showStatus(CommandSourceStack source) {
        String enabled = DarkConfig.NATURAL_SPAWNS.get() ? "enabled" : "disabled";
        int chance = (int)Math.round(DarkConfig.NATURAL_SPAWN_CHANCE.get() * 100.0D);
        feedback(source, Component.literal("Eyes in the Dark: auto=" + enabled
                + ", chance=" + chance + "%"
                + ", interval=" + DarkConfig.NATURAL_CHECK_INTERVAL_TICKS.getAsInt()
                + ", cooldown=" + DarkConfig.SERVER_COOLDOWN_TICKS.getAsInt()
                + ", active=" + DarknessEncounterManager.activeCount()));
        return DarknessEncounterManager.activeCount();
    }

    private static String formatSeconds(int ticks) {
        return String.format(java.util.Locale.ROOT, "%.1f", ticks / 20.0D);
    }

    private static void feedback(CommandSourceStack source, Component message) {
        if (DarkConfig.COMMAND_FEEDBACK.get()) {
            source.sendSuccess(() -> message, true);
        }
    }
}
