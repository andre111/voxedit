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

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class ModListWidget<E extends ModListWidget.Entry<E>> extends ContainerWidget implements LayoutWidget {
    private static final Identifier SCROLLER_TEXTURE = Identifier.of("widget/scroller");
    protected final MinecraftClient client;
    private final List<E> children = new ArrayList<>();
    private int padding;
    private double scrollAmount;
    private boolean scrolling;
    private E selected;
    private boolean renderBackground = true;
    private E hoveredEntry;

    public ModListWidget(MinecraftClient client, int width, int height, int y, int padding) {
        super(0, y, width, height, ScreenTexts.EMPTY);
        this.client = client;
        this.padding = padding;
    }
	
	@Override
	public int getHeight() {
		int defHeight = super.getHeight();
		return defHeight > 0 ? defHeight : this.getMaxPosition();
	}

    public int getRowWidth() {
        return width-20;
    }

    public E getSelectedOrNull() {
        return this.selected;
    }

    public void setSelected(@Nullable E entry) {
        this.selected = entry;
    }

    public E getFirst() {
        return children.get(0);
    }

    public void setRenderBackground(boolean renderBackground) {
        this.renderBackground = renderBackground;
    }

    public final List<E> children() {
        return this.children;
    }

    protected void clearEntries() {
        this.children.clear();
        this.selected = null;
    }

    protected E getEntry(int index) {
        return children().get(index);
    }

    protected int addEntry(E entry) {
    	entry.parentList = this;
        this.children.add(entry);
        this.refreshPositions();
        return this.children.size() - 1;
    }

    protected void addEntryToTop(E entry) {
    	entry.parentList = this;
        double d = this.getMaxScroll() - this.getScrollAmount();
        this.children.add(0, entry);
        this.refreshPositions();
        this.setScrollAmount(this.getMaxScroll() - d);
    }

    protected boolean removeEntryWithoutScrolling(E entry) {
        double d = this.getMaxScroll() - this.getScrollAmount();
        boolean bl = this.removeEntry(entry);
        this.setScrollAmount(this.getMaxScroll() - d);
        return bl;
    }

    protected int getEntryCount() {
        return this.children().size();
    }

    protected boolean isSelectedEntry(E entry) {
        return Objects.equals(this.getSelectedOrNull(), entry);
    }

    protected final E getEntryAtPosition(double x, double y) {
        for(E child : children) {
        	if(child.getX() < x && x < child.getX() + child.getWidth() && child.getY() < y && y < child.getY() + child.getHeight()) return child;
        }
        return null;
    }

    protected int getMaxPosition() {
    	int height = padding;
    	for(E child : children) height += child.getHeight()+1;
        return height;
    }

    protected void renderDecorations(DrawContext context, int mouseX, int mouseY) {
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        int j;
        int i;
        hoveredEntry = isMouseOver(mouseX, mouseY) ? getEntryAtPosition(mouseX, mouseY) : null;
        if (renderBackground) {
            context.setShaderColor(0.125f, 0.125f, 0.125f, 1.0f);
            i = 32;
            context.drawTexture(Screen.MENU_BACKGROUND_TEXTURE, getX(), getY(), getRight(), getBottom() + (int) getScrollAmount(), getWidth(), getHeight(), 32, 32);
            context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        this.enableScissor(context);
        this.renderList(context, mouseX, mouseY, delta);
        context.disableScissor();
        if (renderBackground) {
            i = 4;
            context.fillGradient(RenderLayer.getGuiOverlay(), getX(), getY(), getRight(), getY() + 4, Colors.BLACK, 0, 0);
            context.fillGradient(RenderLayer.getGuiOverlay(), getX(), getBottom() - 4, getRight(), getBottom(), 0, Colors.BLACK, 0);
        }
        if (super.getHeight() > 0 && (i = this.getMaxScroll()) > 0) {
            j = this.getScrollbarPositionX();
            int k = (int)((getHeight() * getHeight()) / (float) getMaxPosition());
            k = MathHelper.clamp(k, 32, getHeight() - 8);
            int l = (int)this.getScrollAmount() * (getHeight() - k) / i + this.getY();
            if (l < this.getY()) {
                l = this.getY();
            }
            context.fill(j, this.getY(), j + 6, this.getBottom(), -16777216);
            context.drawGuiTexture(SCROLLER_TEXTURE, j, l, 6, k);
        }
        this.renderDecorations(context, mouseX, mouseY);
        RenderSystem.disableBlend();
    }

    protected void enableScissor(DrawContext context) {
        context.enableScissor(getX(), getY(), getRight(), getBottom());
    }

    protected void centerScrollOn(E entry) {
        this.setScrollAmount(entry.getY() + entry.getHeight() / 2 - getHeight() / 2);
    }

    protected void ensureVisible(E entry) {
        if(entry.getY() < getY()) {
            scroll(getY()-entry.getY());
        } else if(entry.getY() + entry.getHeight() > getY() + getHeight()) {
            scroll(-(entry.getY() + entry.getHeight() - (getY() + getHeight())));
        }
    }

    private void scroll(int amount) {
        setScrollAmount(getScrollAmount() + amount);
    }

    public double getScrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double amount) {
        scrollAmount = MathHelper.clamp(amount, 0.0, getMaxScroll());
        refreshPositions();
    }

    public int getMaxScroll() {
        return Math.max(0, getMaxPosition() - (height - 4));
    }

    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= getScrollbarPositionX() && mouseX < (getScrollbarPositionX() + 6);
    }

    protected int getScrollbarPositionX() {
        return getX()+getWidth()-5;
    }

    protected boolean isSelectButton(int button) {
        return button == 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isSelectButton(button)) {
            return false;
        }
        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        E entry = this.getEntryAtPosition(mouseX, mouseY);
        if (entry != null) {
            if (entry.mouseClicked(mouseX, mouseY, button)) {
                Element entry2 = this.getFocused();
                if (entry2 != entry && entry2 instanceof ParentElement) {
                    ParentElement parentElement = (ParentElement)entry2;
                    parentElement.setFocused(null);
                }
                this.setFocused(entry);
                this.setDragging(true);
                return true;
            }
        }
        return this.scrolling;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.getFocused() != null) {
            this.getFocused().mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }
        if (button != 0 || !this.scrolling) {
            return false;
        }
        if (mouseY < (double)this.getY()) {
            this.setScrollAmount(0.0);
        } else if (mouseY > (double)this.getBottom()) {
            this.setScrollAmount(this.getMaxScroll());
        } else {
            double d = Math.max(1, this.getMaxScroll());
            int i = getHeight();
            int j = MathHelper.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
            double e = Math.max(1.0, d / (double)(i - j));
            this.setScrollAmount(this.getScrollAmount() + deltaY * e);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        setScrollAmount(getScrollAmount() - verticalAmount * 20);
        return true;
    }

    @Override
    public void setFocused(Element focused) {
        super.setFocused(focused);
        int i = children.indexOf(focused);
        if (i >= 0) {
        	E entry = children.get(i);
            setSelected(entry);
            if (client.getNavigationType().isKeyboard()) {
            	ensureVisible(entry);
            }
        } else {
        	setSelected(null);
        }
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction) {
        return getNeighboringEntry(direction, entry -> true);
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction, Predicate<E> predicate) {
        return getNeighboringEntry(direction, predicate, this.getSelectedOrNull());
    }

    @Nullable
    protected E getNeighboringEntry(NavigationDirection direction, Predicate<E> predicate, @Nullable E selected) {
        int offset = switch(direction) {
            case RIGHT, LEFT -> 0;
            case UP -> -1;
            case DOWN -> 1;
        };
        if(!this.children().isEmpty() && offset != 0) {
            int startIndex = selected == null ? (offset > 0 ? 0 : children().size() - 1) : children().indexOf(selected) + offset;
            for (int index = startIndex; index >= 0 && index < children.size(); index += offset) {
                E entry = children().get(index);
                if (!predicate.test(entry)) continue;
                return entry;
            }
        }
        return null;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseY >= this.getY() && mouseY <= this.getBottom() && mouseX >= this.getX() && mouseX <= this.getRight();
    }

    protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
        for(E child : children) {
        	if(child.getY() + child.getHeight() < getY()) continue;
        	if(child.getY() > getY() + getHeight()) return;
        	
        	if(child.isFocused()) {
                this.drawSelectionHighlight(context, child.getX(), child.getY(), child.getWidth(), child.getHeight(), 0xFFFFFFFF, -16777216);
        	}
        	child.render(context, mouseX, mouseY, delta);
        }
    }

    protected void drawSelectionHighlight(DrawContext context, int entryX, int entryY, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        context.fill(entryX-2, entryY-2, entryX+entryWidth+2, entryY+entryHeight-1, borderColor);
        context.fill(entryX-1, entryY-1, entryX+entryWidth+1, entryY+entryHeight-2, fillColor);
    }

    public int getRowLeft() {
        return getX() + getWidth() / 2 - getRowWidth() / 2 + 2;
    }

    public int getRowRight() {
        return this.getRowLeft() + this.getRowWidth();
    }

    @Override
    public Selectable.SelectionType getType() {
        if (this.isFocused()) {
            return Selectable.SelectionType.FOCUSED;
        }
        if (this.hoveredEntry != null) {
            return Selectable.SelectionType.HOVERED;
        }
        return Selectable.SelectionType.NONE;
    }

    @Nullable
    protected E remove(int index) {
    	E entry = children.get(index);
        if (this.removeEntry(children.get(index))) {
            return entry;
        }
        return null;
    }

    protected boolean removeEntry(E entry) {
        boolean bl = this.children.remove(entry);
        if (bl && entry == this.getSelectedOrNull()) {
            this.setSelected(null);
        }
        return bl;
    }

    @Nullable
    protected E getHoveredEntry() {
        return this.hoveredEntry;
    }

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

    protected void appendNarrations(NarrationMessageBuilder builder, E entry) {
        int i;
        List<E> list = this.children();
        if (list.size() > 1 && (i = list.indexOf(entry)) != -1) {
            builder.put(NarrationPart.POSITION, (Text)Text.translatable("narrator.position.list", i + 1, list.size()));
        }
    }

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		for(E child : children) consumer.accept(child);
	}
	
	@Override
	public void refreshPositions() {
		int currentX = getX() + (width-getRowWidth()) / 2;
		int currentY = (int) (getY() + padding - getScrollAmount());
		for(E child : children) {
			child.setPosition(currentX, currentY);
			child.setWidth(getRowWidth());
			currentY += child.getHeight()+1;
		}
		LayoutWidget.super.refreshPositions();
	}

    @Environment(value=EnvType.CLIENT)
    public abstract static class Entry<E extends Entry<E>> extends ContainerWidget implements LayoutWidget {
        public Entry() {
			super(0, 0, 0, 11, Text.empty());
		}

        ModListWidget<E> parentList;

        @Override
        public void setFocused(boolean focused) {
        }

        @Override
        public boolean isFocused() {
            return this.parentList.getFocused() == this;
        }
        
	    @Override
	    public boolean mouseClicked(double mouseX, double mouseY, int button) {
	    	boolean child = super.mouseClicked(mouseX, mouseY, button);
	    	return child || (getX() <= mouseX && mouseX <= getX() + getWidth() && getY() <= mouseY && mouseY <= getY() + getHeight()); 
	    }

		@Override
		public void forEachElement(Consumer<Widget> consumer) {
			for(Element child : children()) {
				if(child instanceof Widget widget) consumer.accept(widget);
			}
		}
		
		@Override
		public void refreshPositions() {
			positionChildren();
			LayoutWidget.super.refreshPositions();
		}
        
        public abstract int getHeight();
        
        public abstract void positionChildren();
    }
}