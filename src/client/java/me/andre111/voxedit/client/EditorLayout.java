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
package me.andre111.voxedit.client;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.client.gui.widget.EditorWidget;
import net.minecraft.util.Identifier;

public record EditorLayout(List<PanelLocation> panelLocations) {
	public static final Codec<EditorLayout> CODEC = PanelLocation.CODEC.listOf().xmap(EditorLayout::new, EditorLayout::panelLocations);
	public static final EditorLayout EMPTY = new EditorLayout(List.of());
	
	public static record PanelLocation(Identifier panel, EditorWidget.Location location) {
		private static final Codec<PanelLocation> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(
						Identifier.CODEC.fieldOf("panel").forGetter(PanelLocation::panel),
						Codec.STRING.xmap(EditorWidget.Location::valueOf, EditorWidget.Location::name).fieldOf("location").forGetter(PanelLocation::location)
				)
				.apply(instance, PanelLocation::new));
	}
}
