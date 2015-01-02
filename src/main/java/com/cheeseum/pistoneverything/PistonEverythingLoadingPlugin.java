package com.cheeseum.pistoneverything;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

//@MCVersion(value = "1.6.4")
@IFMLLoadingPlugin.SortingIndex(1500) // anything >1000 loads after runtime deobfuscation
public class PistonEverythingLoadingPlugin implements IFMLLoadingPlugin {
	
	public static File jarLocation;
	public static boolean runtimeDeobfuscationEnabled;
	
	@Deprecated
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{ PistonEverythingTransformerASM.class.getName() };
	}

	@Override
	public String getModContainerClass() {
		return PistonEverythingMod.class.getName();
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		jarLocation = (File) data.get("coremodLocation");
		runtimeDeobfuscationEnabled = (Boolean)data.get("runtimeDeobfuscationEnabled");
		
		//XXX: initializing this here is far from ideal, but it works
		PistonEverythingTransformerASM.obfMapper = new PistonEverythingObfuscationMapper(runtimeDeobfuscationEnabled);
		PistonEverythingTransformerASM.initClassMappings();
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}
}
