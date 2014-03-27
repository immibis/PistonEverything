package cheeseum.pistoneverything;

import java.io.File;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class PistonEverythingMod extends DummyModContainer {
	private Configuration config;
	
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
       
        // initialize the default whitelist with some vanilla blocks
        String[] defaultWhitelist = {
        	"54",
        	"130"
        };
        
        Property whitelist = config.get("filter", "whitelist", defaultWhitelist, "format is 'blockid: meta' or just 'blockid' to match any metadata");
        
        if (whitelist.isList()) {
        	for (String entry : whitelist.getStringList()) {
        		entry = entry.replace("\"", "");
        		String entryData[] = entry.split(":");
        		
        		if (entryData.length > 1) {
        			PistonEverything.whitelistBlock(
        					Integer.parseInt(entryData[0]),
        					Integer.parseInt(entryData[1])
        			);
        		} else if (entryData.length == 1) { // wildcard metadata
        			PistonEverything.whitelistBlock(Integer.parseInt(entryData[0]), -1);
        		}
        	}
        }
        
        this.config.save();
    }
}
