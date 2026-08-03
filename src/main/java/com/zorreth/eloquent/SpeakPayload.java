package com.zorreth.eloquent;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

public record SpeakPayload(String text) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SpeakPayload> TYPE = new CustomPacketPayload.Type<>(Eloquent.id("speak"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpeakPayload> CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, SpeakPayload::text, SpeakPayload::new);

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
