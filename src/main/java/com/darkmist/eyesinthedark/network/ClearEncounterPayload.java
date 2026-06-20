package com.darkmist.eyesinthedark.network;

import com.darkmist.eyesinthedark.EyesInTheDark;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClearEncounterPayload(int reason) implements CustomPacketPayload {
    public static final Type<ClearEncounterPayload> TYPE = new Type<>(EyesInTheDark.id("clear_encounter"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClearEncounterPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClearEncounterPayload decode(RegistryFriendlyByteBuf buffer) {
            return new ClearEncounterPayload(buffer.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ClearEncounterPayload payload) {
            buffer.writeVarInt(payload.reason());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
