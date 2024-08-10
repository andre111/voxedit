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

import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.EntryOrTag;
import me.andre111.voxedit.data.Setting;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

public class FilterBlock extends Filter {
	public static final Setting<List<EntryOrTag<Block>>> BLOCKS = Setting.ofRegistryEntryOrTag("blocks", Registries.BLOCK).listOf(List.of(), 0, Text.translatable("voxedit.filter.voxedit.block"));

	public FilterBlock() {
		super(List.of(BLOCKS));
	}

	@Override
	public boolean check(FilterContext context, Config config) {
		Block block = context.view().getBlockState(context.pos()).getBlock();
		
		var list = BLOCKS.get(config);
		for(var entryOrTag : list) {
			if(entryOrTag.value().left().isPresent() && entryOrTag.value().left().get().equals(block)) return true;
			if(entryOrTag.value().right().isPresent() && entryOrTag.value().right().get().contains(Registries.BLOCK.getEntry(block))) return true;
		}
		
		return false;
	}
}
