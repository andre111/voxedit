package me.andre111.voxedit.client.renderer;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionTypes;

@Environment(value=EnvType.CLIENT)
public class TemplateRenderer {
	private static RenderWorld renderWorld;
	private static StructureTemplate lastTemplate;
	private static boolean rendering;

	public static void render(StructureTemplate template, WorldRenderContext context) {
		if(rendering) return;
		if(template == null) return;
		
		MinecraftClient mc = MinecraftClient.getInstance();
		if(template != lastTemplate) {
			renderWorld = new RenderWorld(mc, new WorldRenderer(mc, mc.getEntityRenderDispatcher(), mc.getBlockEntityRenderDispatcher(), new BufferBuilderStorage(1)), 4, 0);
			StructurePlacementData data = new StructurePlacementData();
			data.setBoundingBox(BlockBox.create(BlockPos.ZERO, BlockPos.ZERO.add(template.getSize())));
			boolean placed = template.place(renderWorld, BlockPos.ORIGIN, BlockPos.ORIGIN, data, renderWorld.getRandom(), Block.FORCE_STATE | Block.SKIP_DROPS | Block.REDRAW_ON_MAIN_THREAD | Block.NOTIFY_ALL_AND_REDRAW);
			System.out.println(template.getSize());
			System.out.println(placed);
			System.out.println(renderWorld.getBlockState(BlockPos.ORIGIN));
			renderWorld.renderer.reload();
			data.getBoundingBox().forEachVertex(bp -> {
				ChunkSectionPos pos = ChunkSectionPos.from(bp);
				renderWorld.renderer.scheduleBlockRender(pos.getSectionX(), pos.getSectionY(), pos.getSectionZ());
			});
			renderWorld.renderer.scheduleBlockRender(0, 0, 0);
			renderWorld.renderer.scheduleTerrainUpdate();
			lastTemplate = template;
		}

		ClientWorld worldBU = mc.world;
		try {
			rendering = true;
			mc.world = renderWorld;
			renderWorld.render(context);
		} finally {
			mc.world = worldBU;
			rendering = false;
		}
	}

	private static final class RenderWorld extends ClientWorld implements ServerWorldAccess {
		private final RenderChunkManager chunkManager;
		private final WorldRenderer renderer;
		
		public RenderWorld(MinecraftClient mc, WorldRenderer renderer, int loadDistance, int simulationDistance) {
			super(
					mc.getNetworkHandler(), 
					new ClientWorld.Properties(Difficulty.NORMAL, false, true), 
					null, 
					mc.world.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE).getEntry(DimensionTypes.OVERWORLD).get(),
					loadDistance, 
					simulationDistance, 
					() -> mc.getProfiler(), 
					renderer, 
					false, 
					0
			);
			
			this.chunkManager = new RenderChunkManager(this, loadDistance);
			this.renderer = renderer;
			this.renderer.setWorld(this);
			this.renderer.reload(mc.getResourceManager());
		}
		
		@Override
	    public ClientChunkManager getChunkManager() {
	        return this.chunkManager;
	    }

		@Override
		public ServerWorld toServerWorld() {
			// TODO Auto-generated method stub
			return null;
		}

		private void render(WorldRenderContext context) {
			MinecraftClient mc = MinecraftClient.getInstance();
			
			//Matrix3f matrix3f = new Matrix3f(context.matrixStack().peek().getNormalMatrix()).invert();
	        //RenderSystem.setInverseViewRotationMatrix(matrix3f);
			
			//renderer.frustum = context.frustum();
			//renderer.render(context.matrixStack(), context.tickDelta(), context.limitTime(), false, context.camera(), context.gameRenderer(), context.lightmapTextureManager(), context.projectionMatrix());
			//System.out.println(renderer.isRenderingReady(BlockPos.ORIGIN));
			
			renderer.onResized(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight());
		}
	}
	private static final class RenderChunkManager extends ClientChunkManager {
		private final RenderWorld world;
		private final Map<ChunkPos, WorldChunk> chunks = new HashMap<>();
		
		public RenderChunkManager(RenderWorld world, int loadDistance) {
			super(world, loadDistance);
			this.world = world;
		}

	    @Nullable
	    public WorldChunk getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
	    	ChunkPos pos = new ChunkPos(i, j);
	    	if(!chunks.containsKey(pos)) chunks.put(pos, new WorldChunk(world, pos));
	    	return chunks.get(pos);
	    }
	}
}
