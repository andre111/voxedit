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
package me.andre111.voxedit.editor.action;

import java.util.function.BiConsumer;
import java.util.function.Function;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.history.EditHistoryReader;
import me.andre111.voxedit.editor.history.EditHistoryWriter;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.StructureWorldAccess;

public abstract class EditAction<A extends EditAction<A>> {
	public abstract Type<A> type();
	public abstract void undo(StructureWorldAccess world, EditStats stats);
	public abstract void redo(StructureWorldAccess world, EditStats stats);
	
	public static abstract class Type<A extends EditAction<A>> {
		public final Identifier id() {
			return VoxEdit.ACTION_TYPE_REGISTRY.getId(this);
		}
		
		public abstract void write(A action, EditHistoryWriter writer);
		public abstract A read(EditHistoryReader reader);
		
		public static <A extends EditAction<A>> Type<A> create(Identifier id, BiConsumer<A, EditHistoryWriter> writerImpl, Function<EditHistoryReader, A> readerImpl) {
			Type<A> type = new Type<A>() {
				@Override
				public void write(A action, EditHistoryWriter writer) {
					writerImpl.accept(action, writer);
				}

				@Override
				public A read(EditHistoryReader reader) {
					return readerImpl.apply(reader);
				}
			};
			Registry.register(VoxEdit.ACTION_TYPE_REGISTRY, id, type);
			return type;
		}
	}
}
