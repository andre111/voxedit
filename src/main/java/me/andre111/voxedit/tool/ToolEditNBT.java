package me.andre111.voxedit.tool;

import java.util.List;

import me.andre111.voxedit.editor.EditType;
import me.andre111.voxedit.editor.Editor;
import me.andre111.voxedit.editor.action.ModifyEntityAction;
import me.andre111.voxedit.network.ServerNetworking;
import me.andre111.voxedit.state.ServerState;
import me.andre111.voxedit.tool.data.Context;
import me.andre111.voxedit.tool.data.RaycastTargets;
import me.andre111.voxedit.tool.data.Target;
import me.andre111.voxedit.tool.data.ToolConfig;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class ToolEditNBT extends Tool {

	public ToolEditNBT() {
		super(Properties.of().create());
	}

	@Override
	public RaycastTargets getRaycastTargets(ToolConfig config) {
		return RaycastTargets.BLOCKS_AND_ENTITIES;
	}

	@Override
	public void performAction(ServerPlayerEntity player, Action action, List<Target> targets, Context context, ToolConfig config, ServerState state) {
		// TODO Auto-generated method stub
		if(targets.size() != 1) return;
		Target target = targets.get(0);
		
		if(target.entity().isPresent()) {
			Entity entity = player.getServerWorld().getEntity(target.getEntity());
			if(entity == null) return;
			
			NbtCompound oldNbt = entity.writeNbt(new NbtCompound());
			ServerNetworking.serverSendOpenNBTEditor(player, oldNbt, (nbt) -> {
				if(nbt.equals(oldNbt)) return;
				
				Editor.undoableAction(player, player.getServerWorld(), Text.translatable("item.voxedit.editor"), new ModifyEntityAction(entity.getUuid(), oldNbt, nbt)).inform(player, EditType.PERFORM);
			});
		} else if(target.pos().isPresent()) {
			BlockPos targetPos = target.getBlockPos();
    		BlockEntity be = player.getServerWorld().getBlockEntity(targetPos);
    		if(be == null) return;
    		
    			NbtCompound oldNbt = be.createNbtWithId(player.getServerWorld().getRegistryManager());
			ServerNetworking.serverSendOpenNBTEditor(player, be.createNbt(player.getServerWorld().getRegistryManager()), (nbt) -> {
				if(nbt.equals(oldNbt)) return;
				
				Editor.undoable(player, player.getServerWorld(), Text.translatable("item.voxedit.editor"), (editable) -> {
					BlockEntity newBe = editable.getBlockEntity(targetPos);
					if(newBe != null) newBe.read(nbt, player.getServerWorld().getRegistryManager());
				}, null, false).inform(player, EditType.PERFORM);
			});
		}
	}

}
