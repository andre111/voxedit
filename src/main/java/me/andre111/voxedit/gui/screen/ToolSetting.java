package me.andre111.voxedit.gui.screen;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import me.andre111.voxedit.BlockPalette;
import me.andre111.voxedit.ClientState;
import me.andre111.voxedit.gui.widget.BlockPaletteDisplayWidget;
import me.andre111.voxedit.gui.widget.IntSliderWidget;
import me.andre111.voxedit.gui.widget.RegistryEntryWidget;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget.DisplayAxis;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class ToolSetting<V, TC extends ToolConfig> {
	protected final Text title;
	protected final Function<TC, V> reader;
	protected final BiFunction<TC, V, TC> writer;
	
	public ToolSetting(Text title, Function<TC, V> reader, BiFunction<TC, V, TC> writer) {
		this.title = title;
		this.reader = reader;
		this.writer = writer;
	}
	
	@SuppressWarnings("unchecked")
	protected V read() {
		return reader.apply((TC) ClientState.active.selected().config());
	}
	
	@SuppressWarnings("unchecked")
	protected void write(V value) {
		ClientState.sendConfigChange(writer.apply((TC) ClientState.active.selected().config(), value));
	}
	
	public abstract List<ClickableWidget> create(Screen screen, int x, int y, int width, int height);
	public abstract void reload();
	
	public static <TC extends ToolConfig> TSBlockPalette<TC> blockPalette(Text title, boolean includeProperties, boolean showWeights, Function<TC, BlockPalette> reader, BiFunction<TC, BlockPalette, TC> writer) {
		return new TSBlockPalette<>(title, includeProperties, showWeights, reader, writer);
	}
	
	public static <TC extends ToolConfig> TSBoolean<TC> bool(Text title, Function<TC, Boolean> reader, BiFunction<TC, Boolean, TC> writer) {
		return new TSBoolean<>(title, reader, writer);
	}
	
	public static <TC extends ToolConfig, E extends Enum<E>> TSEnum<TC, E> ofEnum(Text title, Function<E, Text> toText, E[] values, Function<TC, E> reader, BiFunction<TC, E, TC> writer) {
		return new TSEnum<>(title, toText, values, reader, writer);
	}
	
	public static <TC extends ToolConfig> TSIntRange<TC> intRange(Text title, int min, int max, Function<TC, Integer> reader, BiFunction<TC, Integer, TC> writer) {
		return new TSIntRange<>(title, min, max, reader, writer);
	}
	
	public static <TC extends ToolConfig, T> TSIdentifier<TC, T> identifier(Text title, RegistryKey<? extends Registry<T>> registryKey, Function<TC, Identifier> reader, BiFunction<TC, Identifier, TC> writer) {
		return new TSIdentifier<>(title, registryKey, reader, writer);
	}

	public static class TSBlockPalette<TC extends ToolConfig> extends ToolSetting<BlockPalette, TC> {
		private final boolean includeProperties;
		private final boolean showWeights;
		private BlockPaletteDisplayWidget display;
		
		public TSBlockPalette(Text title, boolean includeProperties, boolean showWeights, Function<TC, BlockPalette> reader, BiFunction<TC, BlockPalette, TC> writer) {
			super(title, reader, writer);
			this.includeProperties = includeProperties;
			this.showWeights = showWeights;
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			DirectionalLayoutWidget container = new DirectionalLayoutWidget(x, y, DisplayAxis.HORIZONTAL);
			container.spacing(2);

			List<ClickableWidget> elements = List.of(
					container.add(ButtonWidget.builder(title, (button) -> {
						MinecraftClient.getInstance().setScreen(new EditBlockPaletteScreen(screen, title, 0, includeProperties, showWeights, read(), palette -> {
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
	public static class TSBoolean<TC extends ToolConfig> extends ToolSetting<Boolean, TC> {
		private CyclingButtonWidget<Boolean> button;
		
		public TSBoolean(Text title, Function<TC, Boolean> reader, BiFunction<TC, Boolean, TC> writer) {
			super(title, reader, writer);
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(button = CyclingButtonWidget.<Boolean>builder(value -> value ? ScreenTexts.ON : ScreenTexts.OFF).values(new Boolean[] { true, false }).initially(read()).build(x, y, width, height, title, (b, value) -> {
	            write(value);
	        }));
		}
		
		@Override
		public void reload() {
			button.setValue(read());
		}
	}
	public static class TSEnum<TC extends ToolConfig, E extends Enum<E>> extends ToolSetting<E, TC> {
		private final Function<E, Text> toText;
		private final E[] values;
		private CyclingButtonWidget<E> button;
		
		public TSEnum(Text title, Function<E, Text> toText, E[] values, Function<TC, E> reader, BiFunction<TC, E, TC> writer) {
			super(title, reader, writer);
			this.toText = toText;
			this.values = values;
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(button = CyclingButtonWidget.builder(toText).values(values).initially(read()).build(x, y, width, height, title, (b, value) -> {
	            write(value);
	        }));
		}
		
		@Override
		public void reload() {
			button.setValue(read());
		}
	}
	public static class TSIntRange<TC extends ToolConfig> extends ToolSetting<Integer, TC> {
		private final int min;
		private final int max;
		private IntSliderWidget slider;
		
		public TSIntRange(Text title, int min, int max, Function<TC, Integer> reader, BiFunction<TC, Integer, TC> writer) {
			super(title, reader, writer);
			this.min = min;
			this.max = max;
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(slider = new IntSliderWidget(x, y, width, height, title, min, max, read(), (value) -> {
				write(value);
			}));
		}
		
		@Override
		public void reload() {
			slider.setIntValue(read());
		}
	}
	public static class TSIdentifier<TC extends ToolConfig, T> extends ToolSetting<Identifier, TC> {
		private final RegistryKey<? extends Registry<T>> registryKey;
		private RegistryEntryWidget<T> input;
		
		public TSIdentifier(Text title, RegistryKey<? extends Registry<T>> registryKey, Function<TC, Identifier> reader, BiFunction<TC, Identifier, TC> writer) {
			super(title, reader, writer);
			this.registryKey = registryKey;
		}
		
		@Override
		public List<ClickableWidget> create(Screen screen, int x, int y, int width, int height) {
			return List.of(input = new RegistryEntryWidget<T>(MinecraftClient.getInstance().textRenderer, x, y, width, height, registryKey, read(), (value) -> {
				write(value);
			}));
		}
		
		@Override
		public void reload() {
			input.setValue(read());
		}
	}
}
