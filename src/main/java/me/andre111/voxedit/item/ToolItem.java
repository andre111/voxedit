/*
 * Copyright (c) 2023 André Schweiger
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
package me.andre111.voxedit.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.EditType;
import me.andre111.voxedit.editor.EditStats;
import me.andre111.voxedit.editor.Editor;
import me.andre111.voxedit.tool.ConfiguredTool;
import me.andre111.voxedit.tool.Tool;
import me.andre111.voxedit.tool.config.ToolConfig;
import me.andre111.voxedit.tool.data.ToolTargeting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ToolItem extends Item implements VoxEditItem {
	public ToolItem() {
		super(new Item.Settings().maxCount(1).component(VoxEdit.DATA_COMPONENT, new Data(VoxEdit.DEFAULT_TOOL)));
	}
	
	@Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && world instanceof ServerWorld serverWorld && player.isCreative()) {
			if(use(serverWorld, player, hand, true)) {
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			return TypedActionResult.fail(player.getStackInHand(hand));
		}
		return TypedActionResult.consume(player.getStackInHand(hand));
	}
	
	@Override
	public void leftClicked(World world, PlayerEntity player, Hand hand) {
		if(!world.isClient && world instanceof ServerWorld serverWorld  && player.isCreative()) {
			use(serverWorld, player, hand, false);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <TC extends ToolConfig<TC>, T extends Tool<TC, T>> boolean use(ServerWorld serverWorld, PlayerEntity player, Hand hand, boolean rightClick) {
		Data data = player.getStackInHand(hand).get(VoxEdit.DATA_COMPONENT);
		if(data == null) return false;
		
		ConfiguredTool<TC, T> tool = (ConfiguredTool<TC, T>) data.selected();
		BlockHitResult target = ToolTargeting.getTargetOf(player, tool.config());
		if(target == null) return false;
		
		Set<BlockPos> positions = tool.tool().getBlockPositions(serverWorld, target, tool.config());
		if(positions.isEmpty()) {
			player.sendMessage(Text.translatable("voxedit.feedback.noTarget"), true);
			return false;
		}
		
		EditStats result = EditStats.EMPTY;
		if(rightClick) {
			result = Editor.undoable(player, serverWorld, (editable) -> tool.tool().rightClick(editable, player, target, tool.config(), positions));
		} else {
			result = Editor.undoable(player, serverWorld, (editable) -> tool.tool().leftClick(editable, player, target, tool.config(), positions));
		}
		result.inform(player, EditType.PERFORM);
		
		return true;
	}
	
	@Override
	public ItemStack getDefaultStack() {
		return getStackWith(VoxEdit.DEFAULT_TOOL);
	}
	
	public ItemStack getStackWith(ConfiguredTool<?, ?> tool) {
		ItemStack stack = super.getDefaultStack();
		stack.set(VoxEdit.DATA_COMPONENT, new Data(tool));
		return stack;
	}
	
	public ItemStack getStackWith(Data data) {
		ItemStack stack = super.getDefaultStack();
		stack.set(VoxEdit.DATA_COMPONENT, data);
		return stack;
	}
	
	public static class Data {
		public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(
						ConfiguredTool.CODEC.listOf().fieldOf("tools").forGetter(d -> d.tools),
						Codec.INT.fieldOf("selected").forGetter(d -> d.selected)
				)
				.apply(instance, Data::new));
		
		private List<ConfiguredTool<?, ?>> tools = new ArrayList<>();
		private int selected = 0;
		
		public Data(ConfiguredTool<?, ?> tool) {
			tools.add(tool);
			selected = 0;
		}
		public Data(List<ConfiguredTool<?, ?>> tools, int selected) {
			this.tools = new ArrayList<>(tools);
			this.selected = selected;
		}
		
		public ConfiguredTool<?, ?> selected() {
			return tools.get(getValidIndex());
		}
		
		public int selectedIndex() {
			return getValidIndex();
		}
		
		public int size() {
			return tools.size();
		}
		
		public ConfiguredTool<?, ?> get(int index) {
			return tools.get(index);
		}
		
		public Data replaceSelected(ConfiguredTool<?, ?> newTool) {
			tools.set(getValidIndex(), newTool);
			
			return this;
		}
		
		public Data select(int index) {
			if(index < 0) return this;
			
			if(index < tools.size()) {
				// select tool
				selected = index;
			} else if(index == tools.size()) {
				// create new tool and select
				tools.add(VoxEdit.DEFAULT_TOOL);
				selected = index;
			}
			
			return this;
		}
		
		private int getValidIndex() {
			return selected % tools.size();
		}
	}
}
