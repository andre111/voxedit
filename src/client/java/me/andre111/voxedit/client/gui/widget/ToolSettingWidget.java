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
package me.andre111.voxedit.client.gui.widget;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import me.andre111.voxedit.tool.data.ToolConfig;
import me.andre111.voxedit.tool.data.ToolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.screen.ScreenTexts;
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
		private CyclingButtonWidget<E> button;
		
		public FixedValues(ToolSetting.FixedValues<E> setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
			super(setting, configGetter, configSetter);
		}
		
		@Override
		protected List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(button = CyclingButtonWidget.builder(setting.toText()).values(setting.values()).initially(read()).build(x, y, width, height, setting.label(), (b, value) -> {
	            write(value);
	        }));
		}
		
		@Override
		protected void reloadValue() {
			button.setValue(read());
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
		private boolean reloading = false;
		
		public TSRegistry(ToolSetting.TSRegistry<T> setting, Supplier<ToolConfig> configGetter, Consumer<ToolConfig> configSetter) {
			super(setting, configGetter, configSetter);
		}
		
		@Override
		protected List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			if(setting.showFixedSelection()) {
				var widget = new SelectionWidget<T>(width, width/3, height, read(), (value) -> {
					if(reloading) return;
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
}
