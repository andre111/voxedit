/*
 * Copyright (c) 2023 André Schweiger
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
package me.andre111.voxedit.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class Textures {
    public static final Identifier SLOT = new Identifier("container/slot");
    public static final Identifier AIR = new Identifier("voxedit", "air");
    public static final Identifier TOOL = new Identifier("voxedit", "tool");
    public static final Identifier EDITOR = new Identifier("voxedit", "editor");
    
    public static final Identifier NBT_COMPOUND = new Identifier("voxedit", "editor/compound");
    public static final Identifier NBT_LIST = new Identifier("voxedit", "editor/list");
    public static final Identifier NBT_ARRAY = new Identifier("voxedit", "editor/array");
    
    public static final Identifier NBT_BYTE = new Identifier("voxedit", "editor/byte");
    public static final Identifier NBT_SHORT = new Identifier("voxedit", "editor/short");
    public static final Identifier NBT_INT = new Identifier("voxedit", "editor/int");
    public static final Identifier NBT_LONG = new Identifier("voxedit", "editor/long");
    public static final Identifier NBT_FLOAT = new Identifier("voxedit", "editor/float");
    public static final Identifier NBT_DOUBLE = new Identifier("voxedit", "editor/double");
    public static final Identifier NBT_STRING = new Identifier("voxedit", "editor/string");
}
