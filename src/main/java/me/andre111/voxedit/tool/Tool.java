package me.andre111.voxedit.tool;

import java.util.List;
import java.util.Set;

import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.VoxEdit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class Tool {
	public abstract void rightClick(World world, PlayerEntity player, BlockHitResult target, ToolState state, Set<BlockPos> positions);
	public abstract void leftClick(World world, PlayerEntity player, BlockHitResult target, ToolState state, Set<BlockPos> positions);

	public Set<BlockPos> getBlockPositions(World world, BlockHitResult target, ToolState state) {
		return state.getBlockPositions(world, target);
	}
	
	public boolean usesMode() {
		return true;
	}
	
	public boolean usesShape() {
		return true;
	}
	
	public boolean usesRadius() {
		return true;
	}
	
	public boolean usesBlockPalette() {
		return true;
	}
	
	public boolean usesBlockFilter() {
		return true;
	}
	
	public List<ToolState> getCreativeMenuStates() {
		return List.of(ToolState.of(this));
	}
	
	public final Identifier id() {
		return VoxEdit.TOOL_REGISTRY.getId(this);
	}
	
	public final Text asText() {
		Identifier id = id();
		return Text.translatable("voxedit.tool."+id.toTranslationKey());
	}
}
