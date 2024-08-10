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
