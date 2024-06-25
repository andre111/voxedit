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
package me.andre111.voxedit.client.renderer;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import me.andre111.voxedit.selection.Order;
import me.andre111.voxedit.selection.Selection;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SelectionRenderer {
	private VertexBuffer lineBuffer = null;
	private VertexBuffer faceBuffer = null;
	
	public void rebuild(Selection selection) {
		if(selection == null) {
			if(lineBuffer != null) lineBuffer.close();
			lineBuffer = null;
			if(faceBuffer != null) faceBuffer.close();
			faceBuffer = null;
			return;
		}
		
		BufferBuilder lineBuilder = new BufferBuilder(new BufferAllocator(4096), VertexFormat.DrawMode.LINES, VertexFormats.LINES);
		BufferBuilder faceBuilder = new BufferBuilder(new BufferAllocator(4096), VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		
        final float alpha = 1f;
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
            		lineBuilder.vertex(x, y+lineSize, z).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f);
            		lineBuilder.vertex(x+lineSize, y+lineSize, z).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f);
            	}
            	if(!east) {
            		lineBuilder.vertex(x+lineSize, y+lineSize, z).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f);
            		lineBuilder.vertex(x+lineSize, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f);
            	}
            	if(!south) {
            		lineBuilder.vertex(x, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f);
            		lineBuilder.vertex(x+lineSize, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f);
            	}
            	if(!west) {
            		lineBuilder.vertex(x, y+lineSize, z).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f);
            		lineBuilder.vertex(x, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f);
            	}
            }
            if(!down) {
            	if(!north) {
            		lineBuilder.vertex(x, y, z).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f);
            		lineBuilder.vertex(x+lineSize, y, z).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f);
            	}
            	if(!east) {
            		lineBuilder.vertex(x+lineSize, y, z).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f);
            		lineBuilder.vertex(x+lineSize, y, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f);
            	}
            	if(!south) {
            		lineBuilder.vertex(x, y, z+lineSize).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f);
            		lineBuilder.vertex(x+lineSize, y, z+lineSize).color(0, 0, 0, 0.75f).normal(1.0f, 0.0f, 0.0f);
            	}
            	if(!west) {
            		lineBuilder.vertex(x, y, z).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f);
            		lineBuilder.vertex(x, y, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 0.0f, 1.0f);
            	}
            }
            if(!north) {
            	if(!east) {
            		lineBuilder.vertex(x+lineSize, y, z).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f);
            		lineBuilder.vertex(x+lineSize, y+lineSize, z).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f);
            	}
            	if(!west) {
            		lineBuilder.vertex(x, y, z).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f);
            		lineBuilder.vertex(x, y+lineSize, z).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f);
            	}
            }
            if(!south) {
            	if(!east) {
            		lineBuilder.vertex(x+lineSize, y, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f);
            		lineBuilder.vertex(x+lineSize, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f);
            	}
            	if(!west) {
            		lineBuilder.vertex(x, y, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f);
            		lineBuilder.vertex(x, y+lineSize, z+lineSize).color(0, 0, 0, 0.75f).normal(0.0f, 1.0f, 0.0f);
            	}
            }
            
            // faces
            x = (float) pos.getX()-faceExpand;
            y = (float) pos.getY()-faceExpand;
            z = (float) pos.getZ()-faceExpand;

            if(!up) {
            	faceBuilder.vertex(x, y+faceSize, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y+faceSize, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y+faceSize, z+faceSize).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x, y+faceSize, z+faceSize).color(1, 1, 1, alpha);
            }
            if(!down) {
            	faceBuilder.vertex(x, y, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y, z+faceSize).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x, y, z+faceSize).color(1, 1, 1, alpha);
            }
            if(!north) {
            	faceBuilder.vertex(x, y, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y+faceSize, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x, y+faceSize, z).color(1, 1, 1, alpha);
            }
            if(!east) {
            	faceBuilder.vertex(x+faceSize, y, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y+faceSize, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y+faceSize, z+faceSize).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y, z+faceSize).color(1, 1, 1, alpha);
            }
            if(!south) {
            	faceBuilder.vertex(x, y, z+faceSize).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y, z+faceSize).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x+faceSize, y+faceSize, z+faceSize).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x, y+faceSize, z+faceSize).color(1, 1, 1, alpha);
            }
            if(!west) {
            	faceBuilder.vertex(x, y, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x, y+faceSize, z).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x, y+faceSize, z+faceSize).color(1, 1, 1, alpha);
            	faceBuilder.vertex(x, y, z+faceSize).color(1, 1, 1, alpha);
            }
		});
		
		if(lineBuffer != null) lineBuffer.close();
		BuiltBuffer lineBB = lineBuilder.endNullable();
		if(lineBB != null) {
			lineBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
			lineBuffer.bind();
			lineBuffer.upload(lineBB);
			VertexBuffer.unbind();
		} else {
			lineBuffer = null;
		}
		
		if(faceBuffer != null) faceBuffer.close();
		BuiltBuffer faceBB = faceBuilder.endNullable();
		if(faceBB != null) {
			faceBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
			faceBuffer.bind();
			faceBuffer.upload(faceBB);
			VertexBuffer.unbind();
		} else {
			faceBuffer = null;
		}
	}
	
	public void draw(float r, float g, float b, Vec3d cameraPos, Frustum frustum, Matrix4f modelViewMat, Matrix4f projMat, Window window) {
		if(lineBuffer == null && faceBuffer == null) return;
		
		modelViewMat = modelViewMat.translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z, new Matrix4f());

        final float alpha = 0.15f;
        
        if(faceBuffer != null) {
			RenderLayer.getDebugQuads().startDrawing();
	        RenderSystem.enableBlend();
	        RenderSystem.depthMask(false);
	        ShaderProgram shader = RenderSystem.getShader();
	        SchematicRenderer.setDefaultUniforms(shader, VertexFormat.DrawMode.QUADS, modelViewMat, projMat, window);
	        if(shader.colorModulator != null) shader.colorModulator.set(r, g, b, alpha);
	        shader.bind();
	        faceBuffer.bind();
	        faceBuffer.draw();
	        VertexBuffer.unbind();
	        shader.unbind();
	        RenderSystem.depthMask(true);
	        RenderLayer.getDebugQuads().endDrawing();
        }
		
        if(lineBuffer != null) {
			RenderLayer.getLines().startDrawing();
			ShaderProgram shader = RenderSystem.getShader();
	        SchematicRenderer.setDefaultUniforms(shader, VertexFormat.DrawMode.LINES, modelViewMat, projMat, window);
	        shader.bind();
	        lineBuffer.bind();
	        lineBuffer.draw();
	        VertexBuffer.unbind();
	        shader.unbind();
	        RenderLayer.getLines().endDrawing();
        }
	}
}
