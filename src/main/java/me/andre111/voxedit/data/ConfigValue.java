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
package me.andre111.voxedit.data;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

public sealed interface ConfigValue<T> {
	static final Encoder<ConfigValue<?>> ENCODER = new Encoder<>() {
		@Override
		public <T> DataResult<T> encode(ConfigValue<?> input, DynamicOps<T> ops, T prefix) {
			switch(input) {
			case CVInteger integer:
				return DataResult.success(ops.createInt(integer.get()));
			case CVString string:
				return DataResult.success(ops.createString(string.get()));
			case CVList list:
				return DataResult.success(ops.createList(list.get().stream().map(value -> ENCODER.encodeStart(ops, value).getOrThrow())));
			case CVConfigured configured:
				return Configured.GENERIC_CODEC.encodeStart(ops, configured.get());
			case CVSize size:
				return Size.CODEC.encodeStart(ops, size.get());
			}
		}
	};
	static final Decoder<ConfigValue<?>> DECODER =  new Decoder<>() {
		@Override
		public <T> DataResult<Pair<ConfigValue<?>, T>> decode(DynamicOps<T> ops, T input) {
			try {
				DataResult<Number> integerResult = ops.getNumberValue(input);
				if(integerResult.isSuccess()) return DataResult.success(Pair.of(new CVInteger(integerResult.getOrThrow().intValue()), input));
				DataResult<String> stringResult = ops.getStringValue(input);
				if(stringResult.isSuccess()) return DataResult.success(Pair.of(new CVString(stringResult.getOrThrow()), input));
				DataResult<Stream<T>> streamResult = ops.getStream(input);
				if(streamResult.isSuccess()) return DataResult.success(Pair.of(new CVList(streamResult.getOrThrow().map(entry -> DECODER.decode(ops, entry).getOrThrow().getFirst()).toList()), input));
				DataResult<Pair<Configured<?>, T>> configuredResult = Configured.GENERIC_CODEC.decode(ops, input);
				if(configuredResult.isSuccess()) return DataResult.success(configuredResult.getOrThrow().mapFirst(ConfigValue.CVConfigured::new));
				DataResult<Pair<Size, T>> sizeResult = Size.CODEC.decode(ops, input);
				if(sizeResult.isSuccess()) return DataResult.success(sizeResult.getOrThrow().mapFirst(ConfigValue.CVSize::new));
			} catch(Exception e) {
				return DataResult.error(() -> "Invalid config value structure: "+e.getMessage());
			}
			return DataResult.error(() -> "Invalid config value structure");
		}
	};
	public static final Codec<ConfigValue<?>> CODEC = Codec.of(ENCODER, DECODER);
	
	public T get();
	
	public static record CVInteger(Integer value) implements ConfigValue<Integer> {
		@Override
		public Integer get() {
			return value;
		}
	}
	public static record CVString(String value) implements ConfigValue<String> {
		@Override
		public String get() {
			return value;
		}
	}
	public static record CVList(List<? extends ConfigValue<?>> list) implements ConfigValue<List<? extends ConfigValue<?>>> {
		@Override
		public List<? extends ConfigValue<?>> get() {
			return list;
		}
	}
	public static record CVConfigured(Configured<?> value) implements ConfigValue<Configured<?>> {
		@Override
		public Configured<?> get() {
			return value;
		}
	}
	public static record CVSize(Size value) implements ConfigValue<Size> {
		@Override
		public Size get() {
			return value;
		}
	}
}
