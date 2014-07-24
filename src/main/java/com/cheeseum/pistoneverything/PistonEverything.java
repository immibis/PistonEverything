package com.cheeseum.pistoneverything;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.registry.GameRegistry;

public final class PistonEverything
{
	protected static class WhitelistData {
		Block block;
		int meta;
		
		public WhitelistData (Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}
		
		@Override
		public boolean equals (Object o) {
			if (o instanceof WhitelistData) {
				WhitelistData d = (WhitelistData)o;
				return (d.block.equals(this.block) && d.meta == this.meta);
			}
			
			return false;
		}
		
		@Override
		public int hashCode () {
			return block.hashCode() + meta;
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

                // construct and cache a "dummy" te for rendering
                Field cachedTileEntity = c.getDeclaredField("cachedTileEntity");
                TileEntity cachedTE = TileEntity.createAndLoadEntity(teData);
                cachedTE.setWorldObj(tePiston.getWorldObj());
                cachedTE.blockMetadata = tePiston.getBlockMetadata();
                cachedTE.blockType = tePiston.getStoredBlockID();
                
                cachedTileEntity.set(tePiston, cachedTE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
    // TODO: is it REALLY a good idea to pull these out instead of rolling them in bytecode?
	public static void restoreStoredPistonBlock (World worldObj, int xCoord, int yCoord, int zCoord, Block block, int meta, TileEntityPiston tePiston)
	{
	    NBTTagCompound tileEntityData = null;
	     
	    Class c = tePiston.getClass();
	    try {
	        Field storedTileEntityData = c.getDeclaredField("storedTileEntityData");
	        tileEntityData = (NBTTagCompound) storedTileEntityData.get(tePiston);

	        Field cachedTileEntity = c.getDeclaredField("cachedTileEntity");
	        cachedTileEntity.set(tePiston, null);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    worldObj.setBlock(xCoord, yCoord, zCoord, block);
	    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 3);
	    
	    TileEntity te = TileEntity.createAndLoadEntity(tileEntityData);
	    te.xCoord = xCoord;
	    te.yCoord = yCoord;
	    te.zCoord = zCoord;
	    worldObj.setTileEntity(xCoord, yCoord, zCoord, te);

	    worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, block);
	}

	public static NBTTagCompound getBlockTileEntityData(World worldObj, int x, int y, int z)
	{
		NBTTagCompound teData = null;
		TileEntity te = worldObj.getTileEntity(x, y, z);
		if (te != null) {
			teData = new NBTTagCompound();
			te.writeToNBT(teData);
		}
		
		return teData;
	}
	
	public static void whitelistBlock (Block block, int meta)
	{
		blockWhitelist.add(new WhitelistData(block, meta));
	}
	
	public static boolean isBlockWhitelisted (Block block, int meta)
	{
		boolean isWhiteListed = blockWhitelist.contains(new WhitelistData(block, -1)) || blockWhitelist.contains(new WhitelistData(block, meta));
		
		if (doWhitelist) {
		    return isWhiteListed;
		} else { // use as blacklist
		    return !isWhiteListed;
		}
	}
	
	// The odd arguments are just to cut down on the number of World methods we have to track in the transformer
	public static boolean isBlockWhitelisted (Block block, World world, int x, int y, int z)
	{	
		int meta = world.getBlockMetadata(x, y, z);
		if (world.getBlock(x, y, z).hasTileEntity(meta))
		{
			return isBlockWhitelisted(block, meta);
		} 
		return true;
	}
	
	public static void renderPistonBlock (RenderBlocks blockRenderer, Block block, int x, int y, int z, float parTick, TileEntityPiston te) {
	    // render the block, fallback renders in case something throws a tantrum
	    try {
	        blockRenderer.renderBlockAllFaces(block, x, y, z);
	    } catch (Exception e) {
	        try {
	            blockRenderer.renderStandardBlock(block, x, y, z);
	        } catch (Exception ex) {
	            blockRenderer.renderStandardBlock(Blocks.obsidian, x, y, z);
	        }
	    }
	    
	    TileEntity cachedTE = null;
	    Class c = te.getClass();
	    try {
	        Field cachedTileEntity = c.getDeclaredField("cachedTileEntity");
	        cachedTE = (TileEntity) cachedTileEntity.get(te);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    // try to render the stored tileentity if we have one
	    // done last so block renders use the standard piston tessellator set up
	    if (cachedTE != null) {
            TileEntityRendererDispatcher ter = TileEntityRendererDispatcher.instance;
	        
            if (ter.hasSpecialRenderer(cachedTE)) {
                // found that some TEs like to try and tessellate again 
                Tessellator.instance.draw();
                
    	        // from rendertileentity
                int i = ter.field_147550_f.getLightBrightnessForSkyBlocks(x, y, z, 0);
                int j = i % 65536;
                int k = i / 65536;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                
                // avoid goofy lighting
                RenderHelper.enableStandardItemLighting();
                
                // offset render coords according to piston progress
                double renderX = x - ter.staticPlayerX + te.func_145865_b(parTick); //getOffsetX(parTick);
                double renderY = y - ter.staticPlayerY + te.func_145862_c(parTick); //getOffsetY(parTick);
                double renderZ = z - ter.staticPlayerZ + te.func_145859_d(parTick); //getOffsetZ(parTick);
                
                try {
                    ter.renderTileEntityAt(cachedTE, renderX, renderY, renderZ, 1.0f);
                } catch (Exception e) {
                    // expected that some stuff will fail, do nothing
                }
    	       
                // there's a draw that picks up after where we inject
                Tessellator.instance.startDrawingQuads();
	        }
	    }
	}
}
