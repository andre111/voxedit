package me.andre111.voxedit.tool.data;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.shape.Shape;

public class ToolSettings {
	public static final ToolSetting<Boolean> TARGET_FLUIDS = ToolSetting.ofBoolean("targetFluids", false);
	public static final ToolSetting<Shape> SHAPE = ToolSetting.ofUnsynchedRegistry("shape", VoxEdit.SHAPE_SPHERE, VoxEdit.SHAPE_REGISTRY, true, Shape::asText);
}
