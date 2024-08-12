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
import com.google.gson.JsonObject;

import me.andre111.voxedit.client.gui.widget.AutoLayoutContainerWidget;
import me.andre111.voxedit.client.gui.widget.OverlayWidget;
import me.andre111.voxedit.data.jsondef.JsonDef;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;

public abstract class JsonEditWidget<D extends JsonDef> extends AutoLayoutContainerWidget {
	private final D jsonDef;
	private final String jsonName;
	
	public JsonEditWidget(D jsonDef, String jsonName, Element parent, int x, int y, int width, int height) {
		super(parent, x, y, width, height, Text.of(jsonName));
		
		this.jsonDef = jsonDef;
		this.jsonName = jsonName;
	}
	
	public D getDef() {
		return jsonDef;
	}
	
	public String getName() {
		return jsonName;
	}
	
	public void applyValue(JsonObject parentObj) {
		parentObj.add(jsonName, getValue());
	}
	
	public void reloadValue(JsonObject parentObj) {
		setValue(parentObj.get(jsonName));
	}
	
	protected abstract JsonElement getValue();
	protected abstract void setValue(JsonElement element);
	
	public static JsonEditWidget<?> create(JsonDef def, String name, Element parent, OverlayWidget overlay) {
		int width = 300;
		int height = 20;
		
		return switch(def) {
		case JsonDef.Boolean b -> new BooleanJsonEditWidget(b, name, parent, 0, 0, width, height);
		case JsonDef.Integer i -> new IntegerJsonEditWidget(i, name, parent, 0, 0, width, height);
		case JsonDef.Defined d -> new DefinedJsonEditWidget(d.kind(), d, name, parent, 0, 0, width, height, overlay);
		case JsonDef.Complex c -> new ComplexJsonEditWidget(c, name, parent, 0, 0, width, height, overlay);
		//TODO: JsonDef.BlockState
		//TODO: JsonDef.List
		default -> null;
		};
	}
}
