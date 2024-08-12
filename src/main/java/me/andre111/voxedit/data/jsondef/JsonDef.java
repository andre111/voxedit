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
package me.andre111.voxedit.data.jsondef;

import java.util.Map;

public sealed interface JsonDef {
	public boolean optional();
	
	public record Boolean(boolean defaultValue, boolean optional) implements JsonDef {
	}
	public record Integer(int defaultValue, int minValue, int maxValue, boolean optional) implements JsonDef {
	}
	public record Defined(String kind, boolean optional) implements JsonDef {
	}
	public record Complex(Map<String, JsonDef> properties, boolean optional) implements JsonDef {
	}
	public record BlockState() implements JsonDef {
		@Override
		public boolean optional() {
			return false;
		}
	}
	public record List(JsonDef listType, boolean optional) implements JsonDef {
	}
}
