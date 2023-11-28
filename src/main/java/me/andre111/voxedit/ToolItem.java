package me.andre111.voxedit;

import java.util.Set;

import me.andre111.voxedit.editor.Editor;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ToolItem extends Item {
	public ToolItem() {
		super(new Item.Settings().maxCount(1));
	}
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && world instanceof ServerWorld serverWorld && player.isCreative()) {
			if(use(serverWorld, player, hand, true)) {
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			return TypedActionResult.fail(player.getStackInHand(hand));
		}
		return TypedActionResult.consume(player.getStackInHand(hand));
	}
	
	public void leftClicked(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && world instanceof ServerWorld serverWorld  && player.isCreative()) {
			use(serverWorld, player, hand, false);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <TC extends ToolConfig, T extends Tool<TC, T>> boolean use(ServerWorld serverWorld, PlayerEntity player, Hand hand, boolean rightClick) {
		ConfiguredTool<TC, T> tool = (ConfiguredTool<TC, T>) readTool(player.getStackInHand(hand));
		BlockHitResult target = VoxEdit.getTargetOf(player, tool.config());
		if(tool != null && target != null) {
			Set<BlockPos> positions = tool.tool().getBlockPositions(serverWorld, target, tool.config());
			if(positions.isEmpty()) {
				player.sendMessage(Text.of("No valid positions"), true);
				return false;
			}
			
			int count = 0;
			if(rightClick) {
				count = Editor.undoable(player, serverWorld, (editable) -> tool.tool().rightClick(editable, player, target, tool.config(), positions));
			} else {
				count = Editor.undoable(player, serverWorld, (editable) -> tool.tool().leftClick(editable, player, target, tool.config(), positions));
			}
			if(count > 0) player.sendMessage(Text.of("Set "+count+" blocks"), true);
			
			return true;
		}
		return false;
	}
	
	@Override
	public ItemStack getDefaultStack() {
		return getStackWith(VoxEdit.DEFAULT_TOOL);
	}
	
	public ItemStack getStackWith(ConfiguredTool<?, ?> tool) {
		ItemStack stack = super.getDefaultStack();
		storeTool(stack, tool);
		return stack;
	}
	
	public static ConfiguredTool<?, ?> readTool(ItemStack stack) {
		var dataResult = ConfiguredTool.CODEC.decode(NbtOps.INSTANCE, stack.getOrCreateSubNbt("voxedit:tool"));
		if(dataResult.result().isPresent()) {
			return dataResult.result().get().getFirst();
		}
		return VoxEdit.DEFAULT_TOOL;
	}
	
	public static void storeTool(ItemStack stack, ConfiguredTool<?, ?> ct) {
		stack.setSubNbt("voxedit:tool", ConfiguredTool.CODEC.encodeStart(NbtOps.INSTANCE, ct).result().get());
	}
}
