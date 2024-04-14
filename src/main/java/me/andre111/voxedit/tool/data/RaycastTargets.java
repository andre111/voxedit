package me.andre111.voxedit.tool.data;

public record RaycastTargets(boolean targetBlocks, boolean targetFluids, boolean targetEntities) {
	public static final RaycastTargets BLOCKS_ONLY = new RaycastTargets(true, false, false);
	public static final RaycastTargets BLOCKS_AND_FLUIDS = new RaycastTargets(true, true, false);
	public static final RaycastTargets ENTITIES_ONLY = new RaycastTargets(false, false, true);
	public static final RaycastTargets BLOCKS_AND_ENTITIES = new RaycastTargets(true, false, true);
}
