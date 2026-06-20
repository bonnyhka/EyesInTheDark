package com.darkmist.eyesinthedark.client;

import com.darkmist.eyesinthedark.DarkConfig;
import com.darkmist.eyesinthedark.EncounterMode;
import com.darkmist.eyesinthedark.EyesInTheDark;
import com.darkmist.eyesinthedark.ModSounds;
import com.darkmist.eyesinthedark.network.StartEncounterPayload;
import java.util.Random;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = EyesInTheDark.MODID, value = Dist.CLIENT)
public final class ClientDarknessEffects {
    private static final double DIRECT_LOOK_DOT = Math.cos(Math.toRadians(11.0D));
    private static final double VISIBLE_DOT = Math.cos(Math.toRadians(82.0D));
    private static ClientEncounter active;

    private ClientDarknessEffects() {
    }

    public static void start(StartEncounterPayload payload) {
        active = new ClientEncounter(payload.encounterId(), new Vec3(payload.x(), payload.y(), payload.z()), EncounterMode.byId(payload.mode()), payload.seed());
        playConfigured(ModSounds.APPEAR.get(), "appear", SoundEvents.AMBIENT_CAVE.value(), 0.65F, 1.0F);
    }

    public static void clear(int reason) {
        active = null;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || active == null) {
            return;
        }
        active.tick(minecraft, minecraft.player);
        if (active.done) {
            active = null;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || active == null) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        active.render(event.getGuiGraphics(), minecraft, minecraft.player, partialTick);
    }

    private static void playLocal(SoundEvent sound, float volume, float pitch) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.player != null) {
            minecraft.level.playLocalSound(minecraft.player, sound, SoundSource.AMBIENT, volume, pitch);
        }
    }

    private static final class ClientEncounter {
        private final UUID id;
        private final Vec3 origin;
        private final EncounterMode initialMode;
        private final Random random;
        private Vec3 position;
        private State state = State.WATCHING;
        private int age;
        private int stateAge;
        private int idleSounds;
        private boolean done;
        private boolean playedFleeSound;

        private ClientEncounter(UUID id, Vec3 origin, EncounterMode initialMode, int seed) {
            this.id = id;
            this.origin = origin;
            this.position = origin;
            this.initialMode = initialMode;
            this.random = new Random(seed ^ id.getLeastSignificantBits());
            if (initialMode == EncounterMode.STARE) {
                this.state = State.STARE_LOCK;
                playStareStart();
            } else if (initialMode == EncounterMode.BLINK) {
                this.state = State.BLINK;
            } else if (initialMode == EncounterMode.FLEE) {
                this.state = State.FLEE;
            } else if (initialMode == EncounterMode.VANISH) {
                this.state = State.VANISH;
            }
        }

        private void tick(Minecraft minecraft, LocalPlayer player) {
            this.age++;
            this.stateAge++;
            double distance = player.getEyePosition().distanceTo(this.position);

            if (this.state == State.WATCHING) {
                tickWatching(player, distance);
            } else if (this.state == State.STARE_LOCK) {
                tickStareLock(player);
            } else if (this.state == State.BLINK) {
                tickBlink(player);
            } else if (this.state == State.FLEE) {
                tickFlee(player);
            } else if (this.state == State.VANISH) {
                if (this.stateAge == 1) {
                    playConfigured(ModSounds.VANISH.get(), "vanish", SoundEvents.SCULK_SHRIEKER_SHRIEK, 0.45F, 1.0F);
                }
                if (this.stateAge >= DarkConfig.VANISH_DURATION_TICKS.getAsInt()) {
                    this.done = true;
                }
            }
        }

        private void tickWatching(LocalPlayer player, double distance) {
            if (this.initialMode == EncounterMode.IDLE || this.initialMode == EncounterMode.NATURAL) {
                int first = DarkConfig.IDLE_FIRST_SOUND_TICKS.getAsInt();
                int second = DarkConfig.IDLE_SECOND_SOUND_TICKS.getAsInt();
                if (this.age == first) {
                    this.idleSounds++;
                    playConfigured(ModSounds.AMBIENT_ONE.get(), "ambient_one", SoundEvents.WARDEN_AMBIENT, 0.55F, 1.0F);
                }
                if (this.age == second) {
                    this.idleSounds++;
                    playConfigured(ModSounds.AMBIENT_TWO.get(), "ambient_two", SoundEvents.WARDEN_TENDRIL_CLICKS, 0.50F, 1.0F);
                }
                if (this.age >= second + DarkConfig.IDLE_SECOND_SOUND_DURATION_TICKS.getAsInt()) {
                    switchState(State.VANISH);
                    return;
                }
            }

            if (distance <= Math.max(DarkConfig.TOO_CLOSE_DISTANCE.get(), 6.0D)) {
                switchState(this.random.nextBoolean() ? State.VANISH : State.FLEE);
                return;
            }

            if (isDirectlyLookedAt(player)) {
                double roll = this.random.nextDouble();
                if (roll <= DarkConfig.STARE_APPROACH_CHANCE.get()) {
                    switchState(State.STARE_LOCK);
                    playStareStart();
                } else if (this.random.nextDouble() <= DarkConfig.BLINK_REACTION_CHANCE.get()) {
                    switchState(State.BLINK);
                } else {
                    switchState(this.random.nextBoolean() ? State.FLEE : State.VANISH);
                }
            }
        }

        private void tickStareLock(LocalPlayer player) {
            int duration = DarkConfig.STARE_DURATION_TICKS.getAsInt();
            double progress = Mth.clamp(this.stateAge / (double)duration, 0.0D, 1.0D);
            Vec3 eyes = player.getEyePosition();
            Vec3 direction = this.origin.subtract(eyes);
            if (direction.lengthSqr() < 0.0001D) {
                direction = player.getLookAngle();
            }
            direction = direction.normalize();
            double minimumDistance = Math.max(DarkConfig.STARE_MIN_DISTANCE.get(), 6.0D);
            double startDistance = Math.max(eyes.distanceTo(this.origin), minimumDistance);
            double currentDistance = Mth.lerp(easeIn(progress), startDistance, minimumDistance);
            this.position = eyes.add(direction.scale(currentDistance));

            float peak = starePeakIntensity();
            turnCameraToward(player, this.position, 0.24F + peak * 0.20F);
            if (peak > 0.0F) {
                applyCameraTremor(player, peak);
            }
            if (this.stateAge >= duration) {
                switchState(State.VANISH);
            }
        }

        private void tickBlink(LocalPlayer player) {
            if (this.stateAge == 1 || this.stateAge == 15 || this.stateAge == 29) {
                moveToPeripheralBlink(player);
            }
            if (this.stateAge >= DarkConfig.BLINK_DURATION_TICKS.getAsInt()) {
                switchState(State.FLEE);
            }
        }

        private void tickFlee(LocalPlayer player) {
            if (!this.playedFleeSound) {
                this.playedFleeSound = true;
                if (this.random.nextDouble() <= DarkConfig.FLEE_SOUND_CHANCE.get()) {
                    playConfigured(ModSounds.FLEE.get(), "flee", SoundEvents.WARDEN_STEP, 0.95F, 1.0F);
                }
            }
            Vec3 away = this.position.subtract(player.getEyePosition());
            if (away.lengthSqr() < 0.0001D) {
                away = player.getLookAngle().reverse();
            }
            this.position = this.position.add(away.normalize().scale(0.42D + this.stateAge * 0.015D));
            if (this.stateAge >= DarkConfig.FLEE_DURATION_TICKS.getAsInt()) {
                this.done = true;
            }
        }

        private void render(GuiGraphics graphics, Minecraft minecraft, LocalPlayer player, float partialTick) {
            ScreenPoint point = project(player, this.position, graphics.guiWidth(), graphics.guiHeight());
            float visibility = point.visibility;
            if (this.state == State.STARE_LOCK || this.state == State.BLINK) {
                visibility = 1.0F;
            }
            if (visibility <= 0.0F) {
                return;
            }

            float scareIntensity = 0.0F;
            if (this.state == State.STARE_LOCK) {
                scareIntensity = 0.65F + starePeakIntensity();
                renderVhsAndVignette(graphics, point, partialTick, scareIntensity);
            } else if (this.state == State.BLINK) {
                scareIntensity = 0.80F + 0.20F * (float)Math.sin(this.stateAge * 2.7F);
                renderVhsAndVignette(graphics, point, partialTick, scareIntensity);
            }

            float stateFade = 1.0F;
            if (this.state == State.VANISH) {
                int fadeDuration = DarkConfig.VANISH_DURATION_TICKS.getAsInt();
                int holdDuration = Math.min(DarkConfig.VANISH_HOLD_TICKS.getAsInt(), fadeDuration - 1);
                stateFade = 1.0F - Mth.clamp((this.stateAge - holdDuration) / (float)(fadeDuration - holdDuration), 0.0F, 1.0F);
            } else if (this.state == State.FLEE) {
                stateFade = 1.0F - Mth.clamp(this.stateAge / (float)DarkConfig.FLEE_DURATION_TICKS.getAsInt(), 0.0F, 1.0F);
            }

            float flicker = 0.82F + 0.18F * (float)Math.sin((this.age + partialTick) * 0.55F + this.random.nextInt(3));
            if (this.state == State.BLINK) {
                flicker = this.stateAge % 4 == 0 ? 0.12F : 1.0F;
            }
            if (scareIntensity > 0.95F) {
                int offset = Math.round((scareIntensity - 0.95F) * 42.0F);
                drawFace(graphics, point.x - offset, point.y + offset / 2, point.scale, visibility * stateFade * 0.34F);
                drawFace(graphics, point.x + offset, point.y - offset / 2, point.scale, visibility * stateFade * 0.26F);
            }
            drawFace(graphics, point.x, point.y, point.scale, visibility * stateFade * flicker);
        }

        private boolean isDirectlyLookedAt(LocalPlayer player) {
            Vec3 toEyes = this.position.subtract(player.getEyePosition());
            if (toEyes.lengthSqr() < 0.0001D) {
                return true;
            }
            return player.getLookAngle().normalize().dot(toEyes.normalize()) >= DIRECT_LOOK_DOT;
        }

        private void switchState(State next) {
            this.state = next;
            this.stateAge = 0;
        }

        private void moveToPeripheralBlink(LocalPlayer player) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 side = new Vec3(-look.z, 0.0D, look.x).normalize();
            double sign = this.random.nextBoolean() ? 1.0D : -1.0D;
            Vec3 direction = look.scale(0.38D).add(side.scale(0.93D * sign)).normalize();
            double distance = Math.max(DarkConfig.STARE_MIN_DISTANCE.get() + 0.5D, 8.5D + this.random.nextDouble() * 2.5D);
            this.position = player.getEyePosition().add(direction.scale(distance)).add(0.0D, this.random.nextDouble() * 1.1D - 0.5D, 0.0D);
        }

        private float starePeakIntensity() {
            return 1.0F - Mth.clamp(Math.abs(this.stateAge - 108) / 16.0F, 0.0F, 1.0F);
        }

        private void playStareStart() {
            playConfigured(ModSounds.STARE.get(), "stare", SoundEvents.SCULK_SHRIEKER_SHRIEK, 0.75F, 1.0F);
        }
    }

    private static void playConfigured(SoundEvent configured, String fileName, SoundEvent fallback, float volume, float pitch) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getResourceManager().getResource(EyesInTheDark.id("sounds/" + fileName + ".ogg")).isPresent()) {
            playLocal(configured, volume, pitch);
        } else {
            playLocal(fallback, volume, pitch);
        }
    }

    private static ScreenPoint project(LocalPlayer player, Vec3 target, int width, int height) {
        Vec3 eye = player.getEyePosition();
        Vec3 relative = target.subtract(eye);
        double distance = Math.max(relative.length(), 0.001D);
        Vec3 direction = relative.normalize();
        double dot = player.getLookAngle().normalize().dot(direction);
        if (dot < VISIBLE_DOT) {
            return new ScreenPoint(width / 2, height / 2, 1.0F, 0.0F);
        }

        double yawTo = Math.toDegrees(Math.atan2(relative.z, relative.x)) - 90.0D;
        double horizontal = Math.sqrt(relative.x * relative.x + relative.z * relative.z);
        double pitchTo = -Math.toDegrees(Math.atan2(relative.y, horizontal));
        double yawDelta = Mth.wrapDegrees(yawTo - player.getYRot());
        double pitchDelta = pitchTo - player.getXRot();
        double horizontalFov = 104.0D;
        double verticalFov = 70.0D;
        int x = (int)Math.round(width / 2.0D + yawDelta / (horizontalFov * 0.5D) * width / 2.0D);
        int y = (int)Math.round(height / 2.0D + pitchDelta / (verticalFov * 0.5D) * height / 2.0D);
        x = Mth.clamp(x, 18, width - 18);
        y = Mth.clamp(y, 18, height - 18);
        float visibility = (float)Mth.clamp((dot - VISIBLE_DOT) / (1.0D - VISIBLE_DOT), 0.0D, 1.0D);
        float scale = (float)Mth.clamp(6.0D / distance, 0.28D, 0.76D);
        return new ScreenPoint(x, y, scale, visibility);
    }

    private static void drawFace(GuiGraphics graphics, int centerX, int centerY, float scale, float alpha) {
        int eyeW = Math.max(4, Math.round(15.0F * scale));
        int eyeH = Math.max(3, Math.round(6.0F * scale));
        int eyeGap = Math.max(10, Math.round(28.0F * scale));
        int glowColor = argb((int)(90 * alpha), 170, 255, 220);
        int eyeColor = argb((int)(235 * alpha), 230, 255, 238);

        for (int layer = 3; layer >= 1; layer--) {
            int grow = Math.round(layer * 4.0F * scale);
            int color = argb((int)(24 * alpha / layer), 80, 255, 210);
            fillCentered(graphics, centerX - eyeGap, centerY - Math.round(8 * scale), eyeW + grow, eyeH + grow, color);
            fillCentered(graphics, centerX + eyeGap, centerY - Math.round(8 * scale), eyeW + grow, eyeH + grow, color);
        }

        fillCentered(graphics, centerX - eyeGap, centerY - Math.round(8 * scale), eyeW, eyeH, glowColor);
        fillCentered(graphics, centerX + eyeGap, centerY - Math.round(8 * scale), eyeW, eyeH, glowColor);
        fillCentered(graphics, centerX - eyeGap, centerY - Math.round(8 * scale), Math.max(2, eyeW / 3), eyeH, eyeColor);
        fillCentered(graphics, centerX + eyeGap, centerY - Math.round(8 * scale), Math.max(2, eyeW / 3), eyeH, eyeColor);

        int mouthWidth = Math.max(34, Math.round(70.0F * scale));
        int mouthY = centerY + Math.round(17.0F * scale);
        for (int i = 0; i <= 12; i++) {
            double t = -1.0D + i / 6.0D;
            int px = centerX + (int)Math.round(t * mouthWidth * 0.5D);
            int py = mouthY + (int)Math.round((1.0D - t * t) * 9.0D * scale);
            fillCentered(graphics, px, py, Math.max(2, Math.round(7.0F * scale)), Math.max(2, Math.round(3.0F * scale)), argb((int)(180 * alpha), 210, 255, 235));
            if (i % 2 == 0) {
                graphics.fill(px - 1, py + 1, px + 1, py + Math.max(3, Math.round(7.0F * scale)), argb((int)(150 * alpha), 230, 255, 240));
            }
        }
    }

    private static void renderVhsAndVignette(GuiGraphics graphics, ScreenPoint point, float partialTick, float intensity) {
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float clampedIntensity = Mth.clamp(intensity, 0.0F, 1.65F);
        float pulse = 0.55F + 0.45F * (float)Math.sin((point.x + point.y + partialTick) * 0.1F);
        int red = argb((int)(42 * pulse * clampedIntensity), 140, 10, 16);
        graphics.fill(0, 0, width, height, argb((int)(42 * clampedIntensity), 0, 0, 0));
        graphics.fill(0, 0, width, height / 9, argb((int)(95 * clampedIntensity), 0, 0, 0));
        graphics.fill(0, height - height / 8, width, height, argb((int)(115 * clampedIntensity), 0, 0, 0));
        graphics.fill(0, 0, width / 11, height, red);
        graphics.fill(width - width / 11, 0, width, height, red);

        for (int y = 0; y < height; y += 11) {
            int alpha = (int)(((y / 11) % 2 == 0 ? 24 : 12) * clampedIntensity);
            graphics.fill(0, y, width, y + 1, argb(alpha, 220, 255, 245));
        }
        int tearY = Mth.floor((partialTick * 37.0F + point.y * 0.17F) % Math.max(height, 1));
        int tearHeight = Math.max(1, Math.round(3 * clampedIntensity));
        graphics.fill(0, tearY, width, Math.min(height, tearY + tearHeight), argb((int)(58 * clampedIntensity), 180, 255, 235));
    }

    private static void fillCentered(GuiGraphics graphics, int centerX, int centerY, int width, int height, int color) {
        graphics.fill(centerX - width / 2, centerY - height / 2, centerX + (width + 1) / 2, centerY + (height + 1) / 2, color);
    }

    private static void turnCameraToward(LocalPlayer player, Vec3 target, float strength) {
        Vec3 relative = target.subtract(player.getEyePosition());
        double horizontal = Math.sqrt(relative.x * relative.x + relative.z * relative.z);
        float targetYaw = (float)(Math.toDegrees(Math.atan2(relative.z, relative.x)) - 90.0D);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(relative.y, horizontal)));
        player.setYRot(lerpDegrees(player.getYRot(), targetYaw, strength));
        player.setXRot(Mth.clamp(lerpDegrees(player.getXRot(), targetPitch, strength), -90.0F, 90.0F));
        player.setYHeadRot(player.getYRot());
    }

    private static void applyCameraTremor(LocalPlayer player, float intensity) {
        float yawTremor = (float)Math.sin(player.tickCount * 2.9F) * intensity * 1.8F;
        float pitchTremor = (float)Math.cos(player.tickCount * 3.7F) * intensity * 1.1F;
        player.setYRot(player.getYRot() + yawTremor);
        player.setXRot(Mth.clamp(player.getXRot() + pitchTremor, -90.0F, 90.0F));
        player.setYHeadRot(player.getYRot());
    }

    private static float lerpDegrees(float current, float target, float amount) {
        return current + Mth.wrapDegrees(target - current) * amount;
    }

    private static double easeIn(double value) {
        return value * value * (3.0D - 2.0D * value);
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (red << 16) | (green << 8) | blue;
    }

    private enum State {
        WATCHING,
        STARE_LOCK,
        BLINK,
        FLEE,
        VANISH
    }

    private record ScreenPoint(int x, int y, float scale, float visibility) {
    }
}
