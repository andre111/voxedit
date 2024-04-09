package me.andre111.voxedit.state;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import me.andre111.voxedit.network.CPClearSelection;
import me.andre111.voxedit.network.CPSchematic;
import me.andre111.voxedit.network.CPSetSelection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;

public class ServerState {
	private final WrapperLookup registryLookup;
	private final Consumer<CustomPayload> updateConsumer;
	
	private Selection selection = null;
	private Map<Identifier, Schematic> schematics = new HashMap<>();
	
	public ServerState(WrapperLookup registryLookup, Consumer<CustomPayload> updateConsumer) {
		this.registryLookup = registryLookup;
		this.updateConsumer = updateConsumer;
	}
	
	public final Selection getSelection() {
		return selection;
	}
	public final void setSelection(Selection selection, boolean transfer) {
		this.selection = selection;
		
		// send update
		if(transfer) {
			if(selection == null) {
				updateConsumer.accept(new CPClearSelection());
			} else {
				updateConsumer.accept(new CPSetSelection(selection));
			}
		}
	}
	
	public final Schematic schematic(Identifier id) {
		return schematics.get(id);
	}

	public final void schematic(Identifier id, Schematic schematic, boolean transfer) {
		if(schematic == null) schematics.remove(id);
		else schematics.put(id, schematic);
		
		// send update
		if(transfer) {
			NbtCompound nbt = new NbtCompound();
			if(schematic != null) schematic.writeNbt(registryLookup, nbt);
			updateConsumer.accept(new CPSchematic(id, nbt));
		}
	}

	public static enum Command {
		SET_SELECTION,
		CLEAR_SELECTION,
		SET_COPY_BUFFER,
		CLEAR_COPY_BUFFER;
	}
}
