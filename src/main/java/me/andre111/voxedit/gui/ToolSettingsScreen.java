package me.andre111.voxedit.gui;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.IntSliderWidget;
import me.andre111.voxedit.Networking;
import me.andre111.voxedit.ToolState;
import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.ToolState.Mode;
import me.andre111.voxedit.ToolState.Shape;
import me.andre111.voxedit.gui.widget.BlockPaletteDisplayWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget.DisplayAxis;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ToolSettingsScreen extends Screen {
	private int contentWidth = 100;
	private int contentHeight = 96;
	private int padding = 2;
	
	private CyclingButtonWidget<ToolState.Mode> modeSelector;
	private CyclingButtonWidget<ToolState.Shape> shapeSelector;
	private IntSliderWidget radiusSlider;
	
	private DirectionalLayoutWidget blockWidgetContainer;
	private BlockPaletteDisplayWidget blockStateDisplay;

	public ToolSettingsScreen() {
		super(Text.of("Tool Settings"));
		rebuild();
	}
	
	public void rebuild() {
		init(MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight());
	}
	
	public void reload() {
		ToolState state = VoxEdit.active;
		if(modeSelector != null && modeSelector.getValue() != state.mode()) modeSelector.setValue(state.mode());
		if(shapeSelector != null && shapeSelector.getValue() != state.shape()) shapeSelector.setValue(state.shape());
		if(radiusSlider != null && radiusSlider.getIntValue() != state.radius()) radiusSlider.setIntValue(state.radius());
		if(blockStateDisplay != null && !blockStateDisplay.getValue().equals(state.palette())) blockStateDisplay.setValue(state.palette());
	}

	@Override
	protected void init() {
		contentWidth = 100;
		contentHeight = 8;
		if(VoxEdit.activeItem.usesMode()) contentHeight += 22;
		if(VoxEdit.activeItem.usesShape()) contentHeight += 22;
		if(VoxEdit.activeItem.usesRadius()) contentHeight += 22;
		if(VoxEdit.activeItem.usesBlockState()) contentHeight += 22*2;
		
		int x = 2+padding;
		int y = (height-contentHeight-padding*2) / 2;
		
		//TODO: Change title based on tool
		int currentY = y;
		addDrawableChild(new TextWidget(x, currentY, contentWidth, 14, VoxEdit.activeItem.getName().copy().append(": Settings"), textRenderer).alignCenter());
		currentY += 12;
		if(VoxEdit.activeItem.usesMode())  {
			modeSelector = addDrawableChild(CyclingButtonWidget.builder(ToolState.Mode::asText).values(ToolState.Mode.values()).initially(VoxEdit.active.mode()).build(x, currentY, contentWidth, 20, Text.of("Mode"), (button, mode) -> {
	            Networking.clientSendToolState(VoxEdit.active.withMode(mode));
	        }));
			currentY += 22;
		}
		if(VoxEdit.activeItem.usesShape()) {
			shapeSelector = addDrawableChild(CyclingButtonWidget.builder(ToolState.Shape::asText).values(ToolState.Shape.values()).initially(VoxEdit.active.shape()).build(x, currentY, contentWidth, 20, Text.of("Shape"), (button, shape) -> {
	            Networking.clientSendToolState(VoxEdit.active.withShape(shape));
	        }));
			currentY += 22;
		}
		if(VoxEdit.activeItem.usesRadius()) {
			radiusSlider = addDrawableChild(new IntSliderWidget(x, currentY, contentWidth, 20, Text.of("Radius"), 1, 16, VoxEdit.active.radius(), (radius) -> {
				Networking.clientSendToolState(VoxEdit.active.withRadius(radius));
			}));
			currentY += 22;
		}
		
		if(VoxEdit.activeItem.usesBlockState()) {
			blockWidgetContainer = new DirectionalLayoutWidget(x, currentY, DisplayAxis.HORIZONTAL);
			blockWidgetContainer.spacing(2);
			blockStateDisplay = blockWidgetContainer.add(addDrawableChild(new BlockPaletteDisplayWidget(0, 0, 20, 20, VoxEdit.active.palette())));
			blockWidgetContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Set focused"), (button) -> {
				World world = MinecraftClient.getInstance().world;
				if(world != null) {
					BlockPos pos = VoxEdit.getTargetOf(VoxEdit.player, VoxEdit.active);
					if(pos != null && !world.isAir(pos)) {
						Networking.clientSendToolState(VoxEdit.active.withBlockPalette(new BlockPalette(world.getBlockState(pos))));
					}
				}
			}).size(contentWidth-22, 20).build()));
			blockWidgetContainer.refreshPositions();
			currentY += 22;
			
			addDrawableChild(ButtonWidget.builder(Text.of("Edit Palette"), (button) -> {
				MinecraftClient.getInstance().setScreen(new EditBlockPaletteScreen(this, VoxEdit.active.palette(), palette -> {
					Networking.clientSendToolState(VoxEdit.active.withBlockPalette(palette));
				}));
			}).position(x, currentY).size(contentWidth, 20).build());
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
