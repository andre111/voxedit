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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.random.Random;

public class BlockPalette {
	public static final Codec<BlockPalette> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					Entry.CODEC.listOf().fieldOf("entries").forGetter(bp -> bp.entries)
			)
			.apply(instance, BlockPalette::new));
	public static final PacketCodec<ByteBuf, BlockPalette> PACKET_CODEC = Entry.PACKET_CODEC.collect(PacketCodecs.toList()).xmap(BlockPalette::new, BlockPalette::getEntries);
	public static final BlockPalette DEFAULT = BlockPalette.builder().add(Blocks.STONE).build();
	
	private List<Entry> entries = new ArrayList<>();
	private int totalWeight = 0;

	public BlockPalette() {
	}
	public BlockPalette(List<Entry> entries) {
		this.entries = new ArrayList<>(entries);
		totalWeight = entries.stream().mapToInt(Entry::weight).sum();
	}
	
	public int size() {
		return entries.size();
	}
	
	public boolean has(Block block) {
		for(Entry entry : entries) {
			if(entry.state.getBlock() == block) return true;
		}
		return false;
	}
	
	public boolean has(BlockState state) {
		for(Entry entry : entries) {
			if(entry.matches(state)) return true;
		}
		return false;
	}
	
	public BlockState get(int index) {
		return entries.get(index).state;
	}
	
	public BlockState getRandom(Random random) {
		int value = random.nextInt(totalWeight);
		for(Entry entry : entries) {
			if(value < entry.weight) return entry.state;
			value -= entry.weight;
		}
		return Blocks.AIR.getDefaultState();
	}
	
	public Entry getEntry(int index) {
		return entries.get(index);
	}
	
	public void setEntry(int index, Entry entry) {
		entries.set(index, entry);
		totalWeight = entries.stream().mapToInt(Entry::weight).sum();
	}
	
	public List<Entry> getEntries() {
		return new ArrayList<>(entries);
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null) return false;
		if(other instanceof BlockPalette otherPallete) return entries.equals(otherPallete.entries);
		return false;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static record Entry(BlockState state, Set<String> specifiedProperties, int weight) {
		private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(
						BlockState.CODEC.fieldOf("state").forGetter(Entry::state),
						Codec.STRING.listOf().xmap(l -> (Set<String>) new HashSet<>(l), s -> new ArrayList<>(s)).optionalFieldOf("specifiedProperties", new HashSet<>()).forGetter(Entry::specifiedProperties),
						Codec.INT.fieldOf("weight").forGetter(Entry::weight)
				)
				.apply(instance, Entry::new));
		private static final PacketCodec<ByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(
				PacketCodecs.codec(BlockState.CODEC), Entry::state,
				PacketCodecs.STRING.collect(PacketCodecs.toCollection(HashSet::new)), Entry::specifiedProperties,
				PacketCodecs.INTEGER, Entry::weight, 
				Entry::new);
		
		public boolean matches(BlockState testState) {
			if(testState.getBlock() != state.getBlock()) return false;
			for(Property<?> property : testState.getProperties()) {
				if(!specifiedProperties.contains(property.getName())) continue;
				if(!testState.get(property).equals(state.get(property))) return false;
			}
			return true;
		}
	}
	
	public static class Builder {
		private List<Entry> entries = new ArrayList<>();
		
		public Builder add(Block block) {
			return add(block, 1);
		}
		
		public Builder add(Block block, int weight) {
			return add(block.getDefaultState(), new HashSet<>(), weight);
		}
		
		public Builder add(BlockState state) {
			return add(state, 1);
		}
		
		public Builder add(BlockState state, int weight) {
			return add(state, state.getProperties().stream().map(Property::getName).collect(Collectors.toCollection(HashSet::new)), weight);
		}
		
		public Builder add(BlockState state, HashSet<String> specifiedProperties, int weight) {
			entries.add(new Entry(state, specifiedProperties, weight));
			return this;
		}
		
		public BlockPalette build() {
			return new BlockPalette(new ArrayList<>(entries));
		}
	}
}
