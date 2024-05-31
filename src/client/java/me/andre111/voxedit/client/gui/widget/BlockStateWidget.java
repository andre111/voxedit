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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.andre111.voxedit.client.gui.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class BlockStateWidget extends ContainerWidget implements LayoutWidget {
	public static final MatrixStack.Entry BLOCK_POSE;
	static {
		MatrixStack matrices = new MatrixStack();
		matrices.translate(-0.7f, 0.4f, 0);
		matrices.multiply(new AxisAngle4f((float) Math.PI, 1, 0, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.multiply(new AxisAngle4f((float) (30 * Math.PI / 180), 1, 0, 0).get(new Quaternionf()), 0, 0, 0);
		matrices.multiply(new AxisAngle4f((float) (45 * Math.PI / 180), 0, 1, 0).get(new Quaternionf()), 0, 0, 0);
		BLOCK_POSE = matrices.peek();
	}

	private final boolean includeProperties;
	private final Consumer<BlockState> consumer;

	private final List<ClickableWidget> children;
	private final BlockWidget blockWidget;

	private BlockState state;

	public BlockStateWidget(int x, int y, int width, int height, BlockState initialValue, boolean includeProperties, Consumer<BlockState> consumer) {
		super(x, y, width, height, Text.empty());

		this.includeProperties = includeProperties;
		this.consumer = consumer;
		this.state = initialValue;

		this.children = new ArrayList<>();
		this.blockWidget = new BlockWidget(MinecraftClient.getInstance().textRenderer, x, y, width, height, initialValue.getBlock(), this::changedBlock);
		this.children.add(blockWidget);
		rebuild();
	}

	public BlockState getValue() {
		if(state == null) {
			state = blockWidget.block.getDefaultState();
			if(includeProperties) {

			}
		}
		return state;
	}

	public Set<String> getSpecifiedProperties() {
		//TODO: implement
		return new HashSet<>();
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		for(var child : children) child.render(context, mouseX, mouseY, delta);
		
		int previewSize = includeProperties ? height / 2 : height;
		context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		context.drawGuiTexture(Textures.SLOT, this.getX()+width, this.getY() + (height - previewSize) / 2, previewSize, previewSize);
		context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

		BlockState state = getValue();
		if(state.isAir()) {
			context.drawGuiTexture(Textures.AIR, this.getX()+width+2, this.getY() + (height - previewSize) / 2 + 2, previewSize-4, previewSize-4);
		} else {
			MatrixStack matrices = context.getMatrices();
			matrices.push();
			matrices.translate(getX()+width+previewSize/2, getY()+height/2, 200);
			float scale = 10 * (20.0f / Math.min(width, previewSize));
			matrices.scale(scale, scale, scale);
			matrices.multiplyPositionMatrix(BLOCK_POSE.getPositionMatrix());
			MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(state, matrices, context.getVertexConsumers(), LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
			matrices.pop();
		}
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		children.forEach(consumer);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public void refreshPositions() {
		blockWidget.setDimensionsAndPosition(width, includeProperties ? height / 2 : height, getX(), getY());
		if(includeProperties) {
			if(children.size() > 1) {
				int x = getX();
				int y = getY() + height / 2;
				int w = width / (children.size() - 1);
				int h = height / 2;
				for(ClickableWidget widget : children) {
					if(widget == blockWidget) continue;
					widget.setDimensionsAndPosition(w, h, x, y);
					x += w;
				}
			} else {
				blockWidget.setY(getY() + height / 4);
			}
		}
		LayoutWidget.super.refreshPositions();
	}

	private void changedBlock() {
		state = null;
		state = getValue();

		rebuild();

		consumer.accept(state);
	}
	
	private void changedProperty() {
		consumer.accept(state);
	}
	
	private void rebuild() {
		children.clear();
		children.add(blockWidget);
		if(includeProperties && state != null) {
			for(Property<?> property : state.getProperties()) {
				addPropertyButton(property);
			}
		}
		refreshPositions();
	}
	
	private <T extends Comparable<T>> void addPropertyButton(Property<T> property) {
		var button = CyclingButtonWidget.<T>builder(v -> Text.of(property.name(v)))
		.values(CyclingButtonWidget.Values.of(property.getValues()))
		.initially(state.get(property))
		.build(Text.of(property.getName()), (b, v) -> {
			state = getValue().with(property, v);
			changedProperty();
		});
		children.add(button);
	}

	private static class BlockWidget extends TextFieldWidget {
		private Block block;
		private String suggestion;

		public BlockWidget(TextRenderer textRenderer, int x, int y, int width, int height, Block initialValue, Runnable onChange) {
			super(textRenderer, x, y, width, height, Text.empty());
			this.block = initialValue;

			this.setMaxLength(200);
			this.setText(Optional.ofNullable(Registries.BLOCK.getId(block)).map(id -> id.toString()).orElse("minecraft:air"));

			this.setChangedListener((string) -> {
				var builder = new SuggestionsBuilder(string, string.toLowerCase(Locale.ROOT), string.length());
				CommandSource.suggestIdentifiers(Registries.BLOCK.getIds(), builder).thenAccept((suggestions) -> {
					setSuggestion("");
					for(Suggestion suggestion : suggestions.getList()) {
						if(suggestion.getText().startsWith(string)) {
							setSuggestion(suggestion.getText().substring(string.length()));
							break;
						}
					}
				});

				this.setEditableColor(0xFF0000);

				Identifier id = Identifier.tryParse(string);
				if(id != null) {
					Block newBlock = Registries.BLOCK.get(id);
					if(newBlock != null) {
						this.setEditableColor(0xFFFFFF);
						if(newBlock != block) {
							block = newBlock;
							onChange.run();
						}
					}
				}
			});
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
	}
}
