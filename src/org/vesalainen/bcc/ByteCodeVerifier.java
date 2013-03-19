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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.T;

/**
 *
 * @author tkv
 */
public class ByteCodeVerifier extends OpCodeUtil
{
    private static final TypeMirror returnAddress = T.ReturnAddress;

    private ClassFile cf;
    private Deque<Context> branch = new ArrayDeque<>();
    private CodeDataInput in;
    private int opCodePosition;
    private Set<Integer> goneThrough = new HashSet<>();
    private Map<Integer,Deque<TypeMirror>> stackAt = new HashMap<>();
    private Map<Integer,TypeMirror[]> lvAt = new HashMap<>();
    private int maxLocal;
    private int maxStack;
    private byte[] code;
    private ExceptionTable[] exceptionTable;
    private MethodCompiler mc;
    private boolean[] coverage; // TODO evaluate BitSet!
    private TypeMirror[] lvType;

    public ByteCodeVerifier(
            byte[] code,
            ExceptionTable[] exceptionTable,
            ClassFile classFile,
            MethodCompiler mc
            )
    {
        this.code = code;
        this.exceptionTable = exceptionTable;
        this.in = new CodeDataInput(code);
        this.cf = classFile;
        this.mc = mc;
        coverage = new boolean[code.length];
        lvType = new TypeMirror[mc.localSize()];
        int index = 0;
        lvType[index++] = classFile.asType();

        for (TypeMirror param : mc.getParameters())
        {
            if (T.isInteger(param))
            {
                lvType[index] = T.Int;
            }
            else
            {
                lvType[index] = param;
            }
            if (T.isCategory2(lvType[index]))
            {
                index++;
            }
            index++;
        }
    }

    public void verify() throws IOException
    {
        try
        {
            branch.add(new Context(0, new OperandStack(), lvType));
            for (ExceptionTable et : exceptionTable)
            {
                OperandStack os = new OperandStack();
                os.add(T.getTypeFor(Throwable.class));
                branch.add(new Context(et.getHandler(), os, lvType));
            }
            while (!branch.isEmpty())
            {
                Context ctx = branch.pop();
                int start = ctx.getPc();
                int ms = run(ctx);
                int end = in.pc();
                Arrays.fill(coverage, start, end, true);
                maxStack = Math.max(maxStack, ms);
            }
        }
        catch (Throwable ex)
        {
            String at = mc.getMethodDescription()+" At "+opCodePosition+": ";
            ByteCodeDump dump = new ByteCodeDump(code, cf);
            try
            {
                dump.print(mc);
            }
            catch (Exception exx)
            {
                exx.printStackTrace();
            }
            throw new IllegalArgumentException(at, ex);
        }
        List<Integer> clist = new ArrayList<>();
        for (int ii=0;ii<coverage.length;ii++)
        {
            if (!coverage[ii])
            {
                clist.add(ii);
                while (ii<coverage.length && !coverage[ii])
                {
                    ii++;
                }
                clist.add(ii);
            }
        }
        if (!clist.isEmpty())
        {
            ByteCodeDump dump = new ByteCodeDump(code, cf);
            try
            {
                dump.print(mc);
            }
            catch (Exception exx)
            {
                exx.printStackTrace();
            }
            for (int ii=0;ii<clist.size();ii+=2)
            {
                System.err.println(clist.get(ii)+"-"+clist.get(ii+1));
            }
            throw new IllegalArgumentException(mc.getMethodDescription()+": part of code not covered");
        }
    }
    private void branch(int pc, int offset, OperandStack s, TypeMirror[] lv)
    {
        maxStack = Math.max(maxStack, s.getMax());
        int target = pc+offset;
        if (target < 0 || target > 0xffff)
        {
            throw new VerifyError(mc.getMethodDescription()+" goto target illegal "+target);
        }
        Deque<TypeMirror> old = stackAt.get(target);
        if (old != null)
        {
            String stackBefore = old.toString();
            String stackNow = s.toString();
            if (!stackBefore.equals(stackNow))
            {
                throw new VerifyError(mc.getMethodDescription()+" stack differs in branch "+pc+" -> "+target
                        +"  before:"+stackBefore+"  now   :"+stackNow);
            }
            TypeMirror[] lvBefore = lvAt.get(target);
            TypeMirror[] lvNow = lv;
            if (!compare(lvBefore, lvNow))
            {
                throw new VerifyError(mc.getMethodDescription()+" local variables differ in branch "+pc+" -> "+target
                        +"  before:"+Arrays.toString(lvBefore)+"  now   :"+Arrays.toString(lvNow));
            }

        }
        else
        {
            stackAt.put(target, s.clone());
            lvAt.put(target, lv);
        }
        if (!goneThrough.contains(target))
        {
            goneThrough.add(target);
            branch.add(new Context(target, s, lv));
        }
    }

