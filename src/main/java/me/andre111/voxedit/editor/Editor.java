package me.andre111.voxedit.editor;

import java.util.function.Consumer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class Editor {
	//TODO: combine "held" actions into one undo state
	public static int undoable(PlayerEntity player, ServerWorld world, Consumer<UndoRecordingStructureWorldAccess> edit) {
		Undo undo = Undo.of(player, world);
		UndoRecordingStructureWorldAccess worldAccess = new UndoRecordingStructureWorldAccess(world, undo);
		edit.accept(worldAccess);
		return worldAccess.apply();
	}
}
