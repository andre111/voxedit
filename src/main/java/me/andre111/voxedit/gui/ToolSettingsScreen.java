package me.andre111.voxedit.gui;

import me.andre111.voxedit.Networking;
import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.gui.widget.BlockPaletteDisplayWidget;
import me.andre111.voxedit.gui.widget.IntSliderWidget;
import me.andre111.voxedit.tool.Tool;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget.DisplayAxis;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class ToolSettingsScreen extends Screen {
	private int contentWidth = 100;
	private int contentHeight = 96;
	private int padding = 2;
	
	private CyclingButtonWidget<ToolState.Mode> modeSelector;
	private CyclingButtonWidget<ToolState.Shape> shapeSelector;
	private IntSliderWidget radiusSlider;
	
	private DirectionalLayoutWidget blockWidgetContainer;
	private BlockPaletteDisplayWidget blockStateDisplay;
	
	private DirectionalLayoutWidget filterWidgetContainer;
	private BlockPaletteDisplayWidget filterDisplay;
	private Tool lastTool = null;

	public ToolSettingsScreen() {
		super(Text.of("Tool Settings"));
		rebuild();
	}
	
	public void rebuild() {
		if(VoxEdit.active == null) return;
		if(lastTool != VoxEdit.active.tool()) {
			lastTool = VoxEdit.active.tool();
			init(MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight());
		} else {
			reload();
		}
	}
	
	private void reload() {
		ToolState state = VoxEdit.active;
		if(state == null) return;
		if(modeSelector != null && modeSelector.getValue() != state.mode()) modeSelector.setValue(state.mode());
		if(shapeSelector != null && shapeSelector.getValue() != state.shape()) shapeSelector.setValue(state.shape());
		if(radiusSlider != null && radiusSlider.getIntValue() != state.radius()) radiusSlider.setIntValue(state.radius());
		if(blockStateDisplay != null && !blockStateDisplay.getValue().equals(state.palette())) blockStateDisplay.setValue(state.palette());
		if(filterDisplay != null && !filterDisplay.getValue().equals(state.filter())) filterDisplay.setValue(state.filter());
	}
		

	@Override
	protected void init() {
		contentWidth = 100;
		contentHeight = 8+22;
		if(VoxEdit.active == null) return;
		if(VoxEdit.active.tool().usesMode()) contentHeight += 22;
		if(VoxEdit.active.tool().usesShape()) contentHeight += 22;
		if(VoxEdit.active.tool().usesRadius()) contentHeight += 22;
		if(VoxEdit.active.tool().usesBlockPalette()) contentHeight += 22;
		if(VoxEdit.active.tool().usesBlockFilter()) contentHeight += 22;
		
		int x = 2+padding;
		int y = (height-contentHeight-padding*2) / 2;
		
		int currentY = y;
		addDrawableChild(new TextWidget(x, currentY, contentWidth, 14, Text.of("Tool Settings"), textRenderer).alignCenter());
		currentY += 12;
		addDrawableChild(CyclingButtonWidget.builder(Tool::asText).values(VoxEdit.TOOL_REGISTRY.stream().toList()).initially(VoxEdit.active.tool()).build(x, currentY, contentWidth, 20, Text.of("Tool"), (button, tool) -> {
            Networking.clientSendToolState(VoxEdit.active.withTool(tool));
        }));
		currentY += 22;
		if(VoxEdit.active.tool().usesMode())  {
			modeSelector = addDrawableChild(CyclingButtonWidget.builder(ToolState.Mode::asText).values(ToolState.Mode.values()).initially(VoxEdit.active.mode()).build(x, currentY, contentWidth, 20, Text.of("Mode"), (button, mode) -> {
	            Networking.clientSendToolState(VoxEdit.active.withMode(mode));
	        }));
			currentY += 22;
		}
		if(VoxEdit.active.tool().usesShape()) {
			shapeSelector = addDrawableChild(CyclingButtonWidget.builder(ToolState.Shape::asText).values(ToolState.Shape.values()).initially(VoxEdit.active.shape()).build(x, currentY, contentWidth, 20, Text.of("Shape"), (button, shape) -> {
	            Networking.clientSendToolState(VoxEdit.active.withShape(shape));
	        }));
			currentY += 22;
		}
		if(VoxEdit.active.tool().usesRadius()) {
			radiusSlider = addDrawableChild(new IntSliderWidget(x, currentY, contentWidth, 20, Text.of("Radius"), 1, 16, VoxEdit.active.radius(), (radius) -> {
				Networking.clientSendToolState(VoxEdit.active.withRadius(radius));
			}));
			currentY += 22;
		}
		
		if(VoxEdit.active.tool().usesBlockPalette()) {
			blockWidgetContainer = new DirectionalLayoutWidget(x, currentY, DisplayAxis.HORIZONTAL);
			blockWidgetContainer.spacing(2);
			blockWidgetContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Edit Palette"), (button) -> {
				MinecraftClient.getInstance().setScreen(new EditBlockPaletteScreen(this, Text.of("Edit Block Palette"), 1, true, true, VoxEdit.active.palette(), palette -> {
					Networking.clientSendToolState(VoxEdit.active.withBlockPalette(palette));
				}));
			}).size(contentWidth-22, 20).build()));
			blockStateDisplay = blockWidgetContainer.add(addDrawableChild(new BlockPaletteDisplayWidget(0, 0, 20, 20, VoxEdit.active.palette())));
			blockWidgetContainer.refreshPositions();
			currentY += 22;
		}
		
		if(VoxEdit.active.tool().usesBlockFilter()) {
			filterWidgetContainer = new DirectionalLayoutWidget(x, currentY, DisplayAxis.HORIZONTAL);
			filterWidgetContainer.spacing(2);
			filterWidgetContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Edit Filter"), (button) -> {
				MinecraftClient.getInstance().setScreen(new EditBlockPaletteScreen(this, Text.of("Edit Block Filter"), 0, false, false, VoxEdit.active.filter(), filter -> {
					Networking.clientSendToolState(VoxEdit.active.withBlockFilter(filter));
				}));
			}).size(contentWidth-22, 20).build()));
			filterDisplay = filterWidgetContainer.add(addDrawableChild(new BlockPaletteDisplayWidget(0, 0, 20, 20, VoxEdit.active.filter())));
			filterWidgetContainer.refreshPositions();
			currentY += 22;
		}
	}
	
	@Override
	public void renderInGameBackground(DrawContext context) {
		int x = 2;
		int y = (context.getScaledWindowHeight()-contentHeight-padding*2) / 2;
        context.fillGradient(x, y, x + contentWidth + padding * 2, y + contentHeight + padding * 2, -1072689136, -804253680);
    }
}
