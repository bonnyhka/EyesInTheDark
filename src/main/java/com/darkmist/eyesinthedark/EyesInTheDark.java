package com.darkmist.eyesinthedark;

import com.darkmist.eyesinthedark.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(EyesInTheDark.MODID)
public final class EyesInTheDark {
    public static final String MODID = "eyesinthedark";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EyesInTheDark(IEventBus modEventBus, ModContainer modContainer) {
        ModSounds.SOUNDS.register(modEventBus);
        modEventBus.addListener(ModNetwork::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, DarkConfig.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
