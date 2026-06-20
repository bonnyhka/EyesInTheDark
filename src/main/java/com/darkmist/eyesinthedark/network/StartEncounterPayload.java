package com.darkmist.eyesinthedark.network;

import com.darkmist.eyesinthedark.EyesInTheDark;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record StartEncounterPayload(UUID encounterId, double x, double y, double z, int mode, int seed) implements CustomPacketPayload {
    public static final Type<StartEncounterPayload> TYPE = new Type<>(EyesInTheDark.id("start_encounter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StartEncounterPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public StartEncounterPayload decode(RegistryFriendlyByteBuf buffer) {
            return new StartEncounterPayload(
                    buffer.readUUID(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readVarInt(),
                    buffer.readInt()
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, StartEncounterPayload payload) {
            buffer.writeUUID(payload.encounterId());
            buffer.writeDouble(payload.x());
            buffer.writeDouble(payload.y());
            buffer.writeDouble(payload.z());
            buffer.writeVarInt(payload.mode());
            buffer.writeInt(payload.seed());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
