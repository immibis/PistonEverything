package cheeseum.pistoneverything;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.world.World;

public class PistonEverything
{
	// FIXME: is it REALLY a good idea to pull these out instead of rolling them in bytecode?
	public static void restoreStoredPistonBlock (World worldObj, int xCoord, int yCoord, int zCoord, int block, int meta, NBTTagCompound tileEntityData)
	{
		worldObj.setBlock(xCoord, yCoord, zCoord, block);
	    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 3);
	    worldObj.notifyBlockOfNeighborChange(xCoord, yCoord, zCoord, block);
	
    	tileEntityData.setInteger("x", xCoord);
    	tileEntityData.setInteger("y", yCoord);
    	tileEntityData.setInteger("z", zCoord);
    	TileEntity te = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord);
    	te.readFromNBT(tileEntityData);
    	te.blockMetadata = meta;
	}

	public static NBTTagCompound getBlockTileEntityData(World worldObj, int x, int y, int z)
	{
		NBTTagCompound teData = null;
		TileEntity te = worldObj.getBlockTileEntity(x, y, z);
		if (te != null) {
			teData = new NBTTagCompound();
			te.writeToNBT(teData);
		}
		
		return teData;
	}
}
