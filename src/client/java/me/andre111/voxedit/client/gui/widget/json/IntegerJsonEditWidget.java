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
package me.andre111.voxedit.client.gui.widget.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import me.andre111.voxedit.client.gui.widget.IntFieldWidget;
import me.andre111.voxedit.client.gui.widget.IntSliderWidget;
import me.andre111.voxedit.data.jsondef.JsonDef;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;

public class IntegerJsonEditWidget extends JsonEditWidget<JsonDef.Integer> {
	private final IntFieldWidget field;
	private final IntSliderWidget slider;

	public IntegerJsonEditWidget(JsonDef.Integer jsonDef, String jsonName, Element parent, int x, int y, int width, int height) {
		super(jsonDef, jsonName, parent, x, y, width, height);
		
		if(jsonDef.minValue() != Integer.MIN_VALUE && jsonDef.maxValue() != Integer.MAX_VALUE) {
			field = null;
			slider = new IntSliderWidget(x, y, width, height, getMessage(), jsonDef.minValue(), jsonDef.maxValue(), jsonDef.defaultValue(), (value) -> {
				//TODO: how can I call applyValue here?
			});
			addChild(slider);
		} else {
			field = new IntFieldWidget(MinecraftClient.getInstance().textRenderer, x, y, width, height, getMessage(), jsonDef.defaultValue(), (value) -> {
				//TODO: how can I call applyValue here?
			});
			slider = null;
			addChild(field);
		}
	}

	@Override
	protected JsonElement getValue() {
		return new JsonPrimitive(field != null ? field.getInt() : slider.getIntValue());
	}

	@Override
	protected void setValue(JsonElement element) {
		if(field != null) field.setInt(element.getAsInt());
		else slider.setIntValue(element.getAsInt());
	}

}
