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

import me.andre111.voxedit.client.ClientState;
import me.andre111.voxedit.client.gui.screen.EditBlockPaletteScreen;
import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.tool.config.ToolConfig;
import me.andre111.voxedit.tool.data.BlockPalette;
import me.andre111.voxedit.tool.data.ToolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget.DisplayAxis;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.util.Identifier;

public abstract class ToolSettingWidget<V, TC extends ToolConfig<TC>, TS extends ToolSetting<V, TC>> {
	protected final TS setting;
	
	public ToolSettingWidget(TS setting) {
		this.setting = setting;
	}
	
	@SuppressWarnings("unchecked")
	protected V read() {
		return setting.reader().apply((TC) ClientState.active.selected().config());
	}
	
	@SuppressWarnings("unchecked")
	protected void write(V value) {
		ClientNetworking.setSelectedConfig(setting.writer().apply((TC) ClientState.active.selected().config(), value));
	}
	
	public abstract List<ClickableWidget> create(Screen screen, int x, int y, int width, int height);
	public abstract void reload();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ToolSettingWidget of(ToolSetting<?, ?> setting) {
		if(setting instanceof ToolSetting.Bool boolSetting) return new Bool<>(boolSetting);
		if(setting instanceof ToolSetting.EnumValue enumSetting) return new EnumValue<>(enumSetting);
		if(setting instanceof ToolSetting.Int intSetting) return new Int<>(intSetting);
		if(setting instanceof ToolSetting.TSBlockPalette paletteSetting) return new TSBlockPalette<>(paletteSetting);
		if(setting instanceof ToolSetting.TSIdentifier identifierSetting) return new TSIdentifier<>(identifierSetting);
		return null;
	}
	
	public static class Bool<TC extends ToolConfig<TC>> extends ToolSettingWidget<Boolean, TC, ToolSetting.Bool<TC>> {
		private CyclingButtonWidget<Boolean> button;
		
		public Bool(ToolSetting.Bool<TC> setting) {
			super(setting);
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(button = CyclingButtonWidget.<Boolean>builder(value -> value ? ScreenTexts.ON : ScreenTexts.OFF).values(new Boolean[] { true, false }).initially(read()).build(x, y, width, height, setting.label(), (b, value) -> {
	            write(value);
	        }));
		}
		
		@Override
		public void reload() {
			button.setValue(read());
		}
	}
	
	public static class EnumValue<TC extends ToolConfig<TC>, E extends Enum<E>> extends ToolSettingWidget<E, TC, ToolSetting.EnumValue<E, TC>> {
		private CyclingButtonWidget<E> button;
		
		public EnumValue(ToolSetting.EnumValue<E, TC> setting) {
			super(setting);
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(button = CyclingButtonWidget.builder(setting.toText()).values(setting.values()).initially(read()).build(x, y, width, height, setting.label(), (b, value) -> {
	            write(value);
	        }));
		}
		
		@Override
		public void reload() {
			button.setValue(read());
		}
	}
	
	public static class Int<TC extends ToolConfig<TC>> extends ToolSettingWidget<Integer, TC, ToolSetting.Int<TC>> {
		private IntSliderWidget slider;
		
		public Int(ToolSetting.Int<TC> setting) {
			super(setting);
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(slider = new IntSliderWidget(x, y, width, height, setting.label(), setting.min(), setting.max(), read(), (value) -> {
				write(value);
			}));
		}
		
		@Override
		public void reload() {
			slider.setIntValue(read());
		}
	}
	

	public static class TSBlockPalette<TC extends ToolConfig<TC>> extends ToolSettingWidget<BlockPalette, TC, ToolSetting.TSBlockPalette<TC>> {
		private BlockPaletteDisplayWidget display;
		
		public TSBlockPalette(ToolSetting.TSBlockPalette<TC> setting) {
			super(setting);
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			DirectionalLayoutWidget container = new DirectionalLayoutWidget(x, y, DisplayAxis.HORIZONTAL);
			container.spacing(2);

			List<ClickableWidget> elements = List.of(
					container.add(ButtonWidget.builder(setting.label(), (button) -> {
						MinecraftClient.getInstance().setScreen(new EditBlockPaletteScreen(screen, setting.label(), 0, setting.includeProperties(), setting.showWeights(), read(), palette -> {
							write(palette);
						}));
					}).size(width-22, height).build()),
					container.add(display = new BlockPaletteDisplayWidget(0, 0, 20, height, read()))
					);
			container.refreshPositions();
			return elements;
		}
		
		@Override
		public void reload() {
			display.setValue(read());
		}
	}
	
	public static class TSIdentifier<TC extends ToolConfig<TC>, T> extends ToolSettingWidget<Identifier, TC, ToolSetting.TSIdentifier<TC, T>> {
		private RegistryEntryWidget<T> input;
		
		public TSIdentifier(ToolSetting.TSIdentifier<TC, T> setting) {
			super(setting);
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(input = new RegistryEntryWidget<T>(MinecraftClient.getInstance().textRenderer, x, y, width, height, setting.registryKey(), read(), (value) -> {
				write(value);
			}));
		}
		
		@Override
		public void reload() {
			input.setValue(read());
		}
	}
}
