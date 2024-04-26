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
package me.andre111.voxedit.state;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.VoxEditUtil;
import me.andre111.voxedit.network.CPSchematic;
import me.andre111.voxedit.schematic.Schematic;
import me.andre111.voxedit.selection.Selection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;

public class ServerState {
	private final WrapperLookup registryLookup;
	private final Consumer<CustomPayload> updateConsumer;
	private final Path persistancePath;
	
	private Map<String, Schematic> schematics = new HashMap<>();
	private Selection selection = null;
	private boolean editorActive = false;
	
	public ServerState(WrapperLookup registryLookup, Consumer<CustomPayload> updateConsumer, Path persistancePath) {
		this.registryLookup = registryLookup;
		this.updateConsumer = updateConsumer;
		this.persistancePath = persistancePath;
		
		// load existing schematics
		try {
			Path schematicsPath = persistancePath.resolve("schematics/");
			Files.createDirectories(schematicsPath);
			Files.list(schematicsPath).filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".cnbt")).forEachOrdered(p -> {
				String name = p.getFileName().toString().replace(".cnbt", "");
				
				NbtCompound nbt = VoxEditUtil.readNbt(p);
				if(nbt == null) {
					VoxEdit.LOGGER.warn("Schematic is not a valid nbt file: "+p.getFileName());
					return;
				}
				
				try {
					Schematic schematic = Schematic.readNbt(registryLookup, nbt);
					schematics.put(name, schematic);
				} catch(Exception e) {
					VoxEdit.LOGGER.warn("Not a valid schematic file: "+p.getFileName());
					VoxEdit.LOGGER.warn("Exception Message: "+e.getMessage());
				}
			});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Schematic schematic(String name) {
		name = VoxEditUtil.removeInvalidChars(name);
		return schematics.get(name);
	}

	public void schematic(String name, Schematic schematic, boolean transfer, boolean persist) {
		name = VoxEditUtil.removeInvalidChars(name);
		if(schematic == null) schematics.remove(name);
		else schematics.put(name, schematic);
		
		// send update
		NbtCompound nbt = new NbtCompound();
		if(transfer || persist) {
			if(schematic != null) schematic.writeNbt(registryLookup, nbt);
		}
		if(transfer) updateConsumer.accept(new CPSchematic(name, nbt));
		if(persist) VoxEditUtil.writeNbt(persistancePath.resolve("schematics/"+name+".cnbt"), nbt);
	}
	
	public void schematicDelete(String name) {
		try {
			Files.deleteIfExists(persistancePath.resolve("schematics/"+name+".cnbt"));
			updateConsumer.accept(new CPSchematic(name, new NbtCompound()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Map<String, Schematic> schematics() {
		return Collections.unmodifiableMap(schematics);
	}
	
	public boolean editorActive() {
		return editorActive;
	}
	
	public void editorActive(boolean editorActive) {
		this.editorActive = editorActive;
	}
	
	public Selection selection() {
		return selection;
	}
	
	public void selection(Selection selection) {
		this.selection = selection;
	}
}