    private boolean compare(TypeMirror[] lvBefore, TypeMirror[] lvNow)
    {
        for (int ii=0;ii<lvNow.length;ii++)
        {
            if (lvBefore[ii] == null && lvNow[ii] == null)
            {
                continue;
            }
            if (lvBefore[ii] == null && lvNow[ii] != null)
            {
                return false;
            }
            if (lvBefore[ii] != null && lvNow[ii] == null)
            {
                return false;
            }
            if (!T.isPrimitive(lvBefore[ii]) && T.isPrimitive(lvNow[ii]))
            {
                return false;
            }
            if (T.isPrimitive(lvBefore[ii]) && !T.isPrimitive(lvNow[ii]))
            {
                return false;
            }
            if (!T.isPrimitive(lvBefore[ii]) && !T.isPrimitive(lvNow[ii]))
            {
                continue;
            }
            if (!T.isSameType(lvBefore[ii], lvNow[ii]))
            {
                return false;
            }
        }
        return true;
    }
    private int run(Context ctx) throws IOException, ClassNotFoundException
    {
        OperandStack s = ctx.getS(); // stack
        TypeMirror[] lv = ctx.getLvType();
        in = in.branch(ctx.getPc());
        int pc = 0;  // index
        int i = 0;  // index
        int c = 0;  // const
        int o = 0;  // offset
        int def = 0;
        int low = 0;
        int high = 0;
        TypeMirror v1 = null;
        TypeMirror v2 = null;
        TypeMirror v3;
        TypeMirror v4;
        ArrayType a1;
        VariableElement fi;
        ExecutableElement me = null;
        ConstantInfo ci = null;
        TypeMirror cw;
        ExecutableElement co;
        TypeElement te;
        while (in.available() > 0)
        {
            pc = in.pc();
            opCodePosition = pc;
            Deque<TypeMirror> ns = new LinkedList<>();
            ns.addAll(s);
            stackAt.put(pc, ns);
            lvAt.put(pc, lv.clone());
            int op = in.readUnsignedByte();
            switch (op)
            {
                case NOP:
                    break;
                case ACONST_NULL:
                    s.push(null);
                    break;
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                    s.push(T.Int);
                    break;
                case LCONST_0:
                case LCONST_1:
                    s.push(T.Long);
                    break;
                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    s.push(T.Float);
                    break;
                case DCONST_0:
                case DCONST_1:
                    s.push(T.Double);
                    break;
                case BIPUSH:
                    in.readByte();
                    s.push(T.Int);
                    break;
                case SIPUSH:
                    in.readShort();
                    s.push(T.Int);
                    break;
                case LDC:
                    i = in.readUnsignedByte();
                    ci = cf.getConstantInfo(i);
                    switch (ci.getTag())
                    {
                        case ConstantInfo.CONSTANT_Integer:
                            s.push(T.Int);
                            break;
                        case ConstantInfo.CONSTANT_Float:
                            s.push(T.Float);
                            break;
                        case ConstantInfo.CONSTANT_String:
                            s.push(T.String);
                            break;
                        default:
                            throw new VerifyError("illegal constant info for ldc "+ci.getTag());
                    }
                    break;
                case LDC_W:
                    i = in.readUnsignedShort();
                    ci = cf.getConstantInfo(i);
                    switch (ci.getTag())
                    {
                        case ConstantInfo.CONSTANT_Integer:
                            s.push(T.Int);
                            break;
                        case ConstantInfo.CONSTANT_Float:
                            s.push(T.Float);
                            break;
                        case ConstantInfo.CONSTANT_String:
                            s.push(T.String);
                            break;
                        default:
                            throw new VerifyError("illegal constant info for ldc_w "+ci.getTag());
                    }
                    break;
                case LDC2_W:
                    i = in.readUnsignedShort();
                    ci = cf.getConstantInfo(i);
                    switch (ci.getTag())
                    {
                        case ConstantInfo.CONSTANT_Long:
                            s.push(T.Long);
                            break;
                        case ConstantInfo.CONSTANT_Double:
                            s.push(T.Double);
                            break;
                        default:
                            throw new VerifyError("illegal constant info for ldc2_w "+ci.getTag());
                    }
                    break;
                case ILOAD:
                    i = in.readUnsignedByte();
                    if (!T.isSameType(T.Int,lvType[i]))
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to int");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(T.Int);
                    break;
                case LLOAD:
                    i = in.readUnsignedByte();
                    if (!T.isSameType(T.Long, lvType[i]) || lvType[i+1] != null)
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to long");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(T.Long);
                    break;
                case FLOAD:
                    i = in.readUnsignedByte();
                    if (!T.isSameType(T.Float, lvType[i]))
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to float");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(T.Float);
                    break;
                case DLOAD:
                    i = in.readUnsignedByte();
                    if (!T.isSameType(T.Double, lvType[i]) || lvType[i+1] != null)
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to double");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(T.Double);
                    break;
                case ALOAD:
                    i = in.readUnsignedByte();
                    if (lvType[i] == null || T.isPrimitive(lvType[i]))
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to reference");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(mc.getLocalType(i));
                    break;
                case ILOAD_0:
                    if (!T.isSameType(T.Int, lvType[0]))
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to int");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(T.Int);
                    break;
                case ILOAD_1:
                    if (!T.isSameType(T.Int, lvType[1]))
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to int");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(T.Int);
                    break;
                case ILOAD_2:
                    if (!T.isSameType(T.Int, lvType[2]))
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to int");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(T.Int);
                    break;
                case ILOAD_3:
                    if (!T.isSameType(T.Int, lvType[3]))
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to int");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(T.Int);
                    break;
                case LLOAD_0:
                    if (!T.isSameType(T.Long, lvType[0]) || lvType[1] != null)
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to long");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(T.Long);
                    break;
                case LLOAD_1:
                    if (!T.isSameType(T.Long, lvType[1]) || lvType[2] != null)
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to long");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(T.Long);
                    break;
                case LLOAD_2:
                    if (!T.isSameType(T.Long, lvType[2]) || lvType[3] != null)
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to long");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(T.Long);
                    break;
                case LLOAD_3:
                    if (!T.isSameType(T.Long, lvType[3]) || lvType[4] != null)
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to long");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(T.Long);
                    break;
                case FLOAD_0:
                    if (!T.isSameType(T.Float, lvType[0]))
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to float");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(T.Float);
                    break;
                case FLOAD_1:
                    if (!T.isSameType(T.Float, lvType[1]))
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to float");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(T.Float);
                    break;
                case FLOAD_2:
                    if (!T.isSameType(T.Float, lvType[2]))
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to float");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(T.Float);
                    break;
                case FLOAD_3:
                    if (!T.isSameType(T.Float, lvType[3]))
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to float");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(T.Float);
                    break;
                case DLOAD_0:
                    if (!T.isSameType(T.Double, lvType[0]) || lvType[1] != null)
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to double");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(T.Double);
                    break;
                case DLOAD_1:
                    if (!T.isSameType(T.Double, lvType[1]) || lvType[2] != null)
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to double");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(T.Double);
                    break;
                case DLOAD_2:
                    if (!T.isSameType(T.Double, lvType[2]) || lvType[3] != null)
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to double");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(T.Double);
                    break;
                case DLOAD_3:
                    if (!T.isSameType(T.Double, lvType[3]) || lvType[4] != null)
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to double");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(T.Double);
                    break;
                case ALOAD_0:
                    if (lvType[0] == null || T.isPrimitive(lvType[0]))
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to reference");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(mc.getLocalType(0));
                    break;
                case ALOAD_1:
                    if (lvType[1] == null || T.isPrimitive(lvType[1]))
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to reference");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(mc.getLocalType(1));
                    break;
                case ALOAD_2:
                    if (lvType[2] == null || T.isPrimitive(lvType[2]))
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to reference");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(mc.getLocalType(2));
                    break;
                case ALOAD_3:
                    if (lvType[3] == null || T.isPrimitive(lvType[3]))
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to reference");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(mc.getLocalType(3));
                    break;
                case IALOAD:
                    verify(T.Int, s.pop());
                    verify(T.IntA, s.pop());
                    s.push(T.Int);
                    break;
                case LALOAD:
                    verify(T.Int, s.pop());
                    verify(T.LongA, s.pop());
                    s.push(T.Long);
                    break;
                case FALOAD:
                    verify(T.Int, s.pop());
                    verify(T.FloatA, s.pop());
                    s.push(T.Float);
                    break;
                case DALOAD:
                    verify(T.Int, s.pop());
                    verify(T.DoubleA, s.pop());
                    s.push(T.Double);
                    break;
                case AALOAD:
                    verify(T.Int, s.pop());
                    a1 = (ArrayType) s.pop();
                    verify(T.ObjectA, v1);
                    s.push(a1.getComponentType());
                    break;
                case BALOAD:
                    verify(T.Int, s.pop());
                    verifyByteOrBooleanArray(s.pop());
                    s.push(T.Int);
                    break;
                case CALOAD:
                    verify(T.Int, s.pop());
                    verify(T.CharA, s.pop());
                    s.push(T.Int);
                    break;
                case SALOAD:
                    verify(T.Int, s.pop());
                    verify(T.ShortA, s.pop());
                    s.push(T.Int);
                    break;
                case ISTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verify(T.Int, s.pop());
                    lvType[i] = T.Int;
                    break;
                case LSTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verify(T.Long, s.pop());
                    lvType[i] = T.Long;
                    lvType[i+1] = null;
                    break;
                case FSTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verify(T.Float, s.pop());
                    lvType[i] = T.Float;
                    break;
                case DSTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verify(T.Double, s.pop());
                    lvType[i] = T.Double;
                    lvType[i+1] = null;
                    break;
                case ASTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verifyLocalType(i, s.pop());
                    lvType[i] = getLocalType(i);
                    break;
                case ISTORE_0:
                    maxLocal = Math.max(0, maxLocal);
                    verify(T.Int, s.pop());
                    lvType[0] = T.Int;
                    break;
                case ISTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verify(T.Int, s.pop());
                    lvType[1] = T.Int;
                    break;
                case ISTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verify(T.Int, s.pop());
                    lvType[2] = T.Int;
                    break;
                case ISTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verify(T.Int, s.pop());
                    lvType[3] = T.Int;
                    break;
                case LSTORE_0:
                    maxLocal = Math.max(0, maxLocal);
                    verify(T.Long, s.pop());
                    lvType[0] = T.Long;
                    lvType[1] = null;
                    break;
                case LSTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verify(T.Long, s.pop());
                    lvType[1] = T.Long;
                    lvType[2] = null;
                    break;
                case LSTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verify(T.Long, s.pop());
                    lvType[2] = T.Long;
                    lvType[3] = null;
                    break;
                case LSTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verify(T.Long, s.pop());
                    lvType[3] = T.Long;
                    lvType[4] = null;
                    break;
                case FSTORE_0:
                    maxLocal = Math.max(0, maxLocal);
                    verify(T.Float, s.pop());
                    lvType[0] = T.Float;
                    break;
                case FSTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verify(T.Float, s.pop());
                    lvType[1] = T.Float;
                    break;
                case FSTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verify(T.Float, s.pop());
                    lvType[2] = T.Float;
                    break;
                case FSTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verify(T.Float, s.pop());
                    lvType[3] = T.Float;
                    break;
                case DSTORE_0:
                    maxLocal = Math.max(0, maxLocal);
                    verify(T.Double, s.pop());
                    lvType[0] = T.Double;
                    lvType[1] = null;
                    break;
                case DSTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verify(T.Double, s.pop());
                    lvType[1] = T.Double;
                    lvType[2] = null;
                    break;
                case DSTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verify(T.Double, s.pop());
                    lvType[2] = T.Double;
                    lvType[3] = null;
                    break;
                case DSTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verify(T.Double, s.pop());
                    lvType[3] = T.Double;
                    lvType[4] = null;
                    break;
                case ASTORE_0:
                    maxLocal = Math.max(0, maxLocal);
                    verifyLocalType(0, s.pop());
                    lvType[0] = getLocalType(0);
                    break;
                case ASTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verifyLocalType(1, s.pop());
                    lvType[1] = getLocalType(1);
                    break;
                case ASTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verifyLocalType(2, s.pop());
                    lvType[2] = getLocalType(2);
                    break;
                case ASTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verifyLocalType(3, s.pop());
                    lvType[3] = getLocalType(3);
                    break;
                case IASTORE:
                    verify(T.Int, s.pop());
                    verify(T.Int, s.pop());
                    verify(T.IntA, s.pop());
                    break;
                case LASTORE:
                    verify(T.Long, s.pop());
                    verify(T.Int, s.pop());
                    verify(T.LongA, s.pop());
                    break;
                case FASTORE:
                    verify(T.Float, s.pop());
                    verify(T.Int, s.pop());
                    verify(T.FloatA, s.pop());
                    break;
                case DASTORE:
                    verify(T.Double, s.pop());
                    verify(T.Int, s.pop());
                    verify(T.DoubleA, s.pop());
                    break;
                case AASTORE:
                    verifyReferenceOrArray(s.pop());
                    verify(T.Int, s.pop());
                    verify(T.ObjectA, s.pop());
                    break;
                case BASTORE:
                    verify(T.Int, s.pop());
                    verify(T.Int, s.pop());
                    verifyByteOrBooleanArray(s.pop());
                    break;
                case CASTORE:
                    verify(T.Int, s.pop());
                    verify(T.Int, s.pop());
                    verify(T.CharA, s.pop());
                    break;
                case SASTORE:
                    verify(T.Short, s.pop());
                    verify(T.Int, s.pop());
                    verify(T.ShortA, s.pop());
                    break;
                case POP:
                    verify1(s.pop());
                    break;
                case POP2:
                    if (c1(s.peek()))
                    {
                        verify1(s.pop());
                        verify1(s.pop());
                    }
                    else
                    {
                        verify2(s.pop());
                    }
                    break;
                case DUP:
                    verify1(s.peek());
                    s.push(s.peek());
                    break;
                case DUP_X1:
                    v1 = s.pop();
                    verify1(v1);
                    v2 = s.pop();
                    verify1(v2);
                    s.push(v1);
                    s.push(v2);
                    s.push(v1);
                    break;
                case DUP_X2:
                    v1 = s.pop();
                    verify1(v1);
                    v2 = s.pop();
                    if (c1(v2))
                    {
                        v3 = s.pop();
                        verify1(v3);
                        s.push(v1);
                        s.push(v3);
                        s.push(v2);
                        s.push(v1);
                    }
                    else
                    {
                        s.push(v1);
                        s.push(v2);
                        s.push(v1);
                    }
                    break;
                case DUP2:
                    v1 = s.pop();
                    if (c1(v1))
                    {
                        v2 = s.pop();
                        verify1(v2);
                        s.push(v2);
                        s.push(v1);
                        s.push(v2);
                        s.push(v1);
                    }
                    else
                    {
                        s.push(v1);
                        s.push(v1);
                    }
                    break;
                case DUP2_X1:
                    v1 = s.pop();
                    if (c1(v1))
                    {
                        v2 = s.pop();
                        verify1(v2);
                        v3 = s.pop();
                        verify1(v3);
                        s.push(v2);
                        s.push(v1);
                        s.push(v3);
                        s.push(v2);
                        s.push(v1);
                    }
                    else
                    {
                        v2 = s.pop();
                        verify2(v2);
                        s.push(v1);
                        s.push(v2);
                        s.push(v1);
                    }
                    break;
                case DUP2_X2:
                    v1 = s.pop();
                    if (c1(v1))
                    {
                        v2 = s.pop();
                        v3 = s.pop();
                        if (c1(v2) && c1(v3))   // form 1
                        {
                            v4 = s.pop();
                            verify1(v4);
                            s.push(v2);
                            s.push(v1);
                            s.push(v4);
                            s.push(v3);
                            s.push(v2);
                            s.push(v1);
                        }
                        else    // form 3
                        {
                            verify1(v2);
                            verify2(v3);
                            s.push(v2);
                            s.push(v1);
                            s.push(v3);
                            s.push(v2);
                            s.push(v1);
                        }
                    }
                    else
                    {
                        v2 = s.pop();
                        if (c1(v2)) // form 2
                        {
                            v3 = s.pop();
                            verify1(v3);
                            s.push(v1);
                            s.push(v3);
                            s.push(v2);
                            s.push(v1);
                        }
                        else    // form 4
                        {
                            s.push(v1);
                            s.push(v2);
                            s.push(v1);
                        }
                    }
                    break;
                case SWAP:
                    v1 = s.pop();
                    verify1(v1);
                    v2 = s.pop();
                    verify1(v2);
                    s.push(v1);
                    s.push(v2);
                    break;
                case IADD:
                case ISUB:
                case IMUL:
                case IDIV:
                case IREM:
                case IAND:
                case IOR:
                case IXOR:
                    verify(T.Int, s.pop());
                    verify(T.Int, s.pop());
                    s.push(T.Int);
                    break;
                case LADD:
                case LSUB:
                case LMUL:
                case LDIV:
                case LREM:
                case LAND:
                case LOR:
                case LXOR:
                    verify(T.Long, s.pop());
                    verify(T.Long, s.pop());
                    s.push(T.Long);
                    break;
                case FADD:
                case FSUB:
                case FMUL:
                case FDIV:
                case FREM:
                    verify(T.Float, s.pop());
                    verify(T.Float, s.pop());
                    s.push(T.Float);
                    break;
                case DADD:
                case DSUB:
                case DMUL:
                case DDIV:
                    verify(T.Double, s.pop());
                    verify(T.Double, s.pop());
                    s.push(T.Double);
                    break;
                case INEG:
                    verify(T.Int, s.peek());
                    break;
                case LNEG:
                    verify(T.Long, s.peek());
                    break;
                case FNEG:
                    verify(T.Float, s.peek());
                    break;
                case DNEG:
                    verify(T.Double, s.peek());
                    break;
                case ISHL:
                case ISHR:
                case IUSHR:
                    verify(T.Int, s.pop());
                    verify(T.Int, s.pop());
                    s.push(T.Int);
                    break;
                case LSHL:
                case LSHR:
                case LUSHR:
                    verify(T.Int, s.pop());
                    verify(T.Long, s.pop());
                    s.push(T.Long);
                    break;
                case IINC:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(maxLocal, i);
                    c = in.readByte();
                    break;
                case I2L:
                    verify(T.Int, s.pop());
                    s.push(T.Long);
                    break;
                case I2F:
                    verify(T.Int, s.pop());
                    s.push(T.Float);
                    break;
                case I2D:
                    verify(T.Int, s.pop());
                    s.push(T.Double);
                    break;
                case L2I:
                    verify(T.Long, s.pop());
                    s.push(T.Int);
                    break;
                case L2F:
                    verify(T.Long, s.pop());
                    s.push(T.Float);
                    break;
                case L2D:
                    verify(T.Long, s.pop());
                    s.push(T.Double);
                    break;
                case F2I:
                    verify(T.Float, s.pop());
                    s.push(T.Int);
                    break;
                case F2L:
                    verify(T.Float, s.pop());
                    s.push(T.Long);
                    break;
                case F2D:
                    verify(T.Float, s.pop());
                    s.push(T.Double);
                    break;
                case D2I:
                    verify(T.Double, s.pop());
                    s.push(T.Int);
                    break;
                case D2L:
                    verify(T.Double, s.pop());
                    s.push(T.Long);
                    break;
                case D2F:
                    verify(T.Double, s.pop());
                    s.push(T.Float);
                    break;
                case I2B:
                case I2C:
                case I2S:
                    verify(T.Int, s.pop());
                    s.push(T.Int);
                    break;
                case LCMP:
                    verify(T.Long, s.pop());
                    verify(T.Long, s.pop());
                    s.push(T.Int);
                    break;
                case FCMPL:
                case FCMPG:
                    verify(T.Float, s.pop());
                    verify(T.Float, s.pop());
                    s.push(T.Int);
                    break;
                case DCMPL:
                case DCMPG:
                    verify(T.Double, s.pop());
                    verify(T.Double, s.pop());
                    s.push(T.Int);
                    break;
                case IFEQ:
                case IFNE:
                case IFLT:
                case IFGE:
                case IFGT:
                case IFLE:
                    o = in.readShort();
                    verify(T.Int, s.pop());
                    branch(pc, o, s, lv);
                    break;
                case IF_ICMPEQ:
                case IF_ICMPNE:
                case IF_ICMPLT:
                case IF_ICMPGE:
                case IF_ICMPGT:
                case IF_ICMPLE:
                    o = in.readShort();
                    verify(T.Int, s.pop());
                    verify(T.Int, s.pop());
                    branch(pc, o, s, lv);
                    break;
                case IF_ACMPEQ:
                case IF_ACMPNE:
                    o = in.readShort();
                    verify(T.Object, s.pop());
                    verify(T.Object, s.pop());
                    branch(pc, o, s, lv);
                    break;
                case GOTO:
                    o = in.readShort();
                    branch(pc, o, s, lv);
                    return s.getMax();
                case GOTO_W:
                    o = in.readInt();
                    branch(pc, o, s, lv);
                    return s.getMax();
                case JSR:
                    o = in.readShort();
                    s.push(returnAddress);
                    branch(pc, o, s, lv);
                    s.pop();
                    branch(pc, 3, s, lv);
                    return s.getMax();
                case JSR_W:
                    o = in.readInt();
                    s.push(returnAddress);
                    branch(pc, o, s, lv);
                    s.pop();
                    branch(pc, 5, s, lv);
                    return s.getMax();
                case RET:
                    o = in.readUnsignedByte();
                    verify(returnAddress, mc.getLocalType(o));
                    return s.getMax();
                case TABLESWITCH:
                    while ((in.pc() % 4) != 0)
                    {
                        in.readByte();
                    }
                    def = in.readInt();
                    low = in.readInt();
                    high = in.readInt();
                    verify(T.Int, s.pop());
                    branch(pc, def, s, lv);
                    for (int ii=0;ii<high-low+1;ii++)
                    {
                        o = in.readInt();
                        branch(pc, o, s, lv);
                    }
                    return s.getMax();
                case LOOKUPSWITCH:
                    while ((in.pc() % 4) != 0)
                    {
                        in.readByte();
                    }
                    def = in.readInt();
                    high = in.readInt();
                    verify(T.Int, s.pop());
                    branch(pc, def, s, lv);
                    low = Integer.MIN_VALUE;
                    for (int ii=0;ii<high;ii++)
                    {
                        o = in.readInt();
                        if (o < low)
                        {
                            throw new VerifyError("lookupswitch not sorted");
                        }
                        low = o;
                        o = in.readInt();
                        branch(pc, o, s, lv);
                    }
                    return s.getMax();
                case IRETURN:
                    if (s.isEmpty())
                    {
                        throw new VerifyError("stack empty when return "+s);
                    }
                    verify(T.Int, s.pop());
                    if (!s.isEmpty())
                    {
                        throw new VerifyError("stack not empty when return "+s);
                    }
                    return s.getMax();
                case LRETURN:
                    if (s.isEmpty())
                    {
                        throw new VerifyError("stack empty when return "+s);
                    }
                    verify(T.Long, s.pop());
                    if (!s.isEmpty())
                    {
                        throw new VerifyError("stack not empty when return "+s);
                    }
                    return s.getMax();
                case FRETURN:
                    if (s.isEmpty())
                    {
                        throw new VerifyError("stack empty when return "+s);
                    }
                    verify(T.Float, s.pop());
                    if (!s.isEmpty())
                    {
                        throw new VerifyError("stack not empty when return "+s);
                    }
                    return s.getMax();
                case DRETURN:
                    if (s.isEmpty())
                    {
                        throw new VerifyError("stack empty when return "+s);
                    }
                    verify(T.Double, s.pop());
                    if (!s.isEmpty())
                    {
                        throw new VerifyError("stack not empty when return "+s);
                    }
                    return s.getMax();
                case ARETURN:
                    if (s.isEmpty())
                    {
                        throw new VerifyError("stack empty when return "+s);
                    }
                    verify(mc.getReturnType(), s.pop());
                    if (!s.isEmpty())
                    {
                        throw new VerifyError("stack not empty when return "+s);
                    }
                    return s.getMax();
                case RETURN:
                    if (!s.isEmpty())
                    {
                        throw new VerifyError("stack not empty when return "+s);
                    }
                    return s.getMax();
                case GETSTATIC:
                    i = in.readUnsignedShort();
                    fi = (VariableElement) cf.getIndexedElement(i);
                    s.push(fi.asType());
                    break;
                case PUTSTATIC:
                    i = in.readUnsignedShort();
                    fi = (VariableElement) cf.getIndexedElement(i);
                    verify(fi.asType(), s.pop());
                    break;
                case GETFIELD:
                    i = in.readUnsignedShort();
                    verify(T.Object, s.pop());
                    fi = (VariableElement) cf.getIndexedElement(i);
                    s.push(fi.asType());
                    break;
                case PUTFIELD:
                    i = in.readUnsignedShort();
                    fi = (VariableElement) cf.getIndexedElement(i);
                    verify(fi.asType(), s.pop());
                    verify(T.Object, s.pop());
                    break;
                case INVOKEVIRTUAL:
                    i = in.readUnsignedShort();
                    me =  (ExecutableElement) cf.getIndexedElement(i);
                    verifyMethod(s, me);
                    verifyVirtualClass(s.pop(), me);
                    s.push(me.getReturnType());
                    break;
                case INVOKESPECIAL: // TODO is there any different in these options
                    i = in.readUnsignedShort();
                    if (cf.getIndexedElement(i).getKind() == ElementKind.CONSTRUCTOR)
                    {
                        co = (ExecutableElement) cf.getIndexedElement(i);
                        verifyMethod(s, co);
                        verifyClass(s.pop(), co);
                    }
                    else
                    {
                        me =  (ExecutableElement) cf.getIndexedElement(i);
                        verifyMethod(s, me);
                        verifyClass(s.pop(), me);
                        s.push(me.getReturnType());
                    }
                    break;
                case INVOKESTATIC:
                    i = in.readUnsignedShort();
                    me =  (ExecutableElement) cf.getIndexedElement(i);
                    verifyMethod(s, me);
                    s.push(me.getReturnType());
                    break;
                case INVOKEINTERFACE:
                    i = in.readUnsignedShort();
                    me =  (ExecutableElement) cf.getIndexedElement(i);
                    i = in.readUnsignedByte();
                    if (i == 0)
                    {
                        throw new IllegalArgumentException("count must not be zero");
                    }
                    i = in.readUnsignedByte();
                    if (i != 0)
                    {
                        throw new IllegalArgumentException("padding must be zero");
                    }
                    verifyMethod(s, me);
                    verifyClass(s.pop(), me);
                    s.push(me.getReturnType());
                    break;
                case NEW:
                    i = in.readUnsignedShort();
                    te = (TypeElement) cf.getIndexedElement(i);
                    cw = te.asType();
                    s.push(cw);
                    break;
                case NEWARRAY:
                    i = in.readByte();
                    verify(T.Int, s.pop());
                    switch (i)
                    {
                        case 4:
                            s.push(T.BooleanA);
                            break;
                        case 5:
                            s.push(T.CharA);
                            break;
                        case 6:
                            s.push(T.FloatA);
                            break;
                        case 7:
                            s.push(T.DoubleA);
                            break;
                        case 8:
                            s.push(T.ByteA);
                            break;
                        case 9:
                            s.push(T.ShortA);
                            break;
                        case 10:
                            s.push(T.IntA);
                            break;
                        case 11:
                            s.push(T.LongA);
                            break;
                        default:
                            throw new VerifyError("array type illegal");
                    }
                    break;
                case ANEWARRAY:
                    i = in.readUnsignedShort();
                    verify(T.Int, s.pop());
                    te = (TypeElement) cf.getIndexedElement(i);
                    s.push(T.ObjectA);
                    break;
                case ARRAYLENGTH:
                    verify(T.ObjectA, s.pop());
                    s.push(T.Int);
                    break;
                case ATHROW:
                    verify(T.Object, s.pop());
                    return s.getMax();
                case CHECKCAST:
                    i = in.readUnsignedShort();
                    v1 = s.pop();
                    verify(T.Object, v1);
                    te = (TypeElement) cf.getIndexedElement(i);
                    s.push(te.asType());
                    break;
                case INSTANCEOF:
                    i = in.readUnsignedShort();
                    verify(T.Object, s.pop());
                    s.push(T.Int);
                    break;
                case MONITORENTER:
                case MONITOREXIT:
                    verify(T.Object, s.pop());
                    break;
                case WIDE:
                    i = in.readUnsignedByte();
                    switch (i)
                    {
                        case ILOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(T.Int);
                            break;
                        case LLOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(T.Long);
                            break;
                        case FLOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(T.Float);
                            break;
                        case DLOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(T.Double);
                            break;
                        case ALOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(mc.getLocalType(i));
                            break;
                        case ISTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verify(T.Int, s.pop());
                            break;
                        case LSTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verify(T.Long, s.pop());
                            break;
                        case FSTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verify(T.Float, s.pop());
                            break;
                        case DSTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verify(T.Double, s.pop());
                            break;
                        case ASTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verifyLocalType(i, s.pop());
                            break;
                        case RET:
                            o = in.readUnsignedShort();
                            verify(returnAddress, mc.getLocalType(o));
                            return s.getMax();
                        case IINC:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(maxLocal, i);
                            c = in.readShort();
                            break;
                        default:
                            throw new IllegalArgumentException("unknown wide op code "+i);
                    }
                    break;
                case MULTIANEWARRAY:
                    i = in.readUnsignedShort();
                    verify(T.Int, s.pop());
                    te = (TypeElement) cf.getIndexedElement(i);
                    i = in.readUnsignedByte();
                    if (i == 0)
                    {
                        throw new IllegalArgumentException("zero dimension at multianewarray");
                    }
                    for (int ii=0;ii<i;ii++)
                    {
                        verify(T.Int, s.pop());
                    }
                    s.push(T.ObjectA);
                    break;
                case IFNULL:
                case IFNONNULL:
                    verify(T.Object, s.pop());
                    o = in.readShort();
                    branch(pc, o, s, lv);
                    break;
                case BREAKPOINT:
                    throw new UnsupportedOperationException("breakpoint");
                case IMPDEP1:
                    throw new UnsupportedOperationException("impdep1");
                case IMPDEP2:
                    throw new UnsupportedOperationException("impdep2");
                default:
                    throw new VerifyError("unknown opcode " + op);
            }
        }
        throw new VerifyError("running past code");
    }

