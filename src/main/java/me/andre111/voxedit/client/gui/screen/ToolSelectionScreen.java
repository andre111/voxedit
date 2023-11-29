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
package me.andre111.voxedit.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.VoxEditClient;
import me.andre111.voxedit.client.gui.Textures;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.client.renderer.ToolRenderer;
import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.tool.ConfiguredTool;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class ToolSelectionScreen extends Screen {
    static final Identifier SELECTION_TEXTURE = new Identifier("gamemode_switcher/selection");
    private static final Identifier TEXTURE = new Identifier("textures/gui/container/gamemode_switcher.png");
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final int TEXTURE_CONTENT_WIDTH = 125;
    private static final int TEXTURE_CONTENT_HEIGHT = 75;
    
    private static final int BUTTON_SIZE = 16+2;
    private static final int BUTTONS_PER_ROW = 6;
    private static final int MAX_ROWS = 2;
    
    private static final Text SELECT_NEXT_TEXT = Text.of("[ ").copy().append(Text.keybind("key.voxedit.openMenu")).append(" ] ").formatted(Formatting.AQUA).append(Text.of("Next").copy().formatted(Formatting.WHITE));
    private static final Text NEW_TOOL_TEXT = Text.of("Add New Tool");

    private int lastMouseX;
    private int lastMouseY;
    private boolean mouseUsedForSelection;
    
    private final List<SelectButtonWidget> buttons = Lists.newArrayList();
    
    private final ToolItem.Data data;
    private int selectedIndex;

    public ToolSelectionScreen(ToolItem.Data data) {
        super(NarratorManager.EMPTY);
        this.data = data;
        this.selectedIndex = data.selectedIndex();
    }

    @Override
    protected void init() {
        selectedIndex = data.selectedIndex();
        
        for (int i = 0; i < Math.min(data.size()+1, BUTTONS_PER_ROW*MAX_ROWS); i++) {
        	int x = (i % BUTTONS_PER_ROW) * BUTTON_SIZE;
        	int y = (i / BUTTONS_PER_ROW) * BUTTON_SIZE;
            buttons.add(new SelectButtonWidget(i < data.size() ? data.get(i) : null, width / 2 - (BUTTONS_PER_ROW * BUTTON_SIZE) / 2 + x, height / 2 - 12 + y));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (checkForClose()) return;
        
        int x = width / 2 - TEXTURE_CONTENT_WIDTH / 2;
        int y = height / 2 - TEXTURE_CONTENT_HEIGHT / 2;
        
        context.getMatrices().push();
        RenderSystem.enableBlend();
        context.drawTexture(TEXTURE, x, y, 0.0f, 0.0f, TEXTURE_CONTENT_WIDTH, TEXTURE_CONTENT_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        context.getMatrices().pop();
        
        Text text = NEW_TOOL_TEXT;
        if(selectedIndex < data.size()) text = data.get(selectedIndex).tool().asText();
        context.drawCenteredTextWithShadow(textRenderer, text, width / 2, y + 8, Colors.WHITE);
        context.drawCenteredTextWithShadow(textRenderer, SELECT_NEXT_TEXT, width / 2, y + TEXTURE_CONTENT_HEIGHT - 11, 0xFFFFFF);
        
        if (!mouseUsedForSelection) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            mouseUsedForSelection = true;
        }
        boolean mouseStatic = lastMouseX == mouseX && lastMouseY == mouseY;
        
        for (int i=0; i<buttons.size(); i++) {
        	SelectButtonWidget buttonWidget = buttons.get(i);
            buttonWidget.render(context, mouseX, mouseY, delta);
            buttonWidget.setSelected(this.selectedIndex == i);
            if (mouseStatic || !buttonWidget.isSelected()) continue;
            this.selectedIndex = i;
        }
    }

    private void apply() {
        if(selectedIndex != data.selectedIndex()) {
        	ClientNetworking.selectTool(selectedIndex);
        }
    }

    private boolean checkForClose() {
        if (!Screen.hasControlDown()) {
            apply();
            client.setScreen(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (VoxEditClient.OPEN_MENU.matchesKey(keyCode, scanCode)) {
            mouseUsedForSelection = false;
            selectedIndex = (selectedIndex + 1) % buttons.size();
            return true;
        }
        if(GLFW.GLFW_KEY_1 <= keyCode && keyCode <= GLFW.GLFW_KEY_9) {
            mouseUsedForSelection = false;
        	int index = keyCode - GLFW.GLFW_KEY_1;
        	if(index < buttons.size()) selectedIndex = index;
        	return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    public class SelectButtonWidget extends ClickableWidget {
        private final ItemStack stack;
        private boolean selected;

        public SelectButtonWidget(ConfiguredTool<?, ?> tc, int x, int y) {
            super(x, y, BUTTON_SIZE, BUTTON_SIZE, tc != null ? tc.tool().asText() : NEW_TOOL_TEXT);
            this.stack = tc != null ? VoxEdit.ITEM_TOOL.getStackWith(tc) : null;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawGuiTexture(Textures.SLOT, getX(), getY(), BUTTON_SIZE, BUTTON_SIZE);
            
            if(stack != null) {
	            context.getMatrices().push();
	            context.getMatrices().translate(getX()+1, getY()+1, 0);
	            ToolRenderer.INSTANCE.render(stack, context);
	            context.getMatrices().pop();
            }
            
            if (selected) {
	            context.getMatrices().push();
	            context.getMatrices().translate(0, 0, 100);
                context.drawGuiTexture(SELECTION_TEXTURE, getX()-2, getY()-2, BUTTON_SIZE+3, BUTTON_SIZE+3);
	            context.getMatrices().pop();
            }
        }

        @Override
        public void appendClickableNarrations(NarrationMessageBuilder builder) {
            appendDefaultNarrations(builder);
        }

        @Override
        public boolean isSelected() {
            return super.isSelected() || selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
}
