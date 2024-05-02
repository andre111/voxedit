package me.andre111.voxedit.selection;

import java.util.Iterator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.andre111.voxedit.VoxEdit;
import me.andre111.voxedit.tool.shape.Shape;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SelectionShape extends SelectionBox {
	public static final Codec<SelectionShape> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(
				BlockBox.CODEC.fieldOf("box").forGetter(SelectionShape::getBoundingBox),
				VoxEdit.SHAPE_REGISTRY.getCodec().fieldOf("shape").forGetter(sel -> sel.shape)
			)
			.apply(instance, SelectionShape::new));
	
	private final Shape shape;
	private final BlockPos center;
	private final double sizeX;
	private final double sizeY;
	private final double sizeZ;

	public SelectionShape(BlockBox box, Shape shape) {
		super(box);
		this.shape = shape;
		
		center = box.getCenter();
		sizeX = box.getBlockCountX() / 2.0;
		sizeY = box.getBlockCountY() / 2.0;
		sizeZ = box.getBlockCountZ() / 2.0;
	}

	@Override
	public boolean contains(BlockPos pos) {
		if(!super.contains(pos)) return false;
		
		return shape.contains(pos.getX()-center.getX(), pos.getY()-center.getY(), pos.getZ()-center.getZ(), Direction.UP, sizeX, sizeY, sizeZ);
	}
	
	@Override
	public Iterator<BlockPos> iterator(Order order) {
		final Iterator<BlockPos> baseIterator = super.iterator(order);
		
		return new Iterator<>() {
			private BlockPos next = findNext();
			
			private BlockPos findNext() {
				while(baseIterator.hasNext()) {
					BlockPos pos = baseIterator.next();
					if(contains(pos)) {
						return pos.toImmutable();
					}
				}
				return null;
			}
			
			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public BlockPos next() {
				BlockPos pos = next;
				next = findNext();
				return pos;
			}
		};
	}
	
	@Override
	public SelectionType<?> type() {
		return VoxEdit.SEL_SHAPE;
	}
	
	public Shape getShape() {
		return shape;
	}
}
