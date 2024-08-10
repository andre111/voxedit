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
package me.andre111.voxedit.data;

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.text.Text;

public interface Configurable<T extends Configurable<T>> {
	public List<Setting<?>> getSettings();
	public Type<T> getType();
	
	public default Text getName() {
		return Text.empty();
	}
	
	public default boolean isValid(Config config) {
		for(var setting : getSettings()) {
    		if(!setting.isValidOrMissing(config)) return false;
    	}
    	return true;
	}
    
    public default boolean has(Setting<?> setting) {
    	for(var s : getSettings()) if(s == setting) return true;
    	return false;
    }
	
	public default Config getDefaultConfig() {
    	Config config = Config.EMPTY;
    	for(var setting : getSettings()) {
    		config = setting.withDefaultValue(config);
    	}
    	return config;
    }
	
	@SuppressWarnings("unchecked")
	public default Configured<T> getDefault() {
		return new Configured<T>((T) this, getDefaultConfig());
	}
	
	public static record Type<T extends Configurable<T>>(Codec<T> baseCodec) {
	}
}
