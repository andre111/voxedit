package me.andre111.voxedit.editor;

import java.util.ArrayList;
import java.util.List;

import me.andre111.voxedit.editor.action.EditAction;
import me.andre111.voxedit.editor.action.SetBlockAction;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UndoRecordingEditable implements Editable {
	private final World world;
	private final Undo undo;
	private final List<EditAction> actions;
	
	public UndoRecordingEditable(World world, Undo undo) {
		this.world = world;
		this.undo = undo;
		this.actions = new ArrayList<>();
	}

	@Override
	public void applyCurrentChanges() {
		for(EditAction action : actions) action.redo(world);
	}

	protected int apply() {
		if(actions.isEmpty()) return 0;
		int count = 0;
		for(EditAction action : actions) count += action.redo(world);
		undo.push(new UndoState(actions));
		return count;
	}

	@Override
	public void setBlock(BlockPos pos, BlockState state) {
		actions.add(new SetBlockAction(world, pos, state));
	}
}
