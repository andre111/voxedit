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
import java.util.List;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.client.EditorState;
import me.andre111.voxedit.client.gui.screen.InputScreen;
import me.andre111.voxedit.client.gui.widget.ModListWidget;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.network.CPCommand;
import me.andre111.voxedit.network.Command;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class EditorPanelHistory extends EditorPanel {
	private TextWidget infoWidget;
	private ButtonWidget clearButton;
	private HistoryListWidget historyWidget;
	private boolean refreshing = false;

	public EditorPanelHistory(EditorWidget parent) {
		super(parent, VoxEdit.id("history"), Text.translatable("voxedit.screen.panel.history"));
		
		EditorState.UPDATE_HISTORY.register(this::refreshPositions);
	}
    
    @Override
    public void refreshPositions() {
    	if(refreshing) return;
    	refreshing = true;
    	
    	clearContent();
    	
    	infoWidget = new TextWidget(width/2-gapX/2, 20, Text.translatable("voxedit.history.size", getFormattedSize()), MinecraftClient.getInstance().textRenderer);
    	addContent(infoWidget);
    	clearButton = ButtonWidget.builder(Text.translatable("voxedit.history.clear"), (button) -> {
    		InputScreen.showConfirmation(parent.getScreen(), Text.translatable("voxedit.prompt.history.clear"), () -> {
    			ClientNetworking.sendCommand(Command.CLEAR_HISTORY);
    		});
    	}).size(width/2-gapX/2, 20).build();
    	addContent(clearButton);
    	historyWidget = new HistoryListWidget();
    	addContent(historyWidget);
    	ButtonWidget undoButton = ButtonWidget.builder(Text.translatable("voxedit.history.undo"), (button) -> { ClientPlayNetworking.send(new CPCommand(Command.UNDO, "")); }).size(width/2-gapX/2, 20).build();
    	if(EditorState.historyIndex() < 0) undoButton.active = false;
    	addContent(undoButton);
    	ButtonWidget redoButton = ButtonWidget.builder(Text.translatable("voxedit.history.redo"), (button) -> { ClientPlayNetworking.send(new CPCommand(Command.REDO, "")); }).size(width/2-gapX/2, 20).build();
    	if(EditorState.historyIndex() >= EditorState.history().size()-1) redoButton.active = false;
    	addContent(redoButton);
        
        super.refreshPositions();
        
        refreshing = false;
    }
    
    private static String getFormattedSize() {
    	long size = EditorState.historySize();
    	if(size < 0) size = 0;
    	
    	return String.format("%.2f MB", size / (1024.0 * 1024.0));
    }


	@Environment(value=EnvType.CLIENT)
	class HistoryListWidget extends ModListWidget<HistoryListWidget.HistoryEntry> {
		public HistoryListWidget() {
			super(MinecraftClient.getInstance(), EditorPanelHistory.this.width, 150, 20, 6);
			updateEntries();
		}

	    @Override
	    public boolean isFocused() {
	        return true;
	    }

		public void updateEntries() {
			clearEntries();
			
			for (int j = 0; j < EditorState.history().size(); j++) {
				addEntry(new HistoryEntry(j));
			}
			
	        setScrollAmount(getMaxScroll());
		}

		@Environment(value=EnvType.CLIENT)
		class HistoryEntry extends ModListWidget.Entry<HistoryEntry> {
			private List<Element> children = new ArrayList<>();
			
			private ButtonWidget widget;

			private HistoryEntry(int index) {
				EditStats stats = EditorState.history().get(index);
				widget = ButtonWidget.builder(stats.fullText(), (button) -> ClientNetworking.sendHistorySelect(index)).size(HistoryListWidget.this.width-6*3, 16).build();
				if(index == EditorState.historyIndex()) widget.active = false;
				children.add(widget);
			}

			@Override
			public int getHeight() {
				return 16;
			}

			@Override
			public void positionChildren() {
				widget.setX(getX());
				widget.setY(getY());
			}

			@Override
			protected void appendClickableNarrations(NarrationMessageBuilder var1) {
			}

			@Override
			public List<? extends Element> children() {
				return children;
			}
			
			@Override
	        public boolean isFocused() {
	            return false;
	        }
		}
	}
}
