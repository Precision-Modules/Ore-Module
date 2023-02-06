package com.precision.ore.asm;

import com.precision.ore.asm.visitors.WorldGeneratorImplVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import com.precision.ore.OreConfig;

public class PrecisionTransformer implements Opcodes, IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String internalName = transformedName.replace('.', '/');
        if(internalName.equals(WorldGeneratorImplVisitor.TARGET_CLASS_NAME) && OreConfig.disableGregTechOreGeneration) {
            ClassReader classReader = new ClassReader(basicClass);
            ClassWriter classWriter = new ClassWriter(classReader,0);
            classReader.accept(new WorldGeneratorImplVisitor(classWriter), 0);
            return classWriter.toByteArray();
        }
        return basicClass;
    }
}
