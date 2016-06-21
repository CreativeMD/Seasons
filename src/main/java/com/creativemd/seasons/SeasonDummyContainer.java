package com.creativemd.seasons;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creativemd.seasons.block.InvisibleLeave;
import com.creativemd.seasons.handler.SeasonBlockHandler;
import com.creativemd.seasons.handler.SeasonEventHandler;
import com.creativemd.seasons.season.Season;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.WorldAccessContainer;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class SeasonDummyContainer  extends DummyModContainer implements WorldAccessContainer {
	
	public static final String modid = "seasons";
	public static final String version = "0.1";
	
	
	public SeasonDummyContainer() {
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = modid;
		meta.name = "Seasons";
		meta.version = version;
		meta.credits = "CreativeMD";
		meta.authorList = Arrays.asList("CreativeMD");
		meta.description = "";
		meta.url = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";
	}
	
	public static HashMap<Block, InvisibleLeave> leaves = new HashMap<>();
	
	public static final Logger logger = LogManager.getLogger(modid);
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}
	
	@Subscribe
	public void init(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(new SeasonEventHandler());
	}
	
	@Subscribe
	public void postInit(FMLLoadCompleteEvent event)
	{
		for (Block block : Block.REGISTRY) {
			try{
				if(block.isLeaves(block.getDefaultState(), null, null))
				{
					InvisibleLeave leave = (InvisibleLeave) new InvisibleLeave(block).setUnlocalizedName("invisible" + block.getRegistryName().getResourcePath());
					leaves.put(block, leave);
					GameRegistry.register(leave.setRegistryName("invisible" + block.getRegistryName().getResourcePath()));
				}
			}catch(Exception e){
				
			}
		}
		
	}

	@Override
	public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("days", Season.currentWorldDays);
		return nbt;
	}

	@Override
	public void readData(SaveHandler handler, WorldInfo info, Map<String, NBTBase> propertyMap, NBTTagCompound tag) {
		Season.currentWorldDays = tag.getInteger("days");
	}

}
