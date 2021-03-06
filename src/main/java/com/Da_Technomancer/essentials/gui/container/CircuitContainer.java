package com.Da_Technomancer.essentials.gui.container;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public abstract class CircuitContainer extends Container{

	public final BlockPos pos;
	public final String[] inputs = new String[inputBars()];

	protected CircuitContainer(ContainerType<? extends CircuitContainer> type, int id, PlayerInventory playerInventory, PacketBuffer data){
		super(type, id);
		if(data == null){
			pos = null;
		}else{
			pos = data.readBlockPos();
			for(int i = 0; i < inputs.length; i++){
				inputs[i] = data.readString(Short.MAX_VALUE);
			}
		}
	}

	public static PacketBuffer createEmptyBuf(){
		return new PacketBuffer(Unpooled.buffer());
	}

	public static PacketBuffer encodeData(PacketBuffer buf, BlockPos pos, String... inputs){
		buf.writeBlockPos(pos);
		for(String input : inputs){
			buf.writeString(input);
		}
		return buf;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn){
		return pos != null && pos.distanceSq(playerIn.getPositionVec(), true) <= 64;
	}

	public abstract int inputBars();
}
