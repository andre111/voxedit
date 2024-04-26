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
