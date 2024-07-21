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
	
	public static record Type<T extends Configurable<T>>(Codec<T> baseCodec) {
	}
}
