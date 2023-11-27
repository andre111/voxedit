package me.andre111.voxedit.editor;

import java.util.function.Consumer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public class Editor {
	//TODO: combine "held" actions into one undo state
	public static int undoable(PlayerEntity player, World world, Consumer<Editable> edit) {
		Undo undo = Undo.of(player, world);
		UndoRecordingEditable editable = new UndoRecordingEditable(world, undo);
		edit.accept(editable);
		return editable.apply();
	}
}
