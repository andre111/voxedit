package me.andre111.voxedit.client.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import me.andre111.voxedit.selection.Order;
import me.andre111.voxedit.selection.Selection;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SelectionRenderer {
	private VertexBuffer lineBuffer = null;
	private VertexBuffer faceBuffer = null;
	
	public void rebuild(Selection selection) {
		BufferBuilder lineBuilder = new BufferBuilder(4096);
		lineBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		BufferBuilder faceBuilder = new BufferBuilder(4096);
        faceBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		
        final float alpha = 0.15f;
        final float lineExpand = 0;
        final float lineSize = 1 + lineExpand * 2;
        final float faceExpand = 0.001f;
        final float faceSize = 1 + faceExpand * 2;
        
		selection.iterator(Order.X_MIN_TO_MAX).forEachRemaining(pos -> {
			pos = pos.toImmutable();
            boolean up = selection.contains(pos.offset(Direction.UP));
            boolean down = selection.contains(pos.offset(Direction.DOWN));
            boolean north = selection.contains(pos.offset(Direction.NORTH));
            boolean east = selection.contains(pos.offset(Direction.EAST));
            boolean south = selection.contains(pos.offset(Direction.SOUTH));
            boolean west = selection.contains(pos.offset(Direction.WEST));

            // lines
            float x = (float) pos.getX()-lineExpand;
            float y = (float) pos.getY()-lineExpand;
            float z = (float) pos.getZ()-lineExpand;
            
            if(!up) {
            	if(!north) {
            		lineBuilder.vertex(x, y+lineSize, z).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f).next();
            		lineBuilder.vertex(x+lineSize, y+lineSize, z).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f).next();
            	}
            	if(!east) {
            		lineBuilder.vertex(x+lineSize, y+lineSize, z).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f).next();
            		lineBuilder.vertex(x+lineSize, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f).next();
            	}
            	if(!south) {
            		lineBuilder.vertex(x, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f).next();
            		lineBuilder.vertex(x+lineSize, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f).next();
            	}
            	if(!west) {
            		lineBuilder.vertex(x, y+lineSize, z).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f).next();
            		lineBuilder.vertex(x, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f).next();
            	}
            }
            if(!down) {
            	if(!north) {
            		lineBuilder.vertex(x, y, z).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f).next();
            		lineBuilder.vertex(x+lineSize, y, z).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f).next();
            	}
            	if(!east) {
            		lineBuilder.vertex(x+lineSize, y, z).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f).next();
            		lineBuilder.vertex(x+lineSize, y, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f).next();
            	}
            	if(!south) {
            		lineBuilder.vertex(x, y, z+lineSize).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f).next();
            		lineBuilder.vertex(x+lineSize, y, z+lineSize).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f).next();
            	}
            	if(!west) {
            		lineBuilder.vertex(x, y, z).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f).next();
            		lineBuilder.vertex(x, y, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f).next();
            	}
            }
            if(!north) {
            	if(!east) {
            		lineBuilder.vertex(x+lineSize, y, z).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f).next();
            		lineBuilder.vertex(x+lineSize, y+lineSize, z).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f).next();
            	}
            	if(!west) {
            		lineBuilder.vertex(x, y, z).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f).next();
            		lineBuilder.vertex(x, y+lineSize, z).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f).next();
            	}
            }
            if(!south) {
            	if(!east) {
            		lineBuilder.vertex(x+lineSize, y, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f).next();
            		lineBuilder.vertex(x+lineSize, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f).next();
            	}
            	if(!west) {
            		lineBuilder.vertex(x, y, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f).next();
            		lineBuilder.vertex(x, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f).next();
            	}
            }
            
            // faces
            x = (float) pos.getX()-faceExpand;
            y = (float) pos.getY()-faceExpand;
            z = (float) pos.getZ()-faceExpand;

            if(!up) {
            	faceBuilder.vertex(x, y+faceSize, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y+faceSize, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y+faceSize, z+faceSize).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x, y+faceSize, z+faceSize).color(1, 1, 1, alpha).next();
            }
            if(!down) {
            	faceBuilder.vertex(x, y, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y, z+faceSize).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x, y, z+faceSize).color(1, 1, 1, alpha).next();
            }
            if(!north) {
            	faceBuilder.vertex(x, y, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y+faceSize, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x, y+faceSize, z).color(1, 1, 1, alpha).next();
            }
            if(!east) {
            	faceBuilder.vertex(x+faceSize, y, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y+faceSize, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y+faceSize, z+faceSize).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y, z+faceSize).color(1, 1, 1, alpha).next();
            }
            if(!south) {
            	faceBuilder.vertex(x, y, z+faceSize).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y, z+faceSize).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x+faceSize, y+faceSize, z+faceSize).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x, y+faceSize, z+faceSize).color(1, 1, 1, alpha).next();
            }
            if(!west) {
            	faceBuilder.vertex(x, y, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x, y+faceSize, z).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x, y+faceSize, z+faceSize).color(1, 1, 1, alpha).next();
            	faceBuilder.vertex(x, y, z+faceSize).color(1, 1, 1, alpha).next();
            }
		});
		
		if(lineBuffer != null) lineBuffer.close();
		lineBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		lineBuffer.bind();
		lineBuffer.upload(lineBuilder.end());
		VertexBuffer.unbind();
		
		if(faceBuffer != null) faceBuffer.close();
		faceBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
		faceBuffer.bind();
		faceBuffer.upload(faceBuilder.end());
		VertexBuffer.unbind();
	}
	
	public void draw(Vec3d cameraPos, Frustum frustum, Matrix4f modelViewMat, Matrix4f projMat, Window window) {
		if(lineBuffer == null || faceBuffer == null) return;
		
		modelViewMat = modelViewMat.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z, new Matrix4f());
        
		RenderLayer.getDebugQuads().startDrawing();
        RenderSystem.enableBlend();
        ShaderProgram shader = RenderSystem.getShader();
        SchematicRenderer.setDefaultUniforms(shader, VertexFormat.DrawMode.QUADS, modelViewMat, projMat, window);
        shader.bind();
        faceBuffer.bind();
        faceBuffer.draw();
        VertexBuffer.unbind();
        shader.unbind();
        RenderLayer.getDebugQuads().endDrawing();
		
		RenderLayer.getLines().startDrawing();
        shader = RenderSystem.getShader();
        SchematicRenderer.setDefaultUniforms(shader, VertexFormat.DrawMode.LINES, modelViewMat, projMat, window);
        shader.bind();
        lineBuffer.bind();
        lineBuffer.draw();
        VertexBuffer.unbind();
        shader.unbind();
        RenderLayer.getLines().endDrawing();
	}
}
