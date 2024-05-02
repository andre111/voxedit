/*
 * Copyright (c) 2024 André Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
