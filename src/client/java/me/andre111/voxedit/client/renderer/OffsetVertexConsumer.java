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

import net.minecraft.client.render.VertexConsumer;

public class OffsetVertexConsumer implements VertexConsumer {
	private final VertexConsumer consumer;
	private int xOffset, yOffset, zOffset;
	
	public OffsetVertexConsumer(VertexConsumer consumer) {
		this.consumer = consumer;
	}
	
	public void setOffset(int x, int y, int z) {
		xOffset = x;
		yOffset = y;
		zOffset = z;
	}

	@Override
	public VertexConsumer vertex(float x, float y, float z) {
		consumer.vertex(x + xOffset, y + yOffset, z + zOffset);
		return this;
	}

	@Override
	public VertexConsumer color(int r, int g, int b, int a) {
		consumer.color(r, g, b, a);
		return this;
	}

	@Override
	public VertexConsumer texture(float u, float v) {
		consumer.texture(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlay(int u, int v) {
		consumer.overlay(u, v);
		return this;
	}

	@Override
	public VertexConsumer light(int u, int v) {
		consumer.light(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		consumer.normal(x, y, z);
		return this;
	}

	/*@Override
	public void next() {
		consumer.next();
	}

	@Override
	public void fixedColor(int r, int g, int b, int a) {
		consumer.fixedColor(r, g, b, a);
	}

	@Override
	public void unfixColor() {
		consumer.unfixColor();
	}*/
}
