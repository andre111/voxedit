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
package me.andre111.voxedit;

import java.util.Map;

import me.andre111.voxedit.tool.data.BlockPalette;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.util.math.Direction;

public class Presets {
	public static void addPalettesIfAbsent(Map<String, BlockPalette> palettes) {
		palettes.putIfAbsent("Stone", BlockPalette.builder()
				.add(Blocks.STONE)
				.build()
				);
		palettes.putIfAbsent("Path", BlockPalette.builder()
				.add(Blocks.DIRT)
				.add(Blocks.COARSE_DIRT)
				.add(Blocks.DIRT_PATH)
				.add(Blocks.GRAVEL)
				.add(Blocks.COBBLESTONE)
				.build()
				);
		palettes.putIfAbsent("Pebbles", BlockPalette.builder()
				.add(Blocks.AIR, 30)
				.add(Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACE, BlockFace.FLOOR).with(ButtonBlock.FACING, Direction.NORTH), 1)
				.add(Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACE, BlockFace.FLOOR).with(ButtonBlock.FACING, Direction.WEST), 1)
				.build()
				);
		palettes.putIfAbsent("Lush Mix", BlockPalette.builder()
				.add(Blocks.GRASS_BLOCK, 3)
				.add(Blocks.MOSS_BLOCK, 3)
				.add(Blocks.GREEN_CONCRETE_POWDER)
				.build()
				);
		palettes.putIfAbsent("Grass Mix", BlockPalette.builder()
				.add(Blocks.AIR, 32)
				.add(Blocks.SHORT_GRASS, 8)
				.add(Blocks.FERN, 8)
				.add(Blocks.POPPY, 1)
				.add(Blocks.DANDELION, 1)
				.build()
				);
	}
}
