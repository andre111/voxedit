package me.andre111.voxedit.client.renderer;

import java.util.concurrent.CompletableFuture;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import me.andre111.voxedit.state.Schematic;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
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

    public void draw(Vec3i origin, Vec3d cameraPos, Frustum frustum, Matrix4f modelViewMat, Matrix4f projMat, boolean xRay) {
        BlockPos pos = schematicView.pos().add(origin);
        
        if(frustum != null) {
	        Box box = Box.enclosing(pos.subtract(new Vec3i(3, 3, 3)), pos.add(schematicView.schematic().getSizeX() + 3, schematicView.schematic().getSizeY() + 3, schematicView.schematic().getSizeZ() + 3));
	        if (!frustum.isVisible(box)) {
	            return;
	        }
        }
        
        build();
        if (vertexBuffers.isEmpty()) {
            return;
        }
        
        Window window = MinecraftClient.getInstance().getWindow();
        Vector3f viewOffset = new Vector3f((float)(pos.getX() - cameraPos.x), (float)(pos.getY() - cameraPos.y), (float)(pos.getZ() - cameraPos.z));

        float colorMult = 2.5f + (float) Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI) * 0.5f;
        if(xRay) {
        	float xRayAlpha = 0.15f;
            drawLayer(RenderLayer.getTranslucent(), viewOffset, modelViewMat, projMat, window, true, colorMult, xRayAlpha);
            drawLayer(RenderLayer.getTripwire(), viewOffset, modelViewMat, projMat, window, true, colorMult, xRayAlpha);
            drawLayer(RenderLayer.getSolid(), viewOffset, modelViewMat, projMat, window, true, colorMult, xRayAlpha);
            drawLayer(RenderLayer.getCutoutMipped(), viewOffset, modelViewMat, projMat, window, true, colorMult, xRayAlpha);
            drawLayer(RenderLayer.getCutout(), viewOffset, modelViewMat, projMat, window, true, colorMult, xRayAlpha);
        } else {
        	colorMult = 1f;
        }
        
        float alpha = 1f;
        drawLayer(RenderLayer.getTranslucent(), viewOffset, modelViewMat, projMat, window, false, colorMult, alpha);
        drawLayer(RenderLayer.getTripwire(), viewOffset, modelViewMat, projMat, window, false, colorMult, alpha);
        drawLayer(RenderLayer.getSolid(), viewOffset, modelViewMat, projMat, window, false, colorMult, alpha);
        drawLayer(RenderLayer.getCutoutMipped(), viewOffset, modelViewMat, projMat, window, false, colorMult, alpha);
        drawLayer(RenderLayer.getCutout(), viewOffset, modelViewMat, projMat, window, false, colorMult, alpha);
    }

    private void drawLayer(RenderLayer layer, Vector3f viewOffset, Matrix4f modelViewMat, Matrix4f projMat, Window window, boolean disableDepthTest, float colorMult, float alpha) {
        VertexBuffer vertexBuffer = vertexBuffers.get(layer);
        if (vertexBuffer == null) {
            return;
        }
        layer.startDrawing();
        if(alpha < 1f) RenderSystem.enableBlend();
        if(disableDepthTest) RenderSystem.disableDepthTest();
        ShaderProgram shader = RenderSystem.getShader();
        setDefaultUniforms(shader, VertexFormat.DrawMode.QUADS, modelViewMat, projMat, window);
        if(shader.colorModulator != null) shader.colorModulator.set(colorMult, colorMult, colorMult, alpha);
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
	
    public static CompletableFuture<Identifier> getPreview(Schematic schematic, int width, int height) {
    	CompletableFuture<Identifier> future = new CompletableFuture<>();
    	
    	SchematicView view = new SchematicView(new BlockPos(0, 0, 0), schematic);
    	SchematicRenderer renderer = new SchematicRenderer(view);
    	renderer.build();
    	renderer.builderFuture.thenRun(() -> {
    		RenderSystem.recordRenderCall(() -> {
                SimpleFramebuffer framebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
                framebuffer.setClearColor(0, 0, 0, 0);
                framebuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
                framebuffer.beginWrite(true);
                
                Vec3d cameraPos = new Vec3d(-100, 50, -100);
                Matrix4f modelViewMat = new Matrix4f().rotationXYZ((float) Math.toRadians(-45), (float) Math.toRadians(180 + 45), 0);
                modelViewMat.translate((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z);
                Matrix4f projMat = new Matrix4f().perspective((float)(90 * 0.01745329238474369), (float) width / (float) height, 0.05f, 16 * 16 * 4);
                
                float size = (float) Math.max(Math.max(schematic.getSizeX(), schematic.getSizeY()), schematic.getSizeZ()) * 2;
                projMat = new Matrix4f().setOrthoSymmetric(size, size, 0.05f, 16 * 16 * 4);
                renderer.draw(Vec3i.ZERO, cameraPos, null, modelViewMat, projMat, false);

                NativeImage nativeImage = ScreenshotRecorder.takeScreenshot(framebuffer);
                Identifier id = MinecraftClient.getInstance().getTextureManager().registerDynamicTexture("voxedit_schematic_preview_", new NativeImageBackedTexture(nativeImage));
                future.complete(id);
                framebuffer.delete();
    		});
    	});
    	
    	return future;
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
