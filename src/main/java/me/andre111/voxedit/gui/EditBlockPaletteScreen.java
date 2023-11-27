package me.andre111.voxedit.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.gui.widget.BlockStateWidget;
import me.andre111.voxedit.gui.widget.IntSliderWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
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
	class BlockPaletteListWidget extends ElementListWidget<me.andre111.voxedit.gui.EditBlockPaletteScreen.BlockPaletteListWidget.BlockPaletteEntry> {
		public BlockPaletteListWidget() {
			super(EditBlockPaletteScreen.this.client, EditBlockPaletteScreen.this.width, EditBlockPaletteScreen.this.height - 80, 20, 24);
			updateEntries();
		}

		@Override
	    public int getRowWidth() {
	        return EditBlockPaletteScreen.this.width - 80;
	    }

		@Override
		protected int getScrollbarPositionX() {
			return this.width - 30;
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

	    @Override
	    protected boolean isSelectedEntry(int index) {
	        return Objects.equals(getSelectedOrNull(), getEntry(index));
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
		class BlockPaletteEntry extends ElementListWidget.Entry<BlockPaletteEntry> {
			private final int index;
			private BlockStateWidget stateWidget;
			private IntSliderWidget weightWidget;

			private BlockPaletteEntry(int index) {
				this.index = index;
				
				BlockPalette.Entry paletteEntry = palette.getEntry(index);
				stateWidget = new BlockStateWidget(textRenderer, 0, 0, 200, 20, includeProperties, paletteEntry.state(), (blockState) -> {
					BlockPalette.Entry oldEntry = palette.getEntry(index);
					palette.setEntry(index, new BlockPalette.Entry(blockState, oldEntry.weight()));
				});
				children.add(stateWidget);
				selectableChildren.add(stateWidget);
				
				if(showWeights) {
					weightWidget = new IntSliderWidget(0, 0, 100, 20, Text.of("Weight"), 1, 32, paletteEntry.weight(), (weight) -> {
						BlockPalette.Entry oldEntry = palette.getEntry(index);
						palette.setEntry(index, new BlockPalette.Entry(oldEntry.state(), weight));
					});
					children.add(weightWidget);
					selectableChildren.add(weightWidget);
				}
			}
			
			@Override
			public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
				stateWidget.setX(x);
				stateWidget.setY(y);
				stateWidget.render(context, mouseX, mouseY, tickDelta);
				
				if(weightWidget != null) {
					weightWidget.setX(x+entryWidth-weightWidget.getWidth()-4);
					weightWidget.setY(y);
					weightWidget.render(context, mouseX, mouseY, tickDelta);
				}
			}

	        @Override
	        public boolean mouseClicked(double mouseX, double mouseY, int button) {
	        	BlockPaletteListWidget.this.setSelected(this);
	        	return super.mouseClicked(mouseX, mouseY, button);
	        }
			
			private List<Element> children = new ArrayList<>();
			private List<Selectable> selectableChildren = new ArrayList<>();

			@Override
			public List<? extends Element> children() {
				return children;
			}

			@Override
			public List<? extends Selectable> selectableChildren() {
				return selectableChildren;
			}
		}
	}
}
