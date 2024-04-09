package me.andre111.voxedit.client.renderer;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import me.andre111.voxedit.state.Schematic;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.chunk.light.LightingProvider;

public record SchematicView(ClientWorld world, BlockPos pos, Schematic schematic) implements BlockRenderView, Iterable<BlockPos> {

	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		return world.getBrightness(direction, shaded);
	}

	@Override
	public LightingProvider getLightingProvider() {
		return SchematicLightingProvider.INSTANCE;
	}

	@Override
	public int getColor(BlockPos pos, ColorResolver colorResolver) {
		return world.getColor(this.pos.add(pos), colorResolver);
	}
	
    @Override
    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return schematic.getBlockEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return schematic.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return schematic.getFluidState(pos);
    }

    @Override
    public int getHeight() {
        return schematic.getHeight();
    }

	@Override
	public int getBottomY() {
		return 0;
	}

    @Override
    public Iterator<BlockPos> iterator() {
        return BlockPos.iterate(0, 0, 0, schematic.getSizeX() - 1, schematic.getSizeY() - 1, schematic.getSizeZ() - 1).iterator();
    }
}
