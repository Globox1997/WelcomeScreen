package net.welcomescreen.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Placement(Anchor anchor, Anchor pivot, int x, int y) {

    public static final MapCodec<Placement> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Anchor.CODEC.optionalFieldOf("anchor", Anchor.TOP_LEFT).forGetter(Placement::anchor),
            Anchor.CODEC.optionalFieldOf("pivot", Anchor.TOP_LEFT).forGetter(Placement::pivot), Codec.INT.optionalFieldOf("x", 0).forGetter(Placement::x),
            Codec.INT.optionalFieldOf("y", 0).forGetter(Placement::y)).apply(instance, Placement::new));

    public Placement withPosition(int x, int y) {
        return new Placement(this.anchor, this.pivot, x, y);
    }
}
