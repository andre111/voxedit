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

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import me.andre111.voxedit.tool.shape.ConfiguredShape;
import me.andre111.voxedit.tool.shape.Shape;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class ToolSettingWidget<V, TS extends ToolSetting<V>> {
	protected final TS setting;
	protected final Supplier<ToolConfig> configGetter;
	protected final Consumer<ToolConfig> configSetter;
	protected boolean reloading = false;
	
	public ToolSettingWidget(TS setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
		this.setting = setting;
		this.configGetter = configGetter;
		this.configSetter = configSetter;
	}
	
	protected V read() {
		return setting.get(configGetter.get());
	}
	
	protected void write(V value) {
		if(reloading) return;
		configSetter.accept(setting.with(configGetter.get(), value));
	}
	
	public void reload() {
		reloading = true;
		reloadValue();
		reloading = false;
	}
	
	protected abstract List<ClickableWidget> create(Screen screen, int x, int y, int width, int height);
	protected abstract void reloadValue();
	
	@SuppressWarnings("unchecked")
	public static ToolSettingWidget<?, ?> of(ToolSetting<?> setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
		if(setting instanceof ToolSetting.Bool boolSetting) return new Bool(boolSetting, configGetter, configSetter);
		if(setting instanceof ToolSetting.FixedValues enumSetting) return new FixedValues<>(enumSetting, configGetter, configSetter);
		if(setting instanceof ToolSetting.Int intSetting) return new Int(intSetting, configGetter, configSetter);
		if(setting instanceof ToolSetting.TSIdentifier identifierSetting) return new TSIdentifier<>(identifierSetting, configGetter, configSetter);
		if(setting instanceof ToolSetting.TSRegistry registrySetting) return new TSRegistry<>(registrySetting, configGetter, configSetter);
		if(setting instanceof ToolSetting.TSShape shapeSetting) return new TSShape(shapeSetting, configGetter, configSetter);
		return null;
	}
	
	public static class Bool extends ToolSettingWidget<Boolean, ToolSetting.Bool> {
		private CyclingButtonWidget<Boolean> button;
		
		public Bool(ToolSetting.Bool setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
			super(setting, configGetter, configSetter);
		}
		
		@Override
		protected List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(button = CyclingButtonWidget.<Boolean>builder(value -> value ? ScreenTexts.ON : ScreenTexts.OFF).values(new Boolean[] { true, false }).initially(read()).build(x, y, width, height, setting.label(), (b, value) -> {
	            write(value);
	        }));
		}
		
		@Override
		protected void reloadValue() {
			button.setValue(read());
		}
	}
	
	public static class FixedValues<E> extends ToolSettingWidget<E, ToolSetting.FixedValues<E>> {
		private Consumer<E> input;
		
		public FixedValues(ToolSetting.FixedValues<E> setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
			super(setting, configGetter, configSetter);
		}
		
		@Override
		protected List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			if(setting.showFixedSelection()) {
				var widget = new SelectionWidget<E>(width, width/3, height, read(), (value) -> {
					write(value);
				});
				for(var entry : setting.values()) widget.addOption(entry, setting.toText().apply(entry));
				input = (v) -> widget.setValue(v);
				return List.of(widget);
			} else {
				var button = CyclingButtonWidget.builder(setting.toText()).values(setting.values()).initially(read()).build(x, y, width, height, setting.label(), (b, value) -> {
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
	
	public static class Int extends ToolSettingWidget<Integer, ToolSetting.Int> {
		private IntSliderWidget slider;
		
		public Int(ToolSetting.Int setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
			super(setting, configGetter, configSetter);
		}
		
		@Override
		protected List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(slider = new IntSliderWidget(x, y, width, height, setting.label(), setting.min(), setting.max(), read(), (value) -> {
				write(value);
			}));
		}
		
		@Override
		protected void reloadValue() {
			slider.setIntValue(read());
		}
	}
	
	public static class TSIdentifier<T> extends ToolSettingWidget<Identifier, ToolSetting.TSIdentifier<T>> {
		private RegistryEntryWidget<T> input;
		
		public TSIdentifier(ToolSetting.TSIdentifier<T> setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
			super(setting, configGetter, configSetter);
		}
		
		@Override
		protected List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(input = RegistryEntryWidget.serverRetrieved(MinecraftClient.getInstance().textRenderer, x, y, width, height, setting.registryKey(), read(), (value) -> {
				write(value);
			}));
		}
		
		@Override
		protected void reloadValue() {
			input.setValue(read());
		}
	}
	
	public static class TSRegistry<T> extends ToolSettingWidget<T, ToolSetting.TSRegistry<T>> {
		private Consumer<Identifier> input;
		
		public TSRegistry(ToolSetting.TSRegistry<T> setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
			super(setting, configGetter, configSetter);
		}
		
		@Override
		protected List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			if(setting.showFixedSelection()) {
				var widget = new SelectionWidget<T>(width, width/3, height, read(), (value) -> {
					write(value);
				});
				for(var entry : setting.registry()) widget.addOption(entry, setting.toText().apply(entry));
				input = (id) -> widget.setValue(setting.registry().get(id));
				return List.of(widget);
			} else {
				var widget = RegistryEntryWidget.direct(MinecraftClient.getInstance().textRenderer, x, y, width, height, setting.registry(), read(), (value) -> {
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
	
	public static class TSShape extends ToolSettingWidget<ConfiguredShape, ToolSetting.TSShape> {
		private SelectionWidget<Shape> shapeWidget;
		private CheckboxWidget splitSizeWidget;
		private IntSliderWidget sizeSlider;
		private IntSliderWidget widthSlider;
		private IntSliderWidget heightSlider;
		private IntSliderWidget lengthSlider;
		private CheckboxWidget offsetWidget;
		private IntSliderWidget offsetWSlider;
		private IntSliderWidget offsetHSlider;
		private IntSliderWidget offsetLSlider;
		
		private boolean reloading = false;

		public TSShape(ToolSetting.TSShape setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
			super(setting, configGetter, configSetter);
		}

		@Override
		protected List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			ConfiguredShape initialValue = read();
			
			List<ClickableWidget> list = new ArrayList<>();
			list.add(new LineHorizontal(width, Text.translatable("voxedit.shape")));
			
			// base shape
			shapeWidget = new SelectionWidget<>(width, width/3, 20, initialValue.shape(), (value) -> {
				if(reloading) return;
				write(read().shape(value));
			});
			for(var entry : VoxEdit.SHAPE_REGISTRY) shapeWidget.addOption(entry, entry.asText());
			list.add(shapeWidget);
			
			// size
			if(setting.showConfig()) {
				splitSizeWidget = CheckboxWidget.builder(Text.translatable("voxedit.shape.settings.splitSize"), MinecraftClient.getInstance().textRenderer).checked(initialValue.splitSize()).callback((cb, value) -> {
					if(reloading) return;
					write(read().splitSize(value));
				}).build();
				splitSizeWidget.setWidth(width);
				list.add(splitSizeWidget);
				
				sizeSlider = new IntSliderWidget(0, 0, width, 20, Text.translatable("voxedit.shape.settings.size"), 1, 16, initialValue.width(), (value) -> {
					if(reloading) return;
					ConfiguredShape currentValue = read();
					if(!currentValue.splitSize()) write(currentValue.size(value));
				});
				list.add(sizeSlider);
				widthSlider = new IntSliderWidget(0, 0, (width-2*2)/3, 20, Text.translatable("voxedit.shape.settings.width"), 1, 16, initialValue.width(), (value) -> {
					if(reloading) return;
					ConfiguredShape currentValue = read();
					if(currentValue.splitSize()) write(currentValue.width(value));
					else write(currentValue.size(value));
				});
				list.add(widthSlider);
				heightSlider = new IntSliderWidget(0, 0, (width-2*2)/3, 20, Text.translatable("voxedit.shape.settings.height"), 1, 16, initialValue.height(), (value) -> {
					if(reloading) return;
					ConfiguredShape currentValue = read();
					if(currentValue.splitSize()) write(currentValue.height(value));
					else write(currentValue.size(value));
				});
				list.add(heightSlider);
				lengthSlider = new IntSliderWidget(0, 0, (width-2*2)/3, 20, Text.translatable("voxedit.shape.settings.length"), 1, 16, initialValue.length(), (value) -> {
					if(reloading) return;
					ConfiguredShape currentValue = read();
					if(currentValue.splitSize()) write(currentValue.length(value));
					else write(currentValue.size(value));
				});
				list.add(lengthSlider);
				
				sizeSlider.active = sizeSlider.visible = !initialValue.splitSize();
				widthSlider.active = widthSlider.visible = initialValue.splitSize();
				heightSlider.active = heightSlider.visible = initialValue.splitSize();
				lengthSlider.active = lengthSlider.visible = initialValue.splitSize();
				
				// offset
				offsetWidget = CheckboxWidget.builder(Text.translatable("voxedit.shape.settings.offset"), MinecraftClient.getInstance().textRenderer).checked(initialValue.offset()).callback((cb, value) -> {
					if(reloading) return;
					write(read().offset(value));
				}).build();
				offsetWidget.setWidth(width);
				list.add(offsetWidget);
				offsetWSlider = new IntSliderWidget(0, 0, (width-2*2)/3, 20, Text.translatable("voxedit.shape.settings.width"), -16, 16, initialValue.offsetW(), (value) -> {
					if(reloading) return;
					write(read().offsetW(value));
				});
				list.add(offsetWSlider);
				offsetHSlider = new IntSliderWidget(0, 0, (width-2*2)/3, 20, Text.translatable("voxedit.shape.settings.height"), -16, 16, initialValue.offsetH(), (value) -> {
					if(reloading) return;
					write(read().offsetH(value));
				});
				list.add(offsetHSlider);
				offsetLSlider = new IntSliderWidget(0, 0, (width-2*2)/3, 20, Text.translatable("voxedit.shape.settings.length"), -16, 16, initialValue.offsetL(), (value) -> {
					if(reloading) return;
					write(read().offsetL(value));
				});
				list.add(offsetLSlider);
				
				offsetWSlider.active = offsetWSlider.visible = initialValue.offset();
				offsetHSlider.active = offsetHSlider.visible = initialValue.offset();
				offsetLSlider.active = offsetLSlider.visible = initialValue.offset();
			}
			
			list.add(new LineHorizontal(width, null));
			return list;
		}

		@Override
		protected void reloadValue() {
			reloading = true;
			
			ConfiguredShape value = read();
			shapeWidget.setValue(value.shape());
			
			if(setting.showConfig()) {
				if(splitSizeWidget.isChecked() != value.splitSize()) splitSizeWidget.onPress();
				sizeSlider.setIntValue(value.width());
				widthSlider.setIntValue(value.width());
				heightSlider.setIntValue(value.height());
				lengthSlider.setIntValue(value.length());
				sizeSlider.active = sizeSlider.visible = !value.splitSize();
				widthSlider.active = widthSlider.visible = value.splitSize();
				heightSlider.active = heightSlider.visible = value.splitSize();
				lengthSlider.active = lengthSlider.visible = value.splitSize();
	
				if(offsetWidget.isChecked() != value.offset()) offsetWidget.onPress();
				offsetWSlider.setIntValue(value.offsetW());
				offsetHSlider.setIntValue(value.offsetH());
				offsetLSlider.setIntValue(value.offsetL());
				offsetWSlider.active = offsetWSlider.visible = value.offset();
				offsetHSlider.active = offsetHSlider.visible = value.offset();
				offsetLSlider.active = offsetLSlider.visible = value.offset();
			}
			
			reloading = false;
		}
	}
}
