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
package me.andre111.voxedit;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper.Impl;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;

public class VoxEditUtil {
	public static BlockEntity copyBlockEntity(WrapperLookup registryLookup, BlockState state, BlockEntity source, BlockPos pos) {
		// create unlinked copy and adjust position
		NbtCompound nbt = source.createNbtWithId(registryLookup);
		return BlockEntity.createFromNbt(pos, state, nbt, registryLookup);
	}
	
	public static <V> V parseJson(String json, Codec<V> codec, V defaultValue, WrapperLookup wrapperLookup) {
		try {
			JsonElement element = (new Gson()).fromJson(json, JsonElement.class);
			V value = codec.decode(RegistryOps.of(JsonOps.INSTANCE, wrapperLookup), element).result().orElse(Pair.of(defaultValue, element)).getFirst();
			return value;
		} catch(JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	public static <V> String toJson(V value, Codec<V> codec, WrapperLookup wrapperLookup) {
		DataResult<JsonElement> result = codec.encodeStart(RegistryOps.of(JsonOps.INSTANCE, wrapperLookup), value);
		if(result.isSuccess()) {
			return (new Gson()).toJson(result.getOrThrow());
		} else {
			return null;
		}
	}
	
	public static <V> V readJson(Path path, Codec<V> codec, V defaultValue) {
		if(!Files.exists(path)) return defaultValue;
		
		try(BufferedReader reader = Files.newBufferedReader(path)) {
			JsonElement element = (new Gson()).fromJson(reader, JsonElement.class);
			V value = codec.decode(JsonOps.INSTANCE, element).result().orElse(Pair.of(defaultValue, element)).getFirst();
			return value;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	public static <V> void writeJson(Path path, Codec<V> codec, V value) {
		DataProvider.writeCodecToPath(DataWriter.UNCACHED, new WrapperLookup() {
			@Override
			public Stream<RegistryKey<? extends Registry<?>>> streamAllRegistryKeys() {
				return null;
			}

			@Override
			public <T> Optional<Impl<T>> getOptionalWrapper(RegistryKey<? extends Registry<? extends T>> var1) {
				return Optional.empty();
			}
		}, codec, value, path);
	}
	
	public static NbtCompound readNbt(Path path) {
		try {
			return NbtIo.readCompressed(path, NbtSizeTracker.ofUnlimitedBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void writeNbt(Path path, NbtCompound nbt) {
		try {
			Files.createDirectories(path.getParent());
			if(!Files.exists(path)) Files.createFile(path);
			
			NbtIo.writeCompressed(nbt, path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String removeInvalidChars(String name) {
		return name.replace("/", "_").replace("\\", "_").replace(":", "_");
	}
}
