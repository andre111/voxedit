package me.andre111.voxedit;

import me.andre111.voxedit.gui.ToolSettingsScreen;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HudRenderer implements HudRenderCallback {
	private static ToolSettingsScreen TOOL_SETTINGS;
	
	public static void init() {
		HudRenderCallback.EVENT.register(new HudRenderer());
	}
	
	public static ToolSettingsScreen getToolSettingsScreen() {
		if(TOOL_SETTINGS == null) TOOL_SETTINGS = new ToolSettingsScreen();
		return TOOL_SETTINGS;
	}

	@SuppressWarnings("resource")
	@Override
	public void onHudRender(DrawContext drawContext, float tickDelta) {
		if(MinecraftClient.getInstance().currentScreen != null) return;
		
		ToolState state = VoxEdit.active;
		if(state == null) return;
		
		var screen = getToolSettingsScreen();
		screen.render(drawContext, -1, -1, tickDelta);
	}
}
