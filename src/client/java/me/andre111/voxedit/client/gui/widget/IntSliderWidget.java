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

import java.util.function.Consumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

@Environment(value=EnvType.CLIENT)
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
