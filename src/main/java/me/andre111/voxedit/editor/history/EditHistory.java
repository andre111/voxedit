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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.network.CPHistoryInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.StructureWorldAccess;

public class EditHistory {
	private final Path path;
	private List<EditHistoryState> states = new ArrayList<>();
	private int index = -1;
	private long cachedSize = -1;
	
	private EditHistory(Path path) {
		this.path = path;

		try {
			if(!Files.exists(path)) {
				Files.createDirectories(path);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// read index
		Path indexPath = path.resolve("index.nbt");
		if(Files.exists(indexPath)) {
			try {
				NbtCompound nbt = NbtIo.read(indexPath);
				if(nbt.contains("index", NbtElement.INT_TYPE)) index = nbt.getInt("index");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// read states
		int loadIndex = 0;
		Path statePath;
		while(Files.exists(statePath = getPath(loadIndex))) {
			try {
				NbtCompound nbt = NbtIo.readCompressed(statePath, NbtSizeTracker.ofUnlimitedBytes());
				states.add(EditHistoryState.read(new EditHistoryReader(nbt)));
				loadIndex++;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
		
		if(index > states.size()-1) {
			System.err.println("Invalid undo state? Index exceeds found history size.");
			index = states.size()-1;
		}
	}
	
	public CPHistoryInfo push(StructureWorldAccess world, EditHistoryState state) {
		// replace/remove undone states
		boolean append = true;
		if(index < states.size()-1) {
			for(int i=states.size()-1; i>index; i--) {
				states.remove(i);
				try {
					Files.deleteIfExists(getPath(i));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			append = false;
		}
		// add new state
		states.add(state);
		setIndex(index+1);
		
		// write to persistant storage
		EditHistoryWriter writer = new EditHistoryWriter();
		state.write(writer);
		Path statePath = path.resolve(String.format("%05d.nbt", index));
		try {
			NbtIo.writeCompressed(writer.toNbt(), statePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cachedSize = -1;
		
		// construct update packet
		if(append) return new CPHistoryInfo(List.of(state.getStats()), index, true, getSize());
		else return new CPHistoryInfo(states.stream().map(EditHistoryState::getStats).toList(), index, false, getSize());
	}
	
	public EditStats undo(StructureWorldAccess world) {
		if(index < 0) return EditStats.EMPTY;
		setIndex(index-1);
		return states.get(index+1).undo(world);
	}
	
	public EditStats redo(StructureWorldAccess world) {
		if(index >= states.size()-1) return EditStats.EMPTY;
		setIndex(index+1);
		return states.get(index).redo(world);
	}
	
	public void clear() {
		int itIndex = 0;
		Path statePath;
		while(Files.exists(statePath = getPath(itIndex))) {
			try {
				Files.delete(statePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			itIndex++;
		}
		try {
			Files.deleteIfExists(path.resolve("index.nbt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		index = -1;
		cachedSize = 0;
	}
	
	public List<EditHistoryState> getStates() {
		return Collections.unmodifiableList(states);
	}
	
	public int getIndex() {
		return index;
	}
	
	public long getSize() {
		if(cachedSize < 0) {
			cachedSize = 0;
			
			int itIndex = 0;
			Path statePath;
			while(Files.exists(statePath = getPath(itIndex))) {
				try {
					cachedSize += Files.size(statePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				itIndex++;
			}
		}
		return cachedSize;
	}
	
	private void setIndex(int newIndex) {
		index = newIndex;
		
		NbtCompound nbt = new NbtCompound();
		nbt.putInt("index", index);
		try {
			NbtIo.write(nbt, path.resolve("index.nbt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Path getPath(int index) {
		return path.resolve(String.format("%05d.nbt", index));
	}
	
	// Access
	private static Map<Key, EditHistory> undos = new HashMap<>();
	public static EditHistory of(PlayerEntity player, ServerWorld world) {
		UUID playerID = player.getUuid();
		Identifier levelLoc = world.getDimension().effects(); //TODO: wrong! get actual id
		Key key = new Key(playerID, levelLoc);
		
		if(!undos.containsKey(key)) {
			undos.put(key, new EditHistory(VoxEdit.dataPath(world.getServer()).resolve("history/"+playerID.toString()+"/"+levelLoc.toUnderscoreSeparatedString()+"/")));
		}
		return undos.get(key);
	}
	private static record Key(UUID player, Identifier world) {
	}
}
