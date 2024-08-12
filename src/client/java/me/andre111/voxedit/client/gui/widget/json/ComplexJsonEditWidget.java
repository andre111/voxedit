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

import me.andre111.voxedit.client.gui.widget.OverlayWidget;
import me.andre111.voxedit.data.jsondef.JsonDef;
import me.andre111.voxedit.data.jsondef.JsonDef.Complex;
import net.minecraft.client.gui.Element;

public class ComplexJsonEditWidget extends JsonEditWidget<JsonDef.Complex> {
	private final List<JsonEditWidget<?>> children = new ArrayList<>();
	
	private JsonObject jsonObject;
	
	public ComplexJsonEditWidget(Complex jsonDef, String jsonName, Element parent, int x, int y, int width, int height, OverlayWidget overlay) {
		super(jsonDef, jsonName, parent, x, y, width, height);
		
		//TODO: add property edit widgets
		for(var e : jsonDef.properties().entrySet()) {
			JsonEditWidget<?> child = JsonEditWidget.create(e.getValue(), e.getKey(), this, overlay);
			if(child != null) addChild(child);
		}
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
		for(var child : children) child.reloadValue(jsonObject);
	}

}
