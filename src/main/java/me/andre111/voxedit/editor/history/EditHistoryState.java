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
package me.andre111.voxedit.editor.history;

import java.util.List;

import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.action.EditAction;
import net.minecraft.server.world.ServerWorld;

public class EditHistoryState {
	private final EditStats stats;
	private final List<EditAction<?>> actions;
	
	public EditHistoryState(EditStats stats, List<EditAction<?>> actions) {
		this.stats = stats;
		this.actions = List.copyOf(actions);
	}
	
	public EditStats getStats() {
		return stats;
	}
	
	public EditStats undo(ServerWorld world) {
		EditStats stats = new EditStats(this.stats.text());
		for(int i=actions.size()-1; i>=0; i--) {
			actions.get(i).undo(world, stats);
		}
		return stats;
	}
	public EditStats redo(ServerWorld world) {
		EditStats stats = new EditStats(this.stats.text());
		for(int i=0; i<actions.size(); i++) {
			actions.get(i).redo(world, stats);
		}
		return stats;
	}
	
	public void write(EditHistoryWriter writer) {
		writer.writeStats(stats);
		writer.writeActions(actions);
	}
	
	public static EditHistoryState read(EditHistoryReader reader) {
		return new EditHistoryState(reader.readStats(), reader.readActions());
	}
}
