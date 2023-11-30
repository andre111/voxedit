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
package me.andre111.voxedit.editor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class EditStats {
	public static final EditStats EMPTY = new EditStats();
	
	private int blockChanges = 0;
	private int blockEntityChanges = 0;
	private int entityChanges = 0;
	
	public int blockChanges() {
		return blockChanges;
	}
	
	public int blockEntityChanges() {
		return blockEntityChanges;
	}
	
	public int entityChanges() {
		return entityChanges;
	}
	
	public void changedBlock() {
		blockChanges++;
	}
	
	public void changedBlockEntity() {
		blockEntityChanges++;
	}
	
	public void changedEntity() {
		entityChanges++;
	}
	
	public void inform(PlayerEntity player, EditType type) {
		MutableText contentText = Text.empty();
		
		if(blockChanges == 1) contentText.append(" ").append(Text.translatable("voxedit.edit.block"));
		if(blockChanges > 1) contentText.append(" ").append(Text.translatable("voxedit.edit.block.multiple", blockChanges));

		if(blockEntityChanges == 1) contentText.append(" ").append(Text.translatable("voxedit.edit.blockEntity"));
		if(blockEntityChanges > 1) contentText.append(" ").append(Text.translatable("voxedit.edit.blockEntity.multiple", blockChanges));

		if(entityChanges == 1) contentText.append(" ").append(Text.translatable("voxedit.edit.entity"));
		if(entityChanges > 1) contentText.append(" ").append(Text.translatable("voxedit.edit.entity.multiple", blockChanges));
		
		MutableText fullText = switch(type) {
		case PERFORM -> Text.translatable("voxedit.action.perform", contentText);
		case UNDO -> Text.translatable("voxedit.action.undo", contentText);
		case REDO -> Text.translatable("voxedit.action.redo", contentText);
		};
		
		player.sendMessage(fullText, true);
	}
}
