package me.andre111.voxedit.gui.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.andre111.voxedit.Networking;
import me.andre111.voxedit.gui.Textures;
import me.andre111.voxedit.gui.widget.ModListWidget;
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

	private ButtonWidget renameButton;
	private ButtonWidget editButton;
	private ButtonWidget deleteButton;
	
	private List<AddNBTElementButtonWidget> addButtons;
	
	private NBTEditorList rootList;
	private NBTEditorListEntry selected;
	private boolean doNotChangeSelection;

	public NBTEditorScreen(NbtCompound root) {
		super(Text.of("NBT Editor"));
		this.root = root;
		this.initialised = false;
	}

	@Override
	protected void init() {
		if(initialised) return;
		initialised = true;
		
		DirectionalLayoutWidget buttonContainer = new DirectionalLayoutWidget(0, 2, DisplayAxis.HORIZONTAL);
		buttonContainer.spacing(2);
		
		int buttonSize = 20;
		cutButton = buttonContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Cut"), (button) -> {
			if(selected != null) {
				client.keyboard.setClipboard(selected.element.asString());
				replaceSelected(null);
			}
		}).tooltip(Tooltip.of(Text.of("Cut Tag"))).size(buttonSize, 20).build()));
		copyButton = buttonContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Copy"), (button) -> {
			client.keyboard.setClipboard(selected != null ? selected.element.asString() : root.asString());
		}).tooltip(Tooltip.of(Text.of("Copy Tag"))).size(buttonSize, 20).build()));
		pasteButton = buttonContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Paste"), (button) -> {
			NbtCompound compound = getClipboardCompound();
			if(compound != null) addElement(CAN_ADD_COMPOUND, () -> compound);
		}).tooltip(Tooltip.of(Text.of("Paste Tag"))).size(buttonSize, 20).build()));
		
		buttonContainer.add(new EmptyWidget(3, 0));

		renameButton = buttonContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Rename"), (button) -> {
			if(selected != null && selected.hasName) {
				NbtCompound parent = (NbtCompound) selected.parent;
				InputScreen.getString(this, Text.of("Enter new Name:"), selected.key, (newName) -> {
					parent.remove(selected.key);
					parent.put(newName, selected.element);
					reload();
				});
			}
		}).tooltip(Tooltip.of(Text.of("Rename Tag"))).size(buttonSize, 20).build()));
		editButton = buttonContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Edit"), (button) -> {
			if(selected != null) {
				if(selected.element instanceof NbtByte nbtByte) {
					InputScreen.getNumber(this, Text.of("Modify value: "), nbtByte.byteValue(), Byte::parseByte, (newValue) -> replaceSelected(NbtByte.of(newValue)));
				} else if(selected.element instanceof NbtShort nbtShort) {
					InputScreen.getNumber(this, Text.of("Modify value: "), nbtShort.shortValue(), Short::parseShort, (newValue) -> replaceSelected(NbtShort.of(newValue)));
				} else if(selected.element instanceof NbtInt nbtInt) {
					InputScreen.getNumber(this, Text.of("Modify value: "), nbtInt.intValue(), Integer::parseInt, (newValue) -> replaceSelected(NbtInt.of(newValue)));
				} else if(selected.element instanceof NbtLong nbtLong) {
					InputScreen.getNumber(this, Text.of("Modify value: "), nbtLong.longValue(), Long::parseLong, (newValue) -> replaceSelected(NbtLong.of(newValue)));
				} else if(selected.element instanceof NbtFloat nbtFloat) {
					InputScreen.getNumber(this, Text.of("Modify value: "), nbtFloat.floatValue(), Float::parseFloat, (newValue) -> replaceSelected(NbtFloat.of(newValue)));
				} else if(selected.element instanceof NbtDouble nbtDouble) {
					InputScreen.getNumber(this, Text.of("Modify value: "), nbtDouble.doubleValue(), Double::parseDouble, (newValue) -> replaceSelected(NbtDouble.of(newValue)));
				} else if(selected.element instanceof NbtString nbtString) {
					InputScreen.getString(this, Text.of("Modify value: "), nbtString.asString(), (newValue) -> replaceSelected(NbtString.of(newValue)));
				}
			}
		}).tooltip(Tooltip.of(Text.of("Edit Value"))).size(buttonSize, 20).build()));
		deleteButton = buttonContainer.add(addDrawableChild(ButtonWidget.builder(Text.of("Delete"), (button) -> {
			replaceSelected(null);
		}).tooltip(Tooltip.of(Text.of("Delete Tag"))).size(buttonSize, 20).build()));

		buttonContainer.add(new EmptyWidget(3, 0));
		
		addButtons = new ArrayList<>();
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Byte Tag"), Textures.NBT_BYTE, getCanAddPredicate(NbtElement.BYTE_TYPE), () -> NbtByte.of((byte) 0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Short Tag"), Textures.NBT_SHORT, getCanAddPredicate(NbtElement.SHORT_TYPE), () -> NbtShort.of((short) 0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Int Tag"), Textures.NBT_INT, getCanAddPredicate(NbtElement.INT_TYPE), () -> NbtInt.of(0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Long Tag"), Textures.NBT_LONG, getCanAddPredicate(NbtElement.LONG_TYPE), () -> NbtLong.of(0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Float Tag"), Textures.NBT_FLOAT, getCanAddPredicate(NbtElement.FLOAT_TYPE), () -> NbtFloat.of(0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Double Tag"), Textures.NBT_DOUBLE, getCanAddPredicate(NbtElement.DOUBLE_TYPE), () -> NbtDouble.of(0)))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add String Tag"), Textures.NBT_STRING, getCanAddPredicate(NbtElement.STRING_TYPE), () -> NbtString.of("")))));
		
		buttonContainer.add(new EmptyWidget(3, 0));
		
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Byte Array Tag"), Textures.NBT_ARRAY, getCanAddPredicate(NbtElement.BYTE_ARRAY_TYPE), () -> new NbtByteArray(new byte[] {})))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Int Array Tag"), Textures.NBT_ARRAY, getCanAddPredicate(NbtElement.INT_ARRAY_TYPE), () -> new NbtIntArray(new int[] {})))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Long Array Tag"), Textures.NBT_ARRAY, getCanAddPredicate(NbtElement.LONG_ARRAY_TYPE), () -> new NbtLongArray(new long[] {})))));
		
		buttonContainer.add(new EmptyWidget(3, 0));
		
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add List Tag"), Textures.NBT_LIST, getCanAddPredicate(NbtElement.LIST_TYPE), () -> new NbtList()))));
		addButtons.add(buttonContainer.add(addDrawableChild(new AddNBTElementButtonWidget(Text.of("Add Compound Tag"), Textures.NBT_COMPOUND, getCanAddPredicate(NbtElement.COMPOUND_TYPE), () -> new NbtCompound()))));
		
		buttonContainer.refreshPositions();
		buttonContainer.setX((width-buttonContainer.getWidth())/2);
		
		rootList = addDrawableChild(new NBTEditorList(root, width, height-40-2*4, 6));
		
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> {
        	Networking.clientSendNBTEditorResult(root);
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

		renameButton.active = selected != null && selected.hasName;
		editButton.active = selected != null && (selected.element instanceof AbstractNbtNumber || selected.element instanceof NbtString);
		deleteButton.active = selected != null;
		
		for(var addButton : addButtons) addButton.active = addButton.canAddTo.test(selected != null ? selected.element : root);
	}
	
	@SuppressWarnings("unchecked")
	private void addElement(Predicate<NbtElement> canAddTo, Supplier<NbtElement> creator) {
		NbtElement parent = selected != null ? selected.element : root;
		if(canAddTo.test(parent)) {
			if(parent instanceof NbtCompound compound) {
				InputScreen.getString(this, Text.of("Enter Tag Name:"), "", (tagName) -> {
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
		Networking.clientSendNBTEditorResult(null);
	}

	@Environment(value=EnvType.CLIENT)
	private class AddNBTElementButtonWidget extends ButtonWidget {
		private final Identifier icon;
		private final Predicate<NbtElement> canAddTo;
		
		protected AddNBTElementButtonWidget(Text tooltip, Identifier icon, Predicate<NbtElement> canAddTo, Supplier<NbtElement> creator) {
			super(0, 0, 20, 20, Text.empty(), (b) -> NBTEditorScreen.this.addElement(canAddTo, creator), (s) -> Text.empty().copy());
			this.icon = icon;
			this.canAddTo = canAddTo;
			setTooltip(Tooltip.of(tooltip));
		}

	    @Override
	    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
	    	super.renderWidget(context, mouseX, mouseY, delta);
	    	context.drawGuiTexture(icon, getX()+(getWidth()-8)/2, getY()+(getHeight()-8)/2, 8, 8);
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
			System.out.println(key+" "+expanded);
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
