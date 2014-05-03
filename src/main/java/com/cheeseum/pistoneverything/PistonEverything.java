package com.cheeseum.pistoneverything;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

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
	
	public static void renderPistonBlock (RenderBlocks blockRenderer, Block block, int x, int y, int z, float parTick, TileEntityPiston te) {
	    NBTTagCompound teData = null;
	    
	    Class c = te.getClass();
	    try {
	        Field storedTileEntityData = c.getDeclaredField("storedTileEntityData");
	        teData = (NBTTagCompound) storedTileEntityData.get(te);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    if (teData != null) {
	        Tessellator tessellator = Tessellator.instance;
            TileEntityRenderer ter = TileEntityRenderer.instance;
	        
	        // from rendertileentity
            int i = TileEntityRenderer.instance.worldObj.getLightBrightnessForSkyBlocks(x, y, z, 0);
            int j = i % 65536;
            int k = i / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            
            // offset render coords according to piston progress
            double renderX = x - ter.staticPlayerX + te.getOffsetX(parTick);
            double renderY = y - ter.staticPlayerY + te.getOffsetY(parTick);
            double renderZ = z - ter.staticPlayerZ + te.getOffsetZ(parTick);
            
            // FIXME: cache this te somewhere, recreating it here is probably really slow
	        TileEntity storedTE = TileEntity.createAndLoadEntity(teData);
	        storedTE.worldObj = ter.instance.worldObj;
	        storedTE.blockMetadata = te.getBlockMetadata();
	        storedTE.blockType = Block.blocksList[te.getStoredBlockID()];
	       
	        TileEntityRenderer.instance.renderTileEntityAt(storedTE, renderX, renderY, renderZ, 1.0f);
	       
	        // re-bind the block texture, very important
	        ter.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
	    }
	   
	    // render the rest of the block, fallback renders in case something throws a tantrum
	    try {
	        blockRenderer.renderBlockAllFaces(block, x, y, z);
	    } catch (Exception e) {
	        try {
	            blockRenderer.renderStandardBlock(block, x, y, z);
	        } catch (Exception ex) {
	            blockRenderer.renderStandardBlock(Block.obsidian, x, y, z);
	        }
	    }
	}
}
