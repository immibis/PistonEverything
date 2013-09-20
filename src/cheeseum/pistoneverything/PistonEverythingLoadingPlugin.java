package cheeseum.pistoneverything;

import java.io.File;
import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion(value = "1.6.2")
public class PistonEverythingLoadingPlugin implements IFMLLoadingPlugin {
	
	public static File jarLocation;
	
	@Override
	@Deprecated
	public String[] getLibraryRequestClass() {
		return null;
	}

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{ PistonEverythingTransformer.class.getName() };
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
	}
}
