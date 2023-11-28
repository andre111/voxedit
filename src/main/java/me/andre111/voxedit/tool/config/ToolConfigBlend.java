package me.andre111.voxedit.tool.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.gui.screen.ToolSetting;
import me.andre111.voxedit.tool.util.Shape;
import net.minecraft.text.Text;

public record ToolConfigBlend(BlockPalette filter, Shape shape, int radius, boolean targetFluids) implements ToolConfig {
	public static final Codec<ToolConfigBlend> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.STRING.optionalFieldOf("shape", Shape.DISC.name()).xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 6).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigBlend::new));

	private static List<? extends ToolSetting<?, ?>> SETTINGS = List.of(
			ToolSetting.blockPalette(Text.of("Edit Filter"), false, false,
					ToolConfigBlend::filter, 
					ToolConfigBlend::withFilter),
			ToolSetting.ofEnum(Text.of("Shape"), Shape::asText, Shape.values(), 
					ToolConfigBlend::shape, 
					ToolConfigBlend::withShape),
			ToolSetting.intRange(Text.of("Radius"), 1, 16,
					ToolConfigBlend::radius, 
					ToolConfigBlend::withRadius)
			);

	@Override
	public List<? extends ToolSetting<?, ?>> getSettings() {
		return SETTINGS;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(shape.asText(), Text.of(radius+""));
	}
	
	public ToolConfigBlend() {
		this(new BlockPalette(), Shape.DISC, 6, false);
	}

	public ToolConfigBlend withFilter(BlockPalette filter) {
		return new ToolConfigBlend(filter, shape, radius, targetFluids);
	}

	public ToolConfigBlend withShape(Shape shape) {
		return new ToolConfigBlend(filter, shape, radius, targetFluids);
	}

	public ToolConfigBlend withRadius(int radius) {
		return new ToolConfigBlend(filter, shape, radius, targetFluids);
	}

	public ToolConfigBlend withTargetFluids(boolean targetFluids) {
		return new ToolConfigBlend(filter, shape, radius, targetFluids);
	}
}
