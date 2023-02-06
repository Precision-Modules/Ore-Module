package com.precision.ore.asm.visitors;

import gregtech.asm.util.ObfMapping;
import org.objectweb.asm.*;

public class WorldGeneratorImplVisitor extends ClassVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "gregtech/api/worldgen/generator/WorldGeneratorImpl";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "generateInternal", "(Lnet/minecraft/world/World;IIIILjava/util/Random;)V");

    public WorldGeneratorImplVisitor(ClassWriter cw) {
        super(Opcodes.ASM5, cw);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if(name.equals(TARGET_METHOD.s_name) && desc.equals(TARGET_METHOD.s_desc))
            return new ReplaceWithEmptyBody(super.visitMethod(access, name, desc, signature, exceptions), (Type.getArgumentsAndReturnSizes(desc)>>2)-1);
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    static class ReplaceWithEmptyBody extends MethodVisitor {
        private final MethodVisitor targetWriter;
        private final int newMaxLocals;

        ReplaceWithEmptyBody(MethodVisitor writer, int newMaxL) {
            // now, we're not passing the writer to the superclass for our radical changes
            super(Opcodes.ASM5);
            targetWriter = writer;
            newMaxLocals = newMaxL;
        }

        // we're only override the minimum to create a code attribute with a sole RETURN

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            targetWriter.visitMaxs(0, newMaxLocals);
        }

        @Override
        public void visitCode() {
            targetWriter.visitCode();
            targetWriter.visitInsn(Opcodes.RETURN);// our new code
        }

        @Override
        public void visitEnd() {
            targetWriter.visitEnd();
        }
    }
}
