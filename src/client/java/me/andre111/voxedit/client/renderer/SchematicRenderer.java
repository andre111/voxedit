package me.andre111.voxedit.client.renderer;

import java.util.concurrent.CompletableFuture;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.util.Window;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;

@Environment(value=EnvType.CLIENT)
public class SchematicRenderer implements AutoCloseable {
	private final SchematicView schematicView;

    private final Reference2ObjectMap<RenderLayer, VertexBuffer> vertexBuffers = new Reference2ObjectArrayMap<RenderLayer, VertexBuffer>();
    private CompletableFuture<SchematicBufferBuilder.Results> builderFuture;
	private boolean doRebuild = true;
	
	public SchematicRenderer(SchematicView schematicView) {
		this.schematicView = schematicView;
	}
	
    private void build() {
        if (builderFuture != null) {
            if (builderFuture.isDone()) {
                try (SchematicBufferBuilder.Results results = builderFuture.join();){
                    results.uploadTo(vertexBuffers);
                    VertexBuffer.unbind();
                }
                builderFuture = null;
            }
        } else if (doRebuild) {
        	doRebuild = false;
            SchematicBufferBuilder subGridMeshBuilder = new SchematicBufferBuilder(MinecraftClient.getInstance().getBlockRenderManager(), schematicView);
            builderFuture = CompletableFuture.supplyAsync(subGridMeshBuilder::build, Util.getMainWorkerExecutor());
        }
    }

    public void draw(Vec3i origin, Camera camera, Frustum frustum, Matrix4f modelViewMat, Matrix4f projMat, boolean xRay) {
        BlockPos pos = schematicView.pos().add(origin);
        Box box = Box.enclosing(pos.subtract(new Vec3i(3, 3, 3)), pos.add(schematicView.schematic().getSizeX() + 3, schematicView.schematic().getSizeY() + 3, schematicView.schematic().getSizeZ() + 3));
        
        if (!frustum.isVisible(box)) {
            return;
        }
        
        build();
        if (vertexBuffers.isEmpty()) {
            return;
        }
        
        Window window = MinecraftClient.getInstance().getWindow();
        Vector3f viewOffset = new Vector3f((float)(pos.getX() - camera.getPos().x), (float)(pos.getY() - camera.getPos().y), (float)(pos.getZ() - camera.getPos().z));

        if(xRay) {
        	float xRayAlpha = 0.15f;
            drawLayer(RenderLayer.getTranslucent(), viewOffset, modelViewMat, projMat, window, true, xRayAlpha);
            drawLayer(RenderLayer.getTripwire(), viewOffset, modelViewMat, projMat, window, true, xRayAlpha);
            drawLayer(RenderLayer.getSolid(), viewOffset, modelViewMat, projMat, window, true, xRayAlpha);
            drawLayer(RenderLayer.getCutoutMipped(), viewOffset, modelViewMat, projMat, window, true, xRayAlpha);
            drawLayer(RenderLayer.getCutout(), viewOffset, modelViewMat, projMat, window, true, xRayAlpha);
        }
        
        float alpha = 0.85f + (float) Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI) * 0.15f;
        drawLayer(RenderLayer.getTranslucent(), viewOffset, modelViewMat, projMat, window, false, alpha);
        drawLayer(RenderLayer.getTripwire(), viewOffset, modelViewMat, projMat, window, false, alpha);
        drawLayer(RenderLayer.getSolid(), viewOffset, modelViewMat, projMat, window, false, alpha);
        drawLayer(RenderLayer.getCutoutMipped(), viewOffset, modelViewMat, projMat, window, false, alpha);
        drawLayer(RenderLayer.getCutout(), viewOffset, modelViewMat, projMat, window, false, alpha);
    }

    private void drawLayer(RenderLayer layer, Vector3f viewOffset, Matrix4f modelViewMat, Matrix4f projMat, Window window, boolean disableDepthTest, float alpha) {
        VertexBuffer vertexBuffer = vertexBuffers.get(layer);
        if (vertexBuffer == null) {
            return;
        }
        layer.startDrawing();
        RenderSystem.enableBlend();
        if(disableDepthTest) RenderSystem.disableDepthTest();
        ShaderProgram shader = RenderSystem.getShader();
        setDefaultUniforms(shader, VertexFormat.DrawMode.QUADS, modelViewMat, projMat, window);
        if(shader.colorModulator != null) shader.colorModulator.set(2f, 2f, 2f, alpha);
        shader.chunkOffset.set(viewOffset.x, viewOffset.y, viewOffset.z);
        shader.bind();
        vertexBuffer.bind();
        vertexBuffer.draw();
        VertexBuffer.unbind();
        shader.unbind();
        layer.endDrawing();
    }

    @Override
    public void close() {
        vertexBuffers.values().forEach(VertexBuffer::close);
        vertexBuffers.clear();
        if (builderFuture != null) {
        	builderFuture.thenAcceptAsync(SchematicBufferBuilder.Results::close, runnable -> RenderSystem.recordRenderCall(runnable::run));
        	builderFuture = null;
        }
    }
	
    // This should be part of shaderprogramm class in future minecraft versions if I understand correctly
    private static void setDefaultUniforms(ShaderProgram shader, VertexFormat.DrawMode mode, Matrix4f modelViewMat, Matrix4f projMat, Window window) {
        for (int i = 0; i < 12; ++i) {
            int j = RenderSystem.getShaderTexture(i);
            shader.addSampler("Sampler" + i, j);
        }
        if (shader.modelViewMat != null) {
        	shader.modelViewMat.set(modelViewMat);
        }
        if (shader.projectionMat != null) {
        	shader.projectionMat.set(projMat);
        }
        if (shader.colorModulator != null) {
        	shader.colorModulator.set(RenderSystem.getShaderColor());
        }
        if (shader.glintAlpha != null) {
        	shader.glintAlpha.set(RenderSystem.getShaderGlintAlpha());
        }
        if (shader.fogStart != null) {
        	shader.fogStart.set(RenderSystem.getShaderFogStart());
        }
        if (shader.fogEnd != null) {
        	shader.fogEnd.set(RenderSystem.getShaderFogEnd());
        }
        if (shader.fogColor != null) {
        	shader.fogColor.set(RenderSystem.getShaderFogColor());
        }
        if (shader.fogShape != null) {
        	shader.fogShape.set(RenderSystem.getShaderFogShape().getId());
        }
        if (shader.textureMat != null) {
        	shader.textureMat.set(RenderSystem.getTextureMatrix());
        }
        if (shader.gameTime != null) {
        	shader.gameTime.set(RenderSystem.getShaderGameTime());
        }
        if (shader.screenSize != null) {
        	shader.screenSize.set((float)window.getWidth(), (float)window.getHeight());
        }
        if (shader.lineWidth != null && (mode == VertexFormat.DrawMode.LINES || mode == VertexFormat.DrawMode.LINE_STRIP)) {
        	shader.lineWidth.set(RenderSystem.getShaderLineWidth());
        }
        RenderSystem.setupShaderLights(shader);
    }
}
