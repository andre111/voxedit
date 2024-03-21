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
package me.andre111.voxedit;

import java.util.ArrayList;

import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.tool.data.BlockPalette;
import me.andre111.voxedit.tool.data.Mode;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

public class Presets {
	public static final ToolItem.Data andre111;
	public static final ItemStack andre111Stack;
	static {
		andre111 = new ToolItem.Data(Util.make(new ArrayList<>(), list -> {
			list.add(VoxEdit.DEFAULT_TOOL);
			list.add(VoxEdit.TOOL_SMOOTH.getWith(VoxEdit.TOOL_SMOOTH.getDefaultConfig().withRadius(7)));
			list.add(VoxEdit.TOOL_BLEND.getDefault());
			list.add(VoxEdit.TOOL_BRUSH.getWith(VoxEdit.TOOL_BRUSH.getDefaultConfig()
					.withMode(Mode.PAINT_TOP)
					.withRadius(5)
					.withPalette(BlockPalette.builder()
							.add(Blocks.DIRT)
							.add(Blocks.COARSE_DIRT)
							.add(Blocks.DIRT_PATH)
							.add(Blocks.GRAVEL)
							.add(Blocks.COBBLESTONE)
							.build()
					)));
			list.add(VoxEdit.TOOL_BRUSH.getWith(VoxEdit.TOOL_BRUSH.getDefaultConfig()
					.withMode(Mode.SCATTER)
					.withCheckCanPlace(true)
					.withPalette(BlockPalette.builder()
							.add(Blocks.AIR, 30)
							.add(Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACE, BlockFace.FLOOR).with(ButtonBlock.FACING, Direction.NORTH), 1)
							.add(Blocks.STONE_BUTTON.getDefaultState().with(ButtonBlock.FACE, BlockFace.FLOOR).with(ButtonBlock.FACING, Direction.WEST), 1)
							.build()
					)));
			list.add(VoxEdit.TOOL_FILL.getDefault());
			// second row
			list.add(VoxEdit.TOOL_BRUSH.getWith(VoxEdit.TOOL_BRUSH.getDefaultConfig()
					.withMode(Mode.PAINT_TOP)
					.withRadius(7)
					.withPalette(BlockPalette.builder()
							.add(Blocks.GRASS_BLOCK, 3)
							.add(Blocks.MOSS_BLOCK, 3)
							.add(Blocks.GREEN_CONCRETE_POWDER)
							.build()
					)));
			list.add(VoxEdit.TOOL_BRUSH.getWith(VoxEdit.TOOL_BRUSH.getDefaultConfig()
					.withMode(Mode.SCATTER)
					.withRadius(6)
					.withCheckCanPlace(true)
					.withPalette(BlockPalette.builder()
							.add(Blocks.AIR, 32)
							.add(Blocks.SHORT_GRASS, 8)
							.add(Blocks.FERN, 8)
							.add(Blocks.POPPY, 1)
							.add(Blocks.DANDELION, 1)
							.build()
					)));
			list.add(VoxEdit.TOOL_PLACE.getDefault());
		}), 0);
		
		ItemStack stack = VoxEdit.ITEM_TOOL.getStackWith(andre111);
		stack.set(DataComponentTypes.CUSTOM_NAME, Text.of("andre111s Presets").copy().setStyle(Style.EMPTY.withItalic(false).withBold(true).withColor(Formatting.GOLD)));
		andre111Stack = stack;
	}
}
