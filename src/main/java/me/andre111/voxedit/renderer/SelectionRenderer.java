package me.andre111.voxedit.renderer;

import java.util.Set;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SelectionRenderer {
	public static void render(Set<BlockPos> positions, MatrixStack matrices, float frame) {
		VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        float expand = 0.01f;
        float one = 1 + expand * 2;
        
        // render outlines
        VertexConsumer lineConsumer = consumer.getBuffer(RenderLayer.getLines());
    	Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Matrix3f matrix3f = matrices.peek().getNormalMatrix();
        
        for(BlockPos pos : positions) {
            boolean up = positions.contains(pos.offset(Direction.UP));
            boolean down = positions.contains(pos.offset(Direction.DOWN));
            boolean north = positions.contains(pos.offset(Direction.NORTH));
            boolean east = positions.contains(pos.offset(Direction.EAST));
            boolean south = positions.contains(pos.offset(Direction.SOUTH));
            boolean west = positions.contains(pos.offset(Direction.WEST));

            float x = (float) (pos.getX() - camPos.x)-expand;
            float y = (float) (pos.getY() - camPos.y)-expand;
            float z = (float) (pos.getZ() - camPos.z)-expand;
            
            // top face
            if(!up) {
            	if(!north) {
                    lineConsumer.vertex(matrix4f, x, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                    lineConsumer.vertex(matrix4f, x+one, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
            	}
            	if(!east) {
                    lineConsumer.vertex(matrix4f, x+one, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                    lineConsumer.vertex(matrix4f, x+one, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
            	}
            	if(!south) {
                    lineConsumer.vertex(matrix4f, x, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                    lineConsumer.vertex(matrix4f, x+one, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
            	}
            	if(!west) {
                    lineConsumer.vertex(matrix4f, x, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                    lineConsumer.vertex(matrix4f, x, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
            	}
            }
            

            // bottom face
            if(!down) {
            	if(!north) {
                    lineConsumer.vertex(matrix4f, x, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                    lineConsumer.vertex(matrix4f, x+one, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
            	}
            	if(!east) {
                    lineConsumer.vertex(matrix4f, x+one, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                    lineConsumer.vertex(matrix4f, x+one, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
            	}
            	if(!south) {
                    lineConsumer.vertex(matrix4f, x, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
                    lineConsumer.vertex(matrix4f, x+one, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 1.0f, 0.0f, 0.0f).next();
            	}
            	if(!west) {
                    lineConsumer.vertex(matrix4f, x, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
                    lineConsumer.vertex(matrix4f, x, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 0.0f, 1.0f).next();
            	}
            }
            
            // sides
            if(!north) {
            	if(!east) {
                    lineConsumer.vertex(matrix4f, x+one, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                    lineConsumer.vertex(matrix4f, x+one, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
            	}
            	if(!west) {
                    lineConsumer.vertex(matrix4f, x, y, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                    lineConsumer.vertex(matrix4f, x, y+one, z).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
            	}
            }
            if(!south) {
            	if(!east) {
                    lineConsumer.vertex(matrix4f, x+one, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                    lineConsumer.vertex(matrix4f, x+one, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
            	}
            	if(!west) {
                    lineConsumer.vertex(matrix4f, x, y, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
                    lineConsumer.vertex(matrix4f, x, y+one, z+one).color(0, 0, 0, 0.75f).normal(matrix3f, 0.0f, 1.0f, 0.0f).next();
            	}
            }
        }
        consumer.drawCurrentLayer();
        
        float alpha = 0.15f;
        VertexConsumer quadConsumer = consumer.getBuffer(RenderLayer.getDebugQuads());
        for(BlockPos pos : positions) {
            boolean up = positions.contains(pos.offset(Direction.UP));
            boolean down = positions.contains(pos.offset(Direction.DOWN));
            boolean north = positions.contains(pos.offset(Direction.NORTH));
            boolean east = positions.contains(pos.offset(Direction.EAST));
            boolean south = positions.contains(pos.offset(Direction.SOUTH));
            boolean west = positions.contains(pos.offset(Direction.WEST));
            
            float x = (float) (pos.getX() - camPos.x)-expand;
            float y = (float) (pos.getY() - camPos.y)-expand;
            float z = (float) (pos.getZ() - camPos.z)-expand;

            if(!up && y < 0) {
            	quadConsumer.vertex(matrix4f, x, y+one, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y+one, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y+one, z+one).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x, y+one, z+one).color(1, 1, 1, alpha).next();
            }
            if(!down && y > 0) {
            	quadConsumer.vertex(matrix4f, x, y, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y, z+one).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x, y, z+one).color(1, 1, 1, alpha).next();
            }
            if(!north && z > 0) {
            	quadConsumer.vertex(matrix4f, x, y, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y+one, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x, y+one, z).color(1, 1, 1, alpha).next();
            }
            if(!east && x < 0) {
            	quadConsumer.vertex(matrix4f, x+one, y, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y+one, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y+one, z+one).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y, z+one).color(1, 1, 1, alpha).next();
            }
            if(!south && z < 0) {
            	quadConsumer.vertex(matrix4f, x, y, z+one).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y, z+one).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x+one, y+one, z+one).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x, y+one, z+one).color(1, 1, 1, alpha).next();
            }
            if(!west && x > 0) {
            	quadConsumer.vertex(matrix4f, x, y, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x, y+one, z).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x, y+one, z+one).color(1, 1, 1, alpha).next();
            	quadConsumer.vertex(matrix4f, x, y, z+one).color(1, 1, 1, alpha).next();
            }
        }
        consumer.drawCurrentLayer();
	}
}
