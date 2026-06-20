package com.darkmist.eyesinthedark;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, EyesInTheDark.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> APPEAR = register("appear");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_ONE = register("ambient_one");
    public static final DeferredHolder<SoundEvent, SoundEvent> AMBIENT_TWO = register("ambient_two");
    public static final DeferredHolder<SoundEvent, SoundEvent> STARE = register("stare");
    public static final DeferredHolder<SoundEvent, SoundEvent> VANISH = register("vanish");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLEE = register("flee");

    private ModSounds() {
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(EyesInTheDark.id(name)));
    }
}
