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
package me.andre111.voxedit.client.gui.widget.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.gui.screen.InputScreen;
import me.andre111.voxedit.client.gui.widget.BlockStateWidget;
import me.andre111.voxedit.client.gui.widget.IntSliderWidget;
import me.andre111.voxedit.client.gui.widget.LineHorizontal;
import me.andre111.voxedit.client.gui.widget.ModListWidget;
import me.andre111.voxedit.client.gui.widget.SelectionWidget;
import me.andre111.voxedit.data.BlockPalette;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class EditorPanelPalette extends EditorPanel {
	private SelectionWidget<String> presets;
	private BlockPaletteListWidget paletteWidget;
    private ButtonWidget removeEntryButton;
    private int minSize = 1;
    private boolean showWeights = true;
    private boolean refreshing = false;

	public EditorPanelPalette(EditorWidget parent) {
		super(parent, VoxEdit.id("palette"), Text.translatable("voxedit.screen.panel.palette"));
	}

    private void updateButtons() {
    	removeEntryButton.active = hasEntrySelected() && EditorState.blockPalette().size() > minSize;
    }

    private boolean hasEntrySelected() {
        return paletteWidget != null && paletteWidget.getSelectedOrNull() != null;
    }
    
    @Override
    public void refreshPositions() {
    	if(refreshing) return;
    	refreshing = true;
    	
    	clearContent();
    	
		// presets / saved configs
		presets = new SelectionWidget<>(width, (width - 4*2)/3, 32, null, this::setPreset);
		presets.setPadding(2);
		presets.setGap(2);
		for(var preset : EditorState.persistant().palettePresets().entrySet()) {
			presets.addOption(preset.getKey(), Text.of(preset.getKey()));
		}
		presets.withAdditionalButton(Text.of("+"), () -> true, this::savePreset);
		presets.withAdditionalButton(Text.of("-"), () -> presets.getValue() != null, this::deletePreset);
		addContent(presets);
		addContent(new LineHorizontal(width, null));
    	
		// palette editor
		paletteWidget = new BlockPaletteListWidget();
		addContent(paletteWidget);
    	removeEntryButton = ButtonWidget.builder(Text.translatable("voxedit.screen.blockPalette.remove"), button -> {
            if (!hasEntrySelected()) return;
            if(EditorState.blockPalette().size() <= minSize) return;
            
            List<BlockPalette.Entry> list = EditorState.blockPalette().getEntries();
            int index = paletteWidget.getSelectedOrNull().index;
            list.remove(index);
            
            paletteWidget.setSelected(list.isEmpty() ? -1 : Math.min(index, list.size() - 1));
            EditorState.blockPalette(new BlockPalette(list));
            paletteWidget.updateEntries();
            updateButtons();
        }).size(width / 2 - gapX, 20).build();
    	addContent(removeEntryButton);
    	
    	addContent(ButtonWidget.builder(Text.translatable("voxedit.screen.blockPalette.add"), button -> {
            List<BlockPalette.Entry> list = EditorState.blockPalette().getEntries();
            list.add(new BlockPalette.Entry(Blocks.STONE.getDefaultState(), new HashSet<>(), 1));
            
            EditorState.blockPalette(new BlockPalette(list));
            paletteWidget.updateEntries();
            paletteWidget.setSelected(paletteWidget.children().size()-1);
            updateButtons();
        }).size(width / 2 - gapX, 20).build());
    	
        updateButtons();
        
        super.refreshPositions();
        
        refreshing = false;
    }
    
    private void setPreset(String name) {
		if(name == null || name.isBlank()) return;
		BlockPalette palette = EditorState.persistant().palettePresets().get(name);
		if(palette == null) return;
		
    	EditorState.blockPalette(palette);
		paletteWidget.updateEntries();
		updateButtons();
    }
	
	private void savePreset() {
		InputScreen.getString(parent.getScreen(), Text.translatable("voxedit.prompt.preset.name"), "", (name) -> {
			if(name == null || name.isBlank()) return;
			BlockPalette existingPalette = EditorState.blockPalette();
			if(existingPalette == null) return;
			BlockPalette palette = new BlockPalette(existingPalette.getEntries());
			
			if(EditorState.persistant().palettePresets().containsKey(name)) {
				InputScreen.showConfirmation(parent.getScreen(), Text.translatable("voxedit.prompt.preset.override", name), () -> {
					EditorState.persistant().palettePreset(name, palette);
					refreshPositions();
				});
			} else {
				EditorState.persistant().palettePreset(name, palette);
				refreshPositions();
			}
		});
	}
	
	private void deletePreset() {
		String name = presets.getValue();
		if(name == null) return;
		
		InputScreen.showConfirmation(parent.getScreen(), Text.translatable("voxedit.prompt.preset.delete", name), () -> {
			EditorState.persistant().deletePalettePreset(name);
			refreshPositions();
		});
	}

	@Environment(value=EnvType.CLIENT)
	class BlockPaletteListWidget extends ModListWidget<BlockPaletteListWidget.BlockPaletteEntry> {
		public BlockPaletteListWidget() {
			super(MinecraftClient.getInstance(), EditorPanelPalette.this.width, 150, 20, 6);
			updateEntries();
		}

		@Override
		public void setSelected(int selectedIndex) {
			super.setSelected(selectedIndex);
			updateButtons();
		}

	    @Override
	    public boolean isFocused() {
	        return true;
	    }

		public void updateEntries() {
			int i = selectedIndex();
			clearEntries();
			
			for (int j = 0; j < EditorState.blockPalette().size(); j++) {
				addEntry(new BlockPaletteEntry(j));
			}
			List<BlockPaletteEntry> list = children();
			if (i >= 0 && i < list.size()) {
				setSelected(i);
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
				stateWidget = new BlockStateWidget(0, 0, BlockPaletteListWidget.this.width-6*2-32-64, paletteEntry.state(), true, (blockState) -> {
					BlockPalette.Entry oldEntry = EditorState.blockPalette().getEntry(index);
					EditorState.blockPalette().setEntry(index, new BlockPalette.Entry(blockState, stateWidget.getSpecifiedProperties(), oldEntry.weight()));
					BlockPaletteListWidget.this.refreshPositions();
				});
				children.add(stateWidget);
				
				if(showWeights) {
					weightWidget = new IntSliderWidget(0, 0, 64, 20, Text.translatable("voxedit.screen.blockPalette.weight"), 1, 32, paletteEntry.weight(), (weight) -> {
						BlockPalette.Entry oldEntry = EditorState.blockPalette().getEntry(index);
						EditorState.blockPalette().setEntry(index, new BlockPalette.Entry(oldEntry.state(), oldEntry.specifiedProperties(), weight));
					});
					children.add(weightWidget);
				}
			}

			@Override
			public int getHeight() {
				return stateWidget.getHeight()+3;
			}

			@Override
			public void positionChildren() {
				stateWidget.setX(getX());
				stateWidget.setY(getY());
				
				if(weightWidget != null) {
					weightWidget.setX(getX()+getWidth()-weightWidget.getWidth());
					weightWidget.setY(getY()+(getHeight()-weightWidget.getHeight())/2);
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
