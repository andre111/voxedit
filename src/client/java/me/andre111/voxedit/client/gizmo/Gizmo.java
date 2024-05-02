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
package me.andre111.voxedit.client.gizmo;

import me.andre111.voxedit.client.EditorState;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

public abstract class Gizmo implements Iterable<Box> {
	public abstract Text getName();
	public abstract void addActions(GizmoActions actions);
	
	public void selected() {}
	public void deselected() {}
	
	public final void modified() {
		EditorState.MODIFY_GIZMO.invoker().accept(this);
	}
}
