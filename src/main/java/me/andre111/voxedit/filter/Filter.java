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
package me.andre111.voxedit.filter;

import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.Configurable;
import me.andre111.voxedit.data.Setting;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class Filter implements Configurable<Filter> {
	private final List<Setting<?>> settings;
	
	public Filter(List<Setting<?>> settings) {
		this.settings = settings;
	}
	
	@Override
	public final List<Setting<?>> getSettings() {
    	return settings;
    }

	@Override
	public Configurable.Type<Filter> getType() {
		return VoxEdit.TYPE_FILTER;
	}
	
	@Override
	public Text getName() {
		return Text.translatable(id().toTranslationKey("voxedit.filter"));
	}
    
	public final Identifier id() {
		return VoxEdit.FILTER_REGISTRY.getId(this);
	}
	
	public final Text asText() {
		Identifier id = id();
		return Text.translatable("voxedit.filter."+id.toTranslationKey());
	}
	
	public abstract boolean check(FilterContext context, Config config);
}
