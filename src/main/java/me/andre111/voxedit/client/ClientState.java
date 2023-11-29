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

import me.andre111.voxedit.client.network.ClientNetworking;
import me.andre111.voxedit.item.ToolItem;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

@Environment(value=EnvType.CLIENT)
public class ClientState {
	public static ClientPlayerEntity player;
	public static ToolItem.Data active;
	public static BlockHitResult target;
	
	public static Set<BlockPos> positions;
	
	public static float cameraSpeed = 2f;
	
	@SuppressWarnings("unchecked")
	public static <TC extends ToolConfig, T extends Tool<TC, T>> void sendConfigChange(TC newConfig) {
		if(active == null) return;
		if(!active.selected().config().getClass().isAssignableFrom(newConfig.getClass())) return;
		ClientNetworking.setTool(new ConfiguredTool<TC, T>((T) active.selected().tool(), newConfig));
	}
}
