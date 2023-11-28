package me.andre111.voxedit.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.gui.widget.BlockStateWidget;
import me.andre111.voxedit.gui.widget.IntSliderWidget;
import me.andre111.voxedit.gui.widget.ModListWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class EditBlockPaletteScreen extends Screen {
    private final Screen parent;
	private final int minSize;
	private final boolean includeProperties;
	private final boolean showWeights;
	private final Consumer<BlockPalette> callback;
	private BlockPalette palette;
	
	private BlockPaletteListWidget paletteWidget;
    private ButtonWidget removeEntryButton;

	protected EditBlockPaletteScreen(Screen parent, Text text, int minSize, boolean includeProperties, boolean showWeights, BlockPalette palette, Consumer<BlockPalette> callback) {
		super(text);

		this.parent = parent;
		this.minSize = minSize;
		this.includeProperties = includeProperties;
		this.showWeights = showWeights;
		this.callback = callback;
		this.palette = new BlockPalette(palette.getEntries());
	}

    @Override
    protected void init() {
    	paletteWidget = addDrawableChild(new BlockPaletteListWidget());
    	removeEntryButton = addDrawableChild(ButtonWidget.builder(Text.of("Remove Entry"), button -> {
            if (!hasEntrySelected()) return;
            if(palette.size() <= minSize) return;
            
            List<BlockPalette.Entry> list = palette.getEntries();
            int index = paletteWidget.getSelectedOrNull().index;
            list.remove(index);
            
            paletteWidget.setSelected(list.isEmpty() ? null : paletteWidget.children().get(Math.min(index, list.size() - 1)));
            palette = new BlockPalette(list);
            paletteWidget.updateEntries();
            updateRemoveEntryButton();
        }).dimensions(width / 2 - 155, height - 52, 150, 20).build());
    	
    	addDrawableChild(ButtonWidget.builder(Text.of("Add Entry"), button -> {
            List<BlockPalette.Entry> list = palette.getEntries();
            list.add(new BlockPalette.Entry(Blocks.STONE.getDefaultState(), 1));
            
            palette = new BlockPalette(list);
            paletteWidget.updateEntries();
            paletteWidget.setSelected(paletteWidget.children().get(paletteWidget.children().size()-1));
            updateRemoveEntryButton();
        }).dimensions(width / 2 + 5, height - 52, 150, 20).build());
    	
        
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
            callback.accept(palette);
            client.setScreen(parent);
        }).dimensions(width / 2 - 155, height - 28, 150, 20).build());
        
        addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
            client.setScreen(parent);
        }).dimensions(width / 2 + 5, height - 28, 150, 20).build());
        
        updateRemoveEntryButton();
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFF);
    }

    private void updateRemoveEntryButton() {
    	removeEntryButton.active = hasEntrySelected() && palette.size() > minSize;
    }

    private boolean hasEntrySelected() {
        return paletteWidget.getSelectedOrNull() != null;
    }


	@Environment(value=EnvType.CLIENT)
	class BlockPaletteListWidget extends ModListWidget<me.andre111.voxedit.gui.screen.EditBlockPaletteScreen.BlockPaletteListWidget.BlockPaletteEntry> {
		public BlockPaletteListWidget() {
			super(EditBlockPaletteScreen.this.client, EditBlockPaletteScreen.this.width, EditBlockPaletteScreen.this.height - 80, 20, 6);
			updateEntries();
		}

		@Override
		public void setSelected(BlockPaletteEntry entry) {
			super.setSelected(entry);
			updateRemoveEntryButton();
		}

	    @Override
	    public boolean isFocused() {
	        return true;
	    }

		public void updateEntries() {
			int i = children().indexOf(getSelectedOrNull());
			clearEntries();
			
			for (int j = 0; j < palette.size(); j++) {
				addEntry(new BlockPaletteEntry(j));
			}
			List<BlockPaletteEntry> list = children();
			if (i >= 0 && i < list.size()) {
				setSelected(list.get(i));
			}
		}

		@Environment(value=EnvType.CLIENT)
		class BlockPaletteEntry extends ModListWidget.Entry<BlockPaletteEntry> {
			private List<Element> children = new ArrayList<>();
			
			private final int index;
			private BlockStateWidget stateWidget;
			private IntSliderWidget weightWidget;

			private BlockPaletteEntry(int index) {
				this.index = index;
				
				BlockPalette.Entry paletteEntry = palette.getEntry(index);
				stateWidget = new BlockStateWidget(textRenderer, 0, 0, 250, 20, includeProperties, paletteEntry.state(), (blockState) -> {
					BlockPalette.Entry oldEntry = palette.getEntry(index);
					palette.setEntry(index, new BlockPalette.Entry(blockState, oldEntry.weight()));
				});
				children.add(stateWidget);
				
				if(showWeights) {
					weightWidget = new IntSliderWidget(0, 0, 100, 20, Text.of("Weight"), 1, 32, paletteEntry.weight(), (weight) -> {
						BlockPalette.Entry oldEntry = palette.getEntry(index);
						palette.setEntry(index, new BlockPalette.Entry(oldEntry.state(), weight));
					});
					children.add(weightWidget);
				}
			}

			@Override
			public int getHeight() {
				return 23;
			}

			@Override
			public void positionChildren() {
				stateWidget.setX(getX());
				stateWidget.setY(getY());
				
				weightWidget.setX(getX()+getWidth()-weightWidget.getWidth());
				weightWidget.setY(getY());
			}

			@Override
			protected void renderWidget(DrawContext context, int mouseX, int mouseY, float tickDelta) {
				stateWidget.render(context, mouseX, mouseY, tickDelta);
				
				if(weightWidget != null) {
					weightWidget.render(context, mouseX, mouseY, tickDelta);
				}
			}

			@Override
			protected void appendClickableNarrations(NarrationMessageBuilder var1) {
			}

			@Override
			public List<? extends Element> children() {
				return children;
			}
		}
	}
}
