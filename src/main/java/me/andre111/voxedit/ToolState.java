package me.andre111.voxedit;

import java.util.HashSet;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

//TODO: sync changes to server
public class ToolState {
	public static final Codec<ToolState> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					BlockState.CODEC.optionalFieldOf("blockState", Blocks.STONE.getDefaultState()).forGetter(ts -> ts.blockState),
					Codec.STRING.optionalFieldOf("mode", "FILL").xmap(str -> Mode.valueOf(str), mode -> mode.name()).forGetter(ts -> ts.mode),
					Codec.STRING.optionalFieldOf("shape", "SPHERE").xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 3).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
			)
			.apply(instance, ToolState::new));
	
	public final BlockState blockState;
	public final Mode mode;
	public final Shape shape;
	public final int radius;
	public final boolean targetFluids;
	
	public ToolState() {
		this(Blocks.STONE.getDefaultState(), Mode.FILL, Shape.SPHERE, 3, false);
	}
	private ToolState(BlockState blockState, Mode mode, Shape shape, int radius, boolean targetFluids) {
		this.blockState = blockState;
		this.mode = mode;
		this.shape = shape;
		this.radius = radius;
		this.targetFluids = targetFluids;
	}
	
	public ToolState withBlockState(BlockState blockState) {
		return new ToolState(blockState, mode, shape, radius, targetFluids);
	}
	
	public ToolState withMode(Mode mode) {
		return new ToolState(blockState, mode, shape, radius, targetFluids);
	}
	
	public ToolState withShape(Shape shape) {
		return new ToolState(blockState, mode, shape, radius, targetFluids);
	}
	
	public ToolState withRadius(int radius) {
		return new ToolState(blockState, mode, shape, radius, targetFluids);
	}
	
	public ToolState withTargetFluids(boolean targetFluids) {
		return new ToolState(blockState, mode, shape, radius, targetFluids);
	}
	
	public Set<BlockPos> getBlockPositions(World world, BlockPos center) {
		Set<BlockPos> positions = new HashSet<>();
		
		for(int x = -radius; x <= radius; x++) {
        	for(int y = -radius; y <= radius; y++) {
        		for(int z = -radius; z <= radius; z++) {
                	if(!shape.offsetPredicate.test(this, x, y, z)) continue;
                	
                	BlockPos pos = center.add(x, y, z);
                	
                	if(!mode.testPredicate.test(this, world, pos)) continue;
                	
        			positions.add(pos);
                }
            }
        }
		
		return positions;
	}
	
	public static enum Shape {
		SPHERE((state, x, y, z) -> Math.sqrt(x*x + y*y + z*z) <= state.radius),
		CUBE((state, x, y, z) -> true);
		
		final OffsetPredicate offsetPredicate;
		
		Shape(OffsetPredicate offsetPredicate) {
			this.offsetPredicate = offsetPredicate;
		}
		
		public static interface OffsetPredicate {
			public boolean test(ToolState state, int x, int y, int z);
		}
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
		
		public static interface TestPredicate {
			public boolean test(ToolState state, World world, BlockPos pos);
		}
	}
	public static boolean isFree(World world, BlockPos pos) {
		return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
	}
}
