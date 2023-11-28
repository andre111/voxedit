package me.andre111.voxedit.tool.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.gui.screen.ToolSetting;
import me.andre111.voxedit.tool.util.Shape;
import net.minecraft.text.Text;

public record ToolConfigFlatten(BlockPalette palette, Shape shape, int radius, boolean targetFluids) implements ToolConfig {
	public static final Codec<ToolConfigFlatten> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("palette", BlockPalette.DEFAULT).forGetter(ts -> ts.palette),
					Codec.STRING.optionalFieldOf("shape", Shape.SPHERE.name()).xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 5).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigFlatten::new));

	private static List<? extends ToolSetting<?, ?>> SETTINGS = List.of(
			ToolSetting.blockPalette(Text.of("Edit Palette"), true, true,
					ToolConfigFlatten::palette, 
					ToolConfigFlatten::withPalette),
			ToolSetting.ofEnum(Text.of("Shape"), Shape::asText, Shape.values(), 
					ToolConfigFlatten::shape, 
					ToolConfigFlatten::withShape),
			ToolSetting.intRange(Text.of("Radius"), 1, 16,
					ToolConfigFlatten::radius, 
					ToolConfigFlatten::withRadius)
			);

	@Override
	public List<? extends ToolSetting<?, ?>> getSettings() {
		return SETTINGS;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(shape.asText(), Text.of(radius+""));
	}
	
	@Override
	public BlockPalette getIconBlocks() {
		return palette;
	}
	
	public ToolConfigFlatten() {
		this(BlockPalette.DEFAULT, Shape.SPHERE, 5, false);
	}

	public ToolConfigFlatten withPalette(BlockPalette palette) {
		return new ToolConfigFlatten(palette, shape, radius, targetFluids);
	}

	public ToolConfigFlatten withShape(Shape shape) {
		return new ToolConfigFlatten(palette, shape, radius, targetFluids);
	}

	public ToolConfigFlatten withRadius(int radius) {
		return new ToolConfigFlatten(palette, shape, radius, targetFluids);
	}

	public ToolConfigFlatten withTargetFluids(boolean targetFluids) {
		return new ToolConfigFlatten(palette, shape, radius, targetFluids);
	}
}
