package me.andre111.voxedit.renderer;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.ToolItem;
import me.andre111.voxedit.gui.Textures;
import me.andre111.voxedit.tool.ConfiguredTool;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

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
	
	@Override
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if(mode != ModelTransformationMode.GUI) return;
		
		DrawContext context = new DrawContext(MinecraftClient.getInstance(), (Immediate) vertexConsumers);
		context.getMatrices().translate(matrices.peek().getPositionMatrix().m30(), matrices.peek().getPositionMatrix().m31(), matrices.peek().getPositionMatrix().m32());
        context.drawGuiTexture(Textures.TOOL, 0, -16, 16, 16);
		
		ConfiguredTool<?, ?> tc = ToolItem.readTool(stack);
		
		// render information
		context.getMatrices().push();
		context.getMatrices().translate(1, 1 - 16, 0);
		context.getMatrices().scale(0.35f, 0.35f, 0.35f);
		int textY = 0;
		context.drawText(MinecraftClient.getInstance().textRenderer, tc.tool().asText(), 0, textY, 0xFFFFFF, true); 
		textY += 10;
		for(Text text : tc.config().getIconTexts()) {
			context.drawText(MinecraftClient.getInstance().textRenderer, text, 0, textY, 0xFFFFFF, true); 
			textY += 10;
		}
		context.getMatrices().pop();
		
		// render palette
		BlockPalette palette = tc.config().getIconBlocks();
		if(palette != null) {
			matrices.push();
			context.getMatrices().translate(0, 0, 200);
			matrices.multiplyPositionMatrix(BLOCK_POSE.getPositionMatrix());
			int blockStateIndex = (int) ((System.currentTimeMillis()) / (1000 * 2)) % palette.size();
			BlockState blockState = palette.get(blockStateIndex);
			MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(blockState, matrices, vertexConsumers, light, overlay);
			matrices.pop();
		}
	}
}
