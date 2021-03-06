package ec3.common.tile;

import DummyCore.Utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import ec3.api.ApiCore;
import ec3.api.IHotBlock;
import ec3.common.item.ItemsCore;
import ec3.common.mod.EssentialCraftCore;

public class TileUltraHeatGenerator extends TileMRUGeneric {
	
	public int currentBurnTime, currentMaxBurnTime;
	public float heat;
	
	public TileUltraHeatGenerator() {
		super();
		balance = -1;
		maxMRU = (int) ApiCore.GENERATOR_MAX_MRU_GENERIC*10;
		slot0IsBoundGem = false;
		setSlotsNum(2);
	}
	
	public boolean canGenerateMRU() {
		return false;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(balance == -1) {
			balance = worldObj.rand.nextFloat()*2;
		}
		if(!worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) {
			if(currentBurnTime > 0) {
				if(!worldObj.isRemote) {
					float mruGenerated = 20;
					float mruFactor = 1.0F;
					Block[] b = new Block[4];
					b[0] = worldObj.getBlock(xCoord+2, yCoord, zCoord);
					b[1] = worldObj.getBlock(xCoord-2, yCoord, zCoord);
					b[2] = worldObj.getBlock(xCoord, yCoord, zCoord+2);
					b[3] = worldObj.getBlock(xCoord, yCoord, zCoord-2);
					int[] ox = new int[] {2, -2, 0, 0};
					int[] oz = new int[] {0, 0, 2, -2};
					for(int i = 0; i < 4; ++i) {
						if(b[i] == Blocks.air)
							mruFactor*=0;
						else if(b[i] == Blocks.netherrack)
							mruFactor*=0.75F;
						else if(b[i] == Blocks.lava)
							mruFactor*=0.95F;
						else if(b[i] == Blocks.fire)
	            			mruFactor*=0.7F;	
	            		else if(b[i] instanceof IHotBlock)
	            			mruFactor*=(((IHotBlock)b[i]).getHeatModifier(getWorldObj(), xCoord+ox[i], yCoord, zCoord+oz[i]));
	            		else
	            			mruFactor*=0.5F;
						
					}
					
					float scaledHeatFactor = 0F;
					if(heat < 1000) {
						scaledHeatFactor = 0.1F + heat/1000F;
						currentBurnTime -= 2.5F/scaledHeatFactor;
					}
					else if(heat > 10000) {
						scaledHeatFactor = 0.001F + 10000/heat;
						currentBurnTime -= 1F*scaledHeatFactor;
					}
					else {
						scaledHeatFactor = 1F;
						--currentBurnTime;
					}
					heat += (mruFactor*scaledHeatFactor);
					if(heat < 1000)
						mruGenerated = heat/100;
					else if(heat > 10000)
						mruGenerated = 80 + heat/1000;
					else
						mruGenerated = heat/124;
					if(mruGenerated >= 1) {
						setMRU((int) (getMRU()+mruGenerated));
						if(getMRU() > getMaxMRU())
							setMRU(getMaxMRU());
					}
				}
			}
			
			
			if(!worldObj.isRemote) {
				if(getStackInSlot(0) != null) {
					if(currentBurnTime <= 0 && getMRU() < getMaxMRU()) {
						currentMaxBurnTime = currentBurnTime = TileEntityFurnace.getItemBurnTime(getStackInSlot(0));
						
						if(currentBurnTime > 0) {
							if(getStackInSlot(0) != null) {
								if(getStackInSlot(1) == null || getStackInSlot(1).stackSize < getInventoryStackLimit()) {
									if(getStackInSlot(1) != null && getStackInSlot(1).getItem() == ItemsCore.magicalSlag) {
										ItemStack stk = getStackInSlot(1);
										++stk.stackSize;
										setInventorySlotContents(1, stk);
									}
									if(getStackInSlot(1) == null) {
										ItemStack stk = new ItemStack(ItemsCore.magicalSlag,1,0);
										setInventorySlotContents(1, stk);
									}
								}
								if(getStackInSlot(0).stackSize == 0)
									setInventorySlotContents(0, getStackInSlot(0).getItem().getContainerItem(getStackInSlot(0)));
								decrStackSize(0, 1);
							}
						}
					}
				}
			}
			else if(heat > 0) {
				worldObj.spawnParticle("flame", xCoord+0.5F, yCoord+0.5F, zCoord+0.5F, 0, 0.1f, 0);
				for(int i = 0; i < 4; ++i) {
					if(i == 0)
						worldObj.spawnParticle("flame", xCoord+0.05D, yCoord+1.2F, zCoord+0.05D, 0, 0.01f, 0);
					if(i == 1)
						worldObj.spawnParticle("flame", xCoord+0.95D, yCoord+1.2F, zCoord+0.05D, 0, 0.01f, 0);
					if(i == 2)
						worldObj.spawnParticle("flame", xCoord+0.05D, yCoord+1.2F, zCoord+0.95D, 0, 0.01f, 0);
					if(i == 3)
						worldObj.spawnParticle("flame", xCoord+0.95D, yCoord+1.2F, zCoord+0.95D, 0, 0.01f, 0);
				}
			}
		}
		for(int i = 2; i < 6; ++i) {
			ForgeDirection rotation = ForgeDirection.VALID_DIRECTIONS[i];
			float rotXAdv = rotation.offsetX-0.5F;
			float rotZAdv = rotation.offsetZ-0.5F;
			EssentialCraftCore.proxy.FlameFX(xCoord+0.725F+rotXAdv/2.2F, yCoord+0.4F, zCoord+0.725F+rotZAdv/2.2F, 0, 0F, 0, 0.8D, 0.5D, 0.5F, 0.5F);
			EssentialCraftCore.proxy.FlameFX(xCoord+0.5F+MathUtils.randomFloat(worldObj.rand)*0.2F, yCoord+0.65F, zCoord+0.5F+MathUtils.randomFloat(worldObj.rand)*0.2F, 0, 0.01F, 0, 0.8D, 0.5D, 0.5F, 1F);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound i) {
		currentBurnTime = i.getInteger("burn");
		currentMaxBurnTime = i.getInteger("burnMax");
		heat = i.getFloat("heat");
		super.readFromNBT(i);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound i) {
		i.setInteger("burn", currentBurnTime);
		i.setInteger("burnMax", currentMaxBurnTime);
		i.setFloat("heat", heat);
		super.writeToNBT(i);
	}
	
	@Override
	public int[] getOutputSlots() {
		return new int[] {1};
	}
	
	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return p_94041_1_ == 0;
	}
}
