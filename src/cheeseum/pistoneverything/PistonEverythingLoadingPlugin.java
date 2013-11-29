package cheeseum.pistoneverything;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

//@MCVersion(value = "1.6.4")
public class PistonEverythingLoadingPlugin implements IFMLLoadingPlugin {
	
	public static File jarLocation;
	public static boolean runtimeDeobfuscationEnabled;
	
	@Override
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
		return null;
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
		PistonEverythingTransformerASM.obfMapper.loadMappings("/deobfuscation_data-" + FMLInjectionData.data()[4] + ".lzma");
	}
}
