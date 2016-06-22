package com.creativemd.seasons.handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Random;

import com.creativemd.seasons.season.Season;
import com.creativemd.seasons.season.SeasonState;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.HorseType;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class SeasonBlockHandler {
	
	public static void updateSnowTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
		//Blocks.SNOW_LAYER
		int layers = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
		
    }
	
	public static void updateFireTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (worldIn.getGameRules().getBoolean("doFireTick"))
        {
            if (!Blocks.FIRE.canPlaceBlockAt(worldIn, pos))
            {
                worldIn.setBlockToAir(pos);
            }

            Block block = worldIn.getBlockState(pos.down()).getBlock();
            boolean flag = block.isFireSource(worldIn, pos.down(), EnumFacing.UP);

            int i = ((Integer)state.getValue(BlockFire.AGE)).intValue();

            if (!flag && worldIn.isRaining() && canDie(worldIn, pos) && rand.nextFloat() < 0.2F + (float)i * 0.03F)
            {
                worldIn.setBlockToAir(pos);
            }
            else
            {
                if (i < 15)
                {
                    state = state.withProperty(BlockFire.AGE, Integer.valueOf(i + rand.nextInt(3) / 2));
                    worldIn.setBlockState(pos, state, 4);
                }

                worldIn.scheduleUpdate(pos, Blocks.FIRE, Blocks.FIRE.tickRate(worldIn) + rand.nextInt(10));

                if (!flag)
                {
                    if (!canNeighborCatchFire(worldIn, pos))
                    {
                        if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) || i > 3)
                        {
                            worldIn.setBlockToAir(pos);
                        }

                        return;
                    }

                    if (!Blocks.FIRE.canCatchFire(worldIn, pos.down(), EnumFacing.UP) && i == 15 && rand.nextInt(4) == 0)
                    {
                        worldIn.setBlockToAir(pos);
                        return;
                    }
                }

                boolean flag1 = worldIn.isBlockinHighHumidity(pos);
                int j = 0;

                if (flag1)
                {
                    j = -50;
                }

                tryCatchFire(worldIn, pos.east(), 300 + j, rand, i, EnumFacing.WEST);
                tryCatchFire(worldIn, pos.west(), 300 + j, rand, i, EnumFacing.EAST);
                tryCatchFire(worldIn, pos.down(), 250 + j, rand, i, EnumFacing.UP);
                tryCatchFire(worldIn, pos.up(), 250 + j, rand, i, EnumFacing.DOWN);
                tryCatchFire(worldIn, pos.north(), 300 + j, rand, i, EnumFacing.SOUTH);
                tryCatchFire(worldIn, pos.south(), 300 + j, rand, i, EnumFacing.NORTH);

                for (int k = -1; k <= 1; ++k)
                {
                    for (int l = -1; l <= 1; ++l)
                    {
                        for (int i1 = -1; i1 <= 4; ++i1)
                        {
                            if (k != 0 || i1 != 0 || l != 0)
                            {
                                int j1 = 100;

                                if (i1 > 1)
                                {
                                    j1 += (i1 - 1) * 100;
                                }

                                BlockPos blockpos = pos.add(k, i1, l);
                                int k1 = getNeighborEncouragement(worldIn, blockpos);

                                if (k1 > 0)
                                {
                                    int l1 = (k1 + 40 + worldIn.getDifficulty().getDifficultyId() * 7) / (i + 30);

                                    if (flag1)
                                    {
                                        l1 /= 2;
                                    }

                                    if (l1 > 0 && rand.nextInt(j1) <= l1 && (!worldIn.isRaining() || !canDie(worldIn, blockpos)))
                                    {
                                        int i2 = i + rand.nextInt(5) / 4;

                                        if (i2 > 15)
                                        {
                                            i2 = 15;
                                        }

                                        worldIn.setBlockState(blockpos, state.withProperty(BlockFire.AGE, Integer.valueOf(i2)), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
	
	protected static boolean canDie(World worldIn, BlockPos pos)
    {
        return worldIn.isRainingAt(pos) || worldIn.isRainingAt(pos.west()) || worldIn.isRainingAt(pos.east()) || worldIn.isRainingAt(pos.north()) || worldIn.isRainingAt(pos.south());
    }
	
	private static void tryCatchFire(World worldIn, BlockPos pos, int chance, Random random, int age, EnumFacing face)
    {
        int i = worldIn.getBlockState(pos).getBlock().getFlammability(worldIn, pos, face);

        if (random.nextInt(chance) < i)
        {
            IBlockState iblockstate = worldIn.getBlockState(pos);

            if (random.nextInt(age + 10) < 5 && !worldIn.isRainingAt(pos))
            {
                int j = age + random.nextInt(5) / 4;

                if (j > 15)
                {
                    j = 15;
                }

                worldIn.setBlockState(pos, Blocks.FIRE.getDefaultState().withProperty(BlockFire.AGE, Integer.valueOf(j)), 3);
            }
            else
            {
                worldIn.setBlockToAir(pos);
            }

            if (iblockstate.getBlock() == Blocks.TNT)
            {
                Blocks.TNT.onBlockDestroyedByPlayer(worldIn, pos, iblockstate.withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
            }
        }
    }
	
	private static boolean canNeighborCatchFire(World worldIn, BlockPos pos)
    {
        for (EnumFacing enumfacing : EnumFacing.values())
        {
            if (Blocks.FIRE.canCatchFire(worldIn, pos.offset(enumfacing), enumfacing.getOpposite()))
            {
                return true;
            }
        }

        return false;
    }

    private static int getNeighborEncouragement(World worldIn, BlockPos pos)
    {
        if (!worldIn.isAirBlock(pos))
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                i = Math.max(worldIn.getBlockState(pos.offset(enumfacing)).getBlock().getFlammability(worldIn, pos.offset(enumfacing), enumfacing.getOpposite()), i);
            }

            return i;
        }
    }
    
    private static NoiseGeneratorPerlin TEMPERATURE_NOISE = null;
    
    public static NoiseGeneratorPerlin getTemperatureNoise()
    {
    	if(TEMPERATURE_NOISE == null)
    		TEMPERATURE_NOISE = ReflectionHelper.getPrivateValue(Biome.class, null, "TEMPERATURE_NOISE");
    	return TEMPERATURE_NOISE;
    }
    
    public static float getFloatTemperature(Biome biome, BlockPos pos)
    {
    	SeasonState state = Season.getCurrentState();
    	if (pos.getY() > 64)
        {
            float f = (float)(getTemperatureNoise().getValue((double)((float)pos.getX() / 8.0F), (double)((float)pos.getZ() / 8.0F)) * 4.0D);
            return state.season.getTemperature(state, biome.getTemperature() - (f + (float)pos.getY() - 64.0F) * 0.05F / 30.0F, pos, biome);
        }
        else
        {
            return state.season.getTemperature(state, biome.getTemperature(), pos, biome);
        }
    }
    
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
	            SeasonState seasonState = Season.getCurrentState();
	            randomTickSpeed = seasonState.season.getRandomTickSpeed(seasonState, randomTickSpeed);
	            
	            boolean flag = world.isRaining();
	            boolean flag1 = world.isThundering();
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
	
	                if (world.provider.canDoLightning(chunk) && flag && flag1 && world.rand.nextInt(100000) == 0)
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
	
	                if (world.provider.canDoRainSnowIce(chunk) && world.rand.nextInt(16) == 0)
	                {
	                    //world.updateLCG = world.updateLCG * 3 + 1013904223;
	                	updateLCG.setInt(world, updateLCG.getInt(world) * 3 + 1013904223);
	                    int j2 = updateLCG.getInt(world) >> 2;
	                    BlockPos blockpos1 = world.getPrecipitationHeight(new BlockPos(j + (j2 & 15), 0, k + (j2 >> 8 & 15)));
	                    BlockPos blockpos2 = blockpos1.down();
	
	                    if (world.canBlockFreezeNoWater(blockpos2))
	                    {
	                        world.setBlockState(blockpos2, Blocks.ICE.getDefaultState());
	                    }
	
	                    if (flag && canSnowAtBody(world, blockpos1, true))
	                    {
	                    	IBlockState state = world.getBlockState(blockpos1);
	                    	increaseSnow(world, blockpos1, state.getValue(BlockSnow.LAYERS)+1);
	                    }
	
	                    if (flag && world.getBiomeGenForCoords(blockpos2).canRain())
	                    {
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
	                                Block block = iblockstate.getBlock();
	                                world.theProfiler.startSection("randomTick");
	
	                                if (block.getTickRandomly())
	                                {
	                                    block.randomTick(world, new BlockPos(k1 + j, i2 + extendedblockstorage.getYLocation(), l1 + k), iblockstate, world.rand);
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
    	}
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
	    		world.setBlockState(pos, below);
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
	
	public static boolean canSnowAtBody(WorldServer world, BlockPos pos, boolean checkLight)
    {
        Biome biome = world.getBiomeGenForCoords(pos);
        float f = biome.getFloatTemperature(pos);

        if (f > 0.15F)
        {
            return false;
        }
        else if (!checkLight)
        {
            return true;
        }
        else
        {
            if (pos.getY() >= 0 && pos.getY() < 256 && world.getLightFor(EnumSkyBlock.BLOCK, pos) < 10)
            {
                IBlockState iblockstate = world.getBlockState(pos);

                if ((iblockstate.getBlock().isAir(iblockstate, world, pos) && Blocks.SNOW_LAYER.canPlaceBlockAt(world, pos)) || iblockstate.getBlock() == Blocks.SNOW_LAYER)
                {
                    return true;
                }
                
                if(Math.random() > 0.1)
                {
                	//iblockstate = world.getBlockState(pos.));
	                
                	if(iblockstate.getMaterial().isReplaceable())
	                	world.setBlockToAir(pos);
                }
                
            }

            return false;
        }
    }
	
	public void updateWeatherBody()
    {
        if (!this.provider.getHasNoSky())
        {
            if (!this.isRemote)
            {
                int i = this.worldInfo.getCleanWeatherTime();

                if (i > 0)
                {
                    --i;
                    this.worldInfo.setCleanWeatherTime(i);
                    this.worldInfo.setThunderTime(this.worldInfo.isThundering() ? 1 : 2);
                    this.worldInfo.setRainTime(this.worldInfo.isRaining() ? 1 : 2);
                }

                int j = this.worldInfo.getThunderTime();

                if (j <= 0)
                {
                    if (this.worldInfo.isThundering())
                    {
                        this.worldInfo.setThunderTime(this.rand.nextInt(12000) + 3600);
                    }
                    else
                    {
                        this.worldInfo.setThunderTime(this.rand.nextInt(168000) + 12000);
                    }
                }
                else
                {
                    --j;
                    this.worldInfo.setThunderTime(j);

                    if (j <= 0)
                    {
                        this.worldInfo.setThundering(!this.worldInfo.isThundering());
                    }
                }

                this.prevThunderingStrength = this.thunderingStrength;

                if (this.worldInfo.isThundering())
                {
                    this.thunderingStrength = (float)((double)this.thunderingStrength + 0.01D);
                }
                else
                {
                    this.thunderingStrength = (float)((double)this.thunderingStrength - 0.01D);
                }

                this.thunderingStrength = MathHelper.clamp_float(this.thunderingStrength, 0.0F, 1.0F);
                int k = this.worldInfo.getRainTime();

                if (k <= 0)
                {
                    if (this.worldInfo.isRaining())
                    {
                        this.worldInfo.setRainTime(this.rand.nextInt(12000) + 12000);
                    }
                    else
                    {
                        this.worldInfo.setRainTime(this.rand.nextInt(168000) + 12000);
                    }
                }
                else
                {
                    --k;
                    this.worldInfo.setRainTime(k);

                    if (k <= 0)
                    {
                        this.worldInfo.setRaining(!this.worldInfo.isRaining());
                    }
                }

                this.prevRainingStrength = this.rainingStrength;

                if (this.worldInfo.isRaining())
                {
                    this.rainingStrength = (float)((double)this.rainingStrength + 0.01D);
                }
                else
                {
                    this.rainingStrength = (float)((double)this.rainingStrength - 0.01D);
                }

                this.rainingStrength = MathHelper.clamp_float(this.rainingStrength, 0.0F, 1.0F);
            }
        }
    }
}
