package me.andre111.voxedit.data;

import java.util.List;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

//TODO: add a configured value type (to avoid having to reparse on every access)
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
			//case CVMap map:
			//	return DataResult.success(ops.createMap(map.get().entrySet().stream().map(e -> new Pair<>(ops.createString(e.getKey()), ENCODER.encodeStart(ops, e.getValue()).getOrThrow()))));
			case CVConfigured configured:
				return Configured.GENERIC_CODEC.encodeStart(ops, configured.get());
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
				//DataResult<MapLike<T>> mapResult = ops.getMap(input);
				//if(mapResult.isSuccess()) return DataResult.success(Pair.of(new CVMap(mapResult.getOrThrow().entries().reduce(new HashMap<String, ConfigValue<?>>(), (map, pair) -> { map.put(ops.getStringValue(pair.getFirst()).getOrThrow(), DECODER.decode(ops, pair.getSecond()).getOrThrow().getFirst()); return map; }, (map1, map2) -> { map1.putAll(map2); return map1; })), input));
				DataResult<Pair<Configured<?>, T>> configuredResult = Configured.GENERIC_CODEC.decode(ops, input);
				if(configuredResult.isSuccess()) return DataResult.success(configuredResult.getOrThrow().mapFirst(ConfigValue.CVConfigured::new));
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
	/*public static record CVMap(Map<String, ConfigValue<?>> map) implements ConfigValue<Map<String, ConfigValue<?>>> {
		@Override
		public Map<String, ConfigValue<?>> get() {
			return map;
		}
	}*/
	
	//TODO: how to handle this with different types
	public static record CVConfigured(Configured<?> value) implements ConfigValue<Configured<?>> {
		@Override
		public Configured<?> get() {
			return value;
		}
	}
}
