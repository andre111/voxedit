package me.andre111.voxedit.state;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.structure.StructureTemplate;

public class ServerState {
	private final Consumer<PacketByteBuf> updateConsumer;
	
	private Selection selection = null;
	private StructureTemplate copyBuffer = null;
	
	public ServerState(Consumer<PacketByteBuf> updateConsumer) {
		this.updateConsumer = updateConsumer;
	}
	
	public final Selection getSelection() {
		return selection;
	}
	public final void setSelection(Selection selection) {
		this.selection = selection;
		
		// send update
		PacketByteBuf buf = PacketByteBufs.create();
		if(selection == null) {
			buf.writeEnumConstant(Command.CLEAR_SELECTION);
		} else {
			buf.writeEnumConstant(Command.SET_SELECTION);
			buf.writeNbt(Selection.CODEC.encodeStart(NbtOps.INSTANCE, selection).result().get());
		}
		updateConsumer.accept(buf);
	}
	
	public final StructureTemplate getCopyBuffer() {
		return copyBuffer;
	}

	public final void setCopyBuffer(StructureTemplate copyBuffer) {
		this.copyBuffer = copyBuffer;
		
		// send update
		PacketByteBuf buf = PacketByteBufs.create();
		if(copyBuffer == null) {
			buf.writeEnumConstant(Command.CLEAR_COPY_BUFFER);
		} else {
			buf.writeEnumConstant(Command.SET_COPY_BUFFER);
			NbtCompound nbt = new NbtCompound();
			copyBuffer.writeNbt(nbt);
			buf.writeNbt(nbt);
		}
		updateConsumer.accept(buf);
	}
	
	public final void read(PacketByteBuf buf) {
		switch(buf.readEnumConstant(Command.class)) {
		case CLEAR_SELECTION:
			selection = null;
			break;
		case SET_SELECTION:
			selection = Selection.CODEC.decode(NbtOps.INSTANCE, buf.readNbt()).result().get().getFirst();
			break;
		case CLEAR_COPY_BUFFER:
			copyBuffer = null;
			break;
		case SET_COPY_BUFFER:
			copyBuffer = new StructureTemplate();
			copyBuffer.readNbt(Registries.BLOCK.getReadOnlyWrapper(), buf.readNbt());
			break;
		default:
			break;
		}
	}

	public static enum Command {
		SET_SELECTION,
		CLEAR_SELECTION,
		SET_COPY_BUFFER,
		CLEAR_COPY_BUFFER;
	}
}
