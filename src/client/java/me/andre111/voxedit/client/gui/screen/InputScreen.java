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
package me.andre111.voxedit.client.gui.screen;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import me.andre111.voxedit.client.VoxEditClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(value=EnvType.CLIENT)
public class InputScreen extends Screen {
	private final Screen parent;
	
	protected InputScreen(Screen parent) {
		super(Text.empty());
		this.parent = parent;
	}
	
	@Override
	public void init() {
		if(parent instanceof UnscaledScreen) VoxEditClient.unscaleGui();
	}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    	if(parent instanceof UnscaledScreen) VoxEditClient.unscaleGui();
    	parent.render(context, mouseX, mouseY, delta);
    	context.getMatrices().translate(0, 0, 200);
    	if(parent instanceof UnscaledScreen) VoxEditClient.unscaleGui();
    	super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
    	if(parent instanceof UnscaledScreen) VoxEditClient.restoreGuiScale();
        this.client.setScreen(parent);
    }
    
    public static <T extends Number> void getNumber(Screen parent, Text title, T value, Function<String, T> parser, Consumer<T> callback) {
    	MinecraftClient client = MinecraftClient.getInstance();
    	
        TextFieldWidget input = new TextFieldWidget(client.textRenderer, 260, 20, Text.empty());
        input.setMaxLength(128);
        input.setChangedListener(string -> {
        	try {
        		parser.apply(string);
        		input.setEditableColor(0xFFFFFF);
        	} catch(NumberFormatException e) {
        		input.setEditableColor(0xFF0000);
        	}
        });
        input.setText(value+"");
        
        create(parent, title, () -> {
        	try {
        		callback.accept(parser.apply(input.getText()));
        	} catch(NumberFormatException e) {
        	}
        }, (adder) -> adder.add(input, 2)).setFocused(input);
    }
    
    public static void getString(Screen parent, Text title, String value, Consumer<String> callback) {
    	MinecraftClient client = MinecraftClient.getInstance();
    	
        TextFieldWidget input = new TextFieldWidget(client.textRenderer, 260, 20, Text.empty());
        input.setMaxLength(128);
        input.setText(value);
        
        create(parent, title, () -> callback.accept(input.getText()), (adder) -> adder.add(input, 2)).setFocused(input);
    }
    
    public static <T> void getSelector(Screen parent, Text title, Text label, T value, List<T> values, Function<T, Text> toText, Consumer<T> callback) {
    	CyclingButtonWidget<T> button = CyclingButtonWidget.builder(toText).values(values).initially(value).build(0, 0, 260, 20, label, (b, v) -> {});
        
        create(parent, title, () -> callback.accept(button.getValue()), (adder) -> adder.add(button, 2)).setFocused(button);
    }
    
    public static void showConfirmation(Screen parent, Text title, Runnable action) {
    	create(parent, title, () -> action.run(), (adder) -> {});
    }
    
    private static InputScreen create(Screen parent, Text title, Runnable callback, ContentCreator creator) {
    	InputScreen screen = new InputScreen(parent);
        
    	if(parent instanceof UnscaledScreen) VoxEditClient.unscaleGui();
    	else VoxEditClient.restoreGuiScale();
    	
    	MinecraftClient client = MinecraftClient.getInstance();
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 4);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(new TextWidget(260, 20, title, client.textRenderer), 2, gridWidget.copyPositioner().marginTop(50));
        creator.addContent(adder);
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            screen.close();
        	callback.run();
        }).width(126).build());
        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            screen.close();
        }).width(126).build());
        gridWidget.refreshPositions();
        
        SimplePositioningWidget.setPos(gridWidget, 0, 0, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), 0.5f, 0.25f);
        gridWidget.forEachChild(screen::addDrawableChild);
        client.setScreen(screen);
       	return screen;
    }
    
    private static interface ContentCreator {
    	public void addContent(GridWidget.Adder adder);
    }
}
