package me.andre111.voxedit.tool.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.gui.screen.ToolSetting;
import me.andre111.voxedit.tool.util.Mode;
import me.andre111.voxedit.tool.util.Shape;
import net.minecraft.text.Text;

public record ToolConfigBrush(BlockPalette palette, BlockPalette filter, Mode mode, Shape shape, int radius, boolean targetFluids) implements ToolConfig {
	public static final Codec<ToolConfigBrush> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("palette", BlockPalette.DEFAULT).forGetter(ts -> ts.palette),
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.STRING.optionalFieldOf("mode", Mode.SOLID.name()).xmap(str -> Mode.valueOf(str), mode -> mode.name()).forGetter(ts -> ts.mode),
					Codec.STRING.optionalFieldOf("shape", Shape.SPHERE.name()).xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 4).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigBrush::new));

	private static List<? extends ToolSetting<?, ?>> SETTINGS = List.of(
			ToolSetting.blockPalette(Text.of("Edit Palette"), true, true,
					ToolConfigBrush::palette, 
					ToolConfigBrush::withPalette),
			ToolSetting.blockPalette(Text.of("Edit Filter"),  false, false,
					ToolConfigBrush::filter, 
					ToolConfigBrush::withFilter),
			ToolSetting.ofEnum(Text.of("Mode"), Mode::asText, Mode.values(), 
					ToolConfigBrush::mode, 
					ToolConfigBrush::withMode),
			ToolSetting.ofEnum(Text.of("Shape"), Shape::asText, Shape.values(), 
					ToolConfigBrush::shape, 
					ToolConfigBrush::withShape),
			ToolSetting.intRange(Text.of("Radius"), 1, 16,
					ToolConfigBrush::radius, 
					ToolConfigBrush::withRadius)
			);

	@Override
	public List<? extends ToolSetting<?, ?>> getSettings() {
		return SETTINGS;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(mode.asText(), shape.asText(), Text.of(radius+""));
	}
	
	@Override
	public BlockPalette getIconBlocks() {
		return palette;
	}

	public ToolConfigBrush() {
		this(BlockPalette.DEFAULT, new BlockPalette(), Mode.SOLID, Shape.SPHERE, 4, false);
	}

	public ToolConfigBrush withPalette(BlockPalette palette) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, targetFluids);
	}

	public ToolConfigBrush withFilter(BlockPalette filter) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, targetFluids);
	}

	public ToolConfigBrush withMode(Mode mode) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, targetFluids);
	}

	public ToolConfigBrush withShape(Shape shape) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, targetFluids);
	}

	public ToolConfigBrush withRadius(int radius) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, targetFluids);
	}

	public ToolConfigBrush withTargetFluids(boolean targetFluids) {
		return new ToolConfigBrush(palette, filter, mode, shape, radius, targetFluids);
	}
}
