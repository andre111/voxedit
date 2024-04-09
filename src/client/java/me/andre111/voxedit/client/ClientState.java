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
package me.andre111.voxedit.client;

import java.util.Set;

import me.andre111.voxedit.state.ServerState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class ClientState extends ServerState {
	public ClientState(WrapperLookup registryLookup) {
		super(registryLookup, buf -> {} /*ClientPlayNetworking.send(VoxEdit.id("state"), buf)*/);
	}
	
	// actual state (in addition to state shared with server)
	private Set<BlockPos> positions;
	
	private float cameraSpeed = 2f;

	public Set<BlockPos> getPositions() {
		return positions;
	}

	public void setPositions(Set<BlockPos> positions) {
		this.positions = positions;
	}

	public float getCameraSpeed() {
		return cameraSpeed;
	}

	public void setCameraSpeed(float cameraSpeed) {
		this.cameraSpeed = cameraSpeed;
	}
}
