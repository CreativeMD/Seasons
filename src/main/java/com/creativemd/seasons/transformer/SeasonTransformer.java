package com.creativemd.seasons.transformer;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.RETURN;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.creativemd.creativecore.transformer.CreativeTransformer;
import com.creativemd.creativecore.transformer.Transformer;
import com.creativemd.creativecore.transformer.TransformerNames;

import net.minecraft.block.Block;

public class SeasonTransformer extends CreativeTransformer {

	public SeasonTransformer() {
		super("seasons");
	}

	@Override
	protected void initTransformers() {
		new Transformer("net.minecraft.block.BlockSnow") {
			
			@Override
			public void transform(ClassNode node) {
				String targetDesc = TransformerNames.patchDESC("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V");
				String targetMethod = TransformerNames.patchMethodName("updateTick", targetDesc, Block.class);
				MethodNode m = findMethod(node, targetMethod, targetDesc);
				m.instructions.clear();
				m.localVariables.clear();
				
				m.instructions.add(new VarInsnNode(ALOAD, 1));
				m.instructions.add(new VarInsnNode(ALOAD, 2));
				m.instructions.add(new VarInsnNode(ALOAD, 3));
				m.instructions.add(new VarInsnNode(ALOAD, 4));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/seasons/handler/SeasonBlockHandler", "updateSnowTick", targetDesc, false));
				
				
				m.instructions.add(new InsnNode(RETURN));;
			}
		};
		new Transformer("net.minecraft.world.biome.Biome") {
			
			@Override
			public void transform(ClassNode node) {
				String targetDesc = TransformerNames.patchDESC("(Lnet/minecraft/util/math/BlockPos;)F");
				String targetMethod = TransformerNames.patchMethodName("getFloatTemperature", targetDesc, "net.minecraft.world.biome.Biome");
				String newDesc = TransformerNames.patchDESC("(Lnet/minecraft/world/biome/Biome;Lnet/minecraft/util/math/BlockPos;)F");
				MethodNode m = findMethod(node, targetMethod, targetDesc);
				m.instructions.clear();
				m.localVariables.clear();
				
				m.instructions.add(new VarInsnNode(ALOAD, 0));
				m.instructions.add(new VarInsnNode(ALOAD, 1));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/seasons/handler/SeasonBiomeHandler", "getFloatTemperature", newDesc, false));
				
				
				m.instructions.add(new InsnNode(FRETURN));
			}
		};
		new Transformer("net.minecraft.world.WorldServer") {
			
			@Override
			public void transform(ClassNode node) {
				String targetDesc = TransformerNames.patchDESC("()V");
				String targetMethod = TransformerNames.patchMethodName("updateBlocks", targetDesc, "net.minecraft.world.WorldServer");
				String newDesc = TransformerNames.patchDESC("(Lnet/minecraft/world/WorldServer;Lnet/minecraft/server/management/PlayerChunkMap;)V");
				MethodNode m = findMethod(node, targetMethod, targetDesc);
				m.instructions.clear();
				m.localVariables.clear();
				
				m.instructions.add(new VarInsnNode(ALOAD, 0));
				m.instructions.add(new VarInsnNode(ALOAD, 0));
				m.instructions.add(new FieldInsnNode(GETFIELD, TransformerNames.patchClassName("net/minecraft/world/WorldServer"), TransformerNames.patchFieldName("thePlayerManager", "net/minecraft/world/WorldServer"), TransformerNames.patchDESC("Lnet/minecraft/server/management/PlayerChunkMap;")));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/creativemd/seasons/handler/SeasonWorldHandler", "updateBlocks", newDesc, false));
				
				m.instructions.add(new InsnNode(RETURN));
				
				m.maxLocals = 1;
				m.maxStack = 2;
			}
		};
	}

}
