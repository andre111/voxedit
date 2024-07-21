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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.shape.ConfiguredShape;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public sealed abstract class Setting<V> {
	private final String key;
	private final Text label;
	private final V defaultValue;
	protected final Function<ConfigValue<?>, V> reader;
	protected final Function<V, ConfigValue<?>> writer;
	
	private Setting(String key, V defaultValue, Function<ConfigValue<?>, V> reader, Function<V, ConfigValue<?>> writer) {
		this.key = key;
		this.label = Text.translatable("voxedit.tool.settings."+key);
		this.defaultValue = defaultValue;
		this.reader = reader;
		this.writer = writer;
	}
	
	public String key() {
		return key;
	}
	
	public Text label() {
		return label;
	}
	
	public V get(Config config) {
		if(!config.values().containsKey(key)) return defaultValue;
		try {
			V value = reader.apply(config.values().get(key));
			if(isValid(value)) return value;
		} catch(Exception e) {}
		return defaultValue;
	}
	
	public Config with(Config config, V value) {
		if(!isValid(value)) return config;
		
		Map<String, ConfigValue<?>> newValues = new HashMap<>(config.values());
		newValues.put(key, writer.apply(value));
		return new Config(newValues);
	}
	
	public V getDefaultValue() {
		return defaultValue;
	}
	
	public Config withDefaultValue(Config config) {
		return with(config, defaultValue);
	}
	
	public boolean isValidOrMissing(Config config) {
		if(!config.values().containsKey(key)) return true;
		try {
			V value = reader.apply(config.values().get(key));
			return isValid(value);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	protected abstract boolean isValid(V value);
	
	
	public Setting<List<V>> listOf(List<V> defaultValue, int maxSize, Text title) {
		return new TSList<>(key, defaultValue, this, maxSize, title);
	}
	
	public static Setting<Boolean> ofBoolean(String key, boolean defaultValue) {
		return new Bool(key, defaultValue);
	}

	public static <E extends Enum<E>> Setting<E> ofEnum(String key, Class<E> enumCls, Function<E, Text> toText, boolean showFixedSelection) {
		return new FixedValues<E>(key, enumCls.getEnumConstants()[0], enumCls.getEnumConstants(), toText, showFixedSelection, name -> Enum.valueOf(enumCls, name), e -> e.name());
	}
	
	public static Setting<Integer> ofInt(String key, int defaultValue, int min, int max) {
		return new Int(key, defaultValue, min, max);
	}
	
	public static <T> Setting<Identifier> ofIdentifier(String key, Identifier defaultValue, RegistryKey<? extends Registry<T>> registryKey) {
		return new TSIdentifier<>(key, defaultValue, registryKey);
	}
	
	public static <T> Setting<T> ofUnsynchedRegistry(String key, T defaultValue, Registry<T> registry, boolean showFixedSelection, Function<T, Text> toText) {
		return new TSRegistry<>(key, defaultValue, registry, showFixedSelection, toText);
	}
	
	public static <T extends Configurable<T>> Setting<Configured<T>> ofNested(String key, Configurable.Type<T> type, Configured<T> defaultValue, Supplier<List<T>> availableValues) {
		return new TSNested<>(key, type, defaultValue, availableValues);
	}
	
	private static sealed abstract class Simple<V> extends Setting<V> {
		public Simple(String key, V defaultValue, Function<String, V> reader, Function<V, String> writer) {
			super(key, defaultValue, value -> value instanceof ConfigValue.CVString string ? reader.apply(string.get()) : defaultValue, value -> new ConfigValue.CVString(writer.apply(value)));
		}
	}
	
	public static final class Bool extends Simple<Boolean> {
		public Bool(String key, boolean defaultValue) {
			super(key, defaultValue, Boolean::parseBoolean, b -> b.toString());
		}

		@Override
		protected boolean isValid(Boolean value) {
			return value != null;
		}
	}
	
	public static final class FixedValues<E> extends Simple<E> {
		private final E[] values;
		private final Function<E, Text> toText;
		private final boolean showFixedSelection;
		
		public FixedValues(String key, E defaultValue, E[] values, Function<E, Text> toText, boolean showFixedSelection, Function<String, E> reader, Function<E, String> writer) {
			super(key, defaultValue, reader, writer);
			this.values = values;
			this.toText = toText;
			this.showFixedSelection = showFixedSelection;
		}
		
		public E[] values() {
			return values;
		}
		
		public Function<E, Text> toText() {
			return toText;
		}
		
		public boolean showFixedSelection() {
			return showFixedSelection;
		}

		@Override
		protected boolean isValid(E value) {
			for(E e : values) if(e.equals(value)) return true;
			return false;
		}
	}

	public static final class Int extends Setting<Integer> {
		private final int min;
		private final int max;
		public Int(String key, int defaultValue, int min, int max) {
			super(key, defaultValue, value -> value instanceof ConfigValue.CVInteger integer ? integer.get() : defaultValue, i -> new ConfigValue.CVInteger(i));
			this.min = min;
			this.max = max;
		}
		
		public int min() {
			return min;
		}
		
		public int max() {
			return max;
		}

		@Override
		protected boolean isValid(Integer value) {
			if(value == null) return false;
			return min <= value && value <= max;
		}
	}
	
	public static final class TSIdentifier<T> extends Simple<Identifier> {
		private final RegistryKey<? extends Registry<T>> registryKey;
		
		public TSIdentifier(String key, Identifier defaultValue, RegistryKey<? extends Registry<T>> registryKey) {
			super(key, defaultValue, Identifier::tryParse, Identifier::toString);
			this.registryKey = registryKey;
		}
		
		public RegistryKey<? extends Registry<T>> registryKey() {
			return registryKey;
		}

		@Override
		protected boolean isValid(Identifier value) {
			return value != null;
		}
	}
	
	public static final class TSRegistry<T> extends Simple<T> {
		private final Registry<T> registry;
		private final boolean showFixedSelection;
		private final Function<T, Text> toText;
		
		public TSRegistry(String key, T defaultValue, Registry<T> registry, boolean showFixedSelection, Function<T, Text> toText) {
			super(key, defaultValue, s -> registry.get(Identifier.tryParse(s)), t -> registry.getId(t).toString());
			if(registry == null) throw new IllegalArgumentException("Missing registry");
			this.registry = registry;
			this.showFixedSelection = showFixedSelection;
			this.toText = toText;
		}
		
		public Registry<T> registry() {
			return registry;
		}
		
		public boolean showFixedSelection() {
			return showFixedSelection;
		}
		
		public Function<T, Text> toText() {
			return toText;
		}
		
		@Override
		protected boolean isValid(T value) {
			return value != null && registry.getId(value) != null;
		}
	}
	
	//TODO: replace with Configured<Shape> and use TSNested?
	public static final class TSShape extends Simple<ConfiguredShape> {
		private final boolean showConfig;
		
		public TSShape(String key, boolean showConfig) {
			super(key, new ConfiguredShape(VoxEdit.SHAPE_SPHERE, false, 5, 5, 5, false, 0, 0, 0),  json -> parse(ConfiguredShape.CODEC, json), value -> toJson(ConfiguredShape.CODEC, value));
			this.showConfig = showConfig;
		}
		
		public boolean showConfig() {
			return showConfig;
		}

		@Override
		protected boolean isValid(ConfiguredShape value) {
			return value != null && value.isValid();
		}
	}
	
	public static final class TSNested<T extends Configurable<T>> extends Setting<Configured<T>> {
		private final Supplier<List<T>> availableValues;
		
		public TSNested(String key, Configurable.Type<T> type, Configured<T> defaultValue, Supplier<List<T>> availableValues) {
			/*super(key, defaultValue, entry -> {
				if(entry instanceof ConfigValue.CVMap mapEntry && mapEntry.get().get("$type") instanceof ConfigValue.CVString typeString) {
					T type = parse(defaultValue.value().getBaseCodec(), typeString.get());
					if(type != null) {
						Config config = new Config(mapEntry.get());
						if(type.isValid(config)) return new Configured<>(type, config);
					}
				}
				return defaultValue;
			}, value -> {
				Map<String, ConfigValue<?>> map = new HashMap<>();
				map.putAll(value.config().values());
				map.put("$type", new ConfigValue.CVString(toJson(value.value().getBaseCodec(), value.value())));
				return new ConfigValue.CVMap(map);
			});*/
			super(key, defaultValue, value -> {
				if(value instanceof ConfigValue.CVConfigured configuredValue) {
					if(configuredValue.get().value().getType().equals(type) && configuredValue.get().isValid()) {
						return (Configured<T>) configuredValue.get();
					}
				}
				return defaultValue;
			}, value -> new ConfigValue.CVConfigured(value));
			
			this.availableValues = availableValues;
		}

		@Override
		protected boolean isValid(Configured<T> value) {
			return value != null && value.isValid();
		}
		
		public List<T> getAvailableValues() {
			return availableValues.get();
		}
	}
	
	public static final class TSList<V, S extends Setting<V>> extends Setting<List<V>> {
		private final S setting;
		private final int maxSize;
		private final Text title;
		
		public TSList(String key, List<V> defaultValue, S setting, int maxSize, Text title) {
			super(key, defaultValue, value -> {
				if(value instanceof ConfigValue.CVList valueList) {
					List<V> list = new ArrayList<>();
					for(ConfigValue<?> entryValue : valueList.get()) list.add(setting.reader.apply(entryValue));
					return list;
				} else {
					return defaultValue;
				}
			}, list -> {
				List<ConfigValue<?>> valueList = new ArrayList<>();
				for(V v : list) valueList.add(setting.writer.apply(v));
				return new ConfigValue.CVList(valueList);
			});
			this.setting = setting;
			this.maxSize = maxSize;
			this.title = title;
		}
		
		public Text title() {
			return title;
		}
		
		public S setting() {
			return setting;
		}

		@Override
		protected boolean isValid(List<V> value) {
			if(value == null) return false;
			if(maxSize > 0 && value.size() > maxSize) return false;
			for(var v : value) if(!setting.isValid(v)) return false;
			return true;
		}
		
	}

	private static final Gson GSON = new Gson();
	private static <T> T parse(Codec<T> codec, String json) {
		JsonElement jsonElement = new JsonPrimitive(json);
		if(json.charAt(0) == '"' || json.charAt(0) == '[' || json.charAt(0) == '{') {
			jsonElement = JsonHelper.deserialize(GSON, json, JsonElement.class);
		}
		return codec.decode(JsonOps.INSTANCE, jsonElement).result().get().getFirst();
	}
	private static <T> String toJson(Codec<T> codec, T value) {
		JsonElement jsonElement = codec.encodeStart(JsonOps.INSTANCE, value).result().get();
		return jsonElement instanceof JsonPrimitive jsonPrimitive && jsonPrimitive.isString() ? jsonPrimitive.getAsString() : jsonElement.toString();
	}
}
