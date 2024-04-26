package me.andre111.voxedit.client.data;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public interface Renderable {
	public void render(WorldRenderContext context);
}
