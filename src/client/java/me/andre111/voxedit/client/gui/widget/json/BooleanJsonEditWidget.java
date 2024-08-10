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

import me.andre111.voxedit.data.jsondef.JsonDef;
import me.andre111.voxedit.data.jsondef.JsonDef.Boolean;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.CheckboxWidget;

public class BooleanJsonEditWidget extends JsonEditWidget<JsonDef.Boolean> {
	private final CheckboxWidget checkbox;

	public BooleanJsonEditWidget(Boolean jsonDef, String jsonName, Element parent, int x, int y, int width, int height) {
		super(jsonDef, jsonName, parent, x, y, width, height);
		
		this.checkbox = CheckboxWidget.builder(getMessage(), MinecraftClient.getInstance().textRenderer).checked(jsonDef.defaultValue()).callback((cb, value) -> {
			//TODO: how can I call applyValue here?
		}).build();
		addChild(checkbox);
	}

	@Override
	protected JsonElement getValue() {
		return new JsonPrimitive(checkbox.isChecked());
	}

	@Override
	protected void setValue(JsonElement element) {
		if(checkbox.isChecked() != element.getAsBoolean()) checkbox.onPress();
	}

}
