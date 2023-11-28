package me.andre111.voxedit.tool.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.gui.screen.ToolSetting;
import me.andre111.voxedit.tool.util.Shape;
import net.minecraft.text.Text;

public record ToolConfigSmooth(BlockPalette filter, Shape shape, int radius, boolean targetFluids) implements ToolConfig {
	public static final Codec<ToolConfigSmooth> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.STRING.optionalFieldOf("shape", Shape.SPHERE.name()).xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 4).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
					)
			.apply(instance, ToolConfigSmooth::new));

	private static List<? extends ToolSetting<?, ?>> SETTINGS = List.of(
			ToolSetting.blockPalette(Text.of("Edit Filter"),  false, false,
					ToolConfigSmooth::filter, 
					ToolConfigSmooth::withFilter),
			ToolSetting.ofEnum(Text.of("Shape"), Shape::asText, Shape.values(), 
					ToolConfigSmooth::shape, 
					ToolConfigSmooth::withShape),
			ToolSetting.intRange(Text.of("Radius"), 1, 16,
					ToolConfigSmooth::radius, 
					ToolConfigSmooth::withRadius)
			);

	@Override
	public List<? extends ToolSetting<?, ?>> getSettings() {
		return SETTINGS;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(shape.asText(), Text.of(radius+""));
	}
	
	public ToolConfigSmooth() {
		this(new BlockPalette(), Shape.SPHERE, 4, false);
	}

	public ToolConfigSmooth withFilter(BlockPalette filter) {
		return new ToolConfigSmooth(filter, shape, radius, targetFluids);
	}

	public ToolConfigSmooth withShape(Shape shape) {
		return new ToolConfigSmooth(filter, shape, radius, targetFluids);
	}

	public ToolConfigSmooth withRadius(int radius) {
		return new ToolConfigSmooth(filter, shape, radius, targetFluids);
	}

	public ToolConfigSmooth withTargetFluids(boolean targetFluids) {
		return new ToolConfigSmooth(filter, shape, radius, targetFluids);
	}
}
