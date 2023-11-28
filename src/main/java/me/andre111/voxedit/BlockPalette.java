package me.andre111.voxedit;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.random.Random;

public class BlockPalette {
	public static final Codec<BlockPalette> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					Entry.CODEC.listOf().fieldOf("entries").forGetter(bp -> bp.entries)
			)
			.apply(instance, BlockPalette::new));
	public static final BlockPalette DEFAULT = new BlockPalette(Blocks.STONE.getDefaultState());
	
	private List<Entry> entries = new ArrayList<>();

	public BlockPalette() {
	}
	public BlockPalette(BlockState state) {
		entries.add(new Entry(state, 1));
	}
	public BlockPalette(List<Entry> entries) {
		this.entries = entries;
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
	
	public BlockState get(int index) {
		return entries.get(index).state;
	}
	
	public BlockState getRandom(Random random) {
		int totalWeight = entries.stream().mapToInt(Entry::weight).sum();
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
	
	public static record Entry(BlockState state, int weight) {
		private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(
						BlockState.CODEC.fieldOf("state").forGetter(e -> e.state),
						Codec.INT.fieldOf("weight").forGetter(e -> e.weight)
				)
				.apply(instance, Entry::new));
	}
}
