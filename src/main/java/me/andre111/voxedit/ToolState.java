package me.andre111.voxedit;

import java.util.HashSet;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.tool.Tool;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

//TODO: sync changes to server
public record ToolState(Tool tool, BlockPalette palette, BlockPalette filter, Mode mode, Shape shape, int radius, boolean targetFluids) {
	public static final Codec<ToolState> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
					Identifier.CODEC.optionalFieldOf("tool", new Identifier("voxedit", "brush")).xmap(VoxEdit.TOOL_REGISTRY::get, Tool::id).forGetter(ts -> ts.tool),
					BlockPalette.CODEC.optionalFieldOf("palette", new BlockPalette(Blocks.STONE.getDefaultState())).forGetter(ts -> ts.palette),
					BlockPalette.CODEC.optionalFieldOf("filter", new BlockPalette()).forGetter(ts -> ts.filter),
					Codec.STRING.optionalFieldOf("mode", "SOLID").xmap(str -> Mode.valueOf(str), mode -> mode.name()).forGetter(ts -> ts.mode),
					Codec.STRING.optionalFieldOf("shape", "SPHERE").xmap(str -> Shape.valueOf(str), shape -> shape.name()).forGetter(ts -> ts.shape),
					Codec.INT.optionalFieldOf("radius", 3).forGetter(ts -> ts.radius),
					Codec.BOOL.optionalFieldOf("targetFluids", false).forGetter(ts -> ts.targetFluids)
			)
			.apply(instance, ToolState::new));
	
	public static ToolState of(Tool tool) {
		return new ToolState(tool, new BlockPalette(Blocks.STONE.getDefaultState()), new BlockPalette(), Mode.SOLID, Shape.SPHERE, 5, false);
	}
	
	public ToolState withTool(Tool tool) {
		return new ToolState(tool, palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withBlockPalette(BlockPalette palette) {
		return new ToolState(tool, palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withBlockFilter(BlockPalette filter) {
		return new ToolState(tool, palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withMode(Mode mode) {
		return new ToolState(tool, palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withShape(Shape shape) {
		return new ToolState(tool, palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withRadius(int radius) {
		return new ToolState(tool, palette, filter, mode, shape, radius, targetFluids);
	}
	
	public ToolState withTargetFluids(boolean targetFluids) {
		return new ToolState(tool, palette, filter, mode, shape, radius, targetFluids);
	}
	
	public Set<BlockPos> getBlockPositions(World world, BlockHitResult target) {
		Set<BlockPos> positions = new HashSet<>();
		
		BlockPos center = target.getBlockPos();
		for(int x = -radius; x <= radius; x++) {
        	for(int y = -radius; y <= radius; y++) {
        		for(int z = -radius; z <= radius; z++) {
                	if(!shape.offsetPredicate.test(target, this, x, y, z)) continue;
                	
                	BlockPos pos = center.add(x, y, z);
                	
                	if(!mode.testPredicate.test(target, this, world, pos)) continue;
                	if(!passesFilter(world.getBlockState(pos))) continue;
                	
        			positions.add(pos);
                }
            }
        }
		
		return positions;
	}
	
	public boolean passesFilter(BlockState state) {
		return filter.size() == 0 || filter.has(state.getBlock());
	}
	
	public static enum Mode {
		SOLID((target, state, world, pos) -> true),
		PAINT((target, state, world, pos) -> {
			if(isFree(world, pos)) return false;
			for(Direction d : Direction.values()) {
				if(isFree(world, pos.offset(d))) return true;
			}
			return false;
		}),
		PAINT_TOP((target, state, world, pos) -> {
			if(isFree(world, pos)) return false;
			return isFree(world, pos.offset(Direction.UP));
		}),
		SCATTER((target, state, world, pos) -> {
			if(!world.isAir(pos)) return false;
			return !isFree(world, pos.offset(target.getSide().getOpposite()));
		});
		
		final TestPredicate testPredicate;
		
		Mode(TestPredicate testPredicate) {
			this.testPredicate = testPredicate;
		}
		
		public Text asText() {
			return Text.of(name());
		}
		
		public static interface TestPredicate {
			public boolean test(BlockHitResult target, ToolState state, World world, BlockPos pos);
		}
	}
	public static enum Shape {
		SPHERE((target, state, x, y, z) -> Math.sqrt(x*x + y*y + z*z) <= state.radius),
		CUBE((target, state, x, y, z) -> true),
		DISC((target, state, x, y, z) -> {
			if(Math.sqrt(x*x + y*y + z*z) > state.radius) return false;
			if(target.getSide().getOffsetX() != 0 && x != 0) return false;
			if(target.getSide().getOffsetY() != 0 && y != 0) return false;
			if(target.getSide().getOffsetZ() != 0 && z != 0) return false;
			return true;
		}),
		CYLINDER((target, state, x, y, z) -> {
			if(target.getSide().getOffsetX() != 0 && Math.sqrt(y*y + z*z) > state.radius) return false;
			if(target.getSide().getOffsetY() != 0 && Math.sqrt(x*x + z*z) > state.radius) return false;
			if(target.getSide().getOffsetZ() != 0 && Math.sqrt(x*x + y*y) > state.radius) return false;
			return true;
		}),
		HOLLOW_SPHERE((target, state, x, y, z) -> {
			double dist = Math.sqrt(x*x + y*y + z*z);
			return (state.radius - 0.5) <= dist && dist <= (state.radius + 0.5);
		}),
		HOLLOW_CUBE((target, state, x, y, z) -> Math.abs(x) == state.radius || Math.abs(y) == state.radius || Math.abs(z) == state.radius);
		
		final OffsetPredicate offsetPredicate;
		
		Shape(OffsetPredicate offsetPredicate) {
			this.offsetPredicate = offsetPredicate;
		}
		
		public Text asText() {
			return Text.of(name());
		}
		
		public static interface OffsetPredicate {
			public boolean test(BlockHitResult target, ToolState state, int x, int y, int z);
		}
	}
	
	public static boolean isFree(World world, BlockPos pos) {
		return world.getBlockState(pos).getCollisionShape(world, pos).isEmpty();
	}
}
