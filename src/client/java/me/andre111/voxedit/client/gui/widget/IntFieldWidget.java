package me.andre111.voxedit.client.gui.widget;

import java.util.function.Consumer;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class IntFieldWidget extends TextFieldWidget {
	private int value;
	private Consumer<Integer> onChange;
	private boolean skipListener = false;

	public IntFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, int value, Consumer<Integer> onChange) {
		super(textRenderer, x, y, width, height, text);
		this.value = value;
		this.onChange = onChange;
		setChangedListener(this::textChanged);
	}
	
	private void textChanged(String textValue) {
		try {
			value = Integer.parseInt(textValue);
			setEditableColor(0xFFFFFFFF);
			if(!skipListener) onChange.accept(value);
		} catch(NumberFormatException e) {
			setEditableColor(0xFFFF0000);
		}
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if(isMouseOver(mouseX, mouseY)) {
			setInt(getInt() + (int) verticalAmount);
			onChange.accept(value);
			return true;
		}
		return false;
	}

	public int getInt() {
		return value;
	}
	
	public void setInt(int value) {
		skipListener = true;
		setText(Integer.toString(value));
		skipListener = false;
	}
}
