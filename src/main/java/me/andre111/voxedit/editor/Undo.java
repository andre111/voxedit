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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Undo {
	private List<UndoState> states = new ArrayList<>();
	private int index = -1;
	
	public void push(UndoState state) {
		// replace/remove undone states
		if(index < states.size()-1) {
			for(int i=states.size()-1; i>index; i--) {
				states.remove(i);
			}
		}
		// add new state
		states.add(state);
		index++;
	}
	
	public EditStats undo(World world) {
		if(index < 0) return EditStats.EMPTY;
		return states.get(index--).undo(world);
	}
	
	public EditStats redo(World world) {
		if(index >= states.size()-1) return EditStats.EMPTY;
		return states.get(++index).redo(world);
	}
	
	// Access
	private static Map<Key, Undo> undos = new HashMap<>();
	public static Undo of(PlayerEntity player, World world) {
		UUID playerID = player.getUuid();
		Identifier levelLoc = world.getDimension().effects(); //TODO: wrong! get actual id
		Key key = new Key(playerID, levelLoc);
		
		if(!undos.containsKey(key)) {
			undos.put(key, new Undo());
		}
		return undos.get(key);
	}
	private static record Key(UUID player, Identifier world) {
	}
}
