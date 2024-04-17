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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import me.andre111.voxedit.network.CPSchematic;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;

public class ServerState {
	private final WrapperLookup registryLookup;
	private final Consumer<CustomPayload> updateConsumer;
	
	private Map<Identifier, Schematic> schematics = new HashMap<>();
	
	public ServerState(WrapperLookup registryLookup, Consumer<CustomPayload> updateConsumer) {
		this.registryLookup = registryLookup;
		this.updateConsumer = updateConsumer;
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
