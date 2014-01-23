package cheeseum.pistoneverything;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod( modid = "PistonEverything", name = "Piston Everything", version = "@VERSION@")
public class PistonEverythingMod {
	public PistonEverythingMod () {
	}
	
	@EventHandler
    public void preInit(FMLPreInitializationEvent event) {
		PistonEverything.whitelistBlock(54, 3);
	}
}
