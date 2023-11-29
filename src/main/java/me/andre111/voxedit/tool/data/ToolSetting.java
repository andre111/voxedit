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

import java.util.function.BiFunction;
import java.util.function.Function;

import me.andre111.voxedit.tool.config.ToolConfig;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public sealed abstract class ToolSetting<V, TC extends ToolConfig<TC>> {
	private final Text label;
	private final Function<TC, V> reader;
	private final BiFunction<TC, V, TC> writer;
	
	private ToolSetting(Text label, Function<TC, V> reader, BiFunction<TC, V, TC> writer) {
		this.label = label;
		this.reader = reader;
		this.writer = writer;
	}
	
	public Text label() {
		return label;
	}
	
	public Function<TC, V> reader() {
		return reader;
	}
	
	public BiFunction<TC, V, TC> writer() {
		return writer;
	}
	
	public static <TC extends ToolConfig<TC>> Bool<TC> bool(Text title, Function<TC, Boolean> reader, BiFunction<TC, Boolean, TC> writer) {
		return new Bool<>(title, reader, writer);
	}
	
	public static <E extends Enum<E>, TC extends ToolConfig<TC>> EnumValue<E, TC> enumValue(Text title, E[] values, Function<E, Text> toText, Function<TC, E> reader, BiFunction<TC, E, TC> writer) {
		return new EnumValue<>(title, values, toText, reader, writer);
	}
	
	public static <TC extends ToolConfig<TC>> Int<TC> integer(Text title, int min, int max, Function<TC, Integer> reader, BiFunction<TC, Integer, TC> writer) {
		return new Int<>(title, min, max, reader, writer);
	}
	
	public static <TC extends ToolConfig<TC>> TSBlockPalette<TC> blockPalette(Text title, boolean includeProperties, boolean showWeights, Function<TC, BlockPalette> reader, BiFunction<TC, BlockPalette, TC> writer) {
		return new TSBlockPalette<>(title, includeProperties, showWeights, reader, writer);
	}
	
	public static <TC extends ToolConfig<TC>, T> TSIdentifier<TC, T> identifier(Text title, RegistryKey<? extends Registry<T>> registryKey, Function<TC, Identifier> reader, BiFunction<TC, Identifier, TC> writer) {
		return new TSIdentifier<>(title, registryKey, reader, writer);
	}
	
	public static final class Bool<TC extends ToolConfig<TC>> extends ToolSetting<Boolean, TC> {
		private Bool(Text label, Function<TC, Boolean> reader, BiFunction<TC, Boolean, TC> writer) {
			super(label, reader, writer);
		}
	}
	
	public static final class EnumValue<E extends Enum<E>, TC extends ToolConfig<TC>> extends ToolSetting<E, TC> {
		private final E[] values;
		private final Function<E, Text> toText;
		private EnumValue(Text label, E[] values, Function<E, Text> toText, Function<TC, E> reader, BiFunction<TC, E, TC> writer) {
			super(label, reader, writer);
			this.values = values;
			this.toText = toText;
		}
		
		public E[] values() {
			return values;
		}
		
		public Function<E, Text> toText() {
			return toText;
		}
	}

	public static final class Int<TC extends ToolConfig<TC>> extends ToolSetting<Integer, TC> {
		private final int min;
		private final int max;
		private Int(Text label, int min, int max, Function<TC, Integer> reader, BiFunction<TC, Integer, TC> writer) {
			super(label, reader, writer);
			this.min = min;
			this.max = max;
		}
		
		public int min() {
			return min;
		}
		
		public int max() {
			return max;
		}
	}
	
	public static final class TSBlockPalette<TC extends ToolConfig<TC>> extends ToolSetting<BlockPalette, TC> {
		private final boolean includeProperties;
		private final boolean showWeights;
		public TSBlockPalette(Text title, boolean includeProperties, boolean showWeights, Function<TC, BlockPalette> reader, BiFunction<TC, BlockPalette, TC> writer) {
			super(title, reader, writer);
			this.includeProperties = includeProperties;
			this.showWeights = showWeights;
		}
		
		public boolean includeProperties() {
			return includeProperties;
		}
		
		public boolean showWeights() {
			return showWeights;
		}
	}
	
	public static final class TSIdentifier<TC extends ToolConfig<TC>, T> extends ToolSetting<Identifier, TC> {
		private final RegistryKey<? extends Registry<T>> registryKey;
		
		public TSIdentifier(Text title, RegistryKey<? extends Registry<T>> registryKey, Function<TC, Identifier> reader, BiFunction<TC, Identifier, TC> writer) {
			super(title, reader, writer);
			this.registryKey = registryKey;
		}
		
		public RegistryKey<? extends Registry<T>> registryKey() {
			return registryKey;
		}
	}
}
