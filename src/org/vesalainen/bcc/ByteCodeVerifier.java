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
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.vesalainen.bcc.type.Generics;

/**
 *
 * @author tkv
 */
public class ByteCodeVerifier extends OpCodeUtil
{
    private static final Type returnAddress = ReturnAddress.class;

    private ClassFile cf;
    private Deque<Context> branch = new ArrayDeque<>();
    private CodeDataInput in;
    private int opCodePosition;
    private Set<Integer> goneThrough = new HashSet<>();
    private Map<Integer,Deque<Type>> stackAt = new HashMap<>();
    private Map<Integer,Type[]> lvAt = new HashMap<>();
    private int maxLocal;
    private int maxStack;
    private byte[] code;
    private ExceptionTable[] exceptionTable;
    private MethodCompiler mc;
    private boolean[] coverage; // TODO evaluate BitSet!
    private Type[] lvType;

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
        lvType = new Type[mc.localSize()];
        int index = 0;
        lvType[index++] = classFile.getClassName();

        for (Type param : mc.getParameters())
        {
            if (Generics.isInteger(param))
            {
                lvType[index] = int.class;
            }
            else
            {
                lvType[index] = param;
            }
            if (c2(lvType[index]))
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
                os.add(Throwable.class);
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
            String at = mc.getMethodName()+" At "+opCodePosition+": ";
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
            throw new IllegalArgumentException(mc.getMethodName()+": part of code not covered");
        }
    }
    private void branch(int pc, int offset, OperandStack s, Type[] lv)
    {
        maxStack = Math.max(maxStack, s.getMax());
        int target = pc+offset;
        if (target < 0 || target > 0xffff)
        {
            throw new VerifyError(mc.getMethodName()+" goto target illegal "+target);
        }
        Deque<Type> old = stackAt.get(target);
        if (old != null)
        {
            String stackBefore = old.toString();
            String stackNow = s.toString();
            if (!stackBefore.equals(stackNow))
            {
                throw new VerifyError(mc.getMethodName()+" stack differs in branch "+pc+" -> "+target
                        +"  before:"+stackBefore+"  now   :"+stackNow);
            }
            Type[] lvBefore = lvAt.get(target);
            Type[] lvNow = lv;
            if (!compare(lvBefore, lvNow))
            {
                throw new VerifyError(mc.getMethodName()+" local variables differ in branch "+pc+" -> "+target
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

    private boolean compare(Type[] lvBefore, Type[] lvNow)
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
            if (!Generics.isPrimitive(lvBefore[ii]) && Generics.isPrimitive(lvNow[ii]))
            {
                return false;
            }
            if (Generics.isPrimitive(lvBefore[ii]) && !Generics.isPrimitive(lvNow[ii]))
            {
                return false;
            }
            if (!Generics.isPrimitive(lvBefore[ii]) && !Generics.isPrimitive(lvNow[ii]))
            {
                continue;
            }
            if (!lvBefore[ii].equals(lvNow[ii]))
            {
                return false;
            }
        }
        return true;
    }
    private int run(Context ctx) throws IOException, ClassNotFoundException
    {
        OperandStack s = ctx.getS(); // stack
        Type[] lv = ctx.getLvType();
        in = in.branch(ctx.getPc());
        int pc = 0;  // index
        int i = 0;  // index
        int c = 0;  // const
        int o = 0;  // offset
        int def = 0;
        int low = 0;
        int high = 0;
        Type v1;
        Type v2 = null;
        Type v3;
        Type v4;
        Member fi;
        Member me = null;
        ConstantInfo ci = null;
        Type cw;
        Member co;
        while (in.available() > 0)
        {
            pc = in.pc();
            opCodePosition = pc;
            Deque<Type> ns = new LinkedList<>();
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
                    s.push(int.class);
                    break;
                case LCONST_0:
                case LCONST_1:
                    s.push(long.class);
                    break;
                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                    s.push(float.class);
                    break;
                case DCONST_0:
                case DCONST_1:
                    s.push(double.class);
                    break;
                case BIPUSH:
                    in.readByte();
                    s.push(int.class);
                    break;
                case SIPUSH:
                    in.readShort();
                    s.push(int.class);
                    break;
                case LDC:
                    i = in.readUnsignedByte();
                    ci = cf.getConstantInfo(i);
                    switch (ci.getTag())
                    {
                        case ConstantInfo.CONSTANT_Integer:
                            s.push(int.class);
                            break;
                        case ConstantInfo.CONSTANT_Float:
                            s.push(float.class);
                            break;
                        case ConstantInfo.CONSTANT_String:
                            s.push(String.class);
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
                            s.push(int.class);
                            break;
                        case ConstantInfo.CONSTANT_Float:
                            s.push(float.class);
                            break;
                        case ConstantInfo.CONSTANT_String:
                            s.push(String.class);
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
                            s.push(long.class);
                            break;
                        case ConstantInfo.CONSTANT_Double:
                            s.push(double.class);
                            break;
                        default:
                            throw new VerifyError("illegal constant info for ldc2_w "+ci.getTag());
                    }
                    break;
                case ILOAD:
                    i = in.readUnsignedByte();
                    if (!int.class.equals(lvType[i]))
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to int");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(int.class);
                    break;
                case LLOAD:
                    i = in.readUnsignedByte();
                    if (!long.class.equals(lvType[i]) || lvType[i+1] != null)
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to long");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(long.class);
                    break;
                case FLOAD:
                    i = in.readUnsignedByte();
                    if (!float.class.equals(lvType[i]))
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to float");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(float.class);
                    break;
                case DLOAD:
                    i = in.readUnsignedByte();
                    if (!double.class.equals(lvType[i]) || lvType[i+1] != null)
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to double");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(double.class);
                    break;
                case ALOAD:
                    i = in.readUnsignedByte();
                    if (lvType[i] == null || Generics.isPrimitive(lvType[i]))
                    {
                        throw new VerifyError("local variable "+i+" "+mc.getLocalName(i)+" not initialized to reference");
                    }
                    maxLocal = Math.max(i, maxLocal);
                    s.push(mc.getLocalType(i));
                    break;
                case ILOAD_0:
                    if (!int.class.equals(lvType[0]))
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to int");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(int.class);
                    break;
                case ILOAD_1:
                    if (!int.class.equals(lvType[1]))
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to int");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(int.class);
                    break;
                case ILOAD_2:
                    if (!int.class.equals(lvType[2]))
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to int");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(int.class);
                    break;
                case ILOAD_3:
                    if (!int.class.equals(lvType[3]))
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to int");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(int.class);
                    break;
                case LLOAD_0:
                    if (!long.class.equals(lvType[0]) || lvType[1] != null)
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to long");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(long.class);
                    break;
                case LLOAD_1:
                    if (!long.class.equals(lvType[1]) || lvType[2] != null)
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to long");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(long.class);
                    break;
                case LLOAD_2:
                    if (!long.class.equals(lvType[2]) || lvType[3] != null)
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to long");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(long.class);
                    break;
                case LLOAD_3:
                    if (!long.class.equals(lvType[3]) || lvType[4] != null)
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to long");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(long.class);
                    break;
                case FLOAD_0:
                    if (!float.class.equals(lvType[0]))
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to float");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(float.class);
                    break;
                case FLOAD_1:
                    if (!float.class.equals(lvType[1]))
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to float");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(float.class);
                    break;
                case FLOAD_2:
                    if (!float.class.equals(lvType[2]))
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to float");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(float.class);
                    break;
                case FLOAD_3:
                    if (!float.class.equals(lvType[3]))
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to float");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(float.class);
                    break;
                case DLOAD_0:
                    if (!double.class.equals(lvType[0]) || lvType[1] != null)
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to double");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(double.class);
                    break;
                case DLOAD_1:
                    if (!double.class.equals(lvType[1]) || lvType[2] != null)
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to double");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(double.class);
                    break;
                case DLOAD_2:
                    if (!double.class.equals(lvType[2]) || lvType[3] != null)
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to double");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(double.class);
                    break;
                case DLOAD_3:
                    if (!double.class.equals(lvType[3]) || lvType[4] != null)
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to double");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(double.class);
                    break;
                case ALOAD_0:
                    if (lvType[0] == null || Generics.isPrimitive(lvType[0]))
                    {
                        throw new VerifyError("local variable 0 "+mc.getLocalName(0)+" not initialized to reference");
                    }
                    maxLocal = Math.max(0, maxLocal);
                    s.push(mc.getLocalType(0));
                    break;
                case ALOAD_1:
                    if (lvType[1] == null || Generics.isPrimitive(lvType[1]))
                    {
                        throw new VerifyError("local variable 1 "+mc.getLocalName(1)+" not initialized to reference");
                    }
                    maxLocal = Math.max(1, maxLocal);
                    s.push(mc.getLocalType(1));
                    break;
                case ALOAD_2:
                    if (lvType[2] == null || Generics.isPrimitive(lvType[2]))
                    {
                        throw new VerifyError("local variable 2 "+mc.getLocalName(2)+" not initialized to reference");
                    }
                    maxLocal = Math.max(2, maxLocal);
                    s.push(mc.getLocalType(2));
                    break;
                case ALOAD_3:
                    if (lvType[3] == null || Generics.isPrimitive(lvType[3]))
                    {
                        throw new VerifyError("local variable 3 "+mc.getLocalName(3)+" not initialized to reference");
                    }
                    maxLocal = Math.max(3, maxLocal);
                    s.push(mc.getLocalType(3));
                    break;
                case IALOAD:
                    verify(int.class, s.pop());
                    verify(int[].class, s.pop());
                    s.push(int.class);
                    break;
                case LALOAD:
                    verify(int.class, s.pop());
                    verify(long[].class, s.pop());
                    s.push(long.class);
                    break;
                case FALOAD:
                    verify(int.class, s.pop());
                    verify(float[].class, s.pop());
                    s.push(float.class);
                    break;
                case DALOAD:
                    verify(int.class, s.pop());
                    verify(double[].class, s.pop());
                    s.push(double.class);
                    break;
                case AALOAD:
                    verify(int.class, s.pop());
                    v1 = s.pop();
                    verify(Object[].class, v1);
                    s.push(Generics.getComponentType(v1));
                    break;
                case BALOAD:
                    verify(int.class, s.pop());
                    verifyByteOrBooleanArray(s.pop());
                    s.push(int.class);
                    break;
                case CALOAD:
                    verify(int.class, s.pop());
                    verify(char[].class, s.pop());
                    s.push(int.class);
                    break;
                case SALOAD:
                    verify(int.class, s.pop());
                    verify(short[].class, s.pop());
                    s.push(int.class);
                    break;
                case ISTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verify(int.class, s.pop());
                    lvType[i] = int.class;
                    break;
                case LSTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verify(long.class, s.pop());
                    lvType[i] = long.class;
                    lvType[i+1] = null;
                    break;
                case FSTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verify(float.class, s.pop());
                    lvType[i] = float.class;
                    break;
                case DSTORE:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(i, maxLocal);
                    verify(double.class, s.pop());
                    lvType[i] = double.class;
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
                    verify(int.class, s.pop());
                    lvType[0] = int.class;
                    break;
                case ISTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verify(int.class, s.pop());
                    lvType[1] = int.class;
                    break;
                case ISTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verify(int.class, s.pop());
                    lvType[2] = int.class;
                    break;
                case ISTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verify(int.class, s.pop());
                    lvType[3] = int.class;
                    break;
                case LSTORE_0:
                    maxLocal = Math.max(0, maxLocal);
                    verify(long.class, s.pop());
                    lvType[0] = long.class;
                    lvType[1] = null;
                    break;
                case LSTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verify(long.class, s.pop());
                    lvType[1] = long.class;
                    lvType[2] = null;
                    break;
                case LSTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verify(long.class, s.pop());
                    lvType[2] = long.class;
                    lvType[3] = null;
                    break;
                case LSTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verify(long.class, s.pop());
                    lvType[3] = long.class;
                    lvType[4] = null;
                    break;
                case FSTORE_0:
                    maxLocal = Math.max(0, maxLocal);
                    verify(float.class, s.pop());
                    lvType[0] = float.class;
                    break;
                case FSTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verify(float.class, s.pop());
                    lvType[1] = float.class;
                    break;
                case FSTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verify(float.class, s.pop());
                    lvType[2] = float.class;
                    break;
                case FSTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verify(float.class, s.pop());
                    lvType[3] = float.class;
                    break;
                case DSTORE_0:
                    maxLocal = Math.max(0, maxLocal);
                    verify(double.class, s.pop());
                    lvType[0] = double.class;
                    lvType[1] = null;
                    break;
                case DSTORE_1:
                    maxLocal = Math.max(1, maxLocal);
                    verify(double.class, s.pop());
                    lvType[1] = double.class;
                    lvType[2] = null;
                    break;
                case DSTORE_2:
                    maxLocal = Math.max(2, maxLocal);
                    verify(double.class, s.pop());
                    lvType[2] = double.class;
                    lvType[3] = null;
                    break;
                case DSTORE_3:
                    maxLocal = Math.max(3, maxLocal);
                    verify(double.class, s.pop());
                    lvType[3] = double.class;
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
                    verify(int.class, s.pop());
                    verify(int.class, s.pop());
                    verify(int[].class, s.pop());
                    break;
                case LASTORE:
                    verify(long.class, s.pop());
                    verify(int.class, s.pop());
                    verify(long[].class, s.pop());
                    break;
                case FASTORE:
                    verify(float.class, s.pop());
                    verify(int.class, s.pop());
                    verify(float[].class, s.pop());
                    break;
                case DASTORE:
                    verify(double.class, s.pop());
                    verify(int.class, s.pop());
                    verify(double[].class, s.pop());
                    break;
                case AASTORE:
                    verifyReferenceOrArray(s.pop());
                    verify(int.class, s.pop());
                    verify(Object[].class, s.pop());
                    break;
                case BASTORE:
                    verify(int.class, s.pop());
                    verify(int.class, s.pop());
                    verifyByteOrBooleanArray(s.pop());
                    break;
                case CASTORE:
                    verify(int.class, s.pop());
                    verify(int.class, s.pop());
                    verify(char[].class, s.pop());
                    break;
                case SASTORE:
                    verify(short.class, s.pop());
                    verify(int.class, s.pop());
                    verify(short[].class, s.pop());
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
                    verify(int.class, s.pop());
                    verify(int.class, s.pop());
                    s.push(int.class);
                    break;
                case LADD:
                case LSUB:
                case LMUL:
                case LDIV:
                case LREM:
                case LAND:
                case LOR:
                case LXOR:
                    verify(long.class, s.pop());
                    verify(long.class, s.pop());
                    s.push(long.class);
                    break;
                case FADD:
                case FSUB:
                case FMUL:
                case FDIV:
                case FREM:
                    verify(float.class, s.pop());
                    verify(float.class, s.pop());
                    s.push(float.class);
                    break;
                case DADD:
                case DSUB:
                case DMUL:
                case DDIV:
                    verify(double.class, s.pop());
                    verify(double.class, s.pop());
                    s.push(double.class);
                    break;
                case INEG:
                    verify(int.class, s.peek());
                    break;
                case LNEG:
                    verify(long.class, s.peek());
                    break;
                case FNEG:
                    verify(float.class, s.peek());
                    break;
                case DNEG:
                    verify(double.class, s.peek());
                    break;
                case ISHL:
                case ISHR:
                case IUSHR:
                    verify(int.class, s.pop());
                    verify(int.class, s.pop());
                    s.push(int.class);
                    break;
                case LSHL:
                case LSHR:
                case LUSHR:
                    verify(int.class, s.pop());
                    verify(long.class, s.pop());
                    s.push(long.class);
                    break;
                case IINC:
                    i = in.readUnsignedByte();
                    maxLocal = Math.max(maxLocal, i);
                    c = in.readByte();
                    break;
                case I2L:
                    verify(int.class, s.pop());
                    s.push(long.class);
                    break;
                case I2F:
                    verify(int.class, s.pop());
                    s.push(float.class);
                    break;
                case I2D:
                    verify(int.class, s.pop());
                    s.push(double.class);
                    break;
                case L2I:
                    verify(long.class, s.pop());
                    s.push(int.class);
                    break;
                case L2F:
                    verify(long.class, s.pop());
                    s.push(float.class);
                    break;
                case L2D:
                    verify(long.class, s.pop());
                    s.push(double.class);
                    break;
                case F2I:
                    verify(float.class, s.pop());
                    s.push(int.class);
                    break;
                case F2L:
                    verify(float.class, s.pop());
                    s.push(long.class);
                    break;
                case F2D:
                    verify(float.class, s.pop());
                    s.push(double.class);
                    break;
                case D2I:
                    verify(double.class, s.pop());
                    s.push(int.class);
                    break;
                case D2L:
                    verify(double.class, s.pop());
                    s.push(float.class);
                    break;
                case D2F:
                    verify(double.class, s.pop());
                    s.push(float.class);
                    break;
                case I2B:
                case I2C:
                case I2S:
                    verify(int.class, s.pop());
                    s.push(int.class);
                    break;
                case LCMP:
                    verify(long.class, s.pop());
                    verify(long.class, s.pop());
                    s.push(int.class);
                    break;
                case FCMPL:
                case FCMPG:
                    verify(float.class, s.pop());
                    verify(float.class, s.pop());
                    s.push(int.class);
                    break;
                case DCMPL:
                case DCMPG:
                    verify(double.class, s.pop());
                    verify(double.class, s.pop());
                    s.push(int.class);
                    break;
                case IFEQ:
                case IFNE:
                case IFLT:
                case IFGE:
                case IFGT:
                case IFLE:
                    o = in.readShort();
                    verify(int.class, s.pop());
                    branch(pc, o, s, lv);
                    break;
                case IF_ICMPEQ:
                case IF_ICMPNE:
                case IF_ICMPLT:
                case IF_ICMPGE:
                case IF_ICMPGT:
                case IF_ICMPLE:
                    o = in.readShort();
                    verify(int.class, s.pop());
                    verify(int.class, s.pop());
                    branch(pc, o, s, lv);
                    break;
                case IF_ACMPEQ:
                case IF_ACMPNE:
                    o = in.readShort();
                    verify(Object.class, s.pop());
                    verify(Object.class, s.pop());
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
                    verify(int.class, s.pop());
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
                    verify(int.class, s.pop());
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
                    verify(int.class, s.pop());
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
                    verify(long.class, s.pop());
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
                    verify(float.class, s.pop());
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
                    verify(double.class, s.pop());
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
                    fi = (Member) cf.getElement(i);
                    s.push(Generics.getType(fi));
                    break;
                case PUTSTATIC:
                    i = in.readUnsignedShort();
                    fi = (Member) cf.getElement(i);
                    verify(Generics.getType(fi), s.pop());
                    break;
                case GETFIELD:
                    i = in.readUnsignedShort();
                    verify(Object.class, s.pop());
                    fi = (Member) cf.getElement(i);
                    s.push(Generics.getType(fi));
                    break;
                case PUTFIELD:
                    i = in.readUnsignedShort();
                    fi = (Member) cf.getElement(i);
                    verify(Generics.getType(fi), s.pop());
                    verify(Object.class, s.pop());
                    break;
                case INVOKEVIRTUAL:
                    i = in.readUnsignedShort();
                    me =  (Member) cf.getElement(i);
                    verifyMethod(s, me);
                    verifyVirtualClass(s.pop(), me);
                    s.push(Generics.getReturnType(me));
                    break;
                case INVOKESPECIAL: // TODO is there any different in these options
                    i = in.readUnsignedShort();
                    if (Generics.isConstructor(cf.getElement(i)))
                    {
                        co = (Member) cf.getElement(i);
                        verifyConstructor(s, co);
                        verifyClass(s.pop(), co);
                    }
                    else
                    {
                        me =  (Member) cf.getElement(i);
                        verifyMethod(s, me);
                        verifyClass(s.pop(), me);
                        s.push(Generics.getReturnType(me));
                    }
                    break;
                case INVOKESTATIC:
                    i = in.readUnsignedShort();
                    me =  (Member) cf.getElement(i);
                    verifyMethod(s, me);
                    s.push(Generics.getReturnType(me));
                    break;
                case INVOKEINTERFACE:
                    i = in.readUnsignedShort();
                    me =  (Member) cf.getElement(i);
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
                    s.push(Generics.getReturnType(me));
                    break;
                case NEW:
                    i = in.readUnsignedShort();
                    cw = (Type) cf.getElement(i);
                    s.push(cw);
                    break;
                case NEWARRAY:
                    i = in.readByte();
                    verify(int.class, s.pop());
                    switch (i)
                    {
                        case 4:
                            s.push(boolean[].class);
                            break;
                        case 5:
                            s.push(char[].class);
                            break;
                        case 6:
                            s.push(float[].class);
                            break;
                        case 7:
                            s.push(double[].class);
                            break;
                        case 8:
                            s.push(byte[].class);
                            break;
                        case 9:
                            s.push(short[].class);
                            break;
                        case 10:
                            s.push(int[].class);
                            break;
                        case 11:
                            s.push(long[].class);
                            break;
                        default:
                            throw new VerifyError("array type illegal");
                    }
                    break;
                case ANEWARRAY:
                    i = in.readUnsignedShort();
                    verify(int.class, s.pop());
                    cw = (Type) cf.getElement(i);
                    s.push(Object[].class);
                    break;
                case ARRAYLENGTH:
                    verify(Object[].class, s.pop());
                    s.push(int.class);
                    break;
                case ATHROW:
                    verify(Object.class, s.pop());
                    return s.getMax();
                case CHECKCAST:
                    i = in.readUnsignedShort();
                    v1 = s.pop();
                    verify(Object.class, v1);
                    cw = (Type) cf.getElement(i);
                    s.push(cw);
                    break;
                case INSTANCEOF:
                    i = in.readUnsignedShort();
                    verify(Object.class, s.pop());
                    s.push(int.class);
                    break;
                case MONITORENTER:
                case MONITOREXIT:
                    verify(Object.class, s.pop());
                    break;
                case WIDE:
                    i = in.readUnsignedByte();
                    switch (i)
                    {
                        case ILOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(int.class);
                            break;
                        case LLOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(long.class);
                            break;
                        case FLOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(float.class);
                            break;
                        case DLOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(double.class);
                            break;
                        case ALOAD:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            s.push(mc.getLocalType(i));
                            break;
                        case ISTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verify(int.class, s.pop());
                            break;
                        case LSTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verify(long.class, s.pop());
                            break;
                        case FSTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verify(float.class, s.pop());
                            break;
                        case DSTORE:
                            i = in.readUnsignedShort();
                            maxLocal = Math.max(i, maxLocal);
                            verify(double.class, s.pop());
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
                    verify(int.class, s.pop());
                    cw = (Type) cf.getElement(i);
                    i = in.readUnsignedByte();
                    if (i == 0)
                    {
                        throw new IllegalArgumentException("zero dimension at multianewarray");
                    }
                    for (int ii=0;ii<i;ii++)
                    {
                        verify(int.class, s.pop());
                    }
                    s.push(Object[].class);
                    break;
                case IFNULL:
                case IFNONNULL:
                    verify(Object.class, s.pop());
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

    private void verify(Type assignee, Type assignable)
    {
        if (Generics.isInteger(assignee) && Generics.isInteger(assignable))
        {
            return;
        }
        if (assignable == null && !Generics.isPrimitive(assignee) )
        {
            return;
        }
        if (!Generics.isAssignableFrom(assignee, assignable))
        {
            throw new VerifyError(assignee+" not assignable from "+assignable);
        }
    }

    private void verify1(Type cls)
    {
        if (!c1(cls))
        {
            throw new VerifyError(cls+" not category 1");
        }
    }

    private void verify2(Type cls)
    {
        if (!c2(cls))
        {
            throw new VerifyError(cls+" not category 1");
        }
    }

    private boolean c1(Type cls)
    {
        return !c2(cls);
    }

    private boolean c2(Type cls)
    {
        return Generics.isCategory2(cls);
    }

    public int getMaxStack()
    {
        return maxStack;
    }

    private Type getLocalType(int index)
    {
        Type lc = mc.getLocalType(index);
        if (lc != null)
        {
            return lc;
        }
        else
        {
            throw new VerifyError("local variable at index "+index+" not set");
        }
    }

    private void verifyLocalType(int index, Type cls)
    {
        Type lc = mc.getLocalType(index);
        if (lc != null)
        {
            verify(lc, cls);
        }
        else
        {
            throw new VerifyError("local variable at index "+index+" not set");
        }
    }

    private void verifyClass(Type objectRef, Member method)
    {
        Type declaringClass = Generics.getDeclaringClass(method);
        if (!Generics.isAssignableFrom(declaringClass, objectRef))
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

    private void verifyVirtualClass(Type objectRef, Member method)
    {
        Type declaringClass = Generics.getDeclaringClass(method);
        if (!Generics.isAssignableFrom(declaringClass, objectRef))
        {
            throw new VerifyError("object "+objectRef+" for method "+method+" not compatible");
        }
        Type thisClass = mc.getLocalType(0);
        if (thisClass.equals(objectRef))
        {
            SubClass subClass = mc.getSubClass();
            if (!subClass.referencesMethod(method))
            {
                throw new VerifyError("object "+objectRef+" for method "+method+" not compatible");
            }
        }
        else
        {
            // must be an existing super class
            Type superClass;
            superClass = objectRef;
            Member m = findSuperClassMethod(superClass, method);
            if (method == null)
            {
                throw new VerifyError("object "+objectRef+" for method "+method+" not compatible");
            }
        }
    }
    private Member findSuperClassMethod(Type superClass, Member me) throws SecurityException
    {
        Type[] parameters;
        parameters = Generics.getParameterTypes(me);
        try
        {
            return Generics.getDeclaredMethod(superClass, Generics.getName(me), parameters);
        }
        catch (NoSuchMethodException ex)
        {
            superClass = Generics.getSuperclass(superClass);
            if (superClass != null)
            {
                return findSuperClassMethod(superClass, me);
            }
            else
            {
                return null;
            }
        }
    }

    private void verifyReferenceOrArray(Type cw)
    {
        if (Generics.isArray(cw))
        {
            return;
        }
        if (Generics.isReference(cw))
        {
            return;
        }
        throw new VerifyError(cw+" not assignable to array");
    }

    private void verifyByteOrBoolean(Type cw)
    {
        if (byte.class.equals(cw))
        {
            return;
        }
        if (boolean.class.equals(cw))
        {
            return;
        }
        throw new VerifyError(cw+" not byte or boolean");
    }

    private void verifyByteOrBooleanArray(Type cw)
    {
        if (byte[].class.equals(cw))
        {
            return;
        }
        if (boolean[].class.equals(cw))
        {
            return;
        }
        throw new VerifyError(cw+" not byte or boolean");
    }

    private class Context
    {

        private int pc;
        private OperandStack s;
        private Type[] lvType;

        public Context(int pc, OperandStack s, Type[] lvType)
        {
            this.pc = pc;
            this.s = new OperandStack(s);
            this.lvType = lvType.clone();
        }

        public Type[] getLvType()
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
    private class OperandStack extends LinkedList<Type>
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
        public void push(Type e)
        {
            if (!void.class.equals(e))
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
        public Type pop()
        {
            Type cls = super.pop();
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
    public static void verifyConstructor(Deque<Type> stack, Member method) throws ClassNotFoundException
    {
        Type[] parameters = Generics.getParameterTypes(method);
        for (int ii=parameters.length-1;ii>=0;ii--)
        {
            Type sub = stack.pop();
            if (!Generics.isAssignableFrom(parameters[ii], sub))
            {
                throw new VerifyError("method "+method+"expected "+parameters[ii]+" but got "+sub+" for arg "+ii);
            }
        }
    }
    public static void verifyMethod(Deque<Type> stack, Member method) throws ClassNotFoundException
    {
        Type[] parameters = Generics.getParameterTypes(method);
        for (int ii=parameters.length-1;ii>=0;ii--)
        {
            Type sub = stack.pop();
            if (Generics.isInteger(parameters[ii]) && Generics.isInteger(sub))
            {
                continue;   // boolean, byte, char, short, int are assignable at byte code
            }
            if (!Generics.isAssignableFrom(parameters[ii], sub))
            {
                throw new VerifyError("method "+method+" expected "+parameters[ii]+" but got "+sub+" for arg "+ii);
            }
        }
    }
}
