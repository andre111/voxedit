package me.andre111.voxedit.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Size(boolean enabled, boolean split, int x, int y, int z) {
	public static final Codec<Size> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
				Codec.BOOL.fieldOf("enabled").forGetter(Size::enabled),
				Codec.BOOL.fieldOf("split").forGetter(Size::split),
				Codec.INT.fieldOf("x").forGetter(Size::x),
				Codec.INT.fieldOf("y").forGetter(Size::y),
				Codec.INT.fieldOf("z").forGetter(Size::z)
			)
			.apply(instance, Size::new));
	
	public Size enabled(boolean enabled) {
		return new Size(enabled, split, x, y, z);
	}
	public Size split(boolean split) {
		return new Size(enabled, split, x, y, z);
	}
	public Size size(int size) {
		return new Size(enabled, false, size, size, size);
	}
	public Size x(int x) {
		return new Size(enabled, split, x, y, z);
	}
	public Size y(int y) {
		return new Size(enabled, split, x, y, z);
	}
	public Size z(int z) {
		return new Size(enabled, split, x, y, z);
	}
}
