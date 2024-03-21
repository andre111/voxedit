package me.andre111.voxedit.network;

import io.netty.buffer.ByteBuf;
import me.andre111.voxedit.VoxEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public record CPRequestRegistry(int requestID, RegistryKey<? extends Registry<?>> registryKey) implements CustomPayload {
	public static final Id<CPRequestRegistry> ID = new Id<>(VoxEdit.id("request_registry"));
	public static final PacketCodec<ByteBuf, CPRequestRegistry> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, CPRequestRegistry::requestID, RegistryKey.createPacketCodec(RegistryKey.ofRegistry(RegistryKeys.ROOT)), CPRequestRegistry::registryKey, CPRequestRegistry::new);
	static {
		PayloadTypeRegistry.playC2S().register(ID, CODEC);
	}
	
	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

}
