package me.andre111.voxedit.selection;

import java.util.Iterator;
import java.util.List;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

public class SelectionAdd implements Selection {
	private final Selection first;
	private final Selection second;
	private final BlockBox boundingBox;
	
	public SelectionAdd(Selection first, Selection second) {
		this.first = first;
		this.second = second;
		this.boundingBox = BlockBox.encompass(List.of(first.getBoundingBox(), second.getBoundingBox())).get();
	}

	@Override
	public boolean contains(BlockPos pos) {
		if(!boundingBox.contains(pos)) return false;
		return first.contains(pos) || second.contains(pos);
	}

	@Override
	public BlockBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public Iterator<BlockPos> iterator(Order order) {
		return new Iterator<>() {
			private final Iterator<BlockPos> firstIterator = first.iterator(order);
			private final Iterator<BlockPos> secondIterator = second.iterator(order);
			
			private BlockPos firstNext;
			private BlockPos secondNext;
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
				while(firstNext == null && firstIterator.hasNext()) firstNext = firstIterator.next();
				while((secondNext == null || first.contains(secondNext)) && secondIterator.hasNext()) secondNext = secondIterator.next();
				
				if(firstNext == null && secondNext == null) {
					next = null;
				} else if(firstNext == null) {
					next = secondNext;
					secondNext = null;
				} else if(secondNext == null) {
					next = firstNext;
					firstNext = null;
				} else {
					if(order.comparator.compare(firstNext, secondNext) <= 0) {
						next = firstNext;
						firstNext = null;
					} else {
						next = secondNext;
						secondNext = null;
					}
				}
			}
		};
	}
}