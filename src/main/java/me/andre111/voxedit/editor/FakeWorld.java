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
package me.andre111.voxedit.editor;

import java.util.List;
import java.util.function.Predicate;

import me.andre111.voxedit.schematic.SchematicLightingProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.minecraft.world.tick.EmptyTickSchedulers;
import net.minecraft.world.tick.QueryableTickScheduler;

public class FakeWorld implements StructureWorldAccess {
	private final ServerWorld world;
	private final BlockState surfaceState;
	private final Random random;
	
	public FakeWorld(ServerWorld world, BlockState surfaceState) {
		this.world = world;
		this.surfaceState = surfaceState;
		this.random = Random.create(1144185);
	}

	@Override
	public ServerWorld toServerWorld() {
		// TODO Auto-generated method stub
		System.err.println("Warning: Access to underlying world - could cause issues");
		new RuntimeException().printStackTrace();
		return world;
	}

	@Override
	public long getTickOrder() {
		return 0;
	}

	@Override
	public QueryableTickScheduler<Block> getBlockTickScheduler() {
		return EmptyTickSchedulers.getClientTickScheduler();
	}

	@Override
	public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
		return EmptyTickSchedulers.getClientTickScheduler();
	}

	@Override
	public WorldProperties getLevelProperties() {
		return world.getLevelProperties();
	}

	@Override
	public LocalDifficulty getLocalDifficulty(BlockPos pos) {
		return new LocalDifficulty(Difficulty.NORMAL, 0, 0, 0);
	}

	@Override
	public MinecraftServer getServer() {
		return world.getServer();
	}

	@Override
	public ServerChunkManager getChunkManager() {
		return world.getChunkManager();
	}

	@Override
	public Random getRandom() {
		return random;
	}

	@Override
	public void playSound(PlayerEntity var1, BlockPos var2, SoundEvent var3, SoundCategory var4, float var5, float var6) {
	}

	@Override
	public void addParticle(ParticleEffect var1, double var2, double var4, double var6, double var8, double var10, double var12) {
	}

	@Override
	public void syncWorldEvent(PlayerEntity var1, int var2, BlockPos var3, int var4) {
	}

	@Override
	public void emitGameEvent(RegistryEntry<GameEvent> var1, Vec3d var2, Emitter var3) {
	}

	@Override
	public List<Entity> getOtherEntities(Entity var1, Box var2, Predicate<? super Entity> var3) {
		return List.of();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> var1, Box var2, Predicate<? super T> var3) {
		return List.of();
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return List.of();
	}

	@Override
	public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
		return null;
	}

	@Override
	public int getTopY(Type var1, int var2, int var3) {
		return 63;
	}

	@Override
	public int getAmbientDarkness() {
		return world.getAmbientDarkness();
	}

	@Override
	public BiomeAccess getBiomeAccess() {
		return world.getBiomeAccess();
	}

	@Override
	public RegistryEntry<Biome> getGeneratorStoredBiome(int var1, int var2, int var3) {
		return world.getGeneratorStoredBiome(var1, var2, var3);
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public int getSeaLevel() {
		return 63;
	}

	@Override
	public DimensionType getDimension() {
		return world.getDimension();
	}

	@Override
	public DynamicRegistryManager getRegistryManager() {
		return world.getRegistryManager();
	}

	@Override
	public FeatureSet getEnabledFeatures() {
		return world.getEnabledFeatures();
	}

	@Override
	public float getBrightness(Direction var1, boolean var2) {
		return 1.0f;
	}

	@Override
	public LightingProvider getLightingProvider() {
		return SchematicLightingProvider.INSTANCE;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		int y = pos.getY();
		if(y == 64) return surfaceState;
		else if(63 >= y && y >= 60) return Blocks.DIRT.getDefaultState();
		else if(y == 32 || y == 31 || y == 30 || y == 29 || y == 28) return Blocks.AIR.getDefaultState();
		else if(59 >= y && y > 0) return Blocks.STONE.getDefaultState();
		else if(y == 0) return Blocks.BEDROCK.getDefaultState();
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}

	@Override
	public WorldBorder getWorldBorder() {
		return new WorldBorder();
	}

	@Override
	public boolean testBlockState(BlockPos pos, Predicate<BlockState> var2) {
		return var2.test(getBlockState(pos));
	}

	@Override
	public boolean testFluidState(BlockPos pos, Predicate<FluidState> var2) {
		return var2.test(getFluidState(pos));
	}

	@Override
	public boolean setBlockState(BlockPos var1, BlockState var2, int var3, int var4) {
		return false;
	}

	@Override
	public boolean removeBlock(BlockPos pos, boolean move) {
        FluidState fluidState = getFluidState(pos);
        return setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_ALL | (move ? Block.MOVED : 0));
	}

	@Override
	public boolean breakBlock(BlockPos pos, boolean var2, Entity var3, int var4) {
		return false;
	}
	
	@Override
	public long getSeed() {
		return 0;
	}
}
