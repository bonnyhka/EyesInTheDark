package com.darkmist.eyesinthedark.network;

import com.darkmist.eyesinthedark.EyesInTheDark;
import java.lang.reflect.InvocationTargetException;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(StartEncounterPayload.TYPE, StartEncounterPayload.STREAM_CODEC, ModNetwork::handleStart);
        registrar.playToClient(ClearEncounterPayload.TYPE, ClearEncounterPayload.STREAM_CODEC, ModNetwork::handleClear);
    }

    private static void handleStart(StartEncounterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> dispatch("start", StartEncounterPayload.class, payload));
    }

    private static void handleClear(ClearEncounterPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> dispatch("clear", int.class, payload.reason()));
    }

    private static void dispatch(String method, Class<?> parameterType, Object argument) {
        if (!FMLEnvironment.dist.isClient()) {
            return;
        }
        try {
            Class<?> effects = Class.forName("com.darkmist.eyesinthedark.client.ClientDarknessEffects");
            effects.getMethod(method, parameterType).invoke(null, argument);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            EyesInTheDark.LOGGER.error("Failed to dispatch client darkness payload", exception);
        }
    }
}
