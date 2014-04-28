package com.cheeseum.pistoneverything;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public final class PistonEverything
{
    protected static class WhitelistData {
		int id;
		int meta;
		
		public WhitelistData (int id, int meta) {
			this.id = id;
			this.meta = meta;
		}
		
		@Override
		public boolean equals (Object o) {
			if (o instanceof WhitelistData) {
				WhitelistData d = (WhitelistData)o;
				return (d.id == this.id && d.meta == this.meta);
			}
			
			return false;
		}
		
		@Override
		public int hashCode () {
			return id + meta;
		}
	}
    
    //public static Icon crateIcon;
	private static List<WhitelistData> blockWhitelist = new ArrayList<WhitelistData>();
    public static boolean doWhitelist;

    public static void storeTileEntity(TileEntityPiston tePiston, NBTTagCompound teData) {
        if (teData != null) {
            Class c = tePiston.getClass();
            try {
                Field storedTileEntityData = c.getDeclaredField("storedTileEntityData");
                storedTileEntityData.set(tePiston, teData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
    // TODO: is it REALLY a good idea to pull these out instead of rolling them in bytecode?
	public static void restoreStoredPistonBlock (World worldObj, int xCoord, int yCoord, int zCoord, int block, int meta, NBTTagCompound tileEntityData)
	{
	    worldObj.setBlock(xCoord, yCoord, zCoord, block);
	    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 3);
	    
	    TileEntity te = TileEntity.createAndLoadEntity(tileEntityData);
	    te.xCoord = xCoord;
	    te.yCoord = yCoord;
	    te.zCoord = zCoord;
	    worldObj.setBlockTileEntity(xCoord, yCoord, zCoord, te);

	    worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, block);
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
		blockWhitelist.add(new WhitelistData(block, meta));
	}
	
	public static boolean isBlockWhitelisted (int block, int meta)
	{
		boolean isWhiteListed = blockWhitelist.contains(new WhitelistData(block, -1)) || blockWhitelist.contains(new WhitelistData(block, meta));
		
		if (doWhitelist) {
		    return isWhiteListed;
		} else { // use as blacklist
		    return !isWhiteListed;
		}
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
	
	public static void renderPistonBlock (RenderBlocks blockRenderer, Block block, int x, int y, int z) {
	    if (block.hasTileEntity(0)) {
	        //blockRenderer.renderBlockUsingTexture(Block.obsidian, x, y, z, crateIcon);
	        blockRenderer.renderBlockAllFaces(Block.obsidian, x, y, z);
	    } else {
	        blockRenderer.renderBlockAllFaces(block, x, y, z);
	    }
	}

}