    private void verify(TypeMirror assignee, TypeMirror assignable)
    {
        if (T.isInteger(assignee) && T.isInteger(assignable))
        {
            return;
        }
        if (assignable.getKind() == TypeKind.NULL && !T.isPrimitive(assignee) )
        {
            return;
        }
        if (!T.isAssignable(assignable, assignee))
        {
            throw new VerifyError(assignee+" not assignable from "+assignable);
        }
    }

    private void verify1(TypeMirror cls)
    {
        if (!c1(cls))
        {
            throw new VerifyError(cls+" not category 1");
        }
    }

    private void verify2(TypeMirror cls)
    {
        if (!c2(cls))
        {
            throw new VerifyError(cls+" not category 1");
        }
    }

    private boolean c1(TypeMirror cls)
    {
        return !c2(cls);
    }

    private boolean c2(TypeMirror cls)
    {
        return T.isCategory2(cls);
    }

    public int getMaxStack()
    {
        return maxStack;
    }

    private TypeMirror getLocalType(int index)
    {
        TypeMirror lc = mc.getLocalType(index);
        if (lc != null)
        {
            return lc;
        }
        else
        {
            throw new VerifyError("local variable at index "+index+" not set");
        }
    }

    private void verifyLocalType(int index, TypeMirror cls)
    {
        TypeMirror lc = mc.getLocalType(index);
        if (lc != null)
        {
            verify(lc, cls);
        }
        else
        {
            throw new VerifyError("local variable at index "+index+" not set");
        }
    }

