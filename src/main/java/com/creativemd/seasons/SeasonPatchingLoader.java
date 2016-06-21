package com.creativemd.seasons;

import java.io.File;
import java.util.Map;

import com.creativemd.seasons.transformer.SeasonTransformer;
import com.creativemd.seasons.transformer.TransformerNames;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion(value = "1.9.4")
public class SeasonPatchingLoader implements IFMLLoadingPlugin {
	
	public static File location;

	
	@Override
	public String[] getASMTransformerClass() {
		return new String[]{SeasonTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return SeasonDummyContainer.class.getName();
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
		TransformerNames.obfuscated = (boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
