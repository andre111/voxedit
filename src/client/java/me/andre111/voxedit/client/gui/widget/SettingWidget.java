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
package me.andre111.voxedit.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import me.andre111.voxedit.data.Setting;
import me.andre111.voxedit.data.Size;
import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.Configurable;
import me.andre111.voxedit.data.Configured;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ContainerWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.LayoutWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class SettingWidget<V, S extends Setting<V>> extends ContainerWidget implements LayoutWidget {
	public static final int BASE_HEIGTH = 16;
	
	protected final S setting;
	protected final Supplier<V> valueGetter;
	protected final Consumer<V> valueSetter;
	protected final Consumer<Setting<?>> notifier;

	protected final LayoutWidget parent;
	protected final List<Element> children = new ArrayList<>();
	protected boolean reloading = false;
	protected int gap = 2;
	protected int paddingX = 0;
	protected int paddingY = 0;

	public SettingWidget(LayoutWidget parent, int x, int y, int width, int height, S setting, Supplier<V> valueGetter, Consumer<V> valueSetter, Consumer<Setting<?>> notifier) {
		super(x, y, width, height, Text.empty());

		this.setting = setting;
		this.valueGetter = valueGetter;
		this.valueSetter = valueSetter;
		this.notifier = notifier;
		
		this.parent = parent;
		this.children.addAll(create());
	}

	protected V read() {
		return valueGetter.get();
	}

	protected void write(V value) {
		if(reloading) return;
		valueSetter.accept(value);
		notifier.accept(setting);
	}

	public void reload() {
		reloading = true;
		reloadValue();
		for(Element child : children()) {
			if(child instanceof SettingWidget<?, ?> settingWidget) settingWidget.reload();
		}
		refreshPositions();
		reloading = false;
	}

	@Override
	public List<? extends Element> children() {
		return children;
	}

	@Override
	public void forEachElement(Consumer<Widget> consumer) {
		for(Element child : children) {
			if(child instanceof Widget widget) consumer.accept(widget);
		}
	}

	@Override
	public void refreshPositions() {
		int oldWidth = width;
		int oldHeight = height;
		
		int x = getX() + paddingX;
		int y = getY() + paddingY;
		int maxHeight = y;
		for(Element child : children) {
			if(child instanceof ClickableWidget widget) {
				if(!widget.visible) continue;

				int childWidth = widget.getWidth();
				int childHeight = widget.getHeight();
				if(x + childWidth > getX()+width-paddingX) {
					x = getX() + paddingX;
					y = maxHeight + gap;
				}
				widget.setPosition(x, y);
				x += childWidth + gap;
				maxHeight = Math.max(maxHeight, y + childHeight);
			}
		}
		height = Math.max(32, maxHeight + paddingY) - getY();

		LayoutWidget.super.refreshPositions();
		
		if(oldWidth != width || oldHeight != height) {
			if(parent != null) {
				parent.refreshPositions();
			}
		}
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float tickDelta) {
		for(Element child : children()) {
			if(child instanceof Drawable drawable) drawable.render(context, mouseX, mouseY, tickDelta);
		}
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder var1) {
		// TODO Auto-generated method stub

	}

	protected abstract List<Element> create();
	protected abstract void reloadValue();

	public static List<SettingWidget<?, ?>> forInstance(LayoutWidget parent, int x, int y, int width, int height, Configurable<?> instance, Supplier<Config> configGetter, Consumer<Config> configSetter, Consumer<Setting<?>> notifier) {
		List<SettingWidget<?, ?>> list = new ArrayList<>();
		for(Setting<?> setting : instance.getSettings()) {
			addOf(parent, x, y, width, height, list, setting, configGetter, configSetter, notifier);
		}
		return list;
	}

	private static <T> void addOf(LayoutWidget parent, int x, int y, int width, int height, List<SettingWidget<?, ?>> list, Setting<T> setting, Supplier<Config> configGetter, Consumer<Config> configSetter, Consumer<Setting<?>> notifier) {
		list.add(of(parent, x, y, width, height, setting, () -> setting.get(configGetter.get()), (value) -> configSetter.accept(configGetter.get().with(setting, value)), notifier));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SettingWidget<?, ?> of(LayoutWidget parent, int x, int y, int width, int height, Setting<T> setting, Supplier<T> configGetter, Consumer<T> configSetter, Consumer<Setting<?>> notifier) {
		if(setting instanceof Setting.Bool boolSetting) return new Bool(parent, x, y, width, height, boolSetting, (Supplier<Boolean>) configGetter, (Consumer<Boolean>) configSetter, notifier);
		if(setting instanceof Setting.FixedValues enumSetting) return new FixedValues<>(parent, x, y, width, height, enumSetting, configGetter, configSetter, notifier);
		if(setting instanceof Setting.Int intSetting) return new Int(parent, x, y, width, height, intSetting, (Supplier<Integer>) configGetter, (Consumer<Integer>) configSetter, notifier);
		if(setting instanceof Setting.TSIdentifier identifierSetting) return new TSIdentifier<>(parent, x, y, width, height, identifierSetting, (Supplier<Identifier>) configGetter, (Consumer<Identifier>) configSetter, notifier);
		if(setting instanceof Setting.TSRegistry registrySetting) return new TSRegistry<>(parent, x, y, width, height, registrySetting, configGetter, configSetter, notifier);
		if(setting instanceof Setting.TSNested nestedSetting) return new TSNested(parent, x, y, width, height, nestedSetting, configGetter, configSetter, notifier);
		if(setting instanceof Setting.TSSize sizeSetting) return new TSSize(parent, x, y, width, height, sizeSetting, (Supplier<Size>) configGetter, (Consumer<Size>) configSetter, notifier);
		if(setting instanceof Setting.TSList listSetting) return new TSList(parent, x, y, width, height, listSetting, configGetter, configSetter, notifier);
		return null;
	}

	public static class Bool extends SettingWidget<Boolean, Setting<Boolean>> {
		private CyclingButtonWidget<Boolean> button;

		public Bool(LayoutWidget parent, int x, int y, int width, int height, Setting<Boolean> setting, Supplier<Boolean> configGetter, Consumer<Boolean> configSetter, Consumer<Setting<?>> notifier) {
			super(parent, x, y, width, height, setting, configGetter, configSetter, notifier);
		}

		@Override
		protected List<Element> create() {
			return List.of(button = CyclingButtonWidget.<Boolean>builder(value -> value ? ScreenTexts.ON : ScreenTexts.OFF).values(new Boolean[] { true, false }).initially(read()).build(getX(), getY(), width, height, setting.label(), (b, value) -> {
				write(value);
			}));
		}

		@Override
		protected void reloadValue() {
			button.setValue(read());
		}
	}

	public static class FixedValues<E> extends SettingWidget<E, Setting.FixedValues<E>> {
		private Consumer<E> input;

		public FixedValues(LayoutWidget parent, int x, int y, int width, int height, Setting.FixedValues<E> setting, Supplier<E> configGetter, Consumer<E> configSetter, Consumer<Setting<?>> notifier) {
			super(parent, x, y, width, height, setting, configGetter, configSetter, notifier);
		}

		@Override
		protected List<Element> create() {
			if(setting.showFixedSelection()) {
				var widget = new SelectionWidget<E>(width, width/3, height, read(), (value) -> {
					write(value);
				});
				for(var entry : setting.values()) widget.addOption(entry, setting.toText().apply(entry));
				input = (v) -> widget.setValue(v);
				return List.of(widget);
			} else {
				var button = CyclingButtonWidget.builder(setting.toText()).values(setting.values()).initially(read()).build(getX(), getY(), width, height, setting.label(), (b, value) -> {
					write(value);
				});
				input = (v) -> button.setValue(v);
				return List.of(button);
			}
		}

		@Override
		protected void reloadValue() {
			input.accept(read());
		}
	}

	public static class Int extends SettingWidget<Integer, Setting.Int> {
		private IntSliderWidget slider;

		public Int(LayoutWidget parent, int x, int y, int width, int height, Setting.Int setting, Supplier<Integer> configGetter, Consumer<Integer> configSetter, Consumer<Setting<?>> notifier) {
			super(parent, x, y, width, height, setting, configGetter, configSetter, notifier);
		}

		@Override
		protected List<Element> create() {
			return List.of(slider = new IntSliderWidget(getX(), getY(), width, height, setting.label(), setting.min(), setting.max(), read(), (value) -> {
				write(value);
			}));
		}

		@Override
		protected void reloadValue() {
			slider.setIntValue(read());
		}
	}

	public static class TSIdentifier<T> extends SettingWidget<Identifier, Setting.TSIdentifier<T>> {
		private RegistryEntryWidget<T> input;

		public TSIdentifier(LayoutWidget parent, int x, int y, int width, int height, Setting.TSIdentifier<T> setting, Supplier<Identifier> configGetter, Consumer<Identifier> configSetter, Consumer<Setting<?>> notifier) {
			super(parent, x, y, width, height, setting, configGetter, configSetter, notifier);
		}

		@Override
		protected List<Element> create() {
			return List.of(input = RegistryEntryWidget.serverRetrieved(MinecraftClient.getInstance().textRenderer, getX(), getY(), width, height, setting.registryKey(), read(), (value) -> {
				write(value);
			}));
		}

		@Override
		protected void reloadValue() {
			input.setValue(read());
		}
	}

	public static class TSRegistry<T> extends SettingWidget<T, Setting.TSRegistry<T>> {
		private Consumer<Identifier> input;

		public TSRegistry(LayoutWidget parent, int x, int y, int width, int height, Setting.TSRegistry<T> setting, Supplier<T> configGetter, Consumer<T> configSetter, Consumer<Setting<?>> notifier) {
			super(parent, x, y, width, height, setting, configGetter, configSetter, notifier);
		}

		@Override
		protected List<Element> create() {
			if(setting.showFixedSelection()) {
				var widget = new SelectionWidget<T>(width, width/3, height, read(), (value) -> {
					write(value);
				});
				for(var entry : setting.registry()) widget.addOption(entry, setting.toText().apply(entry));
				input = (id) -> widget.setValue(setting.registry().get(id));
				return List.of(widget);
			} else {
				var widget = RegistryEntryWidget.direct(MinecraftClient.getInstance().textRenderer, getX(), getY(), width, height, setting.registry(), read(), (value) -> {
					write(value);
				});
				input = widget;
				return List.of(widget);
			}
		}

		@Override
		protected void reloadValue() {
			input.accept(setting.registry().getId(read()));
		}
	}

	public static class TSNested<T extends Configurable<T>> extends SettingWidget<Configured<T>, Setting.TSNested<T>> {
		public TSNested(LayoutWidget parent, int x, int y, int width, int height, Setting.TSNested<T> setting, Supplier<Configured<T>> configGetter, Consumer<Configured<T>> configSetter, Consumer<Setting<?>> notifier) {
			super(parent, x, y, width, height, setting, configGetter, configSetter, notifier);
			paddingX = 6;
			paddingY = 12;
		}

		@Override
		protected List<Element> create() {
			return new ArrayList<>();
		}

		@Override
		protected void reloadValue() {
			children.clear();

			Configured<T> c = read();
			setMessage(c.value().getName());

			var baseWidget = new SelectionWidget<>(width-paddingX*2, (width-paddingX*2)/3, BASE_HEIGTH, c.value(), (value) -> {
				if(reloading) return;
				
				// transfer as much as possible
				Config oldConfig = read().config();
				Config newConfig = value.getDefaultConfig();
				for(Setting<?> setting : value.getSettings()) {
					if(setting.isPresentAndValid(oldConfig)) {
						newConfig = newConfig.withRaw(setting.key(), oldConfig.values().get(setting.key()));
					}
				}
				
				write(new Configured<>(value, newConfig));
				reload();
			});
			for(var entry : setting.getAvailableValues()) baseWidget.addOption(entry, entry.getName());
			children.add(baseWidget);
			
			if(setting.showConfig()) {
				var settingWidgets = forInstance(this, getX(), getY(), width-paddingX*2, BASE_HEIGTH, c.value(), () -> {
					return read().config();
				}, (config) -> {
					write(read().with(config));
				}, (s) -> {});
				for(var sw : settingWidgets) sw.reload();
				children.addAll(settingWidgets);
			}
			
			refreshPositions();
		}

		private Rect2i getTitleRect() {
			int x = getX()+paddingX+1;
			int y = getY();
			int w = width-paddingX*2;
			int h = 12;
			Text text = getMessage();
			if(text != null) {
				TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
				w = textRenderer.getWidth(text) + 4;
			}
			return new Rect2i(x, y, w, h);
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float tickDelta) {
			context.drawBorder(getX()+paddingX/2, getY()+paddingY/2, width-paddingX, height-paddingY, 0xFFFFFFFF);
			
			Text text = getMessage();
			if(text != null) {
				Rect2i titleRect = getTitleRect();
				context.fill(titleRect.getX(), titleRect.getY(), titleRect.getX()+titleRect.getWidth(), titleRect.getY()+titleRect.getHeight(), 0xFF000000);
				context.drawBorder(titleRect.getX(), titleRect.getY(), titleRect.getWidth()+1, titleRect.getHeight(), 0xFFFFFFFF);
				context.drawText(MinecraftClient.getInstance().textRenderer, text, titleRect.getX()+3, titleRect.getY()+2, 0xFFFFFFFF, true);
			}

			super.renderWidget(context, mouseX, mouseY, tickDelta);
		}
	}
	
	public static class TSSize extends SettingWidget<Size, Setting.TSSize> {
		private CheckboxWidget enabledWidget;
		private CheckboxWidget splitWidget;
		private IntSliderWidget combinedSlider;
		private IntSliderWidget xSlider;
		private IntSliderWidget ySlider;
		private IntSliderWidget zSlider;
		
		public TSSize(LayoutWidget parent, int x, int y, int width, int height, Setting.TSSize setting, Supplier<Size> valueGetter, Consumer<Size> valueSetter, Consumer<Setting<?>> notifier) {
			super(parent, x, y, width, height, setting, valueGetter, valueSetter, notifier);
		}

		@Override
		protected List<Element> create() {
			Size initialValue = read();
			List<Element> list = new ArrayList<>();
			
			if(setting.useEnable()) {
				enabledWidget = CheckboxWidget.builder(Text.translatable("voxedit.shape.settings.offset"), MinecraftClient.getInstance().textRenderer).checked(initialValue.enabled()).callback((cb, value) -> {
					if(reloading) return;
					write(read().enabled(value));
				}).build();
				enabledWidget.setWidth(width);
				list.add(enabledWidget);
			}
			if(setting.useSplit()) {
				splitWidget = CheckboxWidget.builder(Text.translatable("voxedit.shape.settings.splitSize"), MinecraftClient.getInstance().textRenderer).checked(initialValue.split()).callback((cb, value) -> {
					if(reloading) return;
					write(read().split(value));
				}).build();
				splitWidget.setWidth(width);
				list.add(splitWidget);
			}

			combinedSlider = new IntSliderWidget(0, 0, width, BASE_HEIGTH, Text.translatable("voxedit.shape.settings.size"), setting.minValue(), setting.maxValue(), initialValue.x(), (value) -> {
				if(reloading) return;
				Size currentValue = read();
				if(!currentValue.split()) write(currentValue.size(value));
			});
			list.add(combinedSlider);
			xSlider = new IntSliderWidget(0, 0, (width-2*2)/3, BASE_HEIGTH, Text.translatable("voxedit.shape.settings.width"), setting.minValue(), setting.maxValue(), initialValue.x(), (value) -> {
				if(reloading) return;
				write(read().x(value));
			});
			list.add(xSlider);
			ySlider = new IntSliderWidget(0, 0, (width-2*2)/3, BASE_HEIGTH, Text.translatable("voxedit.shape.settings.height"), setting.minValue(), setting.maxValue(), initialValue.y(), (value) -> {
				if(reloading) return;
				write(read().y(value));
			});
			list.add(ySlider);
			zSlider = new IntSliderWidget(0, 0, (width-2*2)/3, BASE_HEIGTH, Text.translatable("voxedit.shape.settings.length"), setting.minValue(), setting.maxValue(), initialValue.z(), (value) -> {
				if(reloading) return;
				write(read().z(value));
			});
			list.add(zSlider);

			//xSlider.active = xSlider.visible = !setting.useEnable() || initialValue.enabled();
			//ySlider.active = ySlider.visible = !setting.useEnable() || initialValue.enabled();
			//zSlider.active = zSlider.visible = !setting.useEnable() || initialValue.enabled();
			
			return list;
		}

		@Override
		protected void reloadValue() {
			Size value = read();
			
			if(enabledWidget != null && enabledWidget.isChecked() != value.enabled()) enabledWidget.onPress();
			if(splitWidget != null && splitWidget.isChecked() != value.split()) splitWidget.onPress();
			
			combinedSlider.setIntValue(value.x());
			xSlider.setIntValue(value.x());
			ySlider.setIntValue(value.y());
			zSlider.setIntValue(value.z());
			
			boolean enabled = !setting.useEnable() || value.enabled();
			boolean split = !setting.useSplit() || value.split();
			
			combinedSlider.active = combinedSlider.visible = enabled && !split;
			xSlider.active = xSlider.visible = enabled && split;
			ySlider.active = ySlider.visible = enabled && split;
			zSlider.active = zSlider.visible = enabled && split;
		}
	}

	public static class TSList<T> extends SettingWidget<List<T>, Setting.TSList<T, Setting<T>>> {
		private SettingListWidget listWidget;

		public TSList(LayoutWidget parent, int x, int y, int width, int height, Setting.TSList<T, Setting<T>> setting, Supplier<List<T>> configGetter, Consumer<List<T>> configSetter, Consumer<Setting<?>> notifier) {
			super(parent, x, y, width, height, setting, configGetter, configSetter, notifier);
		}

		@Override
		protected List<Element> create() {
			List<Element> list = new ArrayList<>();
			list.add(new LineHorizontal(width, setting.title()));
			list.add(listWidget = new SettingListWidget(MinecraftClient.getInstance(), width, 150, 0, 2));
			list.add(ButtonWidget.builder(Text.of("+"), (button) -> {
						List<T> newList = new ArrayList<>(read());
						newList.add(setting.setting().getDefaultValue());
						write(newList);
						reload();
					}).size(width/2-gap/2, SettingWidget.BASE_HEIGTH).build());
			list.add(ButtonWidget.builder(Text.of("-"), (button) -> {
						int selected = listWidget.selectedIndex();
						if(selected != -1) {
							List<T> newList = new ArrayList<>(read());
							newList.remove(selected);
							write(newList);
							reload();
						}
					}).size(width/2-gap/2, SettingWidget.BASE_HEIGTH).build());
			return list;
		}

		@Override
		protected void reloadValue() {
			listWidget.clearEntries();
			List<T> list = read();
			for(int i=0; i<list.size(); i++) {
				final int index = i;

				SettingWidget<?, ?> settingWidget = of(listWidget, getX(), getY(), listWidget.getRowWidth(), BASE_HEIGTH, setting.setting(), () -> {
					return read().get(index);
				}, (value) -> {
					List<T> newList = new ArrayList<>(read());
					newList.set(index, value);
					write(newList);
				}, notifier);

				settingWidget.reload();
				listWidget.addEntry(settingWidget);
			}
			listWidget.refreshPositions();
		}
	}

	private static class SettingListWidget extends ModListWidget<SettingListWidget.SettingListEntry> {
		public SettingListWidget(MinecraftClient client, int width, int height, int y, int padding) {
			super(client, width, height, y, padding);
		}

		public void addEntry(ClickableWidget widget) {
			addEntry(new SettingListEntry(widget));
		}

		@Environment(value=EnvType.CLIENT)
		class SettingListEntry extends ModListWidget.Entry<SettingListEntry> {
			private final ClickableWidget widget;
			private final List<ClickableWidget> children;

			private SettingListEntry(ClickableWidget widget) {
				this.widget = widget;
				this.children = List.of(widget);
				this.height = widget.getHeight();
			}

			@Override
			public int getHeight() {
				return widget.getHeight();
			}

			@Override
			public void positionChildren() {
				widget.setPosition(getX(), getY());
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
