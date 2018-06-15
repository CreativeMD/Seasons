package com.creativemd.seasons;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creativemd.creativecore.utils.LinearGraph;
import com.creativemd.seasons.block.InvisibleLeave;
import com.creativemd.seasons.handler.SeasonBlockHandler;
import com.creativemd.seasons.handler.SeasonEventHandler;
import com.creativemd.seasons.season.Season;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.WorldAccessContainer;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@EventBusSubscriber
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
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		for (Block block : Block.REGISTRY) {
			try{
				if(block.isLeaves(block.getDefaultState(), null, null) && !(block instanceof InvisibleLeave) && !leaves.containsKey(block))
				{
					InvisibleLeave leave = (InvisibleLeave) new InvisibleLeave(block).setUnlocalizedName("invisible" + block.getRegistryName().getResourcePath());
					leaves.put(block, leave);
					event.getRegistry().register(leave.setRegistryName("invisible" + block.getRegistryName().getResourcePath()));
				}
			}catch(Exception e){
				
			}
		}
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		
	}
	
	@Subscribe
	public void postInit(FMLLoadCompleteEvent event)
	{
		
	}

	@Override
	public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info) {
		NBTTagCompound days = new NBTTagCompound();
		for (Integer dimID : Season.worldDays.keySet()) {
			days.setLong("dID" + dimID.intValue(), Season.worldDays.get(dimID).longValue());
		}
		return days;
	}

	@Override
	public void readData(SaveHandler handler, WorldInfo info, Map<String, NBTBase> propertyMap, NBTTagCompound tag) {
		Season.worldDays = new HashMap<>();
		for (String key : tag.getKeySet()) {
			if(key.startsWith("dID"))
				Season.worldDays.put(Integer.parseInt(key.replace("dID", "")), tag.getLong(key));
		}
	}

}
