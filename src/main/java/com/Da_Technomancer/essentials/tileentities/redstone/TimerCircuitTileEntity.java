package com.Da_Technomancer.essentials.tileentities.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.ESBlocks;
import com.Da_Technomancer.essentials.blocks.redstone.AbstractCircuit;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import com.Da_Technomancer.essentials.gui.container.CircuitContainer;
import com.Da_Technomancer.essentials.gui.container.TimerCircuitContainer;
import com.Da_Technomancer.essentials.packets.INBTReceiver;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nullable;

@ObjectHolder(Essentials.MODID)
public class TimerCircuitTileEntity extends CircuitTileEntity implements INamedContainerProvider, INBTReceiver, ITickableTileEntity{

	@ObjectHolder("timer_circuit")
	private static TileEntityType<TimerCircuitTileEntity> TYPE = null;

	private static final int MIN_PERIOD = 1;
	private static final int MIN_DURATION = 0;

	public int settingPeriod = 4;
	public String settingStrPeriod = "4";
	public int settingDuration = 2;
	public String settingStrDuration = "2";

	private long ticksExisted = 0;

	public TimerCircuitTileEntity(){
		super(TYPE);
	}

	@Override
	protected AbstractCircuit getOwner(){
		return ESBlocks.timerCircuit;
	}

	public int timerOutput(){
		//Divide by RedstoneUtil.DELAY to convert from gameticks to redstone ticks
		if((ticksExisted / RedstoneUtil.DELAY) % settingPeriod < settingDuration){
			return 1;
		}else{
			return 0;
		}
	}

	@Override
	public void tick(){
		ticksExisted++;

		int clockTime = (int) (ticksExisted / RedstoneUtil.DELAY) % settingPeriod;
		if(!world.isRemote && ticksExisted % RedstoneUtil.DELAY == 0 && (clockTime == 0 || clockTime == settingDuration)){
			//Force circuits to recalculate when output changes
			recalculateOutput();
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt){
		super.write(nbt);
		nbt.putInt("setting_p", settingPeriod);
		nbt.putString("setting_s_p", settingStrPeriod);
		nbt.putInt("setting_d", settingDuration);
		nbt.putString("setting_s_d", settingStrDuration);
		nbt.putLong("existed", ticksExisted);
		return nbt;
	}

	@Override
	public CompoundNBT getUpdateTag(){
		CompoundNBT nbt = super.getUpdateTag();
		nbt.putInt("setting_p", settingPeriod);
		nbt.putString("setting_s_p", settingStrPeriod);
		nbt.putInt("setting_d", settingDuration);
		nbt.putString("setting_s_d", settingStrDuration);
		nbt.putLong("existed", ticksExisted);
		return nbt;
	}

	@Override
	public void read(BlockState state, CompoundNBT nbt){
		super.read(state, nbt);
		settingPeriod = nbt.getInt("setting_p");
		settingStrPeriod = nbt.getString("setting_s_p");
		settingDuration = nbt.getInt("setting_d");
		settingStrDuration = nbt.getString("setting_s_d");
		ticksExisted = nbt.getLong("existed");
	}

	@Override
	public ITextComponent getDisplayName(){
		return new TranslationTextComponent("container.timer_circuit");
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInv, PlayerEntity player){
		return new TimerCircuitContainer(id, playerInv, CircuitContainer.encodeData(CircuitContainer.createEmptyBuf(), pos, settingStrPeriod, settingStrDuration));
	}

	@Override
	public void receiveNBT(CompoundNBT nbt, @Nullable ServerPlayerEntity sender){
		settingPeriod = Math.max(MIN_PERIOD, Math.round(nbt.getFloat("value_0")));
		settingStrPeriod = nbt.getString("text_0");
		settingDuration = Math.max(MIN_DURATION, Math.round(nbt.getFloat("value_1")));
		settingStrDuration = nbt.getString("text_1");
		markDirty();
		recalculateOutput();
	}
}
