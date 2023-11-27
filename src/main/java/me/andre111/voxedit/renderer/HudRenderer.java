package me.andre111.voxedit.renderer;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.VoxEdit;
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
		var currentScreen = MinecraftClient.getInstance().currentScreen;
		if(currentScreen != null) {
			if(currentScreen == getToolSettingsScreen()) {
				int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
				int height =  MinecraftClient.getInstance().getWindow().getScaledHeight();
				drawContext.fillGradient(0, 0, width, height, 0xA0101010, 0xB0101010);
				drawContext.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, "<- Edit Tool Settings", width/2, height/2, 0xFFFFFFFF);
			}
			return;
		}
		
		ToolState state = VoxEdit.active;
		if(state == null) return;
		
		var screen = getToolSettingsScreen();
		screen.render(drawContext, -1, -1, tickDelta);
	}
}