    private void verifyClass(TypeMirror objectRef, ExecutableElement method)
    {
        TypeElement declaringClass = (TypeElement) method.getEnclosingElement();
        if (!T.isAssignable(objectRef, declaringClass.asType()))
        {
            throw new VerifyError("object "+objectRef+" for method "+method+" not compatible");
        }
        /*
        SubClass subClass = mc.getSubClass();
        if (!subClass.referencesMethod(method))
        {
            throw new VerifyError("object "+objectRef+" for method "+method+" not compatible");
        }
        */
    }

    private void verifyVirtualClass(TypeMirror objectRef, ExecutableElement method)
    {
        TypeElement declaringClass = (TypeElement) method.getEnclosingElement();
        if (!T.isAssignable(objectRef, declaringClass.asType()))
        {
            throw new VerifyError("object "+objectRef+" for method "+method+" not compatible");
        }
        TypeMirror thisClass = mc.getLocalType(0);
    }

    private void verifyReferenceOrArray(TypeMirror cw)
    {
        switch (cw.getKind())
        {
            case ARRAY:
            case DECLARED:
                return;
            default:
            throw new VerifyError(cw+" not assignable to array");
        }
    }

    private void verifyByteOrBoolean(TypeMirror cw)
    {
        switch (cw.getKind())
        {
            case BYTE:
            case BOOLEAN:
                return;
            default:
            throw new VerifyError(cw+" not byte or boolean");
        }
    }

