package me.andre111.voxedit.tool;

import me.andre111.voxedit.ToolState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public abstract class ToolItem extends Item {
	private final boolean usesMode;
	private final boolean usesShape;
	private final boolean usesRadius;
	private final boolean usesBlockPalette;
	private final boolean usesBlockFilter;

	public ToolItem(boolean usesMode, boolean usesShape, boolean usesRadius, boolean usesBlockPalette, boolean usesBlockFilter) {
		super(new Item.Settings().maxCount(1));
		this.usesMode = usesMode;
		this.usesShape = usesShape;
		this.usesRadius = usesRadius;
		this.usesBlockPalette = usesBlockPalette;
		this.usesBlockFilter = usesBlockFilter;
	}
	
	@Override
    public abstract TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand);
	
	public abstract void leftClicked(World world, PlayerEntity player, Hand hand);
	
	@Override
	public ItemStack getDefaultStack() {
		ItemStack stack = super.getDefaultStack();
		storeState(stack, ToolState.initial());
		return stack;
	}
	
	public boolean usesMode() {
		return usesMode;
	}
	
	public boolean usesShape() {
		return usesShape;
	}
	
	public boolean usesRadius() {
		return usesRadius;
	}
	
	public boolean usesBlockPalette() {
		return usesBlockPalette;
	}
	
	public boolean usesBlockFilter() {
		return usesBlockFilter;
	}
	
	public static ToolState readState(ItemStack stack) {
		return ToolState.CODEC.decode(NbtOps.INSTANCE, stack.getOrCreateSubNbt("voxedit:toolstate")).result().get().getFirst();
	}
	
	public static void storeState(ItemStack stack, ToolState state) {
		stack.setSubNbt("voxedit:toolstate", ToolState.CODEC.encodeStart(NbtOps.INSTANCE, state).result().get());
	}
}
