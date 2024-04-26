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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import me.andre111.voxedit.VoxEditUtil;
import me.andre111.voxedit.editor.action.EditAction;
import me.andre111.voxedit.editor.action.ModifyBlockEntityAction;
import me.andre111.voxedit.editor.action.SetBlockAction;
import me.andre111.voxedit.editor.history.EditHistory;
import me.andre111.voxedit.editor.history.EditHistoryState;
import me.andre111.voxedit.network.CPHistoryInfo;
import me.andre111.voxedit.schematic.Schematic;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
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
import net.minecraft.world.tick.QueryableTickScheduler;

public class EditorWorld implements StructureWorldAccess {
	private final ServerWorld world;
	private final EditHistory undo;
	private final List<EditAction<?>> actions;
	private final Map<BlockPos, BlockState> changedBlockStates;
	private final Map<BlockPos, ModifiedBlockEntity> changedBlockEntities;
	
	public EditorWorld(ServerWorld world, EditHistory undo) {
		this.world = world;
		this.undo = undo;
		this.actions = new ArrayList<>();
		this.changedBlockStates = new HashMap<>();
		this.changedBlockEntities = new HashMap<>();
	}

	public EditStats apply(ServerPlayerEntity player, Text text) {
		EditStats result = new EditStats(text);
		
		for(var e : changedBlockEntities.entrySet()) {
			EditAction<?> action = e.getValue().asAction(e.getKey());
			if(action != null) actions.add(action);
		}
		if(actions.isEmpty()) return result;
		
		for(EditAction<?> action : actions) {
			action.redo(world, result);
		}
		CPHistoryInfo info = undo.push(world, new EditHistoryState(result, actions));
		ServerPlayNetworking.send(player, info); //TODO: this is really not the correct location for this
		return result;
	}
	
	public EditStats toSchematic(ServerPlayerEntity player, Text text, BlockPos origin) {
		EditStats result = new EditStats(text);
		
		// find bounding box
		BlockBox blockStateBB = BlockBox.encompassPositions(changedBlockStates.keySet()).orElse(null);
		BlockBox blockEntityBB = BlockBox.encompassPositions(changedBlockEntities.keySet()).orElse(null);
		BlockBox boundingBox = null;
		if(blockStateBB != null && blockEntityBB != null) boundingBox = BlockBox.encompass(List.of(blockStateBB, blockEntityBB)).get();
		else if(blockStateBB != null) boundingBox = blockStateBB;
		else if(blockEntityBB != null) boundingBox = blockEntityBB;
		
		// create schematic
		if(boundingBox != null) {
			int offsetX = boundingBox.getMinX() - origin.getX();
			int offsetY = boundingBox.getMinY() - origin.getY();
			int offsetZ = boundingBox.getMinZ() - origin.getZ();
			BlockPos.Mutable sourcePos = new BlockPos.Mutable();
			BlockPos.Mutable schematicPos = new BlockPos.Mutable();
			List<BlockState> blockStates = new ArrayList<>();
			Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
			for(int y=0; y<boundingBox.getBlockCountY(); y++) {
				for(int z=0; z<boundingBox.getBlockCountZ(); z++) {
					for(int x=0; x<boundingBox.getBlockCountX(); x++) {
						sourcePos.set(boundingBox.getMinX()+x, boundingBox.getMinY()+y, boundingBox.getMinZ()+z);
						schematicPos.set(x, y, z);
						
						BlockState state = changedBlockStates.getOrDefault(sourcePos, Blocks.STRUCTURE_VOID.getDefaultState());
						blockStates.add(state);
						if(changedBlockEntities.containsKey(sourcePos)) {
							BlockEntity copy = VoxEditUtil.copyBlockEntity(getRegistryManager(), state, changedBlockEntities.get(sourcePos).be, schematicPos);
							blockEntities.put(schematicPos.toImmutable(), copy);
						}
					}
				}
			}
			Schematic schematic = new Schematic(offsetX, offsetY, offsetZ, boundingBox.getBlockCountX(), boundingBox.getBlockCountY(), boundingBox.getBlockCountZ(), blockStates, blockEntities);
			result.setSchematic(schematic);
		}
		
		return result;
	}

	@Override
	public ServerWorld toServerWorld() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTickOrder() {
		return 0;
	}

	@Override
	public QueryableTickScheduler<Block> getBlockTickScheduler() {
		return world.getBlockTickScheduler();
	}

