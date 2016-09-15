package com.creativemd.seasons.handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

import com.creativemd.seasons.season.Season;
import com.creativemd.seasons.season.SeasonState;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class SeasonWorldHandler {
	
private static Field updateLCG = null;
    
    private static Field getUpdateLCG()
    {
    	if(updateLCG == null)
    		updateLCG = ReflectionHelper.findField(World.class, "updateLCG");
    	updateLCG.setAccessible(true);
    	return updateLCG;	
    }
	
    private static Method playerCheckLight = null;
    private static Method adjustPosToNearbyEntity = null;
    
	public static void updateBlocks(WorldServer world, PlayerChunkMap playerManager)
    {
		try{
			if(playerCheckLight == null)
				playerCheckLight = ReflectionHelper.findMethod(WorldServer.class, null, new String[]{"playerCheckLight"});
			playerCheckLight.invoke(world);
			
			Field updateLCG = getUpdateLCG();
	
	        if (world.getWorldInfo().getTerrainType() == WorldType.DEBUG_WORLD)
	        {
	            Iterator<Chunk> iterator1 = playerManager.getChunkIterator();
	
	            while (iterator1.hasNext())
	            {
	                ((Chunk)iterator1.next()).onTick(false);
	            }
	        }
	        else
	        {
	            int randomTickSpeed = world.getGameRules().getInt("randomTickSpeed");
	            SeasonState seasonState = Season.getCurrentState(world);
	            randomTickSpeed = seasonState.season.getRandomTickSpeed(seasonState, randomTickSpeed);
	            
	            boolean isRaining = world.isRaining();
	            boolean isThundering = world.isThundering();
	            world.theProfiler.startSection("pollingChunks");
	
	            for (Iterator<Chunk> iterator = world.getPersistentChunkIterable(playerManager.getChunkIterator()); iterator.hasNext(); world.theProfiler.endSection())
	            {
	                world.theProfiler.startSection("getChunk");
	                Chunk chunk = (Chunk)iterator.next();
	                int j = chunk.xPosition * 16;
	                int k = chunk.zPosition * 16;
	                world.theProfiler.endStartSection("checkNextLight");
	                chunk.enqueueRelightChecks();
	                world.theProfiler.endStartSection("tickChunk");
	                chunk.onTick(false);
	                world.theProfiler.endStartSection("thunder");
	
	                if (world.provider.canDoLightning(chunk) && isRaining && isThundering && world.rand.nextInt(100000) == 0)
	                {
	                	
	                	updateLCG.setInt(world, updateLCG.getInt(world) * 3 + 1013904223);
	                    //world.updateLCG = world.updateLCG * 3 + 1013904223;
	                    int l = updateLCG.getInt(world) >> 2;
			            if(adjustPosToNearbyEntity == null)
			            	adjustPosToNearbyEntity = ReflectionHelper.findMethod(WorldServer.class, null, new String[]{"adjustPosToNearbyEntity"}, BlockPos.class);
	                    BlockPos blockpos = (BlockPos) adjustPosToNearbyEntity.invoke(world, new BlockPos(j + (l & 15), 0, k + (l >> 8 & 15)));
	
	                    if (world.isRainingAt(blockpos))
	                    {
	                        DifficultyInstance difficultyinstance = world.getDifficultyForLocation(blockpos);
	
	                        if (world.rand.nextDouble() < (double)difficultyinstance.getAdditionalDifficulty() * 0.05D)
	                        {
	                            EntityHorse entityhorse = new EntityHorse(world);
	                            entityhorse.setType(HorseType.SKELETON);
	                            entityhorse.setSkeletonTrap(true);
	                            entityhorse.setGrowingAge(0);
	                            entityhorse.setPosition((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
	                            world.spawnEntityInWorld(entityhorse);
	                            world.addWeatherEffect(new EntityLightningBolt(world, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), true));
	                        }
	                        else
	                        {
	                            world.addWeatherEffect(new EntityLightningBolt(world, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), false));
	                        }
	                    }
	                }
	
	                world.theProfiler.endStartSection("iceandsnow");
	                
	                Blocks.SNOW.setTickRandomly(true);
	                
	                if (world.provider.canDoRainSnowIce(chunk) && world.rand.nextInt(16) == 0)
	                {
	                    //world.updateLCG = world.updateLCG * 3 + 1013904223;
	                	updateLCG.setInt(world, updateLCG.getInt(world) * 3 + 1013904223);
	                    int j2 = updateLCG.getInt(world) >> 2;
	                    BlockPos blockpos1 = world.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
	                    BlockPos blockpos2 = blockpos1.down();
	                    if (blockpos1.getY() >= 1 && blockpos1.getY() < 256)
	                    {
		                    Biome biome = world.getBiomeGenForCoords(blockpos2);
		                    float temperature = biome.getFloatTemperature(blockpos2);
		                    IBlockState state = world.getBlockState(blockpos1);
		                    IBlockState stateDown = world.getBlockState(blockpos2);
		                    
		                    if(temperature < 0.15F)
		                    {
		                    	//COLD
		                    	
		                    	if (isRaining && canSnowAtPos(world, state, blockpos1))
		                    	{
		                    		int layer = 1;
		                    		if(state.getBlock() == Blocks.SNOW_LAYER)
		                    			layer = state.getValue(BlockSnow.LAYERS)+1;
		                    		increaseSnow(world, blockpos1, layer);
		                    	}
		                    	
		                    	if(canWaterFreeze(world, stateDown, blockpos2, true))
		                    		world.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
		                    }else{
		                    	//NOT COLD
		                    	
		                    	boolean canMelt = false;
		                    	
		                    	if(temperature < 0.75F)
		                    	{
		                    		//WARM
		                    		canMelt = temperature > Math.random();
		                    	}else{
		                    		//HOT
		                    		canMelt = true;
		                    	}
		                    	
		                    	if(canMelt)
		                    	{
		                    		if(stateDown.getBlock() == Blocks.ICE)
			                    		world.setBlockState(blockpos2, Blocks.WATER.getDefaultState());
		                    		else if(stateDown.getBlock() == Blocks.SNOW_LAYER) {
		                    			int layer = stateDown.getValue(BlockSnow.LAYERS);
		                    			if(layer > 1)
		                    				world.setBlockToAir(blockpos2);
		                    				//world.setBlockState(blockpos2, stateDown.withProperty(BlockSnow.LAYERS, layer-1));
		                    			else
		                    				world.setBlockToAir(blockpos2);
		                    		}else if(stateDown.getBlock() == Blocks.SNOW) {
		                    			world.setBlockToAir(blockpos2);
		                    			//world.setBlockState(blockpos2, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 7));
		                    		}
		                    	}
		                    	
		                    }
		                    
		                    if (isRaining && biome.canRain())
		                    	world.getBlockState(blockpos2).getBlock().fillWithRain(world, blockpos2);
	                    }
	                }
	
	                world.theProfiler.endStartSection("tickBlocks");
	
	                if (randomTickSpeed > 0)
	                {
	                    for (ExtendedBlockStorage extendedblockstorage : chunk.getBlockStorageArray())
	                    {
	                        if (extendedblockstorage != Chunk.NULL_BLOCK_STORAGE && extendedblockstorage.getNeedsRandomTick())
	                        {
	                            for (int i1 = 0; i1 < randomTickSpeed; ++i1)
	                            {
	                            	
	                                //world.updateLCG = world.updateLCG * 3 + 1013904223;
	                                //int j1 = world.updateLCG >> 2;
		                            updateLCG.setInt(world, updateLCG.getInt(world) * 3 + 1013904223);
		                            int j1 = updateLCG.getInt(world) >> 2;
	                                int k1 = j1 & 15;
	                                int l1 = j1 >> 8 & 15;
	                                int i2 = j1 >> 16 & 15;
	                                IBlockState iblockstate = extendedblockstorage.get(k1, i2, l1);
	                                BlockPos pos = new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k);
	                                Biome biome = world.getBiomeGenForCoords(pos);
	    		                    float temperature = biome.getFloatTemperature(pos);
	                                Block block = iblockstate.getBlock();
	                                world.theProfiler.startSection("randomTick");
	
	                                if (block.getTickRandomly())
	                                {
	                                    block.randomTick(world, pos, iblockstate, world.rand);
	                                }
	                                if (block == Blocks.SNOW){
	                                	if(temperature > 0.75F)
	                            			world.setBlockToAir(pos);
	                                }
	
	                                world.theProfiler.endSection();
	                            }
	                        }
	                    }
	                }
	            }
	
	            world.theProfiler.endSection();
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
    }
	
	public static boolean canWaterFreeze(WorldServer world, IBlockState iblockstate, BlockPos pos, boolean noWaterAdj)
    {
        Block block = iblockstate.getBlock();
        if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && ((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue() == 0)
        {
            if (!noWaterAdj)
            {
                return true;
            }

            boolean flag = isWater(world, pos.west()) && isWater(world, pos.east()) && isWater(world, pos.north()) && isWater(world, pos.south());

            if (!flag)
            {
                return true;
            }
        }
        return false;
    }
	
	private static boolean isWater(WorldServer world, BlockPos pos)
    {
        return world.getBlockState(pos).getMaterial() == Material.WATER;
    }
	
	public static void increaseSnow(WorldServer world, BlockPos pos, int layer)
	{
		IBlockState state = world.getBlockState(pos);
    	if(state.getBlock() == Blocks.SNOW_LAYER)
    	{
    		if(layer <= state.getValue(BlockSnow.LAYERS))
    			state = null;
    		else{
	    		if(layer >= 8)
	    			state = Blocks.SNOW.getDefaultState();
	    		else
	    			state = state.withProperty(BlockSnow.LAYERS, layer);
    		}
    	}else if(state.getBlock() != Blocks.AIR)
    		return ;
    	else if(layer == 1)
    		state = Blocks.SNOW_LAYER.getDefaultState();
    	else
    		state = null;
    	
    	if(state != null)
    	{
	    	IBlockState below = world.getBlockState(pos.down());
	    	int value = 0;
	    	if(below.getBlock() == Blocks.SNOW_LAYER && below.getValue(BlockSnow.LAYERS) < 8)
	    	{
	    		value = below.getValue(BlockSnow.LAYERS)+1;
	    		if(value >= 8)
	    			below = Blocks.SNOW.getDefaultState();
	    		else
	    			below = below.withProperty(BlockSnow.LAYERS, value);
	    		world.setBlockState(pos.down(), below);
	    	}
	    	
	    	if(world.getBlockState(pos.down()).getBlock() != Blocks.SNOW_LAYER && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos))
	    	{
	    		world.setBlockState(pos, state);
	    		if(layer > 0)
	    		{
	    			increaseSnow(world, pos.north(), layer-1);
	    			increaseSnow(world, pos.east(), layer-1);
	    			increaseSnow(world, pos.south(), layer-1);
	    			increaseSnow(world, pos.west(), layer-1);
	    		}
	    	}
    	}
	}
	
	public static boolean canSnowAtPos(WorldServer world, IBlockState iblockstate, BlockPos pos)
    {
        if ((iblockstate.getBlock().isAir(iblockstate, world, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos)) || iblockstate.getBlock() == Blocks.SNOW_LAYER)
        	return true;
        
        /*if(Math.random() > 0.1)
        {
        	//iblockstate = world.getBlockState(pos.));
            
        	if(iblockstate.getMaterial().isReplaceable())
            	world.setBlockToAir(pos);
        }*/
        return false;
    }
	
	public static void updateWeatherBody(WorldServer world)
    {
        if (!world.provider.getHasNoSky())
        {
            if (!world.isRemote)
            {
                int i = world.getWorldInfo().getCleanWeatherTime();

                if (i > 0)
                {
                    --i;
                    world.getWorldInfo().setCleanWeatherTime(i);
                    world.getWorldInfo().setThunderTime(world.getWorldInfo().isThundering() ? 1 : 2);
                    world.getWorldInfo().setRainTime(world.getWorldInfo().isRaining() ? 1 : 2);
                }

                int j = world.getWorldInfo().getThunderTime();

                if (j <= 0)
                {
                    if (world.getWorldInfo().isThundering())
                    {
                        world.getWorldInfo().setThunderTime(world.rand.nextInt(12000) + 3600);
                    }
                    else
                    {
                        world.getWorldInfo().setThunderTime(world.rand.nextInt(168000) + 12000);
                    }
                }
                else
                {
                    --j;
                    world.getWorldInfo().setThunderTime(j);

                    if (j <= 0)
                    {
                        world.getWorldInfo().setThundering(!world.getWorldInfo().isThundering());
                    }
                }

                world.prevThunderingStrength = world.thunderingStrength;

                if (world.getWorldInfo().isThundering())
                {
                    world.thunderingStrength = (float)((double)world.thunderingStrength + 0.01D);
                }
                else
                {
                    world.thunderingStrength = (float)((double)world.thunderingStrength - 0.01D);
                }

                world.thunderingStrength = MathHelper.clamp_float(world.thunderingStrength, 0.0F, 1.0F);
                int k = world.getWorldInfo().getRainTime();

                if (k <= 0)
                {
                    if (world.getWorldInfo().isRaining())
                    {
                        world.getWorldInfo().setRainTime(world.rand.nextInt(12000) + 12000);
                    }
                    else
                    {
                        world.getWorldInfo().setRainTime(world.rand.nextInt(168000) + 12000);
                    }
                }
                else
                {
                    --k;
                    world.getWorldInfo().setRainTime(k);

                    if (k <= 0)
                    {
                        world.getWorldInfo().setRaining(!world.getWorldInfo().isRaining());
                    }
                }

                world.prevRainingStrength = world.rainingStrength;

                if (world.getWorldInfo().isRaining())
                {
                    world.rainingStrength = (float)((double)world.rainingStrength + 0.01D);
                }
                else
                {
                    world.rainingStrength = (float)((double)world.rainingStrength - 0.01D);
                }

                world.rainingStrength = MathHelper.clamp_float(world.rainingStrength, 0.0F, 1.0F);
            }
        }
    }
	
}
