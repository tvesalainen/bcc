/*
 * Copyright (C) 2012 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.vesalainen.bcc;

import java.io.IOException;
import java.lang.reflect.Member;

/**
 *
 * @author tkv
 */
public class ByteCodeDump extends OpCodeUtil
{
    private static final String[] NEWARRAYSTRING = {"boolean[]", "char[]", "float[]", "double[]", "byte[]", "short[]", "int[]", "long[]" };
    private String methodName;
    private ClassFile cf;
    private CodeDataInput in;
    private LineNumberPrintStream out;

    public ByteCodeDump(byte[] code, ClassFile classFile)
    {
        this(code, classFile, new LineNumberPrintStream(System.err));
    }
    public ByteCodeDump(
            byte[] code,
            ClassFile classFile,
            LineNumberPrintStream out
            )
    {
        this.in = new CodeDataInput(code);
        this.cf = classFile;
        this.out = out;
    }

    public void print(MethodCompiler mc) throws IOException
    {
        print(mc, null);
    }

    public void print(MethodCompiler mc, LineNumberTable lineNumberTable) throws IOException
    {
        int pc = 0;  // index
        int o = 0;
        int def = 0;
        int low = 0;
        int high = 0;
        Member fi;
        Member me;
        Member co;
        ConstantInfo ci = null;
        int b;
        int s;
        int us;
        int ub;

        out.println(mc.getMethodDescription());
        while (in.available() > 0)
        {
            pc = in.pc();
            int op = in.readUnsignedByte();
            Object label = mc.labelForAddress(pc);
            if (label != null)
            {
                out.println(label+":");
            }
            if (lineNumberTable != null)
            {
                lineNumberTable.addLineNumber(pc, out.getLinenumber());
            }
            out.print(pc + ":  " + OpCodeUtil.string(op));
            switch (op)
            {
                case NOP:
                    break;
                case ACONST_NULL:
                    break;
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                    break;
                case LCONST_0:
                case LCONST_1:
                    break;
                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    break;
                case DCONST_0:
                case DCONST_1:
                    break;
                case BIPUSH:
                    b = in.readByte();
                    out.print(" " + b);
                    if (Character.isLetterOrDigit(b))
                    {
                        out.print(" //'"+(char)b+"'");
                    }
                    break;
                case SIPUSH:
                    s = in.readShort();
                    out.print(" " + s);
                    break;
                case LDC:
                    ub = in.readUnsignedByte();
                    out.print(" " + ub);
                    break;
                case LDC_W:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    break;
                case LDC2_W:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    break;
                case ILOAD:
                case LLOAD:
                case FLOAD:
                case DLOAD:
                case ALOAD:
                    ub = in.readUnsignedByte();
                    out.print(" " + ub);
                    out.print(" // "+mc.getLocalDescription(ub));
                    break;
                case ILOAD_0:
                case LLOAD_0:
                case FLOAD_0:
                case DLOAD_0:
                case ALOAD_0:
                    out.print(" // "+mc.getLocalDescription(0));
                    break;
                case ILOAD_1:
                case LLOAD_1:
                case FLOAD_1:
                case DLOAD_1:
                case ALOAD_1:
                    out.print(" // "+mc.getLocalDescription(1));
                    break;
                case ILOAD_2:
                case LLOAD_2:
                case FLOAD_2:
                case DLOAD_2:
                case ALOAD_2:
                    out.print(" // "+mc.getLocalDescription(2));
                    break;
                case ILOAD_3:
                case LLOAD_3:
                case FLOAD_3:
                case DLOAD_3:
                case ALOAD_3:
                    out.print(" // "+mc.getLocalDescription(3));
                    break;
                case IALOAD:
                    break;
                case LALOAD:
                    break;
                case FALOAD:
                    break;
                case DALOAD:
                    break;
                case AALOAD:
                    break;
                case BALOAD:
                    break;
                case CALOAD:
                    break;
                case SALOAD:
                    break;
                case ISTORE:
                case LSTORE:
                case FSTORE:
                case DSTORE:
                case ASTORE:
                    ub = in.readUnsignedByte();
                    out.print(" " + ub);
                    out.print(" // "+mc.getLocalDescription(ub));
                    break;
                case ISTORE_0:
                case LSTORE_0:
                case FSTORE_0:
                case DSTORE_0:
                case ASTORE_0:
                    out.print(" // "+mc.getLocalDescription(0));
                    break;
                case ISTORE_1:
                case LSTORE_1:
                case FSTORE_1:
                case DSTORE_1:
                case ASTORE_1:
                    out.print(" // "+mc.getLocalDescription(1));
                    break;
                case ISTORE_2:
                case LSTORE_2:
                case FSTORE_2:
                case DSTORE_2:
                case ASTORE_2:
                    out.print(" // "+mc.getLocalDescription(2));
                    break;
                case ISTORE_3:
                case LSTORE_3:
                case FSTORE_3:
                case DSTORE_3:
                case ASTORE_3:
                    out.print(" // "+mc.getLocalDescription(3));
                    break;
                case IASTORE:
                    break;
                case LASTORE:
                    break;
                case FASTORE:
                    break;
                case DASTORE:
                    break;
                case AASTORE:
                    break;
                case BASTORE:
                    break;
                case CASTORE:
                    break;
                case SASTORE:
                    break;
                case POP:
                    break;
                case POP2:
                    break;
                case DUP:
                    break;
                case DUP_X1:
                    break;
                case DUP_X2:
                    break;
                case DUP2:
                    break;
                case DUP2_X1:
                    break;
                case DUP2_X2:
                    break;
                case SWAP:
                    break;
                case IADD:
                case ISUB:
                case IMUL:
                case IDIV:
                case IREM:
                case IAND:
                case IOR:
                case IXOR:
                    break;
                case LADD:
                case LSUB:
                case LMUL:
                case LDIV:
                case LREM:
                case LAND:
                case LOR:
                case LXOR:
                    break;
                case FADD:
                case FSUB:
                case FMUL:
                case FDIV:
                case FREM:
                    break;
                case DADD:
                case DSUB:
                case DMUL:
                case DDIV:
                    break;
                case INEG:
                    break;
                case LNEG:
                    break;
                case FNEG:
                    break;
                case DNEG:
                    break;
                case ISHL:
                case ISHR:
                case IUSHR:
                    break;
                case LSHL:
                case LSHR:
                case LUSHR:
                    break;
                case IINC:
                    ub = in.readUnsignedByte();
                    out.print(" " + ub);
                    b = in.readByte();
                    out.print(" " + b);
                    out.print(" // "+mc.getLocalName(ub)+"+="+b);
                    break;
                case I2L:
                    break;
                case I2F:
                    break;
                case I2D:
                    break;
                case L2I:
                    break;
                case L2F:
                    break;
                case L2D:
                    break;
                case F2I:
                    break;
                case F2L:
                    break;
                case F2D:
                    break;
                case D2I:
                    break;
                case D2L:
                    break;
                case D2F:
                    break;
                case I2B:
                case I2C:
                case I2S:
                    break;
                case LCMP:
                    break;
                case FCMPL:
                case FCMPG:
                    break;
                case DCMPL:
                case DCMPG:
                    break;
                case IFEQ:
                case IFNE:
                case IFLT:
                case IFGE:
                case IFGT:
                case IFLE:
                    s = in.readShort()+pc;
                    out.print(" " + s);
                    label = mc.labelForAddress(s);
                    if (label != null)
                    {
                        out.print(" //" + label);
                    }
                    break;
                case IF_ICMPEQ:
                case IF_ICMPNE:
                case IF_ICMPLT:
                case IF_ICMPGE:
                case IF_ICMPGT:
                case IF_ICMPLE:
                case IF_ACMPEQ:
                case IF_ACMPNE:
                    s = in.readShort()+pc;
                    out.print(" " + s);
                    label = mc.labelForAddress(s);
                    if (label != null)
                    {
                        out.print(" //" + label);
                    }
                    break;
                case GOTO:
                    s = in.readShort()+pc;
                    out.print(" " + s);
                    label = mc.labelForAddress(s);
                    if (label != null)
                    {
                        out.print(" //" + label);
                    }
                    break;
                case GOTO_W:
                    s = in.readInt()+pc;
                    out.print(" " + s);
                    label = mc.labelForAddress(s);
                    if (label != null)
                    {
                        out.print(" //" + label);
                    }
                    break;
                case JSR:
                    s = in.readShort()+pc;
                    out.print(" " + s);
                    label = mc.labelForAddress(s);
                    if (label != null)
                    {
                        out.print(" //" + label);
                    }
                    break;
                case JSR_W:
                    s = in.readInt()+pc;
                    out.print(" " + s);
                    label = mc.labelForAddress(s);
                    if (label != null)
                    {
                        out.print(" //" + label);
                    }
                    break;
                case RET:
                    ub = in.readUnsignedByte();
                    out.print(" // "+mc.getLocalDescription(ub));
                    break;
                case TABLESWITCH:
                    while ((in.pc() % 4) != 0)
                    {
                        in.readByte();
                    }
                    def = in.readInt()+pc;
                    low = in.readInt();
                    high = in.readInt();
                    label = mc.labelForAddress(def);
                    if (label != null)
                    {
                        out.print(" // default -> " + label);
                    }
                    for (int ii = 0; ii < high - low + 1; ii++)
                    {
                        o = in.readInt()+pc;
                        label = mc.labelForAddress(o);
                        if (label != null)
                        {
                            int off = low+ii;
                            out.print("\n   // "+off+" -> " + label);
                        }
                    }
                    break;
                case LOOKUPSWITCH:
                    while ((in.pc() % 4) != 0)
                    {
                        in.readByte();
                    }
                    def = in.readInt()+pc;
                    high = in.readInt();
                    label = mc.labelForAddress(def);
                    if (label != null)
                    {
                        out.print(" // default -> " + label);
                    }
                    else
                    {
                        out.print(" // default -> ???");
                    }

                    for (int ii = 0; ii < high; ii++)
                    {
                        int value = in.readInt();
                        int offset = in.readInt()+pc;
                        label = mc.labelForAddress(offset);
                        if (label != null)
                        {
                            out.print("\n   // "+value+" -> " + label);
                        }
                        else
                        {
                            out.print("\n   // "+value+" -> ???");
                        }
                    }
                    break;
                case IRETURN:
                    break;
                case LRETURN:
                    break;
                case FRETURN:
                    break;
                case DRETURN:
                    break;
                case ARETURN:
                    break;
                case RETURN:
                    break;
                case GETSTATIC:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getFieldDescription(us));
                    break;
                case PUTSTATIC:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getFieldDescription(us));
                    break;
                case GETFIELD:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getFieldDescription(us));
                    break;
                case PUTFIELD:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getFieldDescription(us));
                    break;
                case INVOKEVIRTUAL:
                case INVOKESPECIAL:
                case INVOKESTATIC:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getMethodDescription(us));
                    break;
                case INVOKEINTERFACE:
                    us = in.readUnsignedShort();
                    ub = in.readUnsignedByte();
                    in.readUnsignedByte();
                    out.print(" " + us);
                    out.print(" // "+cf.getMethodDescription(us)+" count="+ub);
                    break;
                case NEW:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getClassDescription(us));
                    break;
                case NEWARRAY:
                    b = in.readByte();
                    out.print(" " + b);
                    out.print(" // "+NEWARRAYSTRING[b-4]);
                    break;
                case ANEWARRAY:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getClassDescription(us));
                    break;
                case ARRAYLENGTH:
                    break;
                case ATHROW:
                    break;
                case CHECKCAST:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getClassDescription(us));
                    break;
                case INSTANCEOF:
                    us = in.readUnsignedShort();
                    out.print(" " + us);
                    out.print(" // "+cf.getClassDescription(us));
                    break;
                case MONITORENTER:
                case MONITOREXIT:
                    break;
                case WIDE:
                    ub = in.readUnsignedByte();
                    out.print(" " + OpCodeUtil.string(ub));
                    switch (ub)
                    {
                        case ILOAD:
                        case LLOAD:
                        case FLOAD:
                        case DLOAD:
                        case ALOAD:
                        case ISTORE:
                        case LSTORE:
                        case FSTORE:
                        case DSTORE:
                        case ASTORE:
                            ub = in.readUnsignedShort();
                            out.print(" " + ub);
                            out.print(" // "+mc.getLocalDescription(ub));
                            break;
                        case RET:
                            ub = in.readUnsignedShort();
                            out.print(" // "+mc.getLocalDescription(ub));
                            break;
                        case IINC:
                            ub = in.readUnsignedShort();
                            out.print(" " + ub);
                            s = in.readShort();
                            out.print(" " + s);
                            out.print(" // "+mc.getLocalName(ub)+"+="+s);
                            break;
                        default:
                            throw new IllegalArgumentException("unknown wide op code "+ub);
                    }
                    break;
                case MULTIANEWARRAY:
                    us = in.readUnsignedShort();
                    ub = in.readUnsignedByte();
                    out.print(" " + us);
                    out.print(" dim count=" + ub);
                    out.print(" // "+cf.getClassDescription(us));
                    break;
                case IFNULL:
                case IFNONNULL:
                    s = in.readShort()+pc;
                    out.print(" " + s);
                    label = mc.labelForAddress(s);
                    if (label != null)
                    {
                        out.print(" //" + label);
                    }
                    break;
                case BREAKPOINT:
                    throw new UnsupportedOperationException("breakpoint");
                case IMPDEP1:
                    throw new UnsupportedOperationException("impdep1");
                case IMPDEP2:
                    throw new UnsupportedOperationException("impdep2");
                default:
                    throw new UnsupportedOperationException("unknown code="+op);

            }
            out.println();
        }

    }

}
