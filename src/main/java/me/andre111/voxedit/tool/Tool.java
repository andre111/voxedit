package me.andre111.voxedit.tool;

import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.editor.UndoRecordingStructureWorldAccess;
import me.andre111.voxedit.tool.config.ToolConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public abstract class Tool<TC extends ToolConfig, T extends Tool<TC, T>> {
	private final Codec<ConfiguredTool<TC, T>> codec;
	private final TC defaultConfig;

    @SuppressWarnings("unchecked")
	public Tool(Codec<TC> configCodec, TC defaultConfig) {
    	this.codec = configCodec.optionalFieldOf("config", defaultConfig).xmap(config -> new ConfiguredTool<TC, T>((T) this, (TC) config), ct -> (TC) ct.config()).codec();
    	this.defaultConfig = defaultConfig;
    }

    public final Codec<ConfiguredTool<TC, T>> getCodec() {
        return this.codec;
    }
    
    public final TC getDefaultConfig() {
    	return defaultConfig;
    }
	
	public final Identifier id() {
		return VoxEdit.TOOL_REGISTRY.getId(this);
	}
	
	public final Text asText() {
		Identifier id = id();
		return Text.translatable("voxedit.tool."+id.toTranslationKey());
	}
    
    public List<TC> getAdditionalCreativeMenuConfigs() {
    	return List.of();
    }

	public abstract void rightClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, TC config, Set<BlockPos> positions);
	public abstract void leftClick(UndoRecordingStructureWorldAccess world, PlayerEntity player, BlockHitResult target, TC config, Set<BlockPos> positions);
	public abstract Set<BlockPos> getBlockPositions(BlockView world, BlockHitResult target, TC config);
}
