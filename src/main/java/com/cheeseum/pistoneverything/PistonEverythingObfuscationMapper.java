package com.cheeseum.pistoneverything;

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
	
	private boolean runtimeDeobfuscationEnabled;

	public PistonEverythingObfuscationMapper (boolean enableRuntimeDeobfuscation)
	{
		runtimeDeobfuscationEnabled = enableRuntimeDeobfuscation;
	}
	
	public String getFieldMapping (String mcpName, String srgName)
	{
		if (!runtimeDeobfuscationEnabled)
			return mcpName;
		
		return srgName;
	}

	public MethodData getMethodMapping (String mcpName, String srgName, String desc)
	{
		if (!runtimeDeobfuscationEnabled)
			return new MethodData(mcpName, desc);
		
		return new MethodData(srgName, desc);
	}
}
