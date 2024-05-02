package me.andre111.voxedit.client.gizmo;

import java.util.Iterator;
import java.util.List;

import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.renderer.SchematicRenderer;
import me.andre111.voxedit.client.renderer.SchematicView;
import me.andre111.voxedit.network.CPPlaceSchematic;
import me.andre111.voxedit.schematic.Schematic;
import me.andre111.voxedit.schematic.SchematicInfo;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public class SchematicPlacement extends Gizmo implements Positionable, Rotatable90Deg, Renderable {
	private final String name;
	private BlockPos pos;
	private BlockRotation rotation;
	
	private SchematicRenderer renderer;
	private SchematicInfo info;
	
	public SchematicPlacement(String name, BlockPos pos) {
		this.name = name;
		this.pos = pos;
		this.rotation = BlockRotation.NONE;
	}
	
	public String getSchematicName() {
		return name;
	}

	@Override
	public BlockPos getPos() {
		return pos;
	}

	@Override
	public void setPos(BlockPos pos) {
		this.pos = pos;
		modified();
	}

	@Override
	public BlockRotation getRotation() {
		return rotation;
	}

	@Override
	public void setRotation(BlockRotation rotation) {
		this.rotation = rotation;
		if(renderer != null) renderer.close();
		renderer = null;
		modified();
	}

	@Override
	public void render(WorldRenderContext context) {
		render(context, true);
	}

	@Override
	public Text getName() {
		return Text.translatable("voxedit.schematic.name", name);
	}

	@Override
	public void addActions(GizmoActions actions) {
		actions.add(Text.translatable("voxedit.gizmo.delete"), () -> EditorState.removeGizmo(this));
		actions.add(Text.translatable("voxedit.gizmo.place"), () -> {
			ClientPlayNetworking.send(new CPPlaceSchematic(name, pos, rotation));
			EditorState.removeGizmo(this);
		});
	}

	@Override
	public Iterator<Box> iterator() {
		return List.of(new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX()+info.sizeX(), pos.getY()+info.sizeY(), pos.getZ()+info.sizeZ())).iterator();
	}
	
	public void render(WorldRenderContext context, boolean xRay) {
		SchematicRenderer sr = getRenderer();
		if(sr == null) return;
		
		sr.draw(pos, context.camera().getPos(), context.frustum(), context.positionMatrix(), context.projectionMatrix(), xRay);
		var consumer = context.consumers().getBuffer(RenderLayer.getLines());
		double x = pos.getX() - context.camera().getPos().x;
		double y = pos.getY() - context.camera().getPos().y;
		double z = pos.getZ() - context.camera().getPos().z;
		
		float r = EditorState.selected() == this ? 1f : 0f;
		float g = EditorState.selected() == this ? 1f : 0f;
		float b = EditorState.selected() == this ? 1f : 0f;
		WorldRenderer.drawBox(context.matrixStack(), consumer, x, y, z, x+info.sizeX(), y+info.sizeY(), z+info.sizeZ(), r, g, b, 1);
	}
	
	public SchematicRenderer getRenderer() {
		if(renderer == null) {
			Schematic schematic = EditorState.schematic(name);
			if(schematic != null) {
				schematic = schematic.rotated(MinecraftClient.getInstance().world.getRegistryManager(), rotation);
				
				renderer = new SchematicRenderer(new SchematicView(BlockPos.ORIGIN, schematic));
				info = schematic.getInfo();
			}
		}
		return renderer;
	}
	
	public void close() {
		if(renderer != null) {
			renderer.close();
			renderer = null;
		}
	}
}