	@Override
	public QueryableTickScheduler<Fluid> getFluidTickScheduler() {
		return world.getFluidTickScheduler();
	}

	@Override
	public WorldProperties getLevelProperties() {
		return world.getLevelProperties();
	}

	@Override
	public LocalDifficulty getLocalDifficulty(BlockPos pos) {
		return world.getLocalDifficulty(pos);
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
		return world.getRandom();
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
		return world.getOtherEntities(var1, var2, var3);
	}

	@Override
	public <T extends Entity> List<T> getEntitiesByType(TypeFilter<Entity, T> var1, Box var2, Predicate<? super T> var3) {
		return world.getEntitiesByType(var1, var2, var3);
	}

	@Override
	public List<? extends PlayerEntity> getPlayers() {
		return world.getPlayers();
	}

	@Override
	public Chunk getChunk(int var1, int var2, ChunkStatus var3, boolean var4) {
		return world.getChunk(var1, var2, var3, var4);
	}

	@Override
	public int getTopY(Type var1, int var2, int var3) {
		return world.getTopY(var1, var2, var3);
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
		return world.getSeaLevel();
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
		return world.getBrightness(var1, var2);
	}

	@Override
	public LightingProvider getLightingProvider() {
		return world.getLightingProvider();
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		if(changedBlockEntities.containsKey(pos)) {
			return changedBlockEntities.get(pos).be;
		}
		
		BlockEntity oldBe = world.getBlockEntity(pos);
		if(oldBe == null) return null;
		
		BlockEntity newBe = BlockEntity.createFromNbt(pos, getBlockState(pos), oldBe.createNbtWithIdentifyingData(world.getRegistryManager()), world.getRegistryManager());
		changedBlockEntities.put(pos.toImmutable(), new ModifiedBlockEntity(world, newBe));
		return newBe;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		if(changedBlockStates.containsKey(pos)) return changedBlockStates.get(pos);
		return world.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		if(changedBlockStates.containsKey(pos)) return changedBlockStates.get(pos).getFluidState();
		return world.getFluidState(pos);
	}

	@Override
	public WorldBorder getWorldBorder() {
		return world.getWorldBorder();
	}

	@Override
	public boolean testBlockState(BlockPos pos, Predicate<BlockState> predicate) {
		if(changedBlockStates.containsKey(pos)) return predicate.test(changedBlockStates.get(pos));
		return world.testBlockState(pos, predicate);
	}

	@Override
	public boolean testFluidState(BlockPos pos, Predicate<FluidState> predicate) {
		if(changedBlockStates.containsKey(pos)) return predicate.test(changedBlockStates.get(pos).getFluidState());
		return world.testFluidState(pos, predicate);
	}

	@Override
	public boolean setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
		if(getBlockState(pos) == state) return true;
		if(changedBlockEntities.containsKey(pos)) changedBlockEntities.remove(pos);
		
		BlockPos immutablePos = pos.toImmutable();
		actions.add(new SetBlockAction(world, immutablePos, state));
		changedBlockStates.put(immutablePos, state);
		
		if(state.hasBlockEntity()) {
			changedBlockEntities.put(immutablePos, new ModifiedBlockEntity(world, ((BlockEntityProvider) state.getBlock()).createBlockEntity(pos, state)));
		}
		
		return true;
	}

	@Override
	public boolean removeBlock(BlockPos pos, boolean move) {
        FluidState fluidState = getFluidState(pos);
        return setBlockState(pos, fluidState.getBlockState(), Block.NOTIFY_ALL | (move ? Block.MOVED : 0));
	}

	@Override
	public boolean breakBlock(BlockPos pos, boolean var2, Entity var3, int var4) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public long getSeed() {
		return world.getSeed();
	}

	private static class ModifiedBlockEntity {
		private final World world;
		private final BlockEntity be;
		private final NbtCompound oldNbt;
		
		private ModifiedBlockEntity(World world, BlockEntity be) {
			this.world = world;
			this.be = be;
			this.oldNbt = be.createNbtWithId(world.getRegistryManager());
		}
		
		private EditAction<?> asAction(BlockPos pos) {
			NbtCompound newNbt = be.createNbtWithId(world.getRegistryManager());
			if(newNbt.equals(oldNbt)) return null;
			return new ModifyBlockEntityAction(pos, oldNbt, newNbt);
		}
	}
}
