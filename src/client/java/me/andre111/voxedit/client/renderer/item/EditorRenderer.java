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
package me.andre111.voxedit.client.renderer.item;

import me.andre111.voxedit.client.gui.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry.DynamicItemRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class EditorRenderer implements DynamicItemRenderer {
	public static final EditorRenderer INSTANCE = new EditorRenderer();
	
	private EditorRenderer() {
	}
	
	@Override
	public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		if(mode != ModelTransformationMode.GUI) return;
		
		DrawContext context = new DrawContext(MinecraftClient.getInstance(), (Immediate) vertexConsumers);
		context.getMatrices().translate(matrices.peek().getPositionMatrix().m30(), matrices.peek().getPositionMatrix().m31(), matrices.peek().getPositionMatrix().m32());
        context.drawGuiTexture(Textures.EDITOR, 0, -16, 16, 16);
	}
}
