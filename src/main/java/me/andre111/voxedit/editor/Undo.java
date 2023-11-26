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
	
	public void undo(World world) {
		if(index < 0) return;
		states.get(index).undo(world);
		index--;
	}
	
	public void redo(World world) {
		if(index >= states.size()-1) return;
		index++;
		states.get(index).redo(world);
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
