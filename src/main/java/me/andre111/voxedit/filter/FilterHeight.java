package me.andre111.voxedit.filter;

import java.util.List;

import me.andre111.voxedit.data.Config;
import me.andre111.voxedit.data.Setting;
import net.minecraft.text.Text;

public class FilterHeight extends Filter {
	private static final Setting<Op> OPERATION = Setting.ofEnum("operation", Op.class, Op::asText, true);
	private static final Setting<Integer> HEIGHT = Setting.ofInt("height", 64, -64, 256+64);
	
	public FilterHeight() {
		super(List.of(OPERATION, HEIGHT));
	}

	@Override
	public boolean check(FilterContext context, Config config) {
		return switch(OPERATION.get(config)) {
		case EQUAL -> context.pos().getY() == HEIGHT.get(config);
		case GREATER -> context.pos().getY() > HEIGHT.get(config);
		case LESS -> context.pos().getY() < HEIGHT.get(config);
		default -> false;
		};
	}

	public static enum Op {
		EQUAL,
		GREATER,
		LESS;
		
		public Text asText() {
			return Text.translatable("voxedit.filter.height.operation."+name().toLowerCase());
		}
	}
}
