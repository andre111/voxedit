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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class RegistryEntryOrTagWidget<T> extends TextFieldWidget implements Consumer<String> {
	private String value;
	private String suggestion;
	
	private List<String> ids;

    private RegistryEntryOrTagWidget(TextRenderer textRenderer, int x, int y, int width, int height, String initialValue, Consumer<String> consumer, CompletableFuture<List<String>> idsFuture) {
		super(textRenderer, x, y, width, height, Text.empty());
		this.value = initialValue;
		
		idsFuture.thenAccept(ids -> this.ids = ids);
		
		setMaxLength(200);
		setText(initialValue.toString());
		
		setChangedListener((string) -> {
			setSuggestion("");
			if(ids != null) {
				List<String> possibleSuggestions = new ArrayList<>();
				for(String id : ids) {
					if(id.startsWith(string)) possibleSuggestions.add(id);
				}
				
				if(!possibleSuggestions.isEmpty()) {
					possibleSuggestions.sort(String.CASE_INSENSITIVE_ORDER);
					setSuggestion(possibleSuggestions.get(0).substring(string.length()));
				}
			}
			
			if(string == null || (ids != null && !ids.contains(string))) {
				setEditableColor(0xFF0000);
			} else {
				setEditableColor(ids != null ? 0xFFFFFF : 0xFFFF00);
				consumer.accept(value = string);
			}
		});
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
		this.setText(value);
	}

	@Override
	public void accept(String value) {
		setValue(value);
	}
	
	@Override
    public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
		super.setSuggestion(suggestion);
	}

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    	if(isFocused()) {
    		if(keyCode == GLFW.GLFW_KEY_TAB) {
    			if(suggestion != null && !suggestion.isEmpty() && getCursor() == getText().length()) {
    				setText(getText() + suggestion);
    				return true;
    			}
    		}
    	}
    	return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    public static <T> RegistryEntryOrTagWidget<T> direct(TextRenderer textRenderer, int x, int y, int width, int height, Registry<T> registry, String initialValue, Consumer<String> consumer) {
    	List<String> ids = new ArrayList<>();
    	for(Identifier id : registry.getIds()) ids.add(id.toString());
    	registry.streamTags().forEach(tag -> ids.add("#"+tag.id().toString()));
    	return new RegistryEntryOrTagWidget<>(textRenderer, x, y, width, height, initialValue, consumer, CompletableFuture.completedFuture(ids));
    }
}
