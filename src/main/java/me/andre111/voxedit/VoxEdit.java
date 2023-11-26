package me.andre111.voxedit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.blaze3d.systems.RenderSystem;

import me.andre111.voxedit.tool.ToolItem;

public class VoxEdit implements ModInitializer, ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("voxedit");
    
    public static final ToolItem TOOL = Registry.register(Registries.ITEM, new Identifier("voxedit", "tool"), new ToolItem(new Item.Settings().maxCount(1)));
    
    public static final KeyBinding INCREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.increaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_UP, "key.category.voxedit"));
    public static final KeyBinding DECREASE_RADIUS = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.decreaseRadius", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PAGE_DOWN, "key.category.voxedit"));
    public static final KeyBinding UNDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.undo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Z, "key.category.voxedit"));
    public static final KeyBinding REDO = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.voxedit.redo", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.category.voxedit"));
    
	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
    		server.getTickManager().setFrozen(true);
    	});
    	Networking.init();
	}

	@Override
	public void onInitializeClient() {
		BuiltinItemRendererRegistry.INSTANCE.register(TOOL, new ToolRenderer());
		
    	ClientTickEvents.START_CLIENT_TICK.register((mc) -> {
    		if(mc.world != null && mc.player != null) tickClient();
    	});
    	WorldRenderEvents.LAST.register((context) -> {
    		render(context.matrixStack(), context.tickDelta());
    	});
    	ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
    		ItemStack stack = player.getMainHandStack();
    		if(stack.isOf(TOOL)) {
    			if(player.isCreative() && MinecraftClient.getInstance().attackCooldown <= 0) {
    				MinecraftClient.getInstance().attackCooldown = 5;
    				Networking.clientSendCommand(Networking.Command.TOOL_LEFT_CLICK);
    			}
    			return true;
    		}
    		return false;
    	});
	}
	
	private static ClientPlayerEntity player;
	private static ToolState active;
	private static BlockPos target;
	
	@SuppressWarnings("resource")
	public static void tickClient() {
		player = MinecraftClient.getInstance().player;
		active = null;
		ItemStack stack = player.getMainHandStack();
		if(stack.isOf(TOOL)) active = TOOL.readState(stack);
		
		if(active != null) {
			target = getTargetOf(player, active);
			
			if(INCREASE_RADIUS.wasPressed()) {
				Networking.clientSendToolState(active.withRadius(Math.min(active.radius+1, 16)));
			}
			if(DECREASE_RADIUS.wasPressed()) {
				Networking.clientSendToolState(active.withRadius(Math.max(1, active.radius-1)));
			}
		}
		
		while(UNDO.wasPressed()) {
			Networking.clientSendCommand(Networking.Command.UNDO);
		}
		while(REDO.wasPressed()) {
			Networking.clientSendCommand(Networking.Command.REDO);
		}
	}
	
	@SuppressWarnings("resource")
	public static void render(MatrixStack matrices, float frame) {
		if(active != null && target != null) {
            Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
            Vec3d camPos = camera.getPos();
            
            VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            var positions = active.getBlockPositions(MinecraftClient.getInstance().world, target);
            
            // render blocks
            /*
           	for(BlockPos pos : positions) {
    			poseStack.pushPose();
                poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
                
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(active.blockState, poseStack, bs, Brightness.FULL_BRIGHT.pack(), OverlayTexture.NO_OVERLAY);
                
                poseStack.popPose();
            }
            bs.endLastBatch();
            */

            float expand = 0.01f;
            float one = 1 + expand * 2;
            
            // render outlines
            VertexConsumer lineConsumer = consumer.getBuffer(RenderLayer.getLines());
        	Matrix4f matrix4f = matrices.peek().getPositionMatrix();
            Matrix3f matrix3f = matrices.peek().getNormalMatrix();
            
            for(BlockPos pos : positions) {
                boolean up = positions.contains(pos.offset(Direction.UP));
                boolean down = positions.contains(pos.offset(Direction.DOWN));
                boolean north = positions.contains(pos.offset(Direction.NORTH));
                boolean east = positions.contains(pos.offset(Direction.EAST));
                boolean south = positions.contains(pos.offset(Direction.SOUTH));
                boolean west = positions.contains(pos.offset(Direction.WEST));

                float x = (float) (pos.getX() - camPos.x)-expand;
                float y = (float) (pos.getY() - camPos.y)-expand;
                float z = (float) (pos.getZ() - camPos.z)-expand;
                
                // top face
                if(!up) {
                	if(!north) {
                        lineConsumer.vertex(matrix4f, x, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                        lineConsumer.vertex(matrix4f, x+one, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                	}
                	if(!east) {
                        lineConsumer.vertex(matrix4f, x+one, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                        lineConsumer.vertex(matrix4f, x+one, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                	}
                	if(!south) {
                        lineConsumer.vertex(matrix4f, x, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                        lineConsumer.vertex(matrix4f, x+one, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                	}
                	if(!west) {
                        lineConsumer.vertex(matrix4f, x, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                        lineConsumer.vertex(matrix4f, x, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                	}
                }
                

                // bottom face
                if(!down) {
                	if(!north) {
                        lineConsumer.vertex(matrix4f, x, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                        lineConsumer.vertex(matrix4f, x+one, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                	}
                	if(!east) {
                        lineConsumer.vertex(matrix4f, x+one, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                        lineConsumer.vertex(matrix4f, x+one, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                	}
                	if(!south) {
                        lineConsumer.vertex(matrix4f, x, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                        lineConsumer.vertex(matrix4f, x+one, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                	}
                	if(!west) {
                        lineConsumer.vertex(matrix4f, x, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                        lineConsumer.vertex(matrix4f, x, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                	}
                }
                
                // sides
                if(!north) {
                	if(!east) {
                        lineConsumer.vertex(matrix4f, x+one, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                        lineConsumer.vertex(matrix4f, x+one, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                	}
                	if(!west) {
                        lineConsumer.vertex(matrix4f, x, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                        lineConsumer.vertex(matrix4f, x, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                	}
                }
                if(!south) {
                	if(!east) {
                        lineConsumer.vertex(matrix4f, x+one, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                        lineConsumer.vertex(matrix4f, x+one, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                	}
                	if(!west) {
                        lineConsumer.vertex(matrix4f, x, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                        lineConsumer.vertex(matrix4f, x, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                	}
                }
            }
            consumer.drawCurrentLayer();
            
            VertexConsumer quadConsumer = consumer.getBuffer(RenderLayer.getDebugQuads());
            for(BlockPos pos : positions) {
                boolean up = positions.contains(pos.offset(Direction.UP));
                boolean down = positions.contains(pos.offset(Direction.DOWN));
                boolean north = positions.contains(pos.offset(Direction.NORTH));
                boolean east = positions.contains(pos.offset(Direction.EAST));
                boolean south = positions.contains(pos.offset(Direction.SOUTH));
                boolean west = positions.contains(pos.offset(Direction.WEST));
                
                float x = (float) (pos.getX() - camPos.x)-expand;
                float y = (float) (pos.getY() - camPos.y)-expand;
                float z = (float) (pos.getZ() - camPos.z)-expand;

                if(!up && y < 0) {
                	quadConsumer.vertex(matrix4f, x, y+one, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y+one, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y+one, z+one).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x, y+one, z+one).color(1, 1, 1, 0.25f).next();
                }
                if(!down && y > 0) {
                	quadConsumer.vertex(matrix4f, x, y, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y, z+one).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x, y, z+one).color(1, 1, 1, 0.25f).next();
                }
                if(!north && z > 0) {
                	quadConsumer.vertex(matrix4f, x, y, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y+one, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x, y+one, z).color(1, 1, 1, 0.25f).next();
                }
                if(!east && x < 0) {
                	quadConsumer.vertex(matrix4f, x+one, y, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y+one, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y+one, z+one).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y, z+one).color(1, 1, 1, 0.25f).next();
                }
                if(!south && z < 0) {
                	quadConsumer.vertex(matrix4f, x, y, z+one).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y, z+one).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x+one, y+one, z+one).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x, y+one, z+one).color(1, 1, 1, 0.25f).next();
                }
                if(!west && x > 0) {
                	quadConsumer.vertex(matrix4f, x, y, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x, y+one, z).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x, y+one, z+one).color(1, 1, 1, 0.25f).next();
                	quadConsumer.vertex(matrix4f, x, y, z+one).color(1, 1, 1, 0.25f).next();
                }
            }
            consumer.drawCurrentLayer();
            
			RenderSystem.disableBlend();
		}
	}
	
	public static BlockPos getTargetOf(Entity e, ToolState state) {
		HitResult result = e.raycast(40, 0, state.targetFluids);
		if(result instanceof BlockHitResult blockHit) {
			return blockHit.getBlockPos();
		}
		return null;
	}
}
