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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.andre111.voxedit.client.gui.widget.DropdownListWidget;
import me.andre111.voxedit.client.gui.widget.OverlayWidget;
import me.andre111.voxedit.data.jsondef.JsonDef;
import me.andre111.voxedit.data.jsondef.JsonDef.Defined;
import me.andre111.voxedit.data.jsondef.JsonDefLoader;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DefinedJsonEditWidget extends JsonEditWidget<JsonDef.Defined> {
	private final String kind;
	private final OverlayWidget overlay;
	private final DropdownListWidget typeSelector;
	private final List<JsonEditWidget<?>> children = new ArrayList<>();
	
	private JsonObject jsonObject;
	
	public DefinedJsonEditWidget(String kind, Defined jsonDef, String jsonName, Element parent, int x, int y, int width, int height, OverlayWidget overlay) {
		super(jsonDef, jsonName, parent, x, y, width, height);
		
		this.kind = kind;
		this.overlay = overlay;
		
		List<String> types = new ArrayList<>();
		if(jsonDef.optional()) types.add("");
		for(Identifier type : JsonDefLoader.getKnownTypes(kind)) types.add(type.toString());
		
		this.typeSelector = new DropdownListWidget(0, 0, width, 20, Text.of(jsonName+": "), types.isEmpty() ? "" : types.get(0), types, value -> {}, overlay);
		addChild(typeSelector);
	}
	
	@Override
	protected JsonElement getValue() {
		return jsonObject;
	}

	@Override
	protected void setValue(JsonElement element) {
		// TODO Auto-generated method stub
		//check if type is the same and either simply reload children or rebuild gui if changed
		if(!element.isJsonObject()) return;
		
		jsonObject = element.getAsJsonObject();
		if(jsonObject.get("type").getAsString().equals(typeSelector.getValue())) {
			// reload children
			for(var child : children) child.reloadValue(jsonObject);
		} else {
			children.clear();
			children().clear();
			addChild(typeSelector);
			
			//TODO: add property edit widgets
			JsonDef def = JsonDefLoader.getDef(kind, Identifier.tryParse(jsonObject.get("type").getAsString()));
			if(def instanceof JsonDef.Complex c) {
				for(var e : c.properties().entrySet()) {
					JsonEditWidget<?> child = JsonEditWidget.create(e.getValue(), e.getKey(), this, overlay);
					if(child != null) children.add(child);
				}
			}
		}
	}

}
