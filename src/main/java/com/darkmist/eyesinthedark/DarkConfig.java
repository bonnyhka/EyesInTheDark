package com.darkmist.eyesinthedark;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DarkConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue NATURAL_SPAWNS = BUILDER
            .comment("Allow the apparition to spawn naturally for players in dark caves.")
            .define("naturalSpawns", true);

    public static final ModConfigSpec.DoubleValue NATURAL_SPAWN_CHANCE = BUILDER
            .comment("Chance per natural spawn check. The default follows the client request: at least 20%.")
            .defineInRange("naturalSpawnChance", 0.20D, 0.20D, 1.0D);

    public static final ModConfigSpec.IntValue NATURAL_CHECK_INTERVAL_TICKS = BUILDER
            .comment("How often the server checks each player for a natural apparition spawn.")
            .defineInRange("naturalCheckIntervalTicks", 2400, 200, 24000);

    public static final ModConfigSpec.IntValue MAX_NATURAL_Y = BUILDER
            .comment("Natural spawns prefer caves and underground areas at or below this Y level.")
            .defineInRange("maxNaturalY", 48, -64, 320);

    public static final ModConfigSpec.IntValue MAX_LIGHT_LEVEL = BUILDER
            .comment("Maximum local raw light level for natural spawns.")
            .defineInRange("maxLightLevel", 5, 0, 15);

    public static final ModConfigSpec.DoubleValue MIN_SPAWN_DISTANCE = BUILDER
            .comment("Minimum distance behind the player.")
            .defineInRange("minSpawnDistance", 8.0D, 6.0D, 32.0D);

    public static final ModConfigSpec.DoubleValue MAX_SPAWN_DISTANCE = BUILDER
            .comment("Maximum distance behind the player.")
            .defineInRange("maxSpawnDistance", 14.0D, 7.0D, 48.0D);

    public static final ModConfigSpec.DoubleValue STARE_MIN_DISTANCE = BUILDER
            .comment("The apparition never approaches closer than this many blocks during the stare sequence.")
            .defineInRange("stareMinDistance", 7.5D, 6.0D, 32.0D);

    public static final ModConfigSpec.DoubleValue TOO_CLOSE_DISTANCE = BUILDER
            .comment("If the player gets this close, the apparition vanishes or flees.")
            .defineInRange("tooCloseDistance", 6.0D, 4.0D, 12.0D);

    public static final ModConfigSpec.DoubleValue STARE_APPROACH_CHANCE = BUILDER
            .comment("Chance that direct eye contact starts the 9 second forced stare approach.")
            .defineInRange("stareApproachChance", 0.55D, 0.0D, 1.0D);

    public static final ModConfigSpec.DoubleValue BLINK_REACTION_CHANCE = BUILDER
            .comment("Chance that direct eye contact triggers a peripheral blink instead of an immediate retreat.")
            .defineInRange("blinkReactionChance", 0.25D, 0.0D, 1.0D);

    public static final ModConfigSpec.IntValue BLINK_DURATION_TICKS = BUILDER
            .comment("How long the apparition jumps between peripheral positions before it flees.")
            .defineInRange("blinkDurationTicks", 46, 12, 200);

    public static final ModConfigSpec.DoubleValue FLEE_SOUND_CHANCE = BUILDER
            .comment("Chance to play the running-away sound when it flees.")
            .defineInRange("fleeSoundChance", 0.45D, 0.0D, 1.0D);

    public static final ModConfigSpec.IntValue IDLE_FIRST_SOUND_TICKS = BUILDER
            .comment("First idle sound timing after appearance. The default waits for the appearance sound to end.")
            .defineInRange("idleFirstSoundTicks", 80, 40, 600);

    public static final ModConfigSpec.IntValue IDLE_SECOND_SOUND_TICKS = BUILDER
            .comment("Second idle sound timing. The default begins after the first idle sound finishes.")
            .defineInRange("idleSecondSoundTicks", 268, 120, 1200);

    public static final ModConfigSpec.IntValue IDLE_SECOND_SOUND_DURATION_TICKS = BUILDER
            .comment("How long to wait for the second idle sound before the vanish sound starts.")
            .defineInRange("idleSecondSoundDurationTicks", 124, 40, 600);

    public static final ModConfigSpec.IntValue STARE_DURATION_TICKS = BUILDER
            .comment("Forced stare duration. The default matches the supplied 9.273 second stare sound.")
            .defineInRange("stareDurationTicks", 186, 40, 600);

    public static final ModConfigSpec.IntValue FLEE_DURATION_TICKS = BUILDER
            .comment("How long the fleeing effect remains active. The default matches the supplied fleeing sound.")
            .defineInRange("fleeDurationTicks", 130, 20, 600);

    public static final ModConfigSpec.IntValue VANISH_DURATION_TICKS = BUILDER
            .comment("Total vanish duration. The default matches the 3.265 second vanish sound.")
            .defineInRange("vanishDurationTicks", 66, 40, 240);

    public static final ModConfigSpec.IntValue VANISH_HOLD_TICKS = BUILDER
            .comment("How long the face remains visible during the main peaks of the vanish sound.")
            .defineInRange("vanishHoldTicks", 32, 0, 200);

    public static final ModConfigSpec.IntValue SERVER_COOLDOWN_TICKS = BUILDER
            .comment("Minimum cooldown after any natural spawn for the same player.")
            .defineInRange("serverCooldownTicks", 6000, 200, 72000);

    public static final ModConfigSpec.BooleanValue COMMAND_FEEDBACK = BUILDER
            .comment("Send chat feedback when test commands are used.")
            .define("commandFeedback", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private DarkConfig() {
    }
}
