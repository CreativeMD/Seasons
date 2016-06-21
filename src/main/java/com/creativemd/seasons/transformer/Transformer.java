package com.creativemd.seasons.transformer;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.ArrayList;
import java.util.Iterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.creativemd.seasons.handler.SeasonBlockHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;

public abstract class Transformer {
	
	public static ArrayList<Transformer> transformers = new ArrayList<>();
	
	static {
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
	}
	
	private String className;
	
	public Transformer(String className) {
		transformers.add(this);
		this.className = TransformerNames.patchClassName(className);
	}
	
	public boolean is(String className)
	{
		return className.equals(this.className);
	}
	
	public abstract void transform(ClassNode node);
	
	public MethodNode findMethod(ClassNode node, String name, String desc)
	{
		Iterator<MethodNode> methods = node.methods.iterator();
		while(methods.hasNext())
		{
			MethodNode m = methods.next();
			if ((m.name.equals(name) && m.desc.equals(desc)))
				return m;
		}
		return null;
	}
	
	public FieldNode findField(ClassNode node, String name)
	{
		Iterator<FieldNode> fields = node.fields.iterator();
		while(fields.hasNext())
		{
			FieldNode f = fields.next();
			if ((f.name.equals(name)))
				return f;
		}
		return null;
	}
	
	public void done()
	{
		transformers.remove(this);
		if(transformers.size() == 0)
			TransformerNames.emptyLists();
	}
	
}
