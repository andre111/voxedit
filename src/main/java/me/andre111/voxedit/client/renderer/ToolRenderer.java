/*
 * Copyright (c) 2023 André Schweiger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.andre111.voxedit.client.renderer;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import me.andre111.voxedit.client.gui.Textures;
import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.data.BlockPalette;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class ToolRenderer implements DynamicItemRenderer {
	public static final ToolRenderer INSTANCE = new ToolRenderer();
	private static final MatrixStack.Entry BLOCK_POSE;
	static {
		MatrixStack matrices = new MatrixStack();
		matrices.translate(0.55f, 0.85f, 0.0f);
		matrices.multiply(new AxisAngle4f((float) (-150 * Math.PI / 180), 1, 0, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.multiply(new AxisAngle4f((float) (45 * Math.PI / 180), 0, 1, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.scale(0.3f, 0.3f, 0.3f);
		BLOCK_POSE = matrices.peek();
	}
	
	private ToolRenderer() {
	}
	
	@Override
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if(mode != ModelTransformationMode.GUI) return;
		
		DrawContext context = new DrawContext(MinecraftClient.getInstance(), (Immediate) vertexConsumers);
		context.getMatrices().translate(matrices.peek().getPositionMatrix().m30(), matrices.peek().getPositionMatrix().m31()-16, matrices.peek().getPositionMatrix().m32());
        
		render(stack, context);
		
		context.draw();
	}
	
	public void render(ItemStack stack, DrawContext context) {
		context.drawGuiTexture(Textures.TOOL, 0, 0, 16, 16);
		
		ToolItem.Data data = ToolItem.readToolData(stack);
		ConfiguredTool<?, ?> tc = data.selected();
		
		// render information
		context.getMatrices().push();
		context.getMatrices().translate(1, 1, 0);
		context.getMatrices().scale(0.35f, 0.35f, 0.35f);
		int textY = 0;
		context.drawText(MinecraftClient.getInstance().textRenderer, tc.tool().asText(), 0, textY, 0xFFFFFF, true); 
		textY += 10;
		for(Text text : tc.config().getIconTexts()) {
			context.drawText(MinecraftClient.getInstance().textRenderer, text.asTruncatedString(8), 0, textY, 0xFFFFFF, true); 
			textY += 10;
		}
		context.getMatrices().pop();
		
		// render palette
		BlockPalette palette = tc.config().getIconBlocks();
		if(palette != null) {
			context.getMatrices().push();
			context.getMatrices().translate(0, 0, 10);
			context.getMatrices().scale(16, 16, 16);
			context.getMatrices().multiplyPositionMatrix(BLOCK_POSE.getPositionMatrix());
			int blockStateIndex = (int) ((System.currentTimeMillis()) / (1000 * 2)) % palette.size();
			BlockState blockState = palette.get(blockStateIndex);
			MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(blockState, context.getMatrices(), context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
			context.getMatrices().pop();
		}
	}
}
