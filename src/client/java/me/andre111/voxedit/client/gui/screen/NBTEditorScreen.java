/*
 * Copyright (c) 2023 André Schweiger
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.andre111.voxedit.client.gui.Textures;
import me.andre111.voxedit.client.gui.widget.ModListWidget;
import me.andre111.voxedit.client.network.ClientNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget.DisplayAxis;
import net.minecraft.client.gui.widget.EmptyWidget;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtShort;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class NBTEditorScreen extends Screen {
	private final NbtCompound root;
	
	private boolean initialised;
	
	private ButtonWidget cutButton;
	private ButtonWidget copyButton;
	private ButtonWidget pasteButton;

	private ButtonWidget editButton;
	private ButtonWidget renameButton;
	private ButtonWidget deleteButton;
	
	private List<AddNBTElementButtonWidget> addButtons;
	
	private NBTEditorList rootList;
	private NBTEditorListEntry selected;
	private boolean doNotChangeSelection;

	public NBTEditorScreen(NbtCompound root) {
		super(Text.translatable("voxedit.screen.nbtEditor"));
		this.root = root;
		this.initialised = false;
	}

	@Override
	protected void init() {
		if(initialised) return;
		initialised = true;
		
		DirectionalLayoutWidget buttonContainer = new DirectionalLayoutWidget(0, 2, DisplayAxis.HORIZONTAL);
		buttonContainer.spacing(2);
		
		cutButton = buttonContainer.add(addDrawableChild(new IconButtonWidget(Text.translatable("voxedit.screen.nbtEditor.action.cut"), Textures.EDITOR_CUT, (button) -> {
			if(selected != null) {
				client.keyboard.setClipboard(selected.element.asString());
				replaceSelected(null);
			}
		})));
		copyButton = buttonContainer.add(addDrawableChild(new IconButtonWidget(Text.translatable("voxedit.screen.nbtEditor.action.copy"), Textures.EDITOR_COPY, (button) -> {
			client.keyboard.setClipboard(selected != null ? selected.element.asString() : root.asString());
		})));
		pasteButton = buttonContainer.add(addDrawableChild(new IconButtonWidget(Text.translatable("voxedit.screen.nbtEditor.action.paste"), Textures.EDITOR_PASTE, (button) -> {
			NbtCompound compound = getClipboardCompound();
			if(compound != null) addElement(CAN_ADD_COMPOUND, () -> compound);
		})));
		
		buttonContainer.add(new EmptyWidget(3, 0));

		editButton = buttonContainer.add(addDrawableChild(new IconButtonWidget(Text.translatable("voxedit.screen.nbtEditor.action.edit"), Textures.EDITOR_EDIT, (button) -> {
			if(selected != null) {
				if(selected.element instanceof NbtByte nbtByte) {
					InputScreen.getNumber(this, Text.translatable("voxedit.screen.nbtEditor.input.value"), nbtByte.byteValue(), Byte::parseByte, (newValue) -> replaceSelected(NbtByte.of(newValue)));
				} else if(selected.element instanceof NbtShort nbtShort) {
					InputScreen.getNumber(this, Text.translatable("voxedit.screen.nbtEditor.input.value"), nbtShort.shortValue(), Short::parseShort, (newValue) -> replaceSelected(NbtShort.of(newValue)));
				} else if(selected.element instanceof NbtInt nbtInt) {
					InputScreen.getNumber(this, Text.translatable("voxedit.screen.nbtEditor.input.value"), nbtInt.intValue(), Integer::parseInt, (newValue) -> replaceSelected(NbtInt.of(newValue)));
				} else if(selected.element instanceof NbtLong nbtLong) {
					InputScreen.getNumber(this, Text.translatable("voxedit.screen.nbtEditor.input.value"), nbtLong.longValue(), Long::parseLong, (newValue) -> replaceSelected(NbtLong.of(newValue)));
				} else if(selected.element instanceof NbtFloat nbtFloat) {
					InputScreen.getNumber(this, Text.translatable("voxedit.screen.nbtEditor.input.value"), nbtFloat.floatValue(), Float::parseFloat, (newValue) -> replaceSelected(NbtFloat.of(newValue)));
				} else if(selected.element instanceof NbtDouble nbtDouble) {
					InputScreen.getNumber(this, Text.translatable("voxedit.screen.nbtEditor.input.value"), nbtDouble.doubleValue(), Double::parseDouble, (newValue) -> replaceSelected(NbtDouble.of(newValue)));
				} else if(selected.element instanceof NbtString nbtString) {
					InputScreen.getString(this, Text.translatable("voxedit.screen.nbtEditor.input.value"), nbtString.asString(), (newValue) -> replaceSelected(NbtString.of(newValue)));
				}
			}
		})));
		renameButton = buttonContainer.add(addDrawableChild(new IconButtonWidget(Text.translatable("voxedit.screen.nbtEditor.action.rename"), Textures.EDITOR_RENAME, (button) -> {
			if(selected != null && selected.hasName) {
				NbtCompound parent = (NbtCompound) selected.parent;
				InputScreen.getString(this, Text.translatable("voxedit.screen.nbtEditor.input.tagName"), selected.key, (newName) -> {
					parent.remove(selected.key);
					parent.put(newName, selected.element);
					reload();
				});
			}
		})));
		deleteButton = buttonContainer.add(addDrawableChild(new IconButtonWidget(Text.translatable("voxedit.screen.nbtEditor.action.delete"), Textures.EDITOR_DELETE, (button) -> {
			replaceSelected(null);
		})));

		buttonContainer.add(new EmptyWidget(3, 0));
		
		addButtons = new ArrayList<>();
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("byte", Textures.NBT_BYTE, getCanAddPredicate(NbtElement.BYTE_TYPE), () -> NbtByte.of((byte) 0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("short", Textures.NBT_SHORT, getCanAddPredicate(NbtElement.SHORT_TYPE), () -> NbtShort.of((short) 0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("int", Textures.NBT_INT, getCanAddPredicate(NbtElement.INT_TYPE), () -> NbtInt.of(0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("long", Textures.NBT_LONG, getCanAddPredicate(NbtElement.LONG_TYPE), () -> NbtLong.of(0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("float", Textures.NBT_FLOAT, getCanAddPredicate(NbtElement.FLOAT_TYPE), () -> NbtFloat.of(0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("double", Textures.NBT_DOUBLE, getCanAddPredicate(NbtElement.DOUBLE_TYPE), () -> NbtDouble.of(0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("string", Textures.NBT_STRING, getCanAddPredicate(NbtElement.STRING_TYPE), () -> NbtString.of("")))));
		
		buttonContainer.add(new EmptyWidget(3, 0));
		
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("byteArray", Textures.NBT_ARRAY, getCanAddPredicate(NbtElement.BYTE_ARRAY_TYPE), () -> new NbtByteArray(new byte[] {})))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("intArray", Textures.NBT_ARRAY, getCanAddPredicate(NbtElement.INT_ARRAY_TYPE), () -> new NbtIntArray(new int[] {})))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("longArray", Textures.NBT_ARRAY, getCanAddPredicate(NbtElement.LONG_ARRAY_TYPE), () -> new NbtLongArray(new long[] {})))));
		
		buttonContainer.add(new EmptyWidget(3, 0));
		
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("list", Textures.NBT_LIST, getCanAddPredicate(NbtElement.LIST_TYPE), () -> new NbtList()))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget("compound", Textures.NBT_COMPOUND, getCanAddPredicate(NbtElement.COMPOUND_TYPE), () -> new NbtCompound()))));
		
		buttonContainer.refreshPositions();
		buttonContainer.setX((width-buttonContainer.getWidth())/2);
		
		rootList = addDrawableChild(new NBTEditorList(root, width, height-40-2*4, 6));
		
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
        	ClientNetworking.sendNBTEditorResult(root);
            close();
        }).dimensions(width / 2 - 155, height - 20-2, 150, 20).build());
        
        addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, button -> {
           	close();
        }).dimensions(width / 2 + 5, height - 20-2, 150, 20).build());
		
		updateButtons();
	}

	@Override
    protected void initTabNavigation() {
    }
	
	@SuppressWarnings("unchecked")
	private void replaceSelected(NbtElement newElement) {
		if(selected == null) return;
		
		NbtElement parent = selected.parent;
		if(parent instanceof NbtCompound c) {
			c.remove(selected.key);
			if(newElement != null) c.put(selected.key, newElement);
			reload();
		}
		if(parent instanceof AbstractNbtList l) {
			int index = Integer.parseInt(selected.key);
			l.remove(index);
			if(newElement != null && (l.getHeldType() == newElement.getType() || l.getHeldType() == NbtElement.END_TYPE)) l.add(index, newElement);
			reload();
		}
	}
	
	private void reload() {
		//TODO: try to restore selection if possible
		selected = null;
		rootList.reload();
	}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
    	boolean value = super.mouseClicked(mouseX, mouseY, button);
    	if(!value) setSelected(null);
    	doNotChangeSelection = false;
    	return value;
    }
	
	private void setSelected(NBTEditorListEntry selected) {
		if(this.doNotChangeSelection) return;
		
		this.selected = selected;
		this.doNotChangeSelection = true;
		
		updateButtons();
	}
	
	private void updateButtons() {
		cutButton.active = selected != null && selected.element instanceof NbtCompound;
		copyButton.active = selected == null || selected.element instanceof NbtCompound;
		pasteButton.active = CAN_ADD_COMPOUND.test(selected != null ? selected.element : root) && getClipboardCompound() != null;

		editButton.active = selected != null && (selected.element instanceof AbstractNbtNumber || selected.element instanceof NbtString);
		renameButton.active = selected != null && selected.hasName;
		deleteButton.active = selected != null;
		
		for(var addButton : addButtons) addButton.active = addButton.canAddTo.test(selected != null ? selected.element : root);
	}
	
	@SuppressWarnings("unchecked")
	private void addElement(Predicate<NbtElement> canAddTo, Supplier<NbtElement> creator) {
		NbtElement parent = selected != null ? selected.element : root;
		if(canAddTo.test(parent)) {
			if(parent instanceof NbtCompound compound) {
				InputScreen.getString(this, Text.translatable("voxedit.screen.nbtEditor.input.tagName"), "", (tagName) -> {
					if(!compound.contains(tagName)) {
						compound.put(tagName, creator.get());
						reload();
					}
				});
			} else if(parent instanceof AbstractNbtList list) {
				list.add(creator.get());
				reload();
			}
		}
	}
	
	private final Predicate<NbtElement> CAN_ADD_COMPOUND = getCanAddPredicate(NbtElement.COMPOUND_TYPE);
	private Predicate<NbtElement> getCanAddPredicate(byte type) {
		return (parent) -> {
			if(parent instanceof NbtCompound) return true;
			if(parent instanceof NbtByteArray) return type == NbtElement.BYTE_TYPE;
			if(parent instanceof NbtIntArray) return type == NbtElement.INT_TYPE;
			if(parent instanceof NbtLongArray) return type == NbtElement.LONG_TYPE;
			if(parent instanceof NbtList list) {
				return list.getHeldType() ==  NbtElement.END_TYPE || list.getHeldType() == type;
			}
			return false;
		};
	}
	
	private NbtCompound getClipboardCompound() {
		try {
			return StringNbtReader.parse(client.keyboard.getClipboard());
		} catch (CommandSyntaxException e) {
			return null;
		}
	}
	
	@Override
	public void close() {
		super.close();
		ClientNetworking.sendNBTEditorResult(null);
	}
	
	@Environment(value=EnvType.CLIENT)
	private class IconButtonWidget extends ButtonWidget {
		private final Identifier icon;
		protected IconButtonWidget(Text tooltip, Identifier icon, PressAction action) {
			super(0, 0, 20, 20, Text.empty(), action, (s) -> Text.empty().copy());
			this.icon = icon;
			setTooltip(Tooltip.of(tooltip));
		}

	    @Override
	    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
	    	super.renderWidget(context, mouseX, mouseY, delta);
	    	context.drawGuiTexture(icon, getX()+(getWidth()-8)/2, getY()+(getHeight()-8)/2, 8, 8);
	    }
	}

	@Environment(value=EnvType.CLIENT)
	private class AddNBTElementButtonWidget extends IconButtonWidget {
		private final Predicate<NbtElement> canAddTo;
		
		protected AddNBTElementButtonWidget(String type, Identifier icon, Predicate<NbtElement> canAddTo, Supplier<NbtElement> creator) {
			super(Text.translatable("voxedit.screen.nbtEditor.action.add."+type), icon, (b) -> NBTEditorScreen.this.addElement(canAddTo, creator));
			this.canAddTo = canAddTo;
		}
	}

	@Environment(value=EnvType.CLIENT)
	private class NBTEditorList extends ModListWidget<NBTEditorListEntry> {
		private final NbtElement compoundOrList;
		
		public NBTEditorList(NbtElement compoundOrList, int width, int height, int padding) {
			super(NBTEditorScreen.this.client, width, height, 20+2*2, padding);
			this.compoundOrList = compoundOrList;
			reload();
		}
		
		public void reload() {
			double scroll = getScrollAmount();
			
			Map<String, NBTEditorListEntry> oldEntries = new HashMap<>();
			for(NBTEditorListEntry entry : children()) oldEntries.put(entry.key, entry);
			clearEntries();
			
			if(compoundOrList instanceof NbtCompound compound) {
				compound.getKeys().stream().sorted().forEach(key -> {
					if(oldEntries.containsKey(key) && oldEntries.get(key).parent == compoundOrList && oldEntries.get(key).element == compound.get(key)) {
						addEntry(oldEntries.get(key).reload());
					} else {
						addEntry(new NBTEditorListEntry(getWidth(), key, true, compoundOrList, compound.get(key)));
					}
				});
			} else if(compoundOrList instanceof AbstractNbtList<?> list) {
				for(int i=0; i<list.size(); i++) {
					String key = i+"";
					if(oldEntries.containsKey(key) && oldEntries.get(key).parent == compoundOrList && oldEntries.get(key).element == list.get(i)) {
						addEntry(oldEntries.get(key).reload());
					} else {
						addEntry(new NBTEditorListEntry(getWidth(), key, false, compoundOrList, list.get(i)));
					}
				}
			}
			
			setScrollAmount(scroll);
		}
	}

	@Environment(value=EnvType.CLIENT)
	private class NBTEditorListEntry extends ModListWidget.Entry<NBTEditorListEntry> {
		private List<NBTEditorList> children = new ArrayList<>();
		
		private String key;
		private OrderedText displayText;
		private boolean expanded;
		
		private final boolean hasName;
		private final NbtElement parent;
		private final NbtElement element;
		
		private NBTEditorListEntry(int width, String key, boolean hasName, NbtElement parent, NbtElement element) {
			this.key = key;
			this.hasName = hasName;
			this.parent = parent;
			this.element = element;
			
			updateDisplayText();
			
			if(element instanceof NbtCompound || element instanceof AbstractNbtList<?>) {
				NBTEditorList list = new NBTEditorList(element, width-20, -1, 2);
				list.setRenderBackground(false);
				children.add(list);
			}
		}
		
		private void updateDisplayText() {
			displayText = OrderedText.concat(
				OrderedText.styledForwardsVisitedString(key, Style.EMPTY),
				OrderedText.styledForwardsVisitedString(": "+getValueRepresentation(), Style.EMPTY.withColor(0x888888))
			);
		}
		
		private NBTEditorListEntry reload() {
			updateDisplayText();
			for(NBTEditorList child : children) child.reload();
			return this;
		}
		
		private void setExpanded(boolean expanded) {
			this.expanded = expanded;
			rootList.refreshPositions();
		}
		
		@Override
        public void setFocused(boolean focused) {
			if(focused) setSelected(this);
		}

        @Override
        public boolean isFocused() {
            return selected == this;
        }

		@Override
		public List<? extends Element> children() {
			return children;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float tickDelta) {
			Identifier icon = getIcon();
			if(icon != null) {
				context.drawGuiTexture(icon, getX(), getY(), 8, 8);
			}
			context.drawText(textRenderer, displayText, getX()+10, getY(), -1, false);
			
			if(expanded) {
				for(NBTEditorList child : children) {
					child.render(context, mouseX, mouseY, tickDelta);
				}
			}
		}
		
	    @Override
	    public boolean mouseClicked(double mouseX, double mouseY, int button) {
	    	if(getX() < mouseX && mouseX < getX() + 10 && getY() < mouseY && mouseY < getY() + 10) {
	    		setExpanded(!expanded);
	    		return true;
	    	}
	    	return super.mouseClicked(mouseX, mouseY, button);
	    }

		@Override
		public int getHeight() {
			int height = 11;
			if(expanded) {
				for(NBTEditorList child : children) height += child.getHeight();
			}
			return height;
		}

		@Override
		public void positionChildren() {
	    	for(NBTEditorList child : children) {
	    		child.setPosition(getX()+10, getY()+11);
	    	}
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		}
		
		private Identifier getIcon() {
			if(element instanceof NbtCompound) return Textures.NBT_COMPOUND;
			if(element instanceof NbtList) return Textures.NBT_LIST;
			if(element instanceof AbstractNbtList) return Textures.NBT_ARRAY;
			if(element instanceof NbtByte) return Textures.NBT_BYTE;
			if(element instanceof NbtShort) return Textures.NBT_SHORT;
			if(element instanceof NbtInt) return Textures.NBT_INT;
			if(element instanceof NbtLong) return Textures.NBT_LONG;
			if(element instanceof NbtFloat) return Textures.NBT_FLOAT;
			if(element instanceof NbtDouble) return Textures.NBT_DOUBLE;
			if(element instanceof NbtString) return Textures.NBT_STRING;
			return null;
		}
		
		private String getValueRepresentation() {
			if(element instanceof NbtCompound c) return c.getSize()+" Entries";
			if(element instanceof NbtList l) return l.size()+" Entries";
			if(element instanceof AbstractNbtList l) return l.size()+" Entries";
			if(element instanceof NbtByte) return element.asString();
			if(element instanceof NbtShort) return element.asString();
			if(element instanceof NbtInt) return element.asString();
			if(element instanceof NbtLong) return element.asString();
			if(element instanceof NbtFloat) return element.asString();
			if(element instanceof NbtDouble) return element.asString();
			if(element instanceof NbtString) return element.asString();
			return "UNKNOWN NBT TAG";
		}
	}
}
