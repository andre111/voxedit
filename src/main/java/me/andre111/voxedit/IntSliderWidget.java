package me.andre111.voxedit;

import java.util.function.Consumer;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class IntSliderWidget extends SliderWidget {
	private final Text text;
	private final int min;
	private final int max;
	private final Consumer<Integer> valueConsumer;
	
	public IntSliderWidget(int x, int y, int width, int height, Text text, int min, int max, int value, Consumer<Integer> valueConsumer) {
		super(x, y, width, height, text, (value - min) / (double) (max - min));
		
		this.text = text;
		this.min = min;
		this.max = max;
		this.valueConsumer = valueConsumer;
		
		this.setIntValue(value);
	}

	public int getIntValue() {
		return MathHelper.floor(MathHelper.clampedLerp(min, max, value));
	}
	
	public void setIntValue(int intValue) {
		intValue = MathHelper.clamp(intValue, min, max);
		value = MathHelper.clamp((intValue-min) / (double) (max - min), 0.0, 1.0);
		updateMessage();
	}

	@Override
	protected void updateMessage() {
		setMessage(text.copy().append(": "+getIntValue()));
	}

	@Override
	protected void applyValue() {
		valueConsumer.accept(getIntValue());
	}
}
