package me.andre111.voxedit.client.gizmo;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.Vec2f;

public interface RotatableFreeYaw {
	public float getYaw();
	public void setYaw(float yaw);
	

	public default void addYawHandles(List<GizmoHandle> handles) {
		Action action = new Action(this);
		
		handles.add(new GizmoHandle(GizmoHandle.Mode.XZ_PLANE, 1.5f, 1.5f, 0.25f, 0.25f, action));
		
		handles.add(new GizmoHandle(GizmoHandle.Mode.XZ_PLANE, 1.5f+0.25f, 1.5f-0.125f, 0.125f, 0.25f, action));
		handles.add(new GizmoHandle(GizmoHandle.Mode.XZ_PLANE, 1.5f-0.125f, 1.5f+0.25f, 0.25f, 0.125f, action));
		
		handles.add(new GizmoHandle(GizmoHandle.Mode.XZ_PLANE, 1.5f+0.375f, 1.5f-0.5f, 0.125f, 0.5f, action));
		handles.add(new GizmoHandle(GizmoHandle.Mode.XZ_PLANE, 1.5f-0.5f, 1.5f+0.375f, 0.5f, 0.125f, action));
	}
	
	class Action implements GizmoHandle.GizmoHandleAction {
		private final RotatableFreeYaw rot;
		private float startYaw;
		private Vec2f startVec;
		
		Action(RotatableFreeYaw rot) {
			this.rot = rot;
		}

		@Override
		public void activate(float x, float y, float z) {
			startYaw = rot.getYaw();
			startVec = new Vec2f(x, z);
		}

		@Override
		public void change(float x, float y, float z) {
			Vec2f vec = new Vec2f(x, z);
			
			float dot = startVec.dot(vec);
			float det = startVec.x*vec.y - startVec.y*vec.x;
			float angle = (float) Math.atan2(det, dot);
		
			float newYaw = startYaw + angle;
			
			// snapping
			if(Screen.hasShiftDown()) {
				newYaw = (float) (Math.round(newYaw / (Math.PI/4)) * Math.PI / 4);
			}
			
			rot.setYaw(newYaw);
		}

		@Override
		public void cancel() {
			rot.setYaw(startYaw);
		}
		
	}
}
