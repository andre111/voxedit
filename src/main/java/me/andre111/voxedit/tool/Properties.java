package me.andre111.voxedit.tool;

import java.util.List;

import me.andre111.voxedit.tool.data.ToolSetting;

public record Properties(List<ToolSetting<?>> settings, boolean draggable, boolean showPreview) {
	
	public static Builder of(ToolSetting<?>... settings) {
		return new Builder(List.of(settings));
	}
	
	public static class Builder {
		private List<ToolSetting<?>> settings;
		private boolean draggable = false;
		private boolean showPreview = false;
		
		private Builder(List<ToolSetting<?>> settings) {
			this.settings = settings;
		}
		
		public Builder draggable() {
			draggable = true;
			return this;
		}
		
		public Builder showPreview() {
			showPreview = true;
			return this;
		}
		
		public Properties create() {
			return new Properties(settings, draggable, showPreview);
		}
	}
}
