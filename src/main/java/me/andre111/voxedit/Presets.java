package me.andre111.voxedit;

import java.util.ArrayList;

import me.andre111.voxedit.tool.util.Mode;
import net.minecraft.block.Blocks;
import net.minecraft.block.ButtonBlock;
import net.minecraft.block.enums.BlockFace;
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
		
		ItemStack stack = VoxEdit.TOOL_ITEM.getStackWith(andre111);
		stack.setCustomName(Text.of("andre111s Presets").copy().setStyle(Style.EMPTY.withItalic(false).withBold(true).withColor(Formatting.GOLD)));
		andre111Stack = stack;
	}
}
