package com.cheeseum.pistoneverything;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.asm.transformers.deobf.LZMAInputSupplier;
import cpw.mods.fml.relauncher.FMLInjectionData;

public class PistonEverythingObfuscationMapper {
	static class MethodData {
		public MethodData(String name, String desc)
		{
			this.name = name;
			this.desc = desc;
		}
		public String name;
		public String desc;
		
		@Override
		public boolean equals(Object o)
		{
			if (o instanceof MethodData) {
				MethodData d = (MethodData)o;
				return this.name.equals(d.name) && this.desc.equals(d.desc); 
			}
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return this.name.hashCode() + this.desc.hashCode();
		}
	}
	
	private Map<String, String> classMap;
	private Map<String, String> fieldMap;
	private Map<MethodData, MethodData> methodMap;
	private boolean runtimeDeobfuscationEnabled;

	public PistonEverythingObfuscationMapper (boolean enableRuntimeDeobfuscation)
	{
		classMap = new HashMap<String, String>();
		fieldMap = new HashMap<String, String>();
		methodMap = new HashMap<MethodData, MethodData>();
		runtimeDeobfuscationEnabled = enableRuntimeDeobfuscation;
	}
	
	public void loadMappings (String obfDataPath)
	{
        try {
			LZMAInputSupplier zis = new LZMAInputSupplier(FMLInjectionData.class.getResourceAsStream(obfDataPath));
	        InputSupplier<InputStreamReader> srgSupplier = CharStreams.newReaderSupplier(zis,Charsets.UTF_8);
			List<String> srgList = CharStreams.readLines(srgSupplier);
			
			for (String srgMapping : srgList)
			{
				String[] parts = srgMapping.split(" ");
				if (parts[0].equals("CL:"))
				{
					classMap.put(parts[2], parts[1]);
				}
				else if (parts[0].equals("FD:"))
				{
					String obfName = parseMapString(parts[1]);
					String srgName = parseMapString(parts[2]);
					
					fieldMap.put(srgName, obfName);
				}
				else if (parts[0].equals("MD:"))
				{
					String obfName = parseMapString(parts[1]);
					String srgName = parseMapString(parts[3]);
					
					methodMap.put(new MethodData(srgName, parts[4]), new MethodData(obfName, parts[2]));
				}
			}
		} catch (IOException e) {
			FMLLog.severe("An error occurred loading the deobfuscation map data: %s", e.getMessage());
		}
	}

	public String getClassMapping (String mcpName, String srgName)
	{
		if (!runtimeDeobfuscationEnabled)
			return mcpName;
		
		return classMap.containsKey(mcpName) ? classMap.get(mcpName) : mcpName;
	}
	
	public String getFieldMapping (String mcpName, String srgName)
	{
		if (!runtimeDeobfuscationEnabled)
			return mcpName;
		
		return fieldMap.containsKey(srgName) ? fieldMap.get(srgName) : srgName;
	}

	public MethodData getMethodMapping (String mcpName, String srgName, String desc)
	{
		if (!runtimeDeobfuscationEnabled)
			return new MethodData(mcpName, desc);
		
		MethodData md = new MethodData(srgName, desc);
		
		return methodMap.containsKey(md) ? methodMap.get(md) : new MethodData(srgName, desc);
	}
	
	private String parseMapString (String in)
	{
		return in.substring(in.lastIndexOf('/')+1);
	}
}
