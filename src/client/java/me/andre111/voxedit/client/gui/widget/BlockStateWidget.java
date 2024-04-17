/*
 * Copyright (c) 2024 André Schweiger
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
package me.andre111.voxedit.client.gui.widget;

import java.util.Locale;
import java.util.function.Consumer;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.andre111.voxedit.client.gui.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

@Environment(value=EnvType.CLIENT)
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
	
	private BlockState value;
	private String suggestion;
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
				var wrapper = Registries.BLOCK.getReadOnlyWrapper();
				var builder = new SuggestionsBuilder(string, string.toLowerCase(Locale.ROOT), string.length());
				BlockArgumentParser.getSuggestions(wrapper, builder, false, false).thenAccept((suggestions) -> {
					setSuggestion("");
					for(Suggestion suggestion : suggestions.getList()) {
						if(suggestion.getText().startsWith(string)) {
							setSuggestion(suggestion.getText().substring(string.length()));
							break;
						}
					}
				});
				
				BlockResult result = BlockArgumentParser.block(wrapper, string, false);
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
    public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
		super.setSuggestion(suggestion);
	}

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    	if(isFocused()) {
    		if(keyCode == GLFW.GLFW_KEY_TAB) {
    			if(suggestion != null && !suggestion.isEmpty() && getCursor() == getText().length()) {
    				setText(getText() + suggestion);
    				return true;
    			}
    		}
    	}
    	return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
    	super.renderWidget(context, mouseX, mouseY, delta);
    	
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        context.drawGuiTexture(Textures.SLOT, this.getX()+width, this.getY(), height, height);
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        if(value.isAir()) {
            context.drawGuiTexture(Textures.AIR, this.getX()+width+2, this.getY()+2, height-4, height-4);
        } else {
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
}
