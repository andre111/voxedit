package me.andre111.voxedit.selection;

import com.mojang.serialization.Codec;

public class SelectionType<T extends Selection> {
	private final Codec<T> codec;
	
	public SelectionType(Codec<T> codec) {
		this.codec = codec;
	}
	
	public Codec<T> getCodec() {
		return codec;
	}

	public static <T extends Selection> SelectionType<T> of(Codec<T> codec) {
		return new SelectionType<>(codec);
	}
}
