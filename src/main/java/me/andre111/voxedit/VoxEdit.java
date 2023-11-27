package me.andre111.voxedit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.include.com.google.common.base.Objects;

import me.andre111.voxedit.renderer.HudRenderer;
import me.andre111.voxedit.renderer.SelectionRenderer;
import me.andre111.voxedit.renderer.ToolRenderer;
import me.andre111.voxedit.tool.ToolItem;
import me.andre111.voxedit.tool.ToolItemBrush;
import me.andre111.voxedit.tool.ToolItemFill;
import me.andre111.voxedit.tool.ToolItemFlatten;
import me.andre111.voxedit.tool.ToolItemSmooth;

public class VoxEdit implements ModInitializer, ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("voxedit");
    
    public static final ToolItem TOOL_BRUSH = Registry.register(Registries.ITEM, new Identifier("voxedit", "tool_brush"), new ToolItemBrush());
    public static final ToolItem TOOL_SMOOTH = Registry.register(Registries.ITEM, new Identifier("voxedit", "tool_smooth"), new ToolItemSmooth());
    public static final ToolItem TOOL_FILL = Registry.register(Registries.ITEM, new Identifier("voxedit", "tool_fill"), new ToolItemFill());
    public static final ToolItem TOOL_FLATTEN = Registry.register(Registries.ITEM, new Identifier("voxedit", "tool_flatten"), new ToolItemFlatten());
    
    public static final KeyBinding INCREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.increaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, "key.category.voxedit"));
    public static final KeyBinding DECREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.decreaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, "key.category.voxedit"));
    public static final KeyBinding UNDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.undo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.voxedit"));
    public static final KeyBinding REDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.redo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.voxedit"));
    public static final KeyBinding OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.openMenu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "key.category.voxedit"));
    
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
    		server.getTickManager().setFrozen(true);
    	});
    	Networking.init();
	}

	@Override
	public void onInitializeClient() {
		BuiltinItemRendererRegistry.INSTANCE.register(TOOL_BRUSH, new ToolRenderer(false));
		BuiltinItemRendererRegistry.INSTANCE.register(TOOL_SMOOTH, new ToolRenderer(true));
		BuiltinItemRendererRegistry.INSTANCE.register(TOOL_FILL, new ToolRenderer(true));
		BuiltinItemRendererRegistry.INSTANCE.register(TOOL_FLATTEN, new ToolRenderer(true));
		HudRenderer.init();
		
		ItemGroupEvents.MODIFY_ENTRIES_ALL.register((group, entries) -> {
			if(group.getType() == ItemGroup.Type.CATEGORY && entries.shouldShowOpRestrictedItems()) {
				boolean hasCB = entries.getDisplayStacks().stream().filter(stack -> stack.getItem() == Items.COMMAND_BLOCK).findAny().isPresent();
				if(hasCB) {
					entries.add(TOOL_BRUSH.getDefaultStack());
					entries.add(TOOL_SMOOTH.getStackWith(ToolState.initial().withRadius(7)));
					entries.add(TOOL_FILL.getStackWith(ToolState.initial().withRadius(16)));
					entries.add(TOOL_FLATTEN.getStackWith(ToolState.initial().withRadius(6)));

					entries.add(TOOL_BRUSH.getStackWith(ToolState.initial().withMode(ToolState.Mode.PAINT_TOP).withRadius(5).withBlockPalette(new BlockPalette(Util.make(new ArrayList<>(), list -> {
						list.add(new BlockPalette.Entry(Blocks.GRASS_BLOCK.getDefaultState(), 1));
						list.add(new BlockPalette.Entry(Blocks.MOSS_BLOCK.getDefaultState(), 3));
						list.add(new BlockPalette.Entry(Blocks.GREEN_CONCRETE_POWDER.getDefaultState(), 2));
					})))));
					entries.add(TOOL_BRUSH.getStackWith(ToolState.initial().withMode(ToolState.Mode.SCATTER).withRadius(6).withBlockPalette(new BlockPalette(Util.make(new ArrayList<>(), list -> {
						list.add(new BlockPalette.Entry(Blocks.AIR.getDefaultState(), 20));
						list.add(new BlockPalette.Entry(Blocks.SHORT_GRASS.getDefaultState(), 5));
						list.add(new BlockPalette.Entry(Blocks.FERN.getDefaultState(), 5));
						list.add(new BlockPalette.Entry(Blocks.POPPY.getDefaultState(), 1));
						list.add(new BlockPalette.Entry(Blocks.DANDELION.getDefaultState(), 1));
					})))));
				}
			}
		});
		
    	ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
    		if(mc.world != null && mc.player != null) tickClient();
    	});
    	WorldRenderEvents.LAST.register((context) -> {
    		render(context.matrixStack(), context.tickDelta());
    	});
    	ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
    		ItemStack stack = player.getMainHandStack();
    		if(stack.getItem() instanceof ToolItem) {
    			if(player.isCreative() && MinecraftClient.getInstance().attackCooldown <= 0) {
    				MinecraftClient.getInstance().attackCooldown = 5;
    				Networking.clientSendCommand(Networking.Command.TOOL_LEFT_CLICK);
    			}
    			return true;
    		}
    		return false;
    	});
    	ServerEntityEvents.EQUIPMENT_CHANGE.register((livingEntity, equipmentSlot, previous, next) -> {
    		
    	});
	}
	
	public static ClientPlayerEntity player;
	public static ToolItem activeItem;
	public static ToolState active;
	public static BlockHitResult target;
	
	private static Set<BlockPos> positions;
	private static int ticks;
	
	@SuppressWarnings("resource")
	public static void tickClient() {
		ticks++;
		player = MinecraftClient.getInstance().player;
		
		ToolState oldActive = active;
		ToolItem oldActiveItem = activeItem;
		BlockHitResult oldTarget = target;
		active = null;
		activeItem = null;
		ItemStack stack = player.getMainHandStack();
		if(stack.getItem() instanceof ToolItem tool) {
			activeItem = tool;
			active = ToolItem.readState(stack);
		}
		
		if(active != null) {
			if(activeItem != oldActiveItem) { HudRenderer.getToolSettingsScreen().rebuild(); /*positions = null;*/ }
			if(!active.equals(oldActive)) { HudRenderer.getToolSettingsScreen().reload(); positions = null; }
			if(MinecraftClient.getInstance().currentScreen != null) return;
			
			target = getTargetOf(player, active);
			if(oldTarget == null || !Objects.equal(target.getBlockPos(), oldTarget.getBlockPos()) || !Objects.equal(target.getSide(), oldTarget.getSide())) {
				positions = null;
			}
			
			if(INCREASE_RADIUS.wasPressed()) {
				Networking.clientSendToolState(active.withRadius(Math.min(active.radius()+1, 16)));
			}
			if(DECREASE_RADIUS.wasPressed()) {
				Networking.clientSendToolState(active.withRadius(Math.max(1, active.radius()-1)));
			}
			if(OPEN_MENU.wasPressed()) {
				MinecraftClient.getInstance().setScreen(HudRenderer.getToolSettingsScreen());
			}
		}
		
		while(UNDO.wasPressed()) {
			if(Screen.hasControlDown()) Networking.clientSendCommand(Networking.Command.UNDO);
		}
		while(REDO.wasPressed()) {
			if(Screen.hasControlDown()) Networking.clientSendCommand(Networking.Command.REDO);
		}
	}
	
	@SuppressWarnings("resource")
	public static void render(MatrixStack matrices, float frame) {
		if(activeItem != null && active != null && target != null) {
			if(ticks % 200 == 0 || positions == null) {
				ticks++; // just increase the value to avoid recalculation in further frames during same tick
				positions = activeItem.getBlockPositions(MinecraftClient.getInstance().world, target, active);
			}
            
            SelectionRenderer.render(positions, matrices, frame);
		}
	}
	
	public static BlockHitResult getTargetOf(Entity e, ToolState state) {
		HitResult result = e.raycast(64, 0, state.targetFluids());
		if(result instanceof BlockHitResult blockHit) {
			return blockHit;
		}
		return null;
	}
}
