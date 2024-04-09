package me.andre111.voxedit.client.renderer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.LightingProvider;

public class SchematicLightingProvider extends LightingProvider {
    public static final SchematicLightingProvider INSTANCE = new SchematicLightingProvider();

    private SchematicLightingProvider() {
        super(new ChunkProvider(){
			@Override
			public LightSourceView getChunk(int x, int z) {
				return null;
			}

			@Override
			public BlockView getWorld() {
				return EmptyBlockView.INSTANCE;
			}
        }, false, false);
    }

    @Override
    public ChunkLightingView get(LightType lightType) {
        return lightType == LightType.BLOCK ? ConstantChunkLightingView.ZERO : ConstantChunkLightingView.FULL_BRIGHT;
    }
    
    private record ConstantChunkLightingView(int lightLevel) implements ChunkLightingView {
        public static final ConstantChunkLightingView ZERO = new ConstantChunkLightingView(0);
        public static final ConstantChunkLightingView FULL_BRIGHT = new ConstantChunkLightingView(15);

		@Override
		public void checkBlock(BlockPos blockPos) {
		}

		@Override
		public boolean hasUpdates() {
			return false;
		}

		@Override
		public int doLightUpdates() {
			return 0;
		}

		@Override
		public void setSectionStatus(ChunkSectionPos sectionPos, boolean status) {
		}

		@Override
		public void setColumnEnabled(ChunkPos chunkPos, boolean enabled) {
		}

		@Override
		public void propagateLight(ChunkPos chunkPos) {
		}

		@Override
		public ChunkNibbleArray getLightSection(ChunkSectionPos sectionPos) {
			return null;
		}

		@Override
		public int getLightLevel(BlockPos blockPos) {
			return lightLevel;
		}
    }
}
