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
package me.andre111.voxedit.client.input;

import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

public class VoxEditKeyBinding {
	private final Identifier id;
    private final String translationKey;
    private final InputUtil.Key defaultKey;
    private final String category;
    private InputUtil.Key boundKey;
    private boolean pressed;
    private int timesPressed;
    
    public VoxEditKeyBinding(Identifier id, InputUtil.Type type, int code, String category) {
    	this.id = id;
        this.translationKey = id.toTranslationKey("voxedit.key");
        this.defaultKey = this.boundKey = type.createFromCode(code);
        this.category = category;
    }
    
    
}
