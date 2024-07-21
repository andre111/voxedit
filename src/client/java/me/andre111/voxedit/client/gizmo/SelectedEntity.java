package me.andre111.voxedit.client.gizmo;

import java.util.Iterator;
import java.util.List;

import org.joml.Vector3f;

import me.andre111.voxedit.network.CPEntityMove;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class SelectedEntity extends Gizmo implements Positionable, RotatableFreeYaw {
	private final Entity entity;
	
	public SelectedEntity(Entity entity) {
		this.entity = entity;
	}
	
	@Override
	public Iterator<Box> iterator() {
		return List.of(entity.getBoundingBox()).iterator();
	}

	@Override
	public float getYaw() {
		return entity.getYaw();
	}

	@Override
	public void setYaw(float yaw) {
		entity.setYaw(yaw);
		ClientPlayNetworking.send(new CPEntityMove(entity.getUuid(), new Vector3f((float) entity.getPos().x, (float) entity.getPos().y, (float) entity.getPos().z), yaw));
	}

	@Override
	public Vec3d getPos() {
		return entity.getPos();
	}

	@Override
	public void setPos(Vec3d pos) {
		entity.refreshPositionAndAngles(pos, entity.getYaw(), entity.getPitch());
		ClientPlayNetworking.send(new CPEntityMove(entity.getUuid(), new Vector3f((float) pos.x, (float) pos.y, (float) pos.z), entity.getYaw()));
	}

	@Override
	public Text getName() {
		return entity.getName();
	}

	@Override
	public void addActions(GizmoActions actions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addHandles(List<GizmoHandle> handles) {
		addPosHandles(handles);
		addYawHandles(handles);
	}

	@Override
	public Vec3d getHandleOrigin() {
		return entity.getPos();
	}

}
