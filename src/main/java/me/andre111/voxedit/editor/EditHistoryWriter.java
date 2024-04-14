package me.andre111.voxedit.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.action.EditAction;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;

public class EditHistoryWriter {
	private List<EditStats> stats = new ArrayList<>();
	
	private List<EditAction.Type<?>> actionPalette = new ArrayList<>();
	private Map<EditAction.Type<?>, Integer> actionPaletteMap = new HashMap<>();
	private List<Integer> actions = new ArrayList<>();
	private List<Byte> flags = new ArrayList<>();
	
	private List<BlockState> blockStatePalette = new ArrayList<>();
	private Map<BlockState, Integer> blockStatePaletteMap = new HashMap<>();
	private List<Integer> blockStates = new ArrayList<>();
	
	private List<Long> blockPositions = new ArrayList<>();
	
	private List<NbtCompound> nbtCompounds = new ArrayList<>();
	
	public void writeStats(EditStats editStats) {
		stats.add(editStats);
	}
	
	//TODO: rewrite to avoid warnings
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void writeActions(List<EditAction<?>> actions) {
		for(EditAction action : actions) {
			writeAction(action);
		}
	}
	
	public <A extends EditAction<A>> void writeAction(A action) {
		int index = actionPaletteMap.computeIfAbsent(action.type(), type -> {
			actionPalette.add(type);
			return actionPalette.size()-1;
		});
		actions.add(index);
		action.type().write(action, this);
	}
	
	public void writeFlag(byte flag) {
		flags.add(flag);
	}

	public void writeFlag(boolean flag) {
		flags.add(flag ? (byte) 1 : (byte) 0);
	}
	
	public void writeBlockState(BlockState state) {
		int index = blockStatePaletteMap.computeIfAbsent(state, s -> {
			blockStatePalette.add(state);
			return blockStatePalette.size()-1;
		});
		blockStates.add(index);
	}
	
	public void writeBlockPos(BlockPos pos) {
		blockPositions.add(pos.asLong());
	}
	
	public void writeNbt(NbtCompound nbt) {
		nbtCompounds.add(nbt);
	}
	
	public NbtCompound toNbt() {
		NbtCompound root = new NbtCompound();

		root.put("stats", EditStats.WITHOUT_SCHEMATIC_CODEC.listOf().encodeStart(NbtOps.INSTANCE, stats).result().get());
		
		root.put("actionPalette", VoxEdit.ACTION_TYPE_REGISTRY.getCodec().listOf().encodeStart(NbtOps.INSTANCE, actionPalette).result().get());
		root.put("actions", new NbtIntArray(actions));
		
		root.put("flags", new NbtByteArray(flags));
		
		root.put("blockStatePalette", BlockState.CODEC.listOf().encodeStart(NbtOps.INSTANCE, blockStatePalette).result().get());
		root.put("blockStates", new NbtIntArray(blockStates));
		
		root.put("blockPositions", new NbtLongArray(blockPositions));
		
		NbtList nbtList = new NbtList();
		nbtList.addAll(nbtCompounds);
		root.put("nbt", nbtList);

		return root;
	}
}
