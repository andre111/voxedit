package me.andre111.voxedit.tool.config;

import java.util.List;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.gui.screen.ToolSetting;
import net.minecraft.text.Text;

public interface ToolConfig {
	public List<? extends ToolSetting<?, ?>> getSettings();
	
	public int radius();
	public ToolConfig withRadius(int radius);
	
	public default boolean targetFluids() {
		return false;
	}
	
	public default List<Text> getIconTexts() {
		return List.of();
	}
	
	public default BlockPalette getIconBlocks() {
		return null;
	}
}
