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
package me.andre111.voxedit.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.util.Identifier;

@Environment(value=EnvType.CLIENT)
public class Textures {
    public static final Identifier SLOT = new Identifier("container/slot");
    public static final Identifier AIR = new Identifier("voxedit", "air");
    public static final Identifier TOOL = new Identifier("voxedit", "tool");
    public static final Identifier EDITOR = new Identifier("voxedit", "editor");
    public static final Identifier SELECT = new Identifier("voxedit", "select");

    public static final Identifier EDITOR_CUT = new Identifier("voxedit", "editor/cut");
    public static final Identifier EDITOR_COPY = new Identifier("voxedit", "editor/copy");
    public static final Identifier EDITOR_PASTE = new Identifier("voxedit", "editor/paste");
    public static final Identifier EDITOR_EDIT = new Identifier("voxedit", "editor/edit");
    public static final Identifier EDITOR_RENAME = new Identifier("voxedit", "editor/rename");
    public static final Identifier EDITOR_DELETE = new Identifier("voxedit", "editor/delete");
    
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
    
    public static final Identifier BACKGROUND = new Identifier("textures/gui/inworld_menu_background.png");
    public static final ButtonTextures BUTTON = new ButtonTextures(new Identifier("widget/button"), new Identifier("widget/button_disabled"), new Identifier("widget/button_highlighted"));

    public static final Identifier MOVE_RIGHT_HIGHLIGHTED = new Identifier("voxedit", "layout/move_right_highlighted");
    public static final Identifier MOVE_RIGHT = new Identifier("voxedit", "layout/move_right");
    public static final Identifier MOVE_LEFT_HIGHLIGHTED = new Identifier("voxedit", "layout/move_left_highlighted");
    public static final Identifier MOVE_LEFT = new Identifier("voxedit", "layout/move_left");
    public static final Identifier MOVE_UP_HIGHLIGHTED = new Identifier("voxedit", "layout/move_up_highlighted");
    public static final Identifier MOVE_UP = new Identifier("voxedit", "layout/move_up");
    public static final Identifier MOVE_DOWN_HIGHLIGHTED = new Identifier("voxedit", "layout/move_down_highlighted");
    public static final Identifier MOVE_DOWN = new Identifier("voxedit", "layout/move_down");
    
    public static final ButtonTextures BUTTON_MOVE_RIGHT = new ButtonTextures(MOVE_RIGHT, null, MOVE_RIGHT_HIGHLIGHTED);
    public static final ButtonTextures BUTTON_MOVE_LEFT = new ButtonTextures(MOVE_LEFT, null, MOVE_LEFT_HIGHLIGHTED);
    public static final ButtonTextures BUTTON_MOVE_UP = new ButtonTextures(MOVE_UP, null, MOVE_UP_HIGHLIGHTED);
    public static final ButtonTextures BUTTON_MOVE_DOWN = new ButtonTextures(MOVE_DOWN, null, MOVE_DOWN_HIGHLIGHTED);
}
