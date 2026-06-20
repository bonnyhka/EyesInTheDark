package com.darkmist.eyesinthedark;

import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.RandomSource;

public enum EncounterMode {
    NATURAL(0, "natural"),
    IDLE(1, "idle"),
    STARE(2, "stare"),
    FLEE(3, "flee"),
    VANISH(4, "vanish"),
    BLINK(5, "blink");

    public static final String[] COMMAND_NAMES = {"natural", "idle", "stare", "flee", "vanish", "blink"};

    private final int id;
    private final String commandName;

    EncounterMode(int id, String commandName) {
        this.id = id;
        this.commandName = commandName;
    }

    public int id() {
        return this.id;
    }

    public String commandName() {
        return this.commandName;
    }

    public static EncounterMode byId(int id) {
        for (EncounterMode mode : values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return NATURAL;
    }

    public static Optional<EncounterMode> fromCommand(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        for (EncounterMode mode : values()) {
            if (mode.commandName.equals(normalized)) {
                return Optional.of(mode);
            }
        }
        return Optional.empty();
    }

    public static EncounterMode randomReaction(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.32F) {
            return VANISH;
        }
        if (roll < 0.57F) {
            return FLEE;
        }
        if (roll < 0.76F) {
            return BLINK;
        }
        return STARE;
    }
}
