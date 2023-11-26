package me.andre111.voxedit;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class ToolRenderer implements DynamicItemRenderer {
	private static final MatrixStack.Entry BLOCK_POSE;
	static {
		MatrixStack matrices = new MatrixStack();
		matrices.translate(0.45f, 0.2f, 0.0f);
		matrices.multiply(new AxisAngle4f((float) (30 * Math.PI / 180), 1, 0, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.multiply(new AxisAngle4f((float) (45 * Math.PI / 180), 0, 1, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.scale(0.3f, 0.3f, 0.3f);
		BLOCK_POSE = matrices.peek();
	}
	
	@SuppressWarnings("resource")
	@Override
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if(mode != ModelTransformationMode.GUI) return;
		
		DrawContext context = new DrawContext(MinecraftClient.getInstance(), (Immediate) vertexConsumers);
		context.getMatrices().translate(matrices.peek().getPositionMatrix().m30(), matrices.peek().getPositionMatrix().m31(), matrices.peek().getPositionMatrix().m32());
		
		ToolState state = VoxEdit.TOOL.readState(stack);
		
		//TODO: render shape
		switch(state.shape) {
		case CUBE:
			break;
		case SPHERE:
			break;
		default:
			break;
		}
		
		// render size
		context.getMatrices().push();
		context.getMatrices().translate(1, 1 - 16, 200);
		context.drawText(MinecraftClient.getInstance().textRenderer, state.radius+"", 0, 0, 0xFFFFFF, true);
		context.getMatrices().pop();
		
		// render selected block
		matrices.push();
		matrices.multiplyPositionMatrix(BLOCK_POSE.getPositionMatrix());
		MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state.blockState, matrices, vertexConsumers, light, overlay);
		matrices.pop();
	}
}
