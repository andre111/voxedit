package me.andre111.voxedit.client.gui.widget;

import java.util.ArrayList;
import java.util.List;

import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.tool.data.BlockPalette;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class EditorPanelPalette extends EditorPanel {
	private BlockPaletteListWidget paletteWidget;
    private ButtonWidget removeEntryButton;
    private int minSize = 1;
    private boolean includeProperties = true;
    private boolean showWeights = true;

	public EditorPanelPalette(EditorWidget parent, Location location) {
		super(parent, location, Text.translatable("voxedit.screen.panel.palette"));
	}


    private void updateRemoveEntryButton() {
    	removeEntryButton.active = hasEntrySelected() && EditorState.blockPalette().size() > minSize;
    }

    private boolean hasEntrySelected() {
        return paletteWidget.getSelectedOrNull() != null;
    }
    
    @Override
    public void refreshPositions() {
    	children.clear();
    	
		paletteWidget = new BlockPaletteListWidget();
		children.add(paletteWidget);
    	removeEntryButton = ButtonWidget.builder(Text.translatable("voxedit.screen.blockPalette.remove"), button -> {
            if (!hasEntrySelected()) return;
            if(EditorState.blockPalette().size() <= minSize) return;
            
            List<BlockPalette.Entry> list = EditorState.blockPalette().getEntries();
            int index = paletteWidget.getSelectedOrNull().index;
            list.remove(index);
            
            paletteWidget.setSelected(list.isEmpty() ? null : paletteWidget.children().get(Math.min(index, list.size() - 1)));
            EditorState.blockPalette(new BlockPalette(list));
            paletteWidget.updateEntries();
            updateRemoveEntryButton();
        }).size(width / 2 - gap, 20).build();
    	children.add(removeEntryButton);
    	
    	children.add(ButtonWidget.builder(Text.translatable("voxedit.screen.blockPalette.add"), button -> {
            List<BlockPalette.Entry> list = EditorState.blockPalette().getEntries();
            list.add(new BlockPalette.Entry(Blocks.STONE.getDefaultState(), 1));
            
            EditorState.blockPalette(new BlockPalette(list));
            paletteWidget.updateEntries();
            paletteWidget.setSelected(paletteWidget.children().get(paletteWidget.children().size()-1));
            updateRemoveEntryButton();
        }).size(width / 2 - gap, 20).build());
    	
        updateRemoveEntryButton();
        
        super.refreshPositions();
    }


	@Environment(value=EnvType.CLIENT)
	class BlockPaletteListWidget extends ModListWidget<BlockPaletteListWidget.BlockPaletteEntry> {
		public BlockPaletteListWidget() {
			super(MinecraftClient.getInstance(), EditorPanelPalette.this.width-1, 300, 20, 6);
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
			
			for (int j = 0; j < EditorState.blockPalette().size(); j++) {
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
				
				BlockPalette.Entry paletteEntry = EditorState.blockPalette().getEntry(index);
				stateWidget = new BlockStateWidget(MinecraftClient.getInstance().textRenderer, 0, 0, BlockPaletteListWidget.this.width-6*2-32-100, 20, includeProperties, paletteEntry.state(), (blockState) -> {
					BlockPalette.Entry oldEntry = EditorState.blockPalette().getEntry(index);
					EditorState.blockPalette().setEntry(index, new BlockPalette.Entry(blockState, oldEntry.weight()));
				});
				children.add(stateWidget);
				
				if(showWeights) {
					weightWidget = new IntSliderWidget(0, 0, 100, 20, Text.translatable("voxedit.screen.blockPalette.weight"), 1, 32, paletteEntry.weight(), (weight) -> {
						BlockPalette.Entry oldEntry = EditorState.blockPalette().getEntry(index);
						EditorState.blockPalette().setEntry(index, new BlockPalette.Entry(oldEntry.state(), weight));
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
				
				if(weightWidget != null) {
					weightWidget.setX(getX()+getWidth()-weightWidget.getWidth());
					weightWidget.setY(getY());
				}
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
