package me.andre111.voxedit;

import java.util.HashSet;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

//TODO: sync changes to server
public record ToolState(BlockPalette palette, BlockPalette filter, Mode mode, Shape shape, int radius, boolean targetFluids) {
	public static final Codec<ToolState> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockPalette.CODEC.optionalFieldOf("palette", new BlockPalette(Blocks.STONE.getDefaultState())).forGetter(ts -> ts.palette),
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.STRING.optionalFieldOf("mode", "FILL").xmap(str -> Mode.valueOf(str), mode -> mode.name()).forGetter(ts -> ts.mode),
					Codec.STRING.optionalFieldOf("shape", "SPHERE").xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 3).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
			)
			.apply(instance, ToolState::new));
	
	public static ToolState initial() {
		return new ToolState(new BlockPalette(Blocks.STONE.getDefaultState()), new BlockPalette(), Mode.FILL, Shape.SPHERE, 3, false);
	}
	
	public ToolState withBlockPalette(BlockPalette palette) {
		return new ToolState(palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withBlockFilter(BlockPalette filter) {
		return new ToolState(palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withMode(Mode mode) {
		return new ToolState(palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withShape(Shape shape) {
		return new ToolState(palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withRadius(int radius) {
		return new ToolState(palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withTargetFluids(boolean targetFluids) {
		return new ToolState(palette, filter, mode, shape, radius, targetFluids);
	}
	
	public Set<BlockPos> getBlockPositions(World world, BlockPos center) {
		Set<BlockPos> positions = new HashSet<>();
		
		for(int x = -radius; x <= radius; x++) {
        	for(int y = -radius; y <= radius; y++) {
        		for(int z = -radius; z <= radius; z++) {
                	if(!shape.offsetPredicate.test(this, x, y, z)) continue;
                	
                	BlockPos pos = center.add(x, y, z);
                	
                	if(!mode.testPredicate.test(this, world, pos)) continue;
                	if(filter.size() > 0 && !filter.has(world.getBlockState(pos).getBlock())) continue;
                	
        			positions.add(pos);
                }
            }
        }
		
		return positions;
	}
	
	public static enum Mode {
		FILL((state, world, pos) -> true),
		PAINT((state, world, pos) -> {
			if(isFree(world, pos)) return false;
			for(Direction d : Direction.values()) {
				if(isFree(world, pos.offset(d))) return true;
			}
			return false;
		}),
		PAINT_TOP((state, world, pos) -> {
			if(isFree(world, pos)) return false;
			return isFree(world, pos.offset(Direction.UP));
		});
		
		final TestPredicate testPredicate;
		
		Mode(TestPredicate testPredicate) {
			this.testPredicate = testPredicate;
		}
		
		public Text asText() {
			return Text.of(name());
		}
		
		public static interface TestPredicate {
			public boolean test(ToolState state, World world, BlockPos pos);
		}
	}
	public static enum Shape {
		SPHERE((state, x, y, z) -> Math.sqrt(x*x + y*y + z*z) <= state.radius),
		CUBE((state, x, y, z) -> true),
		HOLLOW_SPHERE((state, x, y, z) -> {
			double dist = Math.sqrt(x*x + y*y + z*z);
			return (state.radius - 0.5) <= dist && dist <= (state.radius + 0.5);
		}),
		HOLLOW_CUBE((state, x, y, z) -> Math.abs(x) == state.radius || Math.abs(y) == state.radius || Math.abs(z) == state.radius);
		
		final OffsetPredicate offsetPredicate;
		
		Shape(OffsetPredicate offsetPredicate) {
			this.offsetPredicate = offsetPredicate;
		}
		
		public Text asText() {
			return Text.of(name());
		}
		
		public static interface OffsetPredicate {
			public boolean test(ToolState state, int x, int y, int z);
		}
	}
	
	public static boolean isFree(World world, BlockPos pos) {
		return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
	}
}
