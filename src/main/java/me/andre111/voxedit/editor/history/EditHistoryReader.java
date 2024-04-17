package me.andre111.voxedit.editor.history;

import java.util.ArrayList;
import java.util.List;

import io.netty.handler.codec.DecoderException;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.action.EditAction;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;

public class EditHistoryReader {
	private List<EditStats> stats = new ArrayList<>();
	private int statsIndex = -1;
	
	private List<EditAction.Type<?>> actionPalette;
	private int[] actions;
	
	private byte[] flags;
	private int flagIndex = -1;
	
	private List<BlockState> blockStatePalette;
	private int[] blockStates;
	private int blockStateIndex = -1;
	
	private long[] blockPositions;
	private int blockPositionIndex = -1;
	
	private NbtList nbtCompounds;
	private int nbtCompoundIndex = -1;
	
	
	public EditHistoryReader(NbtCompound root) {
		stats = EditStats.WITHOUT_SCHEMATIC_CODEC.listOf().decode(NbtOps.INSTANCE, root.get("stats")).getOrThrow((error) -> new DecoderException("Failed to decode 'stats' in edit history: " + error)).getFirst();
		
		actionPalette = VoxEdit.ACTION_TYPE_REGISTRY.getCodec().listOf().decode(NbtOps.INSTANCE, root.get("actionPalette")).getOrThrow((error) -> new DecoderException("Failed to decode 'actionPalette' in edit history: " + error)).getFirst();
		actions = root.getIntArray("actions");
		
		flags = root.getByteArray("flags");
		
		blockStatePalette = BlockState.CODEC.listOf().decode(NbtOps.INSTANCE, root.get("blockStatePalette")).getOrThrow((error) -> new DecoderException("Failed to decode 'blockStatePalette' in edit history: " + error)).getFirst();
		blockStates = root.getIntArray("blockStates");
		
		blockPositions = root.getLongArray("blockPositions");
		
		nbtCompounds = root.getList("nbt", NbtElement.COMPOUND_TYPE);
	}
	
	public EditStats readStats() {
		return stats.get(++statsIndex);
	}
	
	public List<EditAction<?>> readActions() {
		List<EditAction<?>> actionList = new ArrayList<>();
		for(int actionTypeIndex : actions) {
			EditAction.Type<?> actionType = actionPalette.get(actionTypeIndex);
			EditAction<?> action = actionType.read(this);
			actionList.add(action);
		}
		return actionList;
	}
	
	public byte readFlag() {
		return flags[++flagIndex];
	}
	
	public boolean readFlagBoolean() {
		return readFlag() == 0 ? false : true;
	}
	
	public BlockState readBlockState() {
		return blockStatePalette.get(blockStates[++blockStateIndex]);
	}
	
	public BlockPos readBlockPos() {
		return BlockPos.fromLong(blockPositions[++blockPositionIndex]);
	}
	
	public NbtCompound readNbt() {
		return nbtCompounds.getCompound(++nbtCompoundIndex);
	}
}
