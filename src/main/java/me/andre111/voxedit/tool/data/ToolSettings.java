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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import me.andre111.voxedit.tool.config.ToolConfig;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ToolSettings<TC extends ToolConfig<TC>> {
	private final List<ToolSetting<?, TC>> settings;
	
	private ToolSettings(List<ToolSetting<?, TC>> settings) {
		this.settings = List.copyOf(settings);
	}
	
	public List<ToolSetting<?, TC>> get() {
		return settings;
	}
	
	public static <TC extends ToolConfig<TC>> ToolSettings<TC> create(Consumer<Builder<TC>> creator) {
		Builder<TC> instance = new Builder<TC>();
		creator.accept(instance);
		return instance.create();
	}
	
	public static class Builder<TC extends ToolConfig<TC>> {
		private final List<ToolSetting<?, TC>> settings = new ArrayList<>();

		public Builder<TC> bool(Text title, Function<TC, Boolean> reader, BiFunction<TC, Boolean, TC> writer) {
			settings.add(new ToolSetting.Bool<>(title, reader, writer));
			return this;
		}
		
		public <E> Builder<TC> fixedValues(Text title, E[] values, Function<E, Text> toText, Function<TC, E> reader, BiFunction<TC, E, TC> writer) {
			settings.add(new ToolSetting.FixedValues<>(title, values, toText, reader, writer));
			return this;
		}
		
		public Builder<TC> integer(Text title, int min, int max, Function<TC, Integer> reader, BiFunction<TC, Integer, TC> writer) {
			settings.add(new ToolSetting.Int<>(title, min, max, reader, writer));
			return this;
		}
		
		public Builder<TC> blockPalette(Text title, boolean includeProperties, boolean showWeights, Function<TC, BlockPalette> reader, BiFunction<TC, BlockPalette, TC> writer) {
			settings.add(new ToolSetting.TSBlockPalette<>(title, includeProperties, showWeights, reader, writer));
			return this;
		}
		
		public <T> Builder<TC> identifier(Text title, RegistryKey<? extends Registry<T>> registryKey, Function<TC, Identifier> reader, BiFunction<TC, Identifier, TC> writer) {
			settings.add(new ToolSetting.TSIdentifier<>(title, registryKey, reader, writer));
			return this;
		}
		
		public ToolSettings<TC> create() {
			return new ToolSettings<>(settings);
		}
	}
}
