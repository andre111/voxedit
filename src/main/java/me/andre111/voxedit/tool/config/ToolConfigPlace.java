package me.andre111.voxedit.tool.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.gui.screen.ToolSetting;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record ToolConfigPlace(Identifier feature, int tries, boolean targetFluids) implements ToolConfig {
	public static final Codec<ToolConfigPlace> CODEC = RecordCodecBuilder.create(instance -> instance
		.group(
				Identifier.CODEC.optionalFieldOf("feature", new Identifier("minecraft", "oak")).forGetter(tc -> tc.feature),
				Codec.INT.optionalFieldOf("tries", 3).forGetter(tc -> tc.tries),
				Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
		)
		.apply(instance, ToolConfigPlace::new));

	private static List<? extends ToolSetting<?, ?>> SETTINGS = List.of(
			ToolSetting.identifier(Text.of("Feature"), RegistryKeys.CONFIGURED_FEATURE, 
					ToolConfigPlace::feature, 
					ToolConfigPlace::withFeature),
			ToolSetting.intRange(Text.of("Tries"), 1, 10,
					ToolConfigPlace::tries, 
					ToolConfigPlace::withTries)
			);

	@Override
	public List<? extends ToolSetting<?, ?>> getSettings() {
		return SETTINGS;
	}

	@Override
	public int radius() {
		return 0;
	}
	
	@Override
	public ToolConfig withRadius(int radius) {
		return this;
	}

	@Override
	public  List<Text> getIconTexts() {
		return List.of(Text.of(feature.getNamespace()), Text.of(feature.getPath()));
	}
	
	public ToolConfigPlace() {
		this(new Identifier("minecraft", "oak"), 3, false);
	}

	public ToolConfigPlace withFeature(Identifier feature) {
		return new ToolConfigPlace(feature, tries, targetFluids);
	}

	public ToolConfigPlace withTries(int tries) {
		return new ToolConfigPlace(feature, tries, targetFluids);
	}

	public ToolConfigPlace withTargetFluids(boolean targetFluids) {
		return new ToolConfigPlace(feature, tries, targetFluids);
	}
}
