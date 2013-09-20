package cheeseum.pistoneverything;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;

import com.google.common.io.ByteStreams;

import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public class PistonEverythingTransformer implements IClassTransformer
{
	private FMLDeobfuscatingRemapper remapper = FMLDeobfuscatingRemapper.INSTANCE;
	
	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2)
	{
		String className = remapper.unmap(arg0);
		String replacePath = String.format("patchedclass/%s.class", className);
		try
		{
			InputStream is = this.getClass().getClassLoader().getResourceAsStream(replacePath);
			if (is == null)	{
				if (PistonEverythingLoadingPlugin.jarLocation != null) {
					JarFile jar = new JarFile(PistonEverythingLoadingPlugin.jarLocation);
					ZipEntry entry = jar.getEntry(replacePath);
					if (entry != null)
					{
						is = jar.getInputStream(entry);
						jar.close();
					}
				}
			}
			if (is != null) { 
				System.out.println(String.format("Replacing %s.class with modified version...", className));
				return ByteStreams.toByteArray(is);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return arg2;
	}
}
