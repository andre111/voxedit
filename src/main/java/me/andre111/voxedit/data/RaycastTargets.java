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
package me.andre111.voxedit.data;

public record RaycastTargets(boolean targetBlocks, boolean targetFluids, boolean targetEntities, boolean targetOther) {
	public static final RaycastTargets BLOCKS_ONLY = new RaycastTargets(true, false, false, false);
	public static final RaycastTargets BLOCKS_AND_FLUIDS = new RaycastTargets(true, true, false, false);
	public static final RaycastTargets ENTITIES_ONLY = new RaycastTargets(false, false, true, false);
	public static final RaycastTargets BLOCKS_AND_ENTITIES = new RaycastTargets(true, false, true, false);
	public static final RaycastTargets BLOCKS_AND_OTHER = new RaycastTargets(true, true, false, true);
	public static final RaycastTargets ALL = new RaycastTargets(true, true, true, true);
}
