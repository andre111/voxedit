package me.andre111.voxedit.client.renderer;

import me.andre111.voxedit.state.Selection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Environment(value=EnvType.CLIENT)
public class SelectionRenderer {
	public static void render(Selection selection, WorldRenderContext context) {
		if(selection == null) return;
		
		BlockPos min = selection.min();
		BlockPos max = selection.max();
		
		VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        float xMin = (float) (min.getX() - camPos.x);
        float yMin = (float) (min.getY() - camPos.y);
        float zMin = (float) (min.getZ() - camPos.z);
        float xMax = (float) (max.getX()+1 - camPos.x);
        float yMax = (float) (max.getY()+1 - camPos.y);
        float zMax = (float) (max.getZ()+1 - camPos.z);
		
		// outline
		VertexConsumer lineConsumer = consumer.getBuffer(RenderLayer.getLines());
    	MatrixStack.Entry matrix = context.matrixStack().peek();

        lineConsumer.vertex(matrix, xMin, yMin, zMin).color(1, 0, 0, 1f).normal(matrix, 1.0f, 0.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMax, yMin, zMin).color(1, 0, 0, 1f).normal(matrix, 1.0f, 0.0f, 0.0f).next();

        lineConsumer.vertex(matrix, xMin, yMin, zMin).color(0, 1, 0, 1f).normal(matrix, 0.0f, 1.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMin, yMax, zMin).color(0, 1, 0, 1f).normal(matrix, 0.0f, 1.0f, 0.0f).next();

        lineConsumer.vertex(matrix, xMin, yMin, zMin).color(0, 0, 1, 1f).normal(matrix, 1.0f, 0.0f, 1.0f).next();
        lineConsumer.vertex(matrix, xMin, yMin, zMax).color(0, 0, 1, 1f).normal(matrix, 1.0f, 0.0f, 1.0f).next();
        

        lineConsumer.vertex(matrix, xMin, yMax, zMin).color(0, 0, 0, 1f).normal(matrix, 1.0f, 0.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMax, yMax, zMin).color(0, 0, 0, 1f).normal(matrix, 1.0f, 0.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMin, yMin, zMax).color(0, 0, 0, 1f).normal(matrix, 1.0f, 0.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMax, yMin, zMax).color(0, 0, 0, 1f).normal(matrix, 1.0f, 0.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMin, yMax, zMax).color(0, 0, 0, 1f).normal(matrix, 1.0f, 0.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMax, yMax, zMax).color(0, 0, 0, 1f).normal(matrix, 1.0f, 0.0f, 0.0f).next();

        lineConsumer.vertex(matrix, xMax, yMin, zMin).color(0, 0, 0, 1f).normal(matrix, 0.0f, 1.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMax, yMax, zMin).color(0, 0, 0, 1f).normal(matrix, 0.0f, 1.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMin, yMin, zMax).color(0, 0, 0, 1f).normal(matrix, 0.0f, 1.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMin, yMax, zMax).color(0, 0, 0, 1f).normal(matrix, 0.0f, 1.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMax, yMin, zMax).color(0, 0, 0, 1f).normal(matrix, 0.0f, 1.0f, 0.0f).next();
        lineConsumer.vertex(matrix, xMax, yMax, zMax).color(0, 0, 0, 1f).normal(matrix, 0.0f, 1.0f, 0.0f).next();

        lineConsumer.vertex(matrix, xMax, yMin, zMin).color(0, 0, 0, 1f).normal(matrix, 0.0f, 0.0f, 1.0f).next();
        lineConsumer.vertex(matrix, xMax, yMin, zMax).color(0, 0, 0, 1f).normal(matrix, 0.0f, 0.0f, 1.0f).next();
        lineConsumer.vertex(matrix, xMin, yMax, zMin).color(0, 0, 0, 1f).normal(matrix, 0.0f, 0.0f, 1.0f).next();
        lineConsumer.vertex(matrix, xMin, yMax, zMax).color(0, 0, 0, 1f).normal(matrix, 0.0f, 0.0f, 1.0f).next();
        lineConsumer.vertex(matrix, xMax, yMax, zMin).color(0, 0, 0, 1f).normal(matrix, 0.0f, 0.0f, 1.0f).next();
        lineConsumer.vertex(matrix, xMax, yMax, zMax).color(0, 0, 0, 1f).normal(matrix, 0.0f, 0.0f, 1.0f).next();

        consumer.drawCurrentLayer();
        
        //TODO: quads
        float alpha = 0.15f;
        float offset = 0.001f;
        xMin -= offset;
        yMin -= offset;
        zMin -= offset;
        xMax += offset;
        yMax += offset;
        zMax += offset;
        
        VertexConsumer quadConsumer = consumer.getBuffer(RenderLayer.getDebugQuads());

        quadConsumer.vertex(matrix, xMin, yMin, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMin, yMax, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMax, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMin, zMin).color(1, 1, 1, alpha).next();
        quadConsumer.vertex(matrix, xMin, yMin, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMin, yMax, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMax, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMin, zMax).color(1, 1, 1, alpha).next();
        
        quadConsumer.vertex(matrix, xMin, yMin, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMin, yMax, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMin, yMax, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMin, yMin, zMax).color(1, 1, 1, alpha).next();
        quadConsumer.vertex(matrix, xMax, yMin, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMax, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMax, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMin, zMax).color(1, 1, 1, alpha).next();

        quadConsumer.vertex(matrix, xMin, yMin, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMin, yMin, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMin, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMin, zMin).color(1, 1, 1, alpha).next();
        quadConsumer.vertex(matrix, xMin, yMax, zMin).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMin, yMax, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMax, zMax).color(1, 1, 1, alpha).next();
    	quadConsumer.vertex(matrix, xMax, yMax, zMin).color(1, 1, 1, alpha).next();

        consumer.drawCurrentLayer();
	}
}
