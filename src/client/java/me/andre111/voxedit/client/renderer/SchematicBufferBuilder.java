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

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

@Environment(value=EnvType.CLIENT)
public class SchematicBufferBuilder {
    private final BlockRenderManager blockRenderer;
    private final SchematicView schematicView;

    public SchematicBufferBuilder(BlockRenderManager blockRenderDispatcher, SchematicView schematicView) {
        this.blockRenderer = blockRenderDispatcher;
        this.schematicView = schematicView;
    }

    public Results build() {
        Reference2ObjectArrayMap<RenderLayer, BufferBuilder> buffers = new Reference2ObjectArrayMap<RenderLayer, BufferBuilder>();
        MatrixStack matrixStack = new MatrixStack();
        Random random = Random.create();
        for (BlockPos blockPos : schematicView) {
            BlockState blockState = schematicView.getBlockState(blockPos);
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
            	BufferBuilder bufferBuilder = SchematicBufferBuilder.startBuilding(buffers, RenderLayers.getFluidLayer(fluidState));
            	
            	// "fixes" liquid renderer only working in 16x16x16 sections by externally adding the correct offset to the vertices
            	OffsetVertexConsumer offsetConsumer = new OffsetVertexConsumer(bufferBuilder);
            	offsetConsumer.setOffset(blockPos.getX() / 16 * 16, blockPos.getY() / 16 * 16, blockPos.getZ() / 16 * 16);
            	
                blockRenderer.renderFluid(blockPos, schematicView, offsetConsumer, blockState, fluidState);
            }
            if (blockState.getRenderType() == BlockRenderType.INVISIBLE) continue;
            BufferBuilder bufferBuilder = SchematicBufferBuilder.startBuilding(buffers, RenderLayers.getBlockLayer(blockState));
            matrixStack.push();
            matrixStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            blockRenderer.renderBlock(blockState, blockPos, schematicView, matrixStack, bufferBuilder, true, random);
            matrixStack.pop();
        }
        return new Results(buffers);
    }

    private static BufferBuilder startBuilding(Reference2ObjectMap<RenderLayer, BufferBuilder> buffers, RenderLayer layer) {
        return buffers.computeIfAbsent(layer, renderType -> {
            return new BufferBuilder(new BufferAllocator(4096), VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        });
    }

    @Environment(value=EnvType.CLIENT)
    public static class Results implements AutoCloseable {
        private final Reference2ObjectMap<RenderLayer, BufferBuilder> builders;

        public Results(Reference2ObjectMap<RenderLayer, BufferBuilder> builders) {
            this.builders = builders;
        }

        public void uploadTo(Reference2ObjectMap<RenderLayer, VertexBuffer> buffers) {
            for (RenderLayer layer : RenderLayer.getBlockLayers()) {
                VertexBuffer vertexBuffer;
                BuiltBuffer renderedBuffer = this.takeLayer(layer);
                if (renderedBuffer == null) {
                    vertexBuffer = buffers.remove(layer);
                    if (vertexBuffer != null) vertexBuffer.close();
                    continue;
                }
                vertexBuffer = buffers.get(layer);
                if (vertexBuffer == null) {
                    vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
                    buffers.put(layer, vertexBuffer);
                }
                vertexBuffer.bind();
                vertexBuffer.upload(renderedBuffer);
            }
        }

        @Nullable
        public BuiltBuffer takeLayer(RenderLayer layer) {
            BufferBuilder bufferBuilder = (BufferBuilder)this.builders.get(layer);
            return bufferBuilder != null ? bufferBuilder.endNullable() : null;
        }

        @Override
        public void close() {
            //this.builders.values().forEach(BufferBuilder::endNullable);
        }
    }
}
