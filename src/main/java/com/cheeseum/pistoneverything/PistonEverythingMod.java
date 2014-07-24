package com.cheeseum.pistoneverything;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

public class PistonEverythingMod extends DummyModContainer {
	private Configuration config;
	private File source;
	
	public PistonEverythingMod () {
		super(new ModMetadata());
		
		ModMetadata md = getMetadata();
		md.modId = "PistonEverything";
		md.name = "Piston Everything";
		md.version = "@VERSION@";
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
	
	@Subscribe
    public void preInit(FMLPreInitializationEvent event) {
		File configFile = event.getSuggestedConfigurationFile();
		this.config = new Configuration(configFile);
        this.config.load();
	}
	
	@Subscribe
	public void postInit(FMLPostInitializationEvent event) {
        String[] defaultWhitelist = {};
        PistonEverything.doWhitelist = config.get(Configuration.CATEGORY_GENERAL, "whitelist", false, "true for whitelist, false for blacklist").getBoolean(true);
        Property whitelist = config.get(Configuration.CATEGORY_GENERAL, "filter", defaultWhitelist, "white/blacklist, one entry per line, format is 'modid:blockid:meta' or just 'modid:blockid' to match any metadata, quotes optional");
        
        if (whitelist.isList()) {
        	for (String entry : whitelist.getStringList()) {
        		entry = entry.replace("\"", "");
        		String entryData[] = entry.split(":");
        		
        		if (entryData.length < 2) {
        			FMLLog.warning("Invalid filter entry %s", entry);
        		} else {
	        		Block block = GameRegistry.findBlock(entryData[0], entryData[1]);
	        		int meta = entryData.length > 2 ? Integer.parseInt(entryData[2]) : -1;
	        		
	        		if (block == null) {
	        			FMLLog.warning("No such block %s:%s found for filter!", entryData[0], entryData[1]);
	        		} else {
	        			PistonEverything.whitelistBlock(block, meta);
	        		}
        		}
        	}
        }
        
        this.config.save();
    }
	
	// TODO: fix assets dir for custom crate texture
	
	/*
	@ForgeSubscribe
	public void texStitchPre(TextureStitchEvent.Pre event) {
	    if (event.map.textureType == 0) {
	        PistonEverything.crateIcon = event.map.registerIcon("pistoneverything:crate");
	    }
	}
	*/
}
