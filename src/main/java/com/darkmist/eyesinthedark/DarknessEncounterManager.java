package com.darkmist.eyesinthedark;

import com.darkmist.eyesinthedark.network.ClearEncounterPayload;
import com.darkmist.eyesinthedark.network.StartEncounterPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DarknessEncounterManager {
    private static final Map<UUID, ServerEncounter> ACTIVE = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private DarknessEncounterManager() {
    }

    public static void tick(MinecraftServer server) {
        long gameTime = server.overworld().getGameTime();
        ACTIVE.entrySet().removeIf(entry -> entry.getValue().expiresAt <= gameTime);

        int interval = DarkConfig.NATURAL_CHECK_INTERVAL_TICKS.getAsInt();
        if (!DarkConfig.NATURAL_SPAWNS.get() || interval <= 0 || gameTime % interval != 0L) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tryNaturalSpawn(player, gameTime);
        }
    }

    public static boolean spawnBehind(ServerPlayer player, EncounterMode mode, boolean forced) {
        long gameTime = player.serverLevel().getGameTime();
        if (!forced && ACTIVE.containsKey(player.getUUID())) {
            return false;
        }

        Vec3 position = findSpawnPosition(player, player.getRandom());
        UUID encounterId = UUID.randomUUID();
        int seed = player.getRandom().nextInt();
        int lifetime = Math.max(
                DarkConfig.IDLE_SECOND_SOUND_TICKS.getAsInt()
                        + DarkConfig.IDLE_SECOND_SOUND_DURATION_TICKS.getAsInt()
                        + DarkConfig.VANISH_DURATION_TICKS.getAsInt(),
                DarkConfig.STARE_DURATION_TICKS.getAsInt() + DarkConfig.VANISH_DURATION_TICKS.getAsInt()
        );
        ACTIVE.put(player.getUUID(), new ServerEncounter(encounterId, gameTime + lifetime));
        COOLDOWNS.put(player.getUUID(), gameTime + DarkConfig.SERVER_COOLDOWN_TICKS.getAsInt());
        PacketDistributor.sendToPlayer(player, new StartEncounterPayload(encounterId, position.x, position.y, position.z, mode.id(), seed));
        return true;
    }

    public static void clear(ServerPlayer player) {
        ACTIVE.remove(player.getUUID());
        PacketDistributor.sendToPlayer(player, new ClearEncounterPayload(0));
    }

    public static int activeCount() {
        return ACTIVE.size();
    }

    private static void tryNaturalSpawn(ServerPlayer player, long gameTime) {
        if (player.isSpectator() || player.isCreative()) {
            return;
        }
        if (ACTIVE.containsKey(player.getUUID())) {
            return;
        }
        long cooldownUntil = COOLDOWNS.getOrDefault(player.getUUID(), 0L);
        if (cooldownUntil > gameTime) {
            return;
        }
        if (!isGoodNaturalSpot(player)) {
            return;
        }
        if (player.getRandom().nextDouble() > DarkConfig.NATURAL_SPAWN_CHANCE.get()) {
            return;
        }
        spawnBehind(player, EncounterMode.NATURAL, false);
    }

    private static boolean isGoodNaturalSpot(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        if (player.getY() > DarkConfig.MAX_NATURAL_Y.getAsInt()) {
            return false;
        }
        if (level.canSeeSky(pos)) {
            return false;
        }
        return level.getMaxLocalRawBrightness(pos) <= DarkConfig.MAX_LIGHT_LEVEL.getAsInt();
    }

    private static Vec3 findSpawnPosition(ServerPlayer player, RandomSource random) {
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double minDistance = Math.max(DarkConfig.MIN_SPAWN_DISTANCE.get(), 6.0D);
        double maxDistance = Math.max(minDistance + 0.1D, DarkConfig.MAX_SPAWN_DISTANCE.get());

        for (int attempt = 0; attempt < 20; attempt++) {
            double distance = Mth.lerp(random.nextDouble(), minDistance, maxDistance);
            double angle = Math.atan2(-look.z, -look.x) + Mth.nextDouble(random, -0.75D, 0.75D);
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            double y = eye.y + Mth.nextDouble(random, -0.55D, 0.85D);
            Vec3 candidate = new Vec3(x, y, z);
            if (isRenderableSpace(level, candidate)) {
                return candidate;
            }
        }

        Vec3 fallback = eye.subtract(look.normalize().scale(minDistance));
        return new Vec3(fallback.x, eye.y + 0.15D, fallback.z);
    }

    private static boolean isRenderableSpace(ServerLevel level, Vec3 position) {
        BlockPos pos = BlockPos.containing(position);
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        BlockState above = level.getBlockState(pos.above());
        return state.getCollisionShape(level, pos).isEmpty() && above.getCollisionShape(level, pos.above()).isEmpty();
    }

    private record ServerEncounter(UUID id, long expiresAt) {
    }
}
