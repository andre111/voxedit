package me.andre111.voxedit.editor.action;

import net.minecraft.world.World;

public abstract class EditAction {
	public abstract int undo(World world);
	public abstract int redo(World world);
}
