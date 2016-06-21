package com.creativemd.seasons.transformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class SeasonTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		if(name.contains("com.creativemd.seasons.transformer"))
			return basicClass;
		return transform(transformedName, basicClass);		
	}
	
	public byte[] transform(String name, byte[] basicClass)
	{
		int i = 0;
		while (i < Transformer.transformers.size()) {
			if(Transformer.transformers.get(i).is(name))
			{
				ClassNode classNode = new ClassNode();
				ClassReader classReader = new ClassReader(basicClass);
				classReader.accept(classNode, 0);
				
				Transformer.transformers.get(i).transform(classNode);
				
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				classNode.accept(writer);
				basicClass = writer.toByteArray();
				
				System.out.println("Patched something!");
				Transformer.transformers.get(i).done();
			}else
				i++;
		}
		return basicClass;
	}

}
