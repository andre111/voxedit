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
package me.andre111.voxedit.client.renderer;

import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import me.andre111.voxedit.schematic.Schematic;
import me.andre111.voxedit.schematic.SchematicLightingProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.ColorResolver;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.light.LightingProvider;

public record SchematicView(BlockPos pos, Schematic schematic) implements BlockRenderView, Iterable<BlockPos> {
	private static final Biome BIOME;
	static {
		BiomeEffects.Builder builder = new BiomeEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(12638463);
		BIOME = new Biome.Builder().precipitation(true).temperature(0.8f).downfall(0.4f).effects(builder.build()).generationSettings(new GenerationSettings.Builder().build()).spawnSettings(new SpawnSettings.Builder().build()).build();
	}
	
	@Override
	public float getBrightness(Direction direction, boolean shaded) {
		if(!shaded) return 1.0f;
        return switch(direction) {
            case DOWN -> 0.5f;
            case UP -> 1.0f;
            case NORTH, SOUTH -> 0.8f;
            case WEST, EAST -> 0.6f;
            default -> 1.0f;
        };
	}

	@Override
	public LightingProvider getLightingProvider() {
		return SchematicLightingProvider.INSTANCE;
	}

	@Override
	public int getColor(BlockPos pos, ColorResolver colorResolver) {
		return colorResolver.getColor(BIOME, 0, 0);
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
