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
package me.andre111.voxedit.schematic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.andre111.voxedit.VoxEditUtil;
import me.andre111.voxedit.editor.EditorWorld;
import me.andre111.voxedit.selection.Selection;
import me.andre111.voxedit.selection.SelectionBox;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class Schematic implements BlockView {
	private final int offsetX;
	private final int offsetY;
	private final int offsetZ;
	private final int sizeX;
	private final int sizeY;
	private final int sizeZ;
	private final List<BlockState> blockStates;
	private final Map<BlockPos, BlockEntity> blockEntities;
	
	public Schematic(int offsetX, int offsetY, int offsetZ, int sizeX, int sizeY, int sizeZ, List<BlockState> blockStates, Map<BlockPos, BlockEntity> blockEntities) {
		long size = (long) sizeX * (long) sizeY * (long) sizeZ;
		if(size > Integer.MAX_VALUE) throw new IllegalArgumentException("Schematic size to large: "+size+" max supported is: "+Integer.MAX_VALUE);
		if(sizeX * sizeY * sizeZ != blockStates.size()) throw new IllegalArgumentException("Blockstate list does not match provided size.");
		
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
		this.blockStates = Collections.unmodifiableList(new ArrayList<>(blockStates));
		this.blockEntities = Collections.unmodifiableMap(new HashMap<>(blockEntities));
	}
	
	public SchematicInfo getInfo() {
		return new SchematicInfo(sizeX, sizeY, sizeZ);
	}
	
	public int getOffsetX() {
		return offsetX;
	}
	
	public int getOffsetY() {
		return offsetY;
	}
	
	public int getOffsetZ() {
		return offsetZ;
	}
	
	public int getSizeX() {
		return sizeX;
	}
	
	public int getSizeY() {
		return sizeY;
	}
	
	public int getSizeZ() {
		return sizeZ;
	}

	@Override
	public int getHeight() {
		return sizeY;
	}

	@Override
	public int getBottomY() {
		return 0;
	}

	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return blockEntities.get(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		int index = Schematic.indexOf(pos, sizeX, sizeY, sizeZ);
		if(index < 0 || index >= blockStates.size()) return Blocks.AIR.getDefaultState();
		return blockStates.get(index);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}
	
	public Schematic rotated(WrapperLookup registryLookup, BlockRotation rotation) {
		if(rotation == BlockRotation.NONE) return this;
		
		BlockPos rotatedMax = new BlockPos(sizeX-1, sizeY-1, sizeZ-1).rotate(rotation);
		int newSizeX = Math.abs(rotatedMax.getX())+1;
		int newSizeY = Math.abs(rotatedMax.getY())+1;
		int newSizeZ = Math.abs(rotatedMax.getZ())+1;
		int translateX = -Math.min(rotatedMax.getX(), 0);
		int translateY = -Math.min(rotatedMax.getY(), 0);
		int translateZ = -Math.min(rotatedMax.getZ(), 0);
		BlockPos rotatedOffset = new BlockPos(offsetX, offsetY, offsetZ).rotate(rotation);

		List<BlockState> rotatedBlockStates = new ArrayList<>(blockStates);
		BlockPos.Mutable schematicPos = new BlockPos.Mutable();
		int index = 0;
		for(int y=0; y<sizeY; y++) {
			for(int z=0; z<sizeZ; z++) {
				for(int x=0; x<sizeX; x++) {
					BlockState state = blockStates.get(index);

					BlockPos rotatedPos = schematicPos.set(x, y, z).rotate(rotation).add(translateX, translateY, translateZ);
					int rotatedIndex = rotatedPos.getY() * newSizeZ * newSizeX + rotatedPos.getZ() * newSizeX + rotatedPos.getX();
					rotatedBlockStates.set(rotatedIndex, state.rotate(rotation));
					
					index++;
				}
			}
		}
		
		Map<BlockPos, BlockEntity> rotatedBlockEntities = new HashMap<>();
		for(var e : blockEntities.entrySet()) {
			BlockPos rotatedPos = e.getKey().rotate(rotation).add(translateX, translateY, translateZ);
			BlockEntity rotatedBE = VoxEditUtil.copyBlockEntity(registryLookup, getBlockState(e.getKey()), e.getValue(), rotatedPos);
			rotatedBlockEntities.put(rotatedPos, rotatedBE);
		}
		
		return new Schematic(rotatedOffset.getX(), rotatedOffset.getY(), rotatedOffset.getZ(), newSizeX, newSizeY, newSizeZ, rotatedBlockStates, rotatedBlockEntities);
	}
	
	public void apply(EditorWorld world, BlockPos pos) {
		BlockPos start = pos.add(offsetX, offsetY, offsetZ);
		
		BlockPos.Mutable targetPos = new BlockPos.Mutable();
		BlockPos.Mutable schematicPos = new BlockPos.Mutable();
		int index = 0;
		for(int y=0; y<sizeY; y++) {
			for(int z=0; z<sizeZ; z++) {
				for(int x=0; x<sizeX; x++) {
					targetPos.set(start.getX()+x, start.getY()+y, start.getZ()+z);
					schematicPos.set(x, y, z);
					
					BlockState state = blockStates.get(index);
					if(state.getBlock() != Blocks.STRUCTURE_VOID) world.setBlockState(targetPos, state, 0);
					
					BlockEntity blockEntity = blockEntities.get(schematicPos);
					if(blockEntity != null) {
						world.getBlockEntity(targetPos).read(blockEntity.createNbt(world.getRegistryManager()), world.getRegistryManager());
					}
					
					index++;
				}
			}
		}
	}
	
	public void writeNbt(WrapperLookup registryLookup, NbtCompound nbt) {
		nbt.putInt("offsetX", offsetX);
		nbt.putInt("offsetY", offsetY);
		nbt.putInt("offsetZ", offsetZ);
		nbt.putInt("sizeX", sizeX);
		nbt.putInt("sizeY", sizeY);
		nbt.putInt("sizeZ", sizeZ);
		
		List<BlockState> paletteList = List.copyOf(Set.copyOf(blockStates));
		Map<BlockState, Integer> paletteIndices = new HashMap<>();
		for(int i=0; i<paletteList.size(); i++) {
			paletteIndices.put(paletteList.get(i), i);
		}
		nbt.put("palette", BlockState.CODEC.listOf().encodeStart(NbtOps.INSTANCE, paletteList).result().get());
		int[] blockStateArray = blockStates.stream().mapToInt(state -> paletteIndices.get(state)).toArray();
		nbt.put("blockStates", new NbtIntArray(blockStateArray));
		
		NbtList blockEntitiesNBT = new NbtList();
		for(BlockEntity blockEntity : blockEntities.values()) {
			blockEntitiesNBT.add(blockEntity.createNbtWithIdentifyingData(registryLookup));
		}
		nbt.put("blockEntities", blockEntitiesNBT);
	}

	public static int indexOf(BlockPos pos, int sizeX, int sizeY, int sizeZ) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if(x < 0 || x >= sizeX) return -1;
		if(y < 0 || y >= sizeY) return -1;
		if(z < 0 || z >= sizeZ) return -1;
		
		return x + z * sizeX + y * sizeX * sizeZ;
	}
	
	public static Schematic readNbt(WrapperLookup registryLookup, NbtCompound nbt) {
		int offsetX = nbt.getInt("offsetX");
		int offsetY = nbt.getInt("offsetY");
		int offsetZ = nbt.getInt("offsetZ");
		int sizeX = nbt.getInt("sizeX");
		int sizeY = nbt.getInt("sizeY");
		int sizeZ = nbt.getInt("sizeZ");
		
		List<BlockState> paletteList = BlockState.CODEC.listOf().decode(NbtOps.INSTANCE, nbt.get("palette")).result().get().getFirst();
		List<BlockState> blockStates = Arrays.stream(nbt.getIntArray("blockStates")).mapToObj(index -> paletteList.get(index)).toList();
		
		Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
		for(NbtElement e : nbt.getList("blockEntities", NbtElement.COMPOUND_TYPE)) {
			BlockPos pos = BlockEntity.posFromNbt((NbtCompound) e);
			BlockEntity blockEntity = BlockEntity.createFromNbt(pos, blockStates.get(indexOf(pos, sizeX, sizeY, sizeZ)), (NbtCompound) e, registryLookup);
			blockEntities.put(pos, blockEntity);
		}
		return new Schematic(offsetX, offsetY, offsetZ, sizeX, sizeY, sizeZ, blockStates, blockEntities);
	}
	

	public static Schematic create(WrapperLookup registryLookup, BlockView view, BlockBox area) {
		return create(registryLookup, view, new SelectionBox(area));
	}
	
	public static Schematic create(WrapperLookup registryLookup, BlockView view, Selection selection) {
		BlockBox area = selection.getBoundingBox();
		BlockPos.Mutable sourcePos = new BlockPos.Mutable();
		BlockPos.Mutable schematicPos = new BlockPos.Mutable();
		List<BlockState> blockStates = new ArrayList<>();
		Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
		for(int y=0; y<area.getBlockCountY(); y++) {
			for(int z=0; z<area.getBlockCountZ(); z++) {
				for(int x=0; x<area.getBlockCountX(); x++) {
					sourcePos.set(area.getMinX()+x, area.getMinY()+y, area.getMinZ()+z);
					schematicPos.set(x, y, z);
					
					BlockState state = selection.contains(sourcePos) ? view.getBlockState(sourcePos) : Blocks.STRUCTURE_VOID.getDefaultState();
					blockStates.add(state);
					BlockEntity blockEntity = view.getBlockEntity(sourcePos);
					if(blockEntity != null) {
						BlockEntity copy = VoxEditUtil.copyBlockEntity(registryLookup, state, blockEntity, schematicPos);
						blockEntities.put(schematicPos.toImmutable(), copy);
					}
				}
			}
		}
		return new Schematic(0, 0, 0, area.getBlockCountX(), area.getBlockCountY(), area.getBlockCountZ(), blockStates, blockEntities);
	}
}
