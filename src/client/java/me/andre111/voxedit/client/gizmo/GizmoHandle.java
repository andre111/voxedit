/*
 * Copyright (c) 2024 André Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.andre111.voxedit.client.gizmo;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public record GizmoHandle(Mode mode, float start1, float start2, float size1, float size2, GizmoHandleAction action) {
	public static enum Mode {
		XZ_PLANE,
		XY_PLANE,
		ZY_PLANE;
	}
	public static interface GizmoHandleAction {
		public void activate(float x, float y, float z);
		public void change(float x, float y, float z);
		public void cancel();
	}
	
	private static final RenderLayer VOXEDIT_GIZMO_HANDLE = RenderLayer.of("voxedit_gizmo_handle", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 1536, false, true, RenderLayer.MultiPhaseParameters.builder().program(RenderLayer.COLOR_PROGRAM).layering(RenderLayer.VIEW_OFFSET_Z_LAYERING).transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY).cull(RenderLayer.ENABLE_CULLING).depthTest(RenderLayer.ALWAYS_DEPTH_TEST).build(false));
	public void render(WorldRenderContext context, Gizmo gizmo) {
		Vec3d gho = gizmo.getHandleOrigin().subtract(context.camera().getPos());
		float gx = (float) gho.x;
		float gy = (float) gho.y;
		float gz = (float) gho.z;
		float scale = (float) MathHelper.clamp(MathHelper.sqrt(gx*gx + gy*gy + gz*gz) / 10.0, 1, 16);
				
		VertexConsumer vertexConsumer = context.consumers().getBuffer(VOXEDIT_GIZMO_HANDLE);
		
		switch(mode) {
		case XZ_PLANE:
			WorldRenderer.renderFilledBoxFace(context.matrixStack(), vertexConsumer, Direction.UP, gx+start1*scale, gy, gz+start2*scale, gx+(start1+size1)*scale, gy, gz+(start2+size2)*scale, 1, 0, 0, 1);
			WorldRenderer.renderFilledBoxFace(context.matrixStack(), vertexConsumer, Direction.DOWN, gx+start1*scale, gy, gz+start2*scale, gx+(start1+size1)*scale, gy, gz+(start2+size2)*scale, 1, 0, 0, 1);
			break;
		case XY_PLANE:			
			WorldRenderer.renderFilledBoxFace(context.matrixStack(), vertexConsumer, Direction.NORTH, gx+start1*scale, gy+start2*scale, gz, gx+(start1+size1)*scale, gy+(start2+size2)*scale, gz, 0, 1, 0, 1);
			WorldRenderer.renderFilledBoxFace(context.matrixStack(), vertexConsumer, Direction.SOUTH, gx+start1*scale, gy+start2*scale, gz, gx+(start1+size1)*scale, gy+(start2+size2)*scale, gz, 0, 1, 0, 1);
			break;
		case ZY_PLANE:			
			WorldRenderer.renderFilledBoxFace(context.matrixStack(), vertexConsumer, Direction.EAST, gx, gy+start2*scale, gz+start1*scale, gx, gy+(start2+size2)*scale, gz+(start1+size1)*scale, 0, 0, 1, 1);
			WorldRenderer.renderFilledBoxFace(context.matrixStack(), vertexConsumer, Direction.WEST, gx, gy+start2*scale, gz+start1*scale, gx, gy+(start2+size2)*scale, gz+(start1+size1)*scale, 0, 0, 1, 1);
			break;
		}
	}
}