    private void verifyByteOrBooleanArray(TypeMirror cw)
    {
        switch (cw.getKind())
        {
            case ARRAY:
                ArrayType at = (ArrayType) cw;
                verifyByteOrBoolean(at.getComponentType());
                return;
            default:
            throw new VerifyError(cw+" not byte or boolean array");
        }
    }

    private class Context
    {

        private int pc;
        private OperandStack s;
        private TypeMirror[] lvType;

        public Context(int pc, OperandStack s, TypeMirror[] lvType)
        {
            this.pc = pc;
            this.s = new OperandStack(s);
            this.lvType = lvType.clone();
        }

        public TypeMirror[] getLvType()
        {
            return lvType.clone();
        }

        public int getPc()
        {
            return pc;
        }

        public OperandStack getS()
        {
            return new OperandStack(s);
        }
    }
    private class OperandStack extends LinkedList<TypeMirror>
    {
        private int max;
        private int size;

        public OperandStack(OperandStack ms)
        {
            super(ms);
            this.max = ms.max;
            this.size = ms.size;
        }

        public OperandStack()
        {
        }

        @Override
        public OperandStack clone()
        {
            return (OperandStack) super.clone();
        }

        public int getMax()
        {
            return max;
        }

        @Override
        public void push(TypeMirror e)
        {
            if (e.getKind() != TypeKind.VOID)
            {
                super.push(e);
                if (c1(e))
                {
                    size++;
                }
                else
                {
                    size += 2;
                }
                max = Math.max(max, size);
            }
        }

        @Override
        public TypeMirror pop()
        {
            TypeMirror cls = super.pop();
            if (c1(cls))
            {
                size--;
            }
            else
            {
                size -= 2;
            }
            return cls;
        }

    }
    public static void verifyMethod(Deque<TypeMirror> stack, ExecutableElement method) throws ClassNotFoundException
    {
        List<VariableElement> parameters = new ArrayList<>();
        parameters.addAll(method.getParameters());
        Collections.reverse(parameters);
        int index = 0;
        for (VariableElement ve : parameters)
        {
            TypeMirror sub = stack.pop();
            if (T.isInteger(ve.asType()) && T.isInteger(sub))
            {
                // boolean, byte, char, short, int are assignable at byte code
            }
            else
            {
                if (!T.isAssignable(sub, ve.asType()))
                {
                    throw new VerifyError("method "+method+"expected "+ve+" but got "+sub+" for arg "+index);
                }
            }
            index++;
        }
    }
}
