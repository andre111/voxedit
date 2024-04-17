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
package me.andre111.voxedit.editor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.state.Schematic;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public class EditStats {
	public static final EditStats EMPTY = new EditStats(Text.empty());
	public static final Codec<EditStats> WITHOUT_SCHEMATIC_CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
				TextCodecs.CODEC.fieldOf("text").forGetter(EditStats::text),
				Codec.INT.fieldOf("blockChanges").forGetter(EditStats::blockChanges),
				Codec.INT.fieldOf("blockEntityChanges").forGetter(EditStats::blockEntityChanges),
				Codec.INT.fieldOf("entityChanges").forGetter(EditStats::entityChanges)
			)
			.apply(instance, EditStats::new));
	public static final PacketCodec<ByteBuf, EditStats> WITHOUT_SCHEMATIC_PACKET_CODEC = PacketCodecs.codec(WITHOUT_SCHEMATIC_CODEC);
	
	private Text text = Text.empty();
	private int blockChanges = 0;
	private int blockEntityChanges = 0;
	private int entityChanges = 0;
	private Schematic schematic = null;
	
	public EditStats(Text text) {
		this.text = text;
	}
	private EditStats(Text text, int blockChanges, int blockEntityChanges, int entityChanges) {
		this.text = text;
		this.blockChanges = blockChanges;
		this.blockEntityChanges = blockEntityChanges;
		this.entityChanges = entityChanges;
	}
	
	public Text text() {
		return text;
	}
	
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
	
	public Schematic schematic() {
		return schematic;
	}
	
	public void setSchematic(Schematic schematic) {
		this.schematic = schematic;
	}
	
	public Text fullText() {
		MutableText contentText = text.copy().append(":");
		
		if(blockChanges == 1) contentText.append(" ").append(Text.translatable("voxedit.edit.block"));
		if(blockChanges > 1) contentText.append(" ").append(Text.translatable("voxedit.edit.block.multiple", blockChanges));

		if(blockEntityChanges == 1) contentText.append(" ").append(Text.translatable("voxedit.edit.blockEntity"));
		if(blockEntityChanges > 1) contentText.append(" ").append(Text.translatable("voxedit.edit.blockEntity.multiple", blockChanges));

		if(entityChanges == 1) contentText.append(" ").append(Text.translatable("voxedit.edit.entity"));
		if(entityChanges > 1) contentText.append(" ").append(Text.translatable("voxedit.edit.entity.multiple", blockChanges));
		
		return contentText;
	}
	
	public void inform(PlayerEntity player, EditType type) {
		Text contentText = fullText();
		
		MutableText fullText = switch(type) {
		case PERFORM -> Text.translatable("voxedit.action.perform", contentText);
		case UNDO -> Text.translatable("voxedit.action.undo", contentText);
		case REDO -> Text.translatable("voxedit.action.redo", contentText);
		};
		
		player.sendMessage(fullText, true);
	}
}
