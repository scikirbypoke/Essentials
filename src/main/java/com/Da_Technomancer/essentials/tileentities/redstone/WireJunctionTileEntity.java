package com.Da_Technomancer.essentials.tileentities.redstone;

import com.Da_Technomancer.essentials.Essentials;
import com.Da_Technomancer.essentials.blocks.redstone.IRedstoneHandler;
import com.Da_Technomancer.essentials.blocks.redstone.RedstoneUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ObjectHolder;

import java.lang.ref.WeakReference;
import java.util.HashSet;

public class WireJunctionTileEntity extends WireTileEntity{

	@ObjectHolder(Essentials.MODID + ":wire_junction")
	private static TileEntityType<WireJunctionTileEntity> TYPE = null;

	public WireJunctionTileEntity(){
		super(TYPE);
	}

	@Override
	protected RedsHandler createRedsHandler(){
		//Use a special handler that routes only in straight lines
		return new JunctionRedsHandler();
	}

	private class JunctionRedsHandler extends RedsHandler{

		@Override
		public float getOutput(){
			return 0;
		}

		@Override
		public void findDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide){
			if(dist + 1 >= RedstoneUtil.getMaxRange()){
				return;
			}
			HashSet<BlockPos> visited = new HashSet<>();
			visited.add(pos);

			Direction dir = fromSide.getOpposite();
			TileEntity neighbor = world.getTileEntity(pos.offset(dir));
			IRedstoneHandler handler;
			if(neighbor != null && (handler = RedstoneUtil.get(neighbor.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, fromSide))) != null){
				if(handler instanceof RedsHandler){
					((RedsHandler) handler).routeDependents(src, dist, fromSide, nominalSide, visited);
				}else{
					handler.findDependents(src, dist, fromSide, nominalSide);
				}
			}
		}

		@Override
		protected void routeDependents(WeakReference<LazyOptional<IRedstoneHandler>> src, int dist, Direction fromSide, Direction nominalSide, HashSet<BlockPos> visited){
			if(!visited.add(pos) || ++dist >= RedstoneUtil.getMaxRange()){
				return;
			}

			Direction dir = fromSide.getOpposite();
			TileEntity neighbor = world.getTileEntity(pos.offset(dir));
			IRedstoneHandler handler;
			if(neighbor != null && (handler = RedstoneUtil.get(neighbor.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, fromSide))) != null){
				if(handler instanceof RedsHandler){
					((RedsHandler) handler).routeDependents(src, dist, fromSide, nominalSide, visited);
				}else{
					handler.findDependents(src, dist, fromSide, nominalSide);
				}
			}
		}

		@Override
		public void requestSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide){
			if(dist + 1 >= RedstoneUtil.getMaxRange()){
				return;
			}
			HashSet<BlockPos> visited = new HashSet<>();
			visited.add(pos);
			Direction dir = toSide.getOpposite();
			TileEntity neighbor = world.getTileEntity(pos.offset(dir));
			IRedstoneHandler handler;
			if(neighbor != null && (handler = RedstoneUtil.get(neighbor.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, toSide))) != null){
				if(handler instanceof RedsHandler){
					((RedsHandler) handler).routeSrc(dependency, dist, toSide, nominalSide, visited);
				}else{
					handler.requestSrc(dependency, dist, toSide, nominalSide);
				}
			}
		}

		@Override
		protected void routeSrc(WeakReference<LazyOptional<IRedstoneHandler>> dependency, int dist, Direction toSide, Direction nominalSide, HashSet<BlockPos> visited){
			if(!visited.add(pos) || ++dist >= RedstoneUtil.getMaxRange()){
				return;
			}

			Direction dir = toSide.getOpposite();
			TileEntity neighbor = world.getTileEntity(pos.offset(dir));
			IRedstoneHandler handler;
			if(neighbor != null && (handler = RedstoneUtil.get(neighbor.getCapability(RedstoneUtil.REDSTONE_CAPABILITY, toSide))) != null){
				if(handler instanceof RedsHandler){
					((RedsHandler) handler).routeSrc(dependency, dist, toSide, nominalSide, visited);
				}else{
					handler.requestSrc(dependency, dist, toSide, nominalSide);
				}
			}
		}
	}
}
