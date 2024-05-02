package me.andre111.voxedit.client.gizmo;

import java.util.function.Consumer;
import java.util.function.Supplier;

import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import net.minecraft.text.Text;

public abstract class GizmoActions {
	public abstract void add(Text text, Runnable action);
	public abstract void add(ToolSetting<?> setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter, Consumer<ToolSetting<?>> notifier);
}
