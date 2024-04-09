package me.andre111.voxedit.selection;

import java.util.Iterator;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class SelectionSubtract implements Selection {
	private final Selection first;
	private final Selection second;
	private final BlockBox boundingBox;
	
	public SelectionSubtract(Selection first, Selection second) {
		this.first = first;
		this.second = second;
		this.boundingBox = first.getBoundingBox();
	}

	@Override
	public boolean contains(BlockPos pos) {
		if(!boundingBox.contains(pos)) return false;
		return first.contains(pos) && !second.contains(pos);
	}

	@Override
	public BlockBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public Iterator<BlockPos> iterator(Order order) {
		return new Iterator<>() {
			private final Iterator<BlockPos> firstIterator = first.iterator(order);
			
			private BlockPos next;
			
			{
				findNext();
			}

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				BlockPos current = next;
				findNext();
				return current;
			}
			
			private void findNext() {
				next = null;
				while((next == null || second.contains(next)) && firstIterator.hasNext()) next = firstIterator.next();
				if(next != null && second.contains(next)) next = null;
			}
		};
	}
}
