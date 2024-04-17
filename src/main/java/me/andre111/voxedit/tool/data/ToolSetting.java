/*
 * Copyright (c) 2023 André Schweiger
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
package me.andre111.voxedit.tool.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public sealed abstract class ToolSetting<V> {
	private final String key;
	private final Text label;
	private final V defaultValue;
	private final Function<String, V> reader;
	private final Function<V, String> writer;
	
	private ToolSetting(String key, V defaultValue, Function<String, V> reader, Function<V, String> writer) {
		this.key = key;
		this.label = Text.translatable("voxedit.tool.settings."+key);
		this.defaultValue = defaultValue;
		this.reader = reader;
		this.writer = writer;
	}
	
	public Text label() {
		return label;
	}
	
	public V get(ToolConfig config) {
		if(!config.values().containsKey(key)) return defaultValue;
		try {
			V value = reader.apply(config.values().get(key));
			if(isValid(value)) return value;
		} catch(Exception e) {}
		return defaultValue;
	}
	
	public ToolConfig with(ToolConfig config, V value) {
		if(!isValid(value)) return config;
		
		Map<String, String> newValues = new HashMap<>(config.values());
		newValues.put(key, writer.apply(value));
		return new ToolConfig(newValues);
	}
	
	public V getDefaultValue() {
		return defaultValue;
	}
	
	public ToolConfig withDefaultValue(ToolConfig config) {
		return with(config, defaultValue);
	}
	
	public boolean isValidOrMissing(ToolConfig config) {
		if(!config.values().containsKey(key)) return true;
		try {
			V value = reader.apply(config.values().get(key));
			return isValid(value);
		} catch(Exception e) {}
		return false;
	}
	
	protected abstract boolean isValid(V value);
	
	
	public static ToolSetting<Boolean> ofBoolean(String key, boolean defaultValue) {
		return new Bool(key, defaultValue);
	}

	public static <E extends Enum<E>> ToolSetting<E> ofEnum(String key, Class<E> enumCls, Function<E, Text> toText) {
		return new FixedValues<E>(key, enumCls.getEnumConstants()[0], enumCls.getEnumConstants(), toText, name -> Enum.valueOf(enumCls, name), e -> e.name());
	}
	
	public static ToolSetting<Integer> ofInt(String key, int defaultValue, int min, int max) {
		return new Int(key, defaultValue, min, max);
	}
	
	public static <T> ToolSetting<Identifier> ofIdentifier(String key, Identifier defaultValue, RegistryKey<? extends Registry<T>> registryKey) {
		return new TSIdentifier<>(key, defaultValue, registryKey);
	}
	
	public static <T> ToolSetting<T> ofUnsynchedRegistry(String key, T defaultValue, Registry<T> registry, boolean showFixedSelection, Function<T, Text> toText) {
		return new TSRegistry<>(key, defaultValue, registry, showFixedSelection, toText);
	}
	
	public static final class Bool extends ToolSetting<Boolean> {
		public Bool(String key, boolean defaultValue) {
			super(key, defaultValue, Boolean::parseBoolean, b -> b.toString());
		}

		@Override
		protected boolean isValid(Boolean value) {
			return value != null;
		}
	}
	
	public static final class FixedValues<E> extends ToolSetting<E> {
		private final E[] values;
		private final Function<E, Text> toText;
		public FixedValues(String key, E defaultValue, E[] values, Function<E, Text> toText, Function<String, E> reader, Function<E, String> writer) {
			super(key, defaultValue, reader, writer);
			this.values = values;
			this.toText = toText;
		}
		
		public E[] values() {
			return values;
		}
		
		public Function<E, Text> toText() {
			return toText;
		}

		@Override
		protected boolean isValid(E value) {
			for(E e : values) if(e.equals(value)) return true;
			return false;
		}
	}

	public static final class Int extends ToolSetting<Integer> {
		private final int min;
		private final int max;
		public Int(String key, int defaultValue, int min, int max) {
			super(key, defaultValue, Integer::parseInt, i -> i.toString());
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
	
	public static final class TSIdentifier<T> extends ToolSetting<Identifier> {
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
	
	public static final class TSRegistry<T> extends ToolSetting<T> {
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
}
