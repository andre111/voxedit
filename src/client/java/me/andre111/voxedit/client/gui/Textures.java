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
    public static final Identifier SLOT = Identifier.of("container/slot");
    public static final Identifier AIR = Identifier.of("voxedit", "air");
    public static final Identifier TOOL = Identifier.of("voxedit", "tool");
    public static final Identifier EDITOR = Identifier.of("voxedit", "editor");
    public static final Identifier SELECT = Identifier.of("voxedit", "select");

    public static final Identifier EDITOR_CUT = Identifier.of("voxedit", "editor/cut");
    public static final Identifier EDITOR_COPY = Identifier.of("voxedit", "editor/copy");
    public static final Identifier EDITOR_PASTE = Identifier.of("voxedit", "editor/paste");
    public static final Identifier EDITOR_EDIT = Identifier.of("voxedit", "editor/edit");
    public static final Identifier EDITOR_RENAME = Identifier.of("voxedit", "editor/rename");
    public static final Identifier EDITOR_DELETE = Identifier.of("voxedit", "editor/delete");
    
    public static final Identifier NBT_COMPOUND = Identifier.of("voxedit", "editor/compound");
    public static final Identifier NBT_LIST = Identifier.of("voxedit", "editor/list");
    public static final Identifier NBT_ARRAY = Identifier.of("voxedit", "editor/array");
    
    public static final Identifier NBT_BYTE = Identifier.of("voxedit", "editor/byte");
    public static final Identifier NBT_SHORT = Identifier.of("voxedit", "editor/short");
    public static final Identifier NBT_INT = Identifier.of("voxedit", "editor/int");
    public static final Identifier NBT_LONG = Identifier.of("voxedit", "editor/long");
    public static final Identifier NBT_FLOAT = Identifier.of("voxedit", "editor/float");
    public static final Identifier NBT_DOUBLE = Identifier.of("voxedit", "editor/double");
    public static final Identifier NBT_STRING = Identifier.of("voxedit", "editor/string");
    
    public static final Identifier BACKGROUND = Identifier.of("textures/gui/inworld_menu_background.png");
    public static final ButtonTextures BUTTON = new ButtonTextures(Identifier.of("widget/button"), Identifier.of("widget/button_disabled"), Identifier.of("widget/button_highlighted"));

    public static final Identifier MOVE_RIGHT_HIGHLIGHTED = Identifier.of("voxedit", "layout/move_right_highlighted");
    public static final Identifier MOVE_RIGHT = Identifier.of("voxedit", "layout/move_right");
    public static final Identifier MOVE_LEFT_HIGHLIGHTED = Identifier.of("voxedit", "layout/move_left_highlighted");
    public static final Identifier MOVE_LEFT = Identifier.of("voxedit", "layout/move_left");
    public static final Identifier MOVE_UP_HIGHLIGHTED = Identifier.of("voxedit", "layout/move_up_highlighted");
    public static final Identifier MOVE_UP = Identifier.of("voxedit", "layout/move_up");
    public static final Identifier MOVE_DOWN_HIGHLIGHTED = Identifier.of("voxedit", "layout/move_down_highlighted");
    public static final Identifier MOVE_DOWN = Identifier.of("voxedit", "layout/move_down");
    
    public static final ButtonTextures BUTTON_MOVE_RIGHT = new ButtonTextures(MOVE_RIGHT, null, MOVE_RIGHT_HIGHLIGHTED);
    public static final ButtonTextures BUTTON_MOVE_LEFT = new ButtonTextures(MOVE_LEFT, null, MOVE_LEFT_HIGHLIGHTED);
    public static final ButtonTextures BUTTON_MOVE_UP = new ButtonTextures(MOVE_UP, null, MOVE_UP_HIGHLIGHTED);
    public static final ButtonTextures BUTTON_MOVE_DOWN = new ButtonTextures(MOVE_DOWN, null, MOVE_DOWN_HIGHLIGHTED);
}
