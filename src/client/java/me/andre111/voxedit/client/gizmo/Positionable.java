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

import java.util.List;

import net.minecraft.util.math.BlockPos;

public interface Positionable {
	
	public BlockPos getPos();
	public void setPos(BlockPos pos);
	
	public default void addPosHandles(List<GizmoHandle> handles) {
		handles.add(new GizmoHandle(GizmoHandle.Mode.XZ_PLANE, 0, 0, 2, 1/8f, new Action(this, 1, 0, 0))); // x-axis
		handles.add(new GizmoHandle(GizmoHandle.Mode.XY_PLANE, 0, 0, 1/8f, 2, new Action(this, 0, 1, 0))); // y-axis
		handles.add(new GizmoHandle(GizmoHandle.Mode.ZY_PLANE, 0, 0, 2, 1/8f, new Action(this, 0, 0, 1))); // z-axis
		
		handles.add(new GizmoHandle(GizmoHandle.Mode.XZ_PLANE, 1/4f, 1/4f, 1/2f, 1/2f, new Action(this, 1, 0, 1))); // xz-plane
		handles.add(new GizmoHandle(GizmoHandle.Mode.XY_PLANE, 1/4f, 1/4f, 1/2f, 1/2f, new Action(this, 1, 1, 0))); // xy-plane
		handles.add(new GizmoHandle(GizmoHandle.Mode.ZY_PLANE, 1/4f, 1/4f, 1/2f, 1/2f, new Action(this, 0, 1, 1))); // zy-plane
	}
	static class Action implements GizmoHandle.GizmoHandleAction {
		private final Positionable positionable;
		private BlockPos startPos;
		private float sx, sy, sz;
		private float mx, my, mz;
		
		Action(Positionable positionable, float mx, float my, float mz) {
			this.positionable = positionable;
			this.mx = mx;
			this.my = my;
			this.mz = mz;
		}
		
		@Override
		public void activate(float x, float y, float z) {
			startPos = positionable.getPos();
			sx = x;
			sy = y;
			sz = z;
		}
		
		@Override
		public void change(float x, float y, float z) {
			x -= sx;
			y -= sy;
			z -= sz;
			positionable.setPos(startPos.add(Math.round(x * mx), Math.round(y * my), Math.round(z * mz)));
		}

		@Override
		public void cancel() {
			positionable.setPos(startPos);
		}
	}
}
