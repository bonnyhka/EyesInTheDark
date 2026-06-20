package com.darkmist.eyesinthedark;

import com.darkmist.eyesinthedark.command.DarknessCommands;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = EyesInTheDark.MODID)
public final class DarknessServerEvents {
    private DarknessServerEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        DarknessEncounterManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        DarknessCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            DarknessEncounterManager.clear(player);
        }
    }
}
