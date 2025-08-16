package com.precision.ore.common.prospector;

import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class PacketProspecting {

	public int chunkX;
	public int chunkZ;
	public int playerChunkX;
	public int playerChunkZ;
	public int posX;
	public int posZ;
	public ProspectorMode mode;
	public final String[] data;

	public PacketProspecting(
			int chunkX, int chunkZ, int playerChunkX, int playerChunkZ, int posX, int posZ, ProspectorMode mode) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.playerChunkX = playerChunkX;
		this.playerChunkZ = playerChunkZ;
		this.posX = posX;
		this.posZ = posZ;
		this.mode = mode;
		this.data = new String[mode == ProspectorMode.ORE ? 2 : 3];
	}

	public static PacketProspecting readPacketData(PacketBuffer buffer) {
		PacketProspecting packet = new PacketProspecting(
				buffer.readInt(),
				buffer.readInt(),
				buffer.readInt(),
				buffer.readInt(),
				buffer.readInt(),
				buffer.readInt(),
				ProspectorMode.VALUES[buffer.readInt()]);
		int length = buffer.readByte();
		for (int i = 0; i < length; ++i) {
			packet.data[i] = buffer.readString(255);
		}
		return packet;
	}

	public static PacketProspecting readPacketData(NBTTagCompound nbt) {
		if (nbt.hasKey("buffer")) {
			return PacketProspecting.readPacketData(
					new PacketBuffer(Unpooled.wrappedBuffer(nbt.getByteArray("buffer"))));
		}
		return null;
	}

	public NBTTagCompound writePacketData() {
		NBTTagCompound nbt = new NBTTagCompound();
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		writePacketData(buffer);
		byte[] bytes = buffer.array();
		nbt.setByteArray("buffer", bytes);
		return nbt;
	}

	public void writePacketData(PacketBuffer buffer) {
		buffer.writeInt(chunkX);
		buffer.writeInt(chunkZ);
		buffer.writeInt(playerChunkX);
		buffer.writeInt(playerChunkZ);
		buffer.writeInt(posX);
		buffer.writeInt(posZ);
		buffer.writeInt(mode.ordinal());
		buffer.writeByte(data.length);
		for (String string : data) {
			buffer.writeString(string);
		}
	}

	public void setData(int option, String data) {
		this.data[option] = data;
	}
}
