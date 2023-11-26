package me.andre111.voxedit.editor.action;

import net.minecraft.world.World;

public abstract class EditAction {
	public abstract void undo(World world);
	public abstract void redo(World world);
}
