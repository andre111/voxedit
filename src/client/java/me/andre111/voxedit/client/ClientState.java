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

import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.state.ServerState;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class ClientState extends ServerState {
	public static final ClientState INSTANCE = new ClientState();
	private ClientState() {
		super(buf -> {} /*ClientPlayNetworking.send(VoxEdit.id("state"), buf)*/);
	}
	
	// helpers
	public static Tool<?, ?> tool() {
		if(INSTANCE.active == null) return null;
		return INSTANCE.active.selected().tool();
	}
	public static ToolConfig<?> config() {
		if(INSTANCE.active == null) return null;
		return INSTANCE.active.selected().config();
	}
	
	// actual state (in addition to state shared with server)
	private ToolItem.Data active;
	private BlockHitResult target;
	
	private Set<BlockPos> positions;
	
	private float cameraSpeed = 2f;

	public ToolItem.Data getActive() {
		return active;
	}

	public void setActive(ToolItem.Data active) {
		this.active = active;
	}

	public BlockHitResult getTarget() {
		return target;
	}

	public void setTarget(BlockHitResult target) {
		this.target = target;
	}

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
