package me.andre111.voxedit.tool.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.gui.screen.ToolSetting;
import net.minecraft.text.Text;

public record ToolConfigFill(BlockPalette palette, BlockPalette filter, int radius, boolean targetFluids) implements ToolConfig {
	public static final Codec<ToolConfigFill> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("palette", BlockPalette.DEFAULT).forGetter(ts -> ts.palette),
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.INT.optionalFieldOf("radius", 10).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigFill::new));

	private static List<? extends ToolSetting<?, ?>> SETTINGS = List.of(
			ToolSetting.blockPalette(Text.of("Edit Palette"), true, true,
					ToolConfigFill::palette, 
					ToolConfigFill::withPalette),
			ToolSetting.blockPalette(Text.of("Edit Filter"),  false, false,
					ToolConfigFill::filter, 
					ToolConfigFill::withFilter),
			ToolSetting.intRange(Text.of("Radius"), 1, 16,
					ToolConfigFill::radius, 
					ToolConfigFill::withRadius)
			);

	@Override
	public List<? extends ToolSetting<?, ?>> getSettings() {
		return SETTINGS;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(Text.of(radius+""));
	}
	
	@Override
	public BlockPalette getIconBlocks() {
		return palette;
	}
	
	public ToolConfigFill() {
		this(BlockPalette.DEFAULT, new BlockPalette(), 10, false);
	}

	public ToolConfigFill withPalette(BlockPalette palette) {
		return new ToolConfigFill(palette, filter, radius, targetFluids);
	}

	public ToolConfigFill withFilter(BlockPalette filter) {
		return new ToolConfigFill(palette, filter, radius, targetFluids);
	}

	public ToolConfigFill withRadius(int radius) {
		return new ToolConfigFill(palette, filter, radius, targetFluids);
	}

	public ToolConfigFill withTargetFluids(boolean targetFluids) {
		return new ToolConfigFill(palette, filter, radius, targetFluids);
	}
}
