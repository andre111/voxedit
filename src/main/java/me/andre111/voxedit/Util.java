package me.andre111.voxedit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class Util {
	public static boolean shouldUseCustomControlls(PlayerEntity player) {
		if(player != null && player.isCreative() && player.getAbilities().flying) {
			ItemStack stack = player.getMainHandStack();
			return stack.isOf(VoxEdit.TOOL_ITEM); // || stack.isOf(VoxEdit.EDITOR_ITEM);
		}
		return false;
	}
}
