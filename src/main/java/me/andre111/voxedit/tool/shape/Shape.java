package me.andre111.voxedit.tool.shape;

import me.andre111.voxedit.VoxEdit;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public abstract class Shape {
	public abstract boolean contains(int x, int y, int z, Direction direction, int sizeX, int sizeY, int sizeZ);
	
	public Identifier id() {
		return VoxEdit.SHAPE_REGISTRY.getId(this);
	}
	
	public Text asText() {
		return Text.translatable("voxedit.shape."+id().toTranslationKey());
	}
}
