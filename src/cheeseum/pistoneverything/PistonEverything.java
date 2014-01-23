package cheeseum.pistoneverything;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class PistonEverything
{
	private static List<Integer> blockWhitelist = new ArrayList<Integer>();
	
	// TODO: is it REALLY a good idea to pull these out instead of rolling them in bytecode?
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
	
	public static void whitelistBlock (int block, int meta)
	{
		blockWhitelist.add((block << 4) ^ meta);
	}
	
	public static boolean isBlockWhitelisted (int block, int meta)
	{
		return blockWhitelist.contains((block << 4) ^ meta);
	}
	
	// The odd arguments are just to cut down on the number of World methods we have to track in the transformer
	public static boolean isBlockWhitelisted (int block, World world, int x, int y, int z)
	{
		if (world.blockHasTileEntity(x, y, z))
		{
			return isBlockWhitelisted(block, world.getBlockMetadata(x, y, z));
		} 
		
		return true;
	}
}
