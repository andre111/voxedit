package me.andre111.voxedit.gui.screen;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class InputScreen extends Screen {
	private final Screen parent;
	
	protected InputScreen(Screen parent) {
		super(Text.empty());
		this.parent = parent;
	}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    	parent.render(context, mouseX, mouseY, delta);
    	context.getMatrices().translate(0, 0, 200);
    	super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public void close() {
        this.client.setScreen(parent);
    }
    
    public static <T extends Number> void getNumber(Screen parent, Text title, T value, Function<String, T> parser, Consumer<T> callback) {
    	MinecraftClient client = MinecraftClient.getInstance();
    	
        TextFieldWidget input = new TextFieldWidget(client.textRenderer, 204, 20, Text.empty());
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
    	
        TextFieldWidget input = new TextFieldWidget(client.textRenderer, 204, 20, Text.empty());
        input.setMaxLength(128);
        input.setText(value);
        
        create(parent, title, () -> callback.accept(input.getText()), (adder) -> adder.add(input, 2)).setFocused(input);
    }
    
    private static InputScreen create(Screen parent, Text title, Runnable callback, ContentCreator creator) {
    	InputScreen screen = new InputScreen(parent);
        
    	MinecraftClient client = MinecraftClient.getInstance();
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 4);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(new TextWidget(204, 20, title, client.textRenderer), 2, gridWidget.copyPositioner().marginTop(50));
        creator.addContent(adder);
        adder.add(ButtonWidget.builder(ScreenTexts.DONE, button -> {
        	callback.run();
            screen.close();
        }).width(98).build());
        adder.add(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            screen.close();
        }).width(98).build());
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
