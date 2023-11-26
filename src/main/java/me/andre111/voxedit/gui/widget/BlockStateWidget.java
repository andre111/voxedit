package me.andre111.voxedit.gui.widget;

import java.util.function.Consumer;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.argument.BlockArgumentParser;
import net.minecraft.command.argument.BlockArgumentParser.BlockResult;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BlockStateWidget extends TextFieldWidget {
	public static final MatrixStack.Entry BLOCK_POSE;
	static {
		MatrixStack matrices = new MatrixStack();
		matrices.translate(-0.7f, 0.4f, 0);
		matrices.multiply(new AxisAngle4f((float) Math.PI, 1, 0, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.multiply(new AxisAngle4f((float) (30 * Math.PI / 180), 1, 0, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.multiply(new AxisAngle4f((float) (45 * Math.PI / 180), 0, 1, 0).get(new Quaternionf()), 0, 0, 0);
		BLOCK_POSE = matrices.peek();
	}
    private static final Identifier TEXTURE = new Identifier("container/slot");
	
	private BlockState value;
	private final boolean includeProperties;
	private final Consumer<BlockState> consumer;

    public BlockStateWidget(TextRenderer textRenderer, int x, int y, int width, int height, boolean includeProperties, BlockState initialValue, Consumer<BlockState> consumer) {
		super(textRenderer, x, y, width, height, Text.empty());
		this.includeProperties = includeProperties;
		this.consumer = consumer;
		this.value = initialValue;
		
		this.setMaxLength(200);
		if(this.includeProperties) {
			this.setText(BlockArgumentParser.stringifyBlockState(value));
		} else {
			this.setText(value.getRegistryEntry().getKey().map(key -> key.getValue().toString()).orElse("air"));
		}
		
		this.setChangedListener((string) -> {
			try {
				BlockResult result = BlockArgumentParser.block(Registries.BLOCK.getReadOnlyWrapper(), string, false);
				this.setEditableColor(0xFFFFFF);
				this.consumer.accept(value = result.blockState());
			} catch (CommandSyntaxException e) {
				this.setEditableColor(0xFF0000);
			}
		});
	}
	
	public BlockState getValue() {
		return value;
	}

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    	super.renderWidget(context, mouseX, mouseY, delta);
    	
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        context.drawGuiTexture(TEXTURE, this.getX()+width, this.getY(), height, height);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(getX()+width+height/2, getY()+height/2, 200);
		float scale = 10 * (20.0f / Math.min(width, height));
		matrices.scale(scale, scale, scale);
		matrices.multiplyPositionMatrix(BLOCK_POSE.getPositionMatrix());
        MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(value, matrices, context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
        matrices.pop();
	}
}
