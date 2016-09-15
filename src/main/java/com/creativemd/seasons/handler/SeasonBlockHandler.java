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
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
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
		//int layers = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
		
		float temperature = worldIn.getBiomeForCoordsBody(pos).getFloatTemperature(pos);
		if(temperature > 0.75F)
			worldIn.setBlockToAir(pos);
    }
	
	public static void updateIceTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        
    }

    protected static void turnIntoWater(World worldIn, BlockPos pos, IBlockState state)
    {
        if (worldIn.provider.doesWaterVaporize())
        {
            worldIn.setBlockToAir(pos);
        }
        else
        {
            state.getBlock().dropBlockAsItem(worldIn, pos, worldIn.getBlockState(pos), 0);
            worldIn.setBlockState(pos, Blocks.WATER.getDefaultState());
            worldIn.notifyBlockOfStateChange(pos, Blocks.WATER);
        }
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
            boolean isFireSource = block.isFireSource(worldIn, pos.down(), EnumFacing.UP);

            int age = ((Integer)state.getValue(BlockFire.AGE)).intValue();

            if (!isFireSource && worldIn.isRaining() && canDie(worldIn, pos) && rand.nextFloat() < 0.2F + (float)age * 0.03F)
            {
                worldIn.setBlockToAir(pos);
            }
            else
            {
                if (age < 15)
                {
                    state = state.withProperty(BlockFire.AGE, Integer.valueOf(age + rand.nextInt(3) / 2));
                    worldIn.setBlockState(pos, state, 4);
                }

                worldIn.scheduleUpdate(pos, Blocks.FIRE, Blocks.FIRE.tickRate(worldIn) + rand.nextInt(10));

                if (!isFireSource)
                {
                    if (!canNeighborCatchFire(worldIn, pos))
                    {
                        if (!worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP) || age > 3)
                        {
                            worldIn.setBlockToAir(pos);
                        }

                        return;
                    }

                    if (!Blocks.FIRE.canCatchFire(worldIn, pos.down(), EnumFacing.UP) && age == 15 && rand.nextInt(4) == 0)
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

                tryCatchFire(worldIn, pos.east(), 300 + j, rand, age, EnumFacing.WEST);
                tryCatchFire(worldIn, pos.west(), 300 + j, rand, age, EnumFacing.EAST);
                tryCatchFire(worldIn, pos.down(), 250 + j, rand, age, EnumFacing.UP);
                tryCatchFire(worldIn, pos.up(), 250 + j, rand, age, EnumFacing.DOWN);
                tryCatchFire(worldIn, pos.north(), 300 + j, rand, age, EnumFacing.SOUTH);
                tryCatchFire(worldIn, pos.south(), 300 + j, rand, age, EnumFacing.NORTH);

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
                                    int l1 = (k1 + 40 + worldIn.getDifficulty().getDifficultyId() * 7) / (age + 30);

                                    if (flag1)
                                    {
                                        l1 /= 2;
                                    }

                                    if (l1 > 0 && rand.nextInt(j1) <= l1 && (!worldIn.isRaining() || !canDie(worldIn, blockpos)))
                                    {
                                        int i2 = age + rand.nextInt(5) / 4;

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
}
