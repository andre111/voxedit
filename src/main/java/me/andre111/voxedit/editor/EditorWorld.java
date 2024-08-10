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
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.GameEvent.Emitter;
import net.minecraft.world.tick.BasicTickScheduler;
import net.minecraft.world.tick.EmptyTickSchedulers;
import net.minecraft.world.tick.QueryableTickScheduler;

public class EditorWorld implements StructureWorldAccess {
	private final StructureWorldAccess world;
	private final List<EditAction<?>> actions;
	private final Map<BlockPos, BlockState> changedBlockStates;
	private final Map<BlockPos, ModifiedBlockEntity> changedBlockEntities;
	
	public EditorWorld(StructureWorldAccess world) {
		this.world = world;
		this.actions = new ArrayList<>();
		this.changedBlockStates = new HashMap<>();
		this.changedBlockEntities = new HashMap<>();
	}

	public EditStats apply(ServerPlayerEntity player, Text text, EditHistory undo) {
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
			Schematic schematic = new Schematic(true, offsetX, offsetY, offsetZ, boundingBox.getBlockCountX(), boundingBox.getBlockCountY(), boundingBox.getBlockCountZ(), blockStates, blockEntities);
			result.setSchematic(schematic);
		}
		
		return result;
	}

	@Override
	public ServerWorld toServerWorld() {
		// TODO Auto-generated method stub
		System.err.println("Warning: Called toServerWorld in EditorWorld, modifications to the returned world will NOT be tracked.");
		return world.toServerWorld();
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
		return world.getLocalDifficulty(pos);
	}

	@Override
	public MinecraftServer getServer() {
		return world.getServer();
	}

	@Override
	public ChunkManager getChunkManager() {
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
	public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) {
		return new EditorWorldChunk(this, new ChunkPos(chunkX, chunkZ));
		//return world.getChunk(var1, var2, var3, var4);
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
	@Deprecated
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
		return false;
	}

	@Override
	public long getSeed() {
		return world.getSeed();
	}

	private static class ModifiedBlockEntity {
		private final StructureWorldAccess world;
		private final BlockEntity be;
		private final NbtCompound oldNbt;
		
		private ModifiedBlockEntity(StructureWorldAccess world, BlockEntity be) {
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

	
	private static class EditorWorldChunk extends Chunk {
		private final EditorWorld editorWorld;

		public EditorWorldChunk(EditorWorld editorWorld, ChunkPos pos) {
			super(pos, UpgradeData.NO_UPGRADE_DATA, editorWorld, editorWorld.getRegistryManager().get(RegistryKeys.BIOME), 0L, null, null);
			this.editorWorld = editorWorld;

			int y = editorWorld.getBottomSectionCoord();
			for(int i=0; i<getSectionArray().length; i++) {
				getSectionArray()[i] = new EditorChunkSection(editorWorld.getRegistryManager().get(RegistryKeys.BIOME), this, ChunkSectionPos.from(pos, y+i));
			}
		}

		@Override
		public BlockEntity getBlockEntity(BlockPos pos) {
			return editorWorld.getBlockEntity(pos);
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			return editorWorld.getBlockState(pos);
		}

		@Override
		public FluidState getFluidState(BlockPos pos) {
			return editorWorld.getFluidState(pos);
		}

		@Override
		public BlockState setBlockState(BlockPos pos, BlockState state, boolean move) {
			BlockState oldState = editorWorld.getBlockState(pos);
			if(editorWorld.setBlockState(pos, state, Block.NOTIFY_ALL | (move ? Block.MOVED : 0))) {
				return oldState;
			} else {
				return null;
			}
		}

		@Override
		public void setBlockEntity(BlockEntity var1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addEntity(Entity var1) {
		}

		@Override
		public ChunkStatus getStatus() {
			return ChunkStatus.FULL;
		}

		@Override
		public void removeBlockEntity(BlockPos pos) {
		}

		@Override
		public NbtCompound getPackedBlockEntityNbt(BlockPos var1, WrapperLookup var2) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BasicTickScheduler<Block> getBlockTickScheduler() {
			return EmptyTickSchedulers.getReadOnlyTickScheduler();
		}

		@Override
		public BasicTickScheduler<Fluid> getFluidTickScheduler() {
			return EmptyTickSchedulers.getReadOnlyTickScheduler();
		}

		@Override
		public TickSchedulers getTickSchedulers() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	private static class EditorChunkSection extends ChunkSection {
		private final EditorWorldChunk chunk;
		private final ChunkSectionPos pos;

		public EditorChunkSection(Registry<Biome> biomeRegistry, EditorWorldChunk chunk, ChunkSectionPos pos) {
			super(biomeRegistry);
			this.chunk = chunk;
			this.pos = pos;
		}
		
		@Override
		public BlockState getBlockState(int x, int y, int z) {
	        return chunk.getBlockState(pos.getMinPos().add(x, y, z));
	    }

		@Override
	    public FluidState getFluidState(int x, int y, int z) {
	        return chunk.getFluidState(pos.getMinPos().add(x, y, z));
	    }

		@Override
	    public void lock() {
	    }

		@Override
	    public void unlock() {
	    }

		@Override
	    public BlockState setBlockState(int x, int y, int z, BlockState state, boolean lock) {
	        return chunk.setBlockState(pos.getMinPos().add(x, y, z), state, false);
	    }
	}
}
