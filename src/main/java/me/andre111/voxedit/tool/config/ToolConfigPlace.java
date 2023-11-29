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
package me.andre111.voxedit.tool.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.tool.data.ToolSetting;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record ToolConfigPlace(Identifier feature, int tries, boolean targetFluids) implements ToolConfig<ToolConfigPlace> {
	public static final Codec<ToolConfigPlace> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(
				Identifier.CODEC.optionalFieldOf("feature", new Identifier("minecraft", "oak")).forGetter(tc -> tc.feature),
				Codec.INT.optionalFieldOf("tries", 3).forGetter(tc -> tc.tries),
				Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
		)
		.apply(instance, ToolConfigPlace::new));
	private static List<ToolSetting<?, ToolConfigPlace>> SETTINGS = List.of(
			ToolSetting.identifier(Text.of("Feature"), RegistryKeys.CONFIGURED_FEATURE, 
					ToolConfigPlace::feature, 
					ToolConfigPlace::withFeature),
			ToolSetting.integer(Text.of("Tries"), 1, 10,
					ToolConfigPlace::tries, 
					ToolConfigPlace::withTries)
			);

	@Override
	public List<ToolSetting<?, ToolConfigPlace>> getSettings() {
		return SETTINGS;
	}

	@Override
	public int radius() {
		return 0;
	}
	
	@Override
	public ToolConfigPlace withRadius(int radius) {
		return this;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(Text.of(feature.getNamespace()), Text.of(feature.getPath()));
	}
	
	public ToolConfigPlace() {
		this(new Identifier("minecraft", "oak"), 3, false);
	}

	public ToolConfigPlace withFeature(Identifier feature) {
		return new ToolConfigPlace(feature, tries, targetFluids);
	}

	public ToolConfigPlace withTries(int tries) {
		return new ToolConfigPlace(feature, tries, targetFluids);
	}

	public ToolConfigPlace withTargetFluids(boolean targetFluids) {
		return new ToolConfigPlace(feature, tries, targetFluids);
	}
}
