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

import com.mojang.datafixers.util.Either;

import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntryList.Named;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public record EntryOrTag<T>(Either<T, Named<T>> value) {
	public boolean isPresent(Registry<T> registry) {
		if(value == null) return false;
		if(value.left().isPresent()) return registry.getId(value.left().get()) != null;
		if(value.right().isPresent()) return true; //TODO: implement?
		return false;
	}
	
	public String asString(Registry<T> registry) {
		if(value == null) return "";
		if(value.left().isPresent()) return registry.getId(value.left().get()).toString();
		if(value.right().isPresent()) return "#"+value.right().get().getTag().id().toString();
		return "";
	}
	
	public static <T> EntryOrTag<T> fromString(Registry<T> registry, String id) {
		if(id == null) return null;
		if(id.startsWith("#")) {
			var tag = registry.getEntryList(TagKey.of(registry.getKey(), Identifier.tryParse(id.substring(1))));
			if(tag.isPresent()) return new EntryOrTag<>(Either.right(tag.get()));
		} else  {
			var entry = registry.get(Identifier.tryParse(id));
			if(entry != null) return new EntryOrTag<>(Either.left(entry));
		}
		return null;
	}
}
