package me.andre111.voxedit.gui.widget;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;

import me.andre111.voxedit.BlockPalette;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BlockPaletteDisplayWidget extends ClickableWidget {
    private static final Identifier TEXTURE = new Identifier("container/slot");
    private static final MatrixStack.Entry BLOCK_POSE;
	static {
		MatrixStack matrices = new MatrixStack();
		matrices.translate(-0.7f, 0.4f, 0);
		matrices.multiply(new AxisAngle4f((float) Math.PI, 1, 0, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.multiply(new AxisAngle4f((float) (30 * Math.PI / 180), 1, 0, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.multiply(new AxisAngle4f((float) (45 * Math.PI / 180), 0, 1, 0).get(new Quaternionf()), 0, 0, 0);
		BLOCK_POSE = matrices.peek();
	}
	
	private BlockPalette value;
	
	public BlockPaletteDisplayWidget(int x, int y, int width, int height, BlockPalette initialValue) {
		super(x, y, width, height, Text.empty());
		this.value = initialValue;
	}
	
	public BlockPalette getValue() {
		return value;
	}
	
	public void setValue(BlockPalette value) {
		this.value = value;
	}

	@SuppressWarnings("resource")
	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        context.drawGuiTexture(TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(getX()+Math.min(width, height)/2, getY()+height/2, 200);
		float scale = 10 * (20.0f / Math.min(width, height));
		matrices.scale(scale, scale, scale);
		matrices.multiplyPositionMatrix(BLOCK_POSE.getPositionMatrix());
		int blockStateIndex = (int) ((System.currentTimeMillis()) / (1000 * 2)) % value.size();
		BlockState blockState = value.get(blockStateIndex);
        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(blockState, matrices, context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
        matrices.pop();
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}
}
