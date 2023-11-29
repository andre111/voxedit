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
package me.andre111.voxedit.editor;

import java.util.List;

import me.andre111.voxedit.editor.action.EditAction;
import net.minecraft.world.World;

public class UndoState {
	private final List<EditAction> actions;
	
	public UndoState(List<EditAction> actions) {
		this.actions = List.copyOf(actions);
	}
	
	public int undo(World world) {
		int count = 0;
		for(int i=actions.size()-1; i>=0; i--) {
			count += actions.get(i).undo(world);
		}
		return count;
	}
	public int redo(World world) {
		int count = 0;
		for(int i=0; i<actions.size(); i++) {
			count += actions.get(i).redo(world);
		}
		return count;
	}
}
