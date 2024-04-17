package me.andre111.voxedit.client.gui.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;

public class SelectionWidget<T> extends ContainerWidget implements LayoutWidget {
	private final List<ButtonWidget> buttons = new ArrayList<>();
	private final Map<T, ButtonWidget> buttonMap = new HashMap<>();
	private final Map<ButtonWidget, Supplier<Boolean>> additionalButtons = new HashMap<>();
	private final int buttonWidth;
	private final int buttonHeight;
	private final Consumer<T> consumer;
	
	private T value;
	private int padding;
	private int gap;

	public SelectionWidget(int width, int buttonWidth, int buttonHeight, T value, Consumer<T> consumer) {
		super(0, 0, width, buttonHeight, Text.empty());
		this.buttonWidth = buttonWidth;
		this.buttonHeight = buttonHeight;
		this.consumer = consumer;
		
		this.value = value;
	}
	
	public void addOption(T value, Text text) {
		if(buttonMap.containsKey(value)) throw new IllegalArgumentException("Trying to add duplicate option for: "+value);
		
		ButtonWidget button = ButtonWidget.builder(text, (b) -> setValue(value)).size(buttonWidth, buttonHeight).build();
		buttons.add(buttons.size()-additionalButtons.size(), button);
		buttonMap.put(value, button);
		
		updateButtonStates();
		refreshPositions();
	}
	
	public void withAdditionalButton(Text text, Supplier<Boolean> activeCheck, Runnable action) {
		ButtonWidget button = ButtonWidget.builder(text, (b) -> action.run()).size(Math.min(buttonWidth, buttonHeight), buttonHeight).build();
		buttons.add(button);
		additionalButtons.put(button, activeCheck);

		updateButtonStates();
		refreshPositions();
	}
	
	public void setValue(T value) {
		this.value = value;
		updateButtonStates();
		consumer.accept(value);
	}
	
	public T getValue() {
		return value;
	}
	
	public void setPadding(int padding) {
		this.padding = padding;
		refreshPositions();
	}
	
	public void setGap(int gap) {
		this.gap = gap;
		refreshPositions();
	}

	@Override
	public List<? extends Element> children() {
		return buttons;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		buttons.forEach(consumer);
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		for(var button : buttons) button.render(context, mouseX, mouseY, delta);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public void refreshPositions() {
		int x = getX() + padding;
		int y = getY();
		int maxHeight = getY();
		for(var button : buttons) {
			if(x + button.getWidth() > getX()+width) {
				x = getX() + padding;
				y = maxHeight + gap;
			}
			button.setPosition(x, y);
			x += button.getWidth() + gap;
			maxHeight = Math.max(maxHeight, y + button.getHeight());
		}
		height = Math.max(buttonHeight, maxHeight - getY());

		LayoutWidget.super.refreshPositions();
	}
	
	private void updateButtonStates() {
		for(var button : buttons) button.setAlpha(0.5f);
		var selected = buttonMap.get(value);
		if(selected != null) selected.setAlpha(1f);
		for(var e : additionalButtons.entrySet()) {
			if(e.getValue().get()) e.getKey().setAlpha(1f);
		}
	}
}
