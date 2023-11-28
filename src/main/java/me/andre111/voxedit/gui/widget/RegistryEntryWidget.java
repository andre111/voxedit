package me.andre111.voxedit.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import me.andre111.voxedit.Networking;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class RegistryEntryWidget<T> extends TextFieldWidget {
	private Identifier value;
	private String suggestion;
	
	private List<Identifier> ids;

    public RegistryEntryWidget(TextRenderer textRenderer, int x, int y, int width, int height, RegistryKey<? extends Registry<T>> registryKey, Identifier initialValue, Consumer<Identifier> consumer) {
		super(textRenderer, x, y, width, height, Text.empty());
		this.value = initialValue;
		
		Networking.clientGetServerRegistryEntries(registryKey).thenAccept(ids -> {
			this.ids = ids;
		});
		
		setMaxLength(200);
		setText(initialValue.toString());
		
		setChangedListener((string) -> {
			setSuggestion("");
			if(ids != null) {
				List<String> possibleSuggestions = new ArrayList<>();
				for(Identifier id : ids) {
					String fullPath = id.toString();
					if(fullPath.startsWith(string)) possibleSuggestions.add(fullPath);
				}
				if(possibleSuggestions.isEmpty()) {
					for(Identifier id : ids) {
						String path = id.getPath();
						if(path.startsWith(string)) possibleSuggestions.add(path);
					}
				}
				
				if(!possibleSuggestions.isEmpty()) {
					possibleSuggestions.sort(String.CASE_INSENSITIVE_ORDER);
					setSuggestion(possibleSuggestions.get(0).substring(string.length()));
				}
			}
			
			Identifier id = Identifier.tryParse(string);
			if(id == null || (ids != null && !ids.contains(id))) {
				setEditableColor(0xFF0000);
			} else {
				setEditableColor(ids != null ? 0xFFFFFF : 0xFFFF00);
				consumer.accept(value = id);
			}
		});
	}
	
	public Identifier getValue() {
		return value;
	}
	
	public void setValue(Identifier value) {
		this.value = value;
		this.setText(value.toString());
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
}
