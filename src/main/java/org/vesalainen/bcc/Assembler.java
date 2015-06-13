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

import org.vesalainen.bcc.Label.Branch;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.Typ;

/**
 *
 * @author tkv
 */
public class Assembler implements OpCode
{

    public static final int T_BOOLEAN = 4;
    public static final int T_CHAR = 5;
    public static final int T_FLOAT = 6;
    public static final int T_DOUBLE = 7;
    public static final int T_BYTE = 8;
    public static final int T_SHORT = 9;
    public static final int T_INT = 10;
    public static final int T_LONG = 11;
    
    protected static final int WIDEFIXOFFSET = 8;
    protected CodeDataOutput out;
    protected Map<String, Label> labels = new HashMap<>();
    protected Map<Integer, Label> labelMap = new HashMap<>();
    protected Deque<TypeASM> asmStack = new ArrayDeque<>();
    private int nextBranch;
    protected TypeASM asm;
    protected Map<TypeKind,TypeASM> types;
    private BooleanASM z;
    private ByteASM b;
    private CharASM c;
    private ShortASM s;
    private IntASM i;
    private LongASM l;
    private FloatASM f;
    private DoubleASM d;
    private VoidASM v;
    private ReferenceASM a;
    private boolean optimizeGoto = true;
    protected boolean wideIndex;


    public Assembler()
    {
        out = new CodeDataOutput(this);
        types = new EnumMap<>(TypeKind.class);
        z = new BooleanASM(out, labels);
        b = new ByteASM(out, labels);
        c = new CharASM(out, labels);
        s = new ShortASM(out, labels);
        i = new IntASM(out, labels);
        l = new LongASM(out, labels);
        f = new FloatASM(out, labels);
        d = new DoubleASM(out, labels);
        a = new ReferenceASM(out, labels);
        v = new VoidASM(out, labels);
        types.put(TypeKind.BOOLEAN, z);
        types.put(TypeKind.BYTE, b);
        types.put(TypeKind.CHAR, c);
        types.put(TypeKind.SHORT, s);
        types.put(TypeKind.INT, i);
        types.put(TypeKind.LONG, l);
        types.put(TypeKind.FLOAT, f);
        types.put(TypeKind.DOUBLE, d);
        types.put(TypeKind.ARRAY, a);
        types.put(TypeKind.DECLARED, a);
        types.put(TypeKind.TYPEVAR, a);
        types.put(TypeKind.VOID, v);
    }

    protected Assembler(CodeDataOutput out, Map<String, Label> labels)
    {
        this.out = out;
        this.labels = labels;
    }
    /**
     * Set goto and jsr to use wide index
     * @param wideIndex 
     */
    public void setWideIndex(boolean wideIndex)
    {
        this.wideIndex = wideIndex;
        if (types != null)
        {
            for (TypeASM t : types.values())
            {
                Assembler as = (Assembler) t;
                as.setWideIndex(wideIndex);
            }
        }
    }

    /**
     * Fixes address here for object
     * @param name
     * @throws IOException
     */
    public void fixAddress(String name) throws IOException
    {
        Label label = labels.get(name);
        if (label == null)
        {
            label = new Label(name);
            labels.put(name, label);
        }
        int pos = position();
        label.setAddress(pos);
        labelMap.put(pos, label);
    }

    public Label getLabel(String name)
    {
        Label label = labels.get(name);
        if (label == null)
        {
            label = new Label(name);
            labels.put(name, label);
        }
        return label;
    }
    
    public Label labelForAddress(int address)
    {
        return labelMap.get(address);
    }

    /**
     * Creates a branch for possibly unknown address
     * @param name
     * @return
     * @throws IOException
     */
    public Branch createBranch(String name) throws IOException
    {
        Label label = labels.get(name);
        if (label == null)
        {
            label = new Label(name);
            labels.put(name, label);
        }
        return label.createBranch(position());
    }

    public boolean isReferenced(String target)
    {
        return labels.containsKey(target);
    }

    public byte[] getCode()
    {
        return out.getCode();
    }
    
    public void fixLabels(byte[] code)
    {
        if (code.length > 0xfffe)
        {
            throw new BranchException("code size "+code.length+" > 65534");
        }
        for (Label label : labels.values())
        {
            label.fixCode(code);
        }
    }

    public int position() throws IOException
    {
        return out.position();
    }
    /**
     * @param type
     */
    private void pushType(TypeMirror type)
    {
        asm = types.get(type.getKind());
        if (asm == null)
        {
            throw new IllegalArgumentException(type+" wrong type");
        }
        asmStack.push(asm);
    }
    /**
     */
    private void popType()
    {
        asmStack.pop();
        asm = asmStack.peek();
    }
    /**
     * @throws IOException
     */
    private void txor() throws IOException
    {
        asm.txor();
    }

    public void txor(TypeMirror type) throws IOException
    {
        pushType(type);
        txor();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tushr() throws IOException
    {
        asm.tushr();
    }

    public void tushr(TypeMirror type) throws IOException
    {
        pushType(type);
        tushr();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tsub() throws IOException
    {
        asm.tsub();
    }

    public void tsub(TypeMirror type) throws IOException
    {
        pushType(type);
        tsub();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tstore(int index) throws IOException
    {
        asm.tstore(index);
    }

    public void tstore(TypeMirror type, int index) throws IOException
    {
        pushType(type);
        tstore(index);
        popType();
    }
    /**
     * @throws IOException
     */
    private void tshr() throws IOException
    {
        asm.tshr();
    }

    public void tshr(TypeMirror type) throws IOException
    {
        pushType(type);
        tshr();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tshl() throws IOException
    {
        asm.tshl();
    }

    public void tshl(TypeMirror type) throws IOException
    {
        pushType(type);
        tshl();
        popType();
    }
    /**
     * @throws IOException
     */
    private void treturn() throws IOException
    {
        asm.treturn();
    }

    public void treturn(TypeMirror type) throws IOException
    {
        pushType(type);
        treturn();
        popType();
    }
    /**
     * @throws IOException
     */
    private void trem() throws IOException
    {
        asm.trem();
    }

    public void trem(TypeMirror type) throws IOException
    {
        pushType(type);
        trem();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tor() throws IOException
    {
        asm.tor();
    }

    public void tor(TypeMirror type) throws IOException
    {
        pushType(type);
        tor();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tneg() throws IOException
    {
        asm.tneg();
    }

    public void tneg(TypeMirror type) throws IOException
    {
        pushType(type);
        tneg();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tmul() throws IOException
    {
        asm.tmul();
    }

    public void tmul(TypeMirror type) throws IOException
    {
        pushType(type);
        tmul();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tload(int index) throws IOException
    {
        asm.tload(index);
    }

    public void tload(TypeMirror type, int index) throws IOException
    {
        pushType(type);
        tload(index);
        popType();
    }
    /**
     * @throws IOException
     */
    private void tipush(int b) throws IOException
    {
        asm.tipush(b);
    }

    public void tipush(TypeMirror type, int b) throws IOException
    {
        pushType(type);
        tipush(b);
        popType();
    }
    /**
     * @throws IOException
     */
    private void tinc(int index, int con) throws IOException
    {
        asm.tinc(index, con);
    }

    public void tinc(TypeMirror type, int index, int con) throws IOException
    {
        pushType(type);
        tinc(index, con);
        popType();
    }
    /**
     * @throws IOException
     */
    private void tdiv() throws IOException
    {
        asm.tdiv();
    }

    public void tdiv(TypeMirror type) throws IOException
    {
        pushType(type);
        tdiv();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tconst_null() throws IOException
    {
        asm.tconst_null();
    }
    /**
     * @throws IOException
     */
    private void tconst(int i) throws IOException
    {
        asm.tconst(i);
    }

    public void tconst(TypeMirror type, int i) throws IOException
    {
        pushType(type);
        tconst(i);
        popType();
    }

    public void tcmpl() throws IOException
    {
        asm.tcmpl();
    }

    public void tcmpl(TypeMirror type) throws IOException
    {
        pushType(type);
        tcmpl();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tcmpg() throws IOException
    {
        asm.tcmpg();
    }

    public void tcmpg(TypeMirror type) throws IOException
    {
        pushType(type);
        tcmpg();
        popType();
    }
    /**
     * @throws IOException
     */
    private void tcmp() throws IOException
    {
        asm.tcmp();
    }

    public void tcmp(TypeMirror type) throws IOException
    {
        pushType(type);
        tcmp();
        popType();
    }
    /**
     * Store into array
     * <p>Stack: ..., arrayref, index, value =&gt; ...
     * @throws IOException
     */
    private void tastore() throws IOException
    {
        asm.tastore();
    }

    public void tastore(TypeMirror type) throws IOException
    {
        pushType(type);
        tastore();
        popType();
    }

    /**
     * @throws IOException
     */
    private void tand() throws IOException
    {
        asm.tand();
    }

    public void tand(TypeMirror type) throws IOException
    {
        pushType(type);
        tand();
        popType();
    }
    /**
     * Load from array
     * <p>Stack: ..., arrayref, index =&gt; ..., value
     * @throws IOException
     */
    private void taload() throws IOException
    {
        asm.taload();
    }
    /**
     * Load from array
     * <p>Stack: ..., arrayref, index =&gt; ..., value
     * @param type
     * @throws IOException
     */
    public void taload(TypeMirror type) throws IOException
    {
        pushType(type);
        taload();
        popType();
    }

/**
     * @throws IOException
     */
    private void tadd() throws IOException
    {
        asm.tadd();
    }

    public void tadd(TypeMirror type) throws IOException
    {
        pushType(type);
        tadd();
        popType();
    }

    public void l2t() throws IOException
    {
        l.l2t();
    }
    /**
     * @throws IOException
     */
    private void if_tcmpne(String target) throws IOException
    {
        asm.if_tcmpne(target);
    }
    /**
     * ne succeeds if and only if value1 != value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @param type
     * @param target
     * @throws IOException 
     */
    public void if_tcmpne(TypeMirror type, String target) throws IOException
    {
        pushType(type);
        if_tcmpne(target);
        popType();
    }
    /**
     * @throws IOException
     */
    private void if_tcmplt(String target) throws IOException
    {
        asm.if_tcmplt(target);
    }

    /**
     * lt succeeds if and only if value1 &lt; value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @param type
     * @param target
     * @throws IOException 
     */
    public void if_tcmplt(TypeMirror type, String target) throws IOException
    {
        pushType(type);
        if_tcmplt(target);
        popType();
    }
    /**
     * @throws IOException
     */
    private void if_tcmple(String target) throws IOException
    {
        asm.if_tcmple(target);
    }

    /**
     * le succeeds if and only if value1 &lt;= value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @param type
     * @param target
     * @throws IOException 
     */
    public void if_tcmple(TypeMirror type, String target) throws IOException
    {
        pushType(type);
        if_tcmple(target);
        popType();
    }
    /**
     * @throws IOException
     */
    private void if_tcmpgt(String target) throws IOException
    {
        asm.if_tcmpgt(target);
    }

    /**
     * gt succeeds if and only if value1 &gt; value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @param type
     * @param target
     * @throws IOException 
     */
    public void if_tcmpgt(TypeMirror type, String target) throws IOException
    {
        pushType(type);
        if_tcmpgt(target);
        popType();
    }
    /**
     * @throws IOException
     */
    private void if_tcmpge(String target) throws IOException
    {
        asm.if_tcmpge(target);
    }

    /**
     * ge succeeds if and only if value1 &gt;= value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @param type
     * @param target
     * @throws IOException 
     */
    public void if_tcmpge(TypeMirror type, String target) throws IOException
    {
        pushType(type);
        if_tcmpge(target);
        popType();
    }
    /**
     * @throws IOException
     */
    private void if_tcmpeq(String target) throws IOException
    {
        asm.if_tcmpeq(target);
    }
    /**
     * eq succeeds if and only if value1 == value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @param type
     * @param target
     * @throws IOException 
     */
    public void if_tcmpeq(TypeMirror type, String target) throws IOException
    {
        pushType(type);
        if_tcmpeq(target);
        popType();
    }

    /**
     * Branch if value1 = value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @throws IOException
     */
    public void if_icmpeq(String target) throws IOException
    {
        i.if_tcmpeq(target);
    }

    /**
     * Branch if value1 != value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @throws IOException
     */
    public void if_icmpne(String target) throws IOException
    {
        i.if_tcmpne(target);
    }

    /**
     * Branch if value1 &lt; value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @throws IOException
     */
    public void if_icmplt(String target) throws IOException
    {
        i.if_tcmplt(target);
    }

    /**
     * Branch if value1 &lt;= value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @throws IOException
     */
    public void if_icmple(String target) throws IOException
    {
        i.if_tcmple(target);
    }

    /**
     * Branch if value1 &gt; value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @throws IOException
     */
    public void if_icmpgt(String target) throws IOException
    {
        i.if_tcmpgt(target);
    }

    /**
     * Branch if value1 &gt;= value2
     * <p>Stack: ..., value1, value2 =&gt; ...
     * @throws IOException
     */
    public void if_icmpge(String target) throws IOException
    {
        i.if_tcmpge(target);
    }

    public void i2t() throws IOException
    {
        i.i2t();
    }

    public void f2t() throws IOException
    {
        f.f2t();
    }

    public void d2t() throws IOException
    {
        d.d2t();
    }

    public void anewarray(int count) throws IOException
    {
        out.writeOpCode(ANEWARRAY);
        out.writeShort(count);
    }

    public void arraylength() throws IOException
    {
        out.writeOpCode(ARRAYLENGTH);
    }

    public void athrow() throws IOException
    {
        out.writeOpCode(ATHROW);
    }

    public void checkcast(int objectref) throws IOException
    {
        out.writeOpCode(CHECKCAST);
        out.writeShort(objectref);
    }
    /**
     * Duplicate the top operand stack value
     * <p>Stack: ..., value =&gt ;..., value, value
     * <p>Duplicate the top value on the operand stack and push the duplicated
     * value onto the operand stack. The dup instruction must not be used
     * unless value is a value of a category 1 computational type
     * @throws IOException
     * @see category1
     */
    public void dup() throws IOException
    {
        out.writeOpCode(DUP);
    }
    /**
     * Duplicate the top operand stack value and insert two values down
     * <p>Stack: ..., value2, value1 =&gt; ..., value1, value2, value1
     * <p>Duplicate the top value on the operand stack and insert the
     * duplicated value two values down in the operand stack. The dup_x1
     * instruction must not be used unless both value1 and value2 are values
     * of a category 1 computational type
     * @throws IOException
     * @see category1
     */
    public void dup_x1() throws IOException
    {
        out.writeOpCode(DUP_X1);
    }
    /**
     * Duplicate the top operand stack value and insert two or three values down
     * <p>Stack1: ..., value3, value2, value1 =&gt; ..., value1, value3, value2, value1
     * <p>where value1, value2, and value3 are all values of a category 1 computational type
     * <p>Stack2: ..., value2, value1 =&gt; ..., value1, value2, value1
     * <p>where value1 is a value of a category 1 computational type and value2 is a value of a category 2 computational type
     * <p>Duplicate the top value on the operand stack and insert the duplicated
     * value two or three values down in the operand stack
     * @throws IOException
     * @see category1
     * @see category2
     */
    public void dup_x2() throws IOException
    {
        out.writeOpCode(DUP_X2);
    }
    /**
     * Duplicate the top one or two operand stack values
     * <p>Stack1: ..., value2, value1 =&gt; ..., value2, value1, value2, value1
     * <p>where both value1 and value2 are values of a category 1 computational type
     * <p>Stack2: ..., value =&gt; ..., value, value
     * <p>where value is a value of a category 2 computational type
     * <p>Duplicate the top one or two values on the operand stack and push the
     * duplicated value or values back onto the operand stack in the original order
     * @throws IOException
     * @see category1
     * @see category2
     */
    public void dup2() throws IOException
    {
        out.writeOpCode(DUP2);
    }
    /**
     * Duplicate the top one or two operand stack values and insert two or three values down
     * <p>Stack1: ..., value3, value2, value1 =&gt; ..., value2, value1, value3, value2, value1
     * <p>where value1, value2, and value3 are all values of a category 1 computational type
     * <p>Stack2: ..., value2, value1 =&gt; ..., value1, value2, value1
     * <p>where value1 is a value of a category 2 computational type and value2 is a value of a category 1 computational type
     * <p>Duplicate the top one or two values on the operand stack and insert the duplicated values, in the original order, one value beneath the original value or values in the operand stack.
     * @throws IOException
     * @see category1
     * @see category2
     */
    public void dup2_x1() throws IOException
    {
        out.writeOpCode(DUP2_X1);
    }
    /**
     * Duplicate the top one or two operand stack values and insert two, three, or four values down
     * <p>Stack1: ..., value4, value3, value2, value1 =&gt; ..., value2, value1, value4, value3, value2, value1
     * <p>where value1, value2, value3, and value4 are all values of a category 1 computational type
     * <p>Stack2: ..., value3, value2, value1 =&gt; ..., value1, value3, value2, value1
     * <p>where value1 is a value of a category 2 computational type and value2 and value3 are both values of a category 1 computational type
     * <p>Stack3: ..., value3, value2, value1 =&gt; ..., value2, value1, value3, value2, value1
     * <p>where value1 and value2 are both values of a category 1 computational type and value3 is a value of a category 2 computational type
     * <p>Stack4: ..., value2, value1 =&gt; ..., value1, value2, value1
     * <p>where value1 and value2 are both values of a category 2 computational type
     * <p>Duplicate the top one or two values on the operand stack and insert the duplicated values, in the original order, into the operand stack
     * @throws IOException
     * @see category1
     * @see category2
     */
    public void dup2_x2() throws IOException
    {
        out.writeOpCode(DUP2_X2);
    }

    public void getfield(int index) throws IOException
    {
        out.writeOpCode(GETFIELD);
        out.writeShort(index);
    }

    public void getstatic(int index) throws IOException
    {
        out.writeOpCode(GETSTATIC);
        out.writeShort(index);
    }
    /**
     * Branch always
     * @param target
     * @throws IOException
     */
    public void goto_n(String target) throws IOException
    {
        if (wideIndex)
        {
            goto_w(target);
        }
        else
        {
            boolean ok = optimizeGoto(target);
            if (!ok)
            {
                Branch branch = createBranch(target);
                int addr = branch.getLabel().getAddress();
                if (addr != -1)
                {
                    int offset = addr - (position());
                    if (offset < Short.MIN_VALUE || offset > Short.MAX_VALUE)
                    {
                        out.writeOpCode(GOTO_W);
                        out.writeInt(branch);
                        return;
                    }
                }
                out.writeOpCode(GOTO);
                out.writeShort(branch);
            }
        }
    }
    public void goto_w(String target) throws IOException
    {
        boolean ok = optimizeGoto(target);
        if (!ok)
        {
            Branch branch = createBranch(target);
            out.writeOpCode(GOTO_W);
            out.writeInt(branch);
        }
    }
    /**
     * Branch always (wide index)
     * @param target
     * @throws IOException
     */
    private boolean optimizeGoto(String target) throws IOException
    {
        if (!optimizeGoto)
        {
            return false;
        }
        // is this fragment starter
        int lastOpCode = out.getLastOpCode();
        switch (lastOpCode)
        {
            case GOTO:
            case GOTO_W:
            case RET:
            case IRETURN:
            case LRETURN:
            case FRETURN:
            case DRETURN:
            case ARETURN:
            case RETURN:
            case TABLESWITCH:
            case LOOKUPSWITCH:
                break;
            default:
                return false;
        }
        int position = position();
        Label label = labelForAddress(position);
        if (label == null)
        {
            return false;
        }
        label.resetAddress();
        Label targetLabel = labels.get(target);
        if (targetLabel != null)
        {
            Label union = new Label(label, targetLabel);
            for (String name : union.getNames())
            {
                labels.remove(name);
                labels.put(name, union);
            }
            labels.put(target, union);
            int addr = targetLabel.getAddress();
            if (addr != -1)
            {
                union.setAddress(addr);
            }
        }
        else
        {
            labels.put(target, label);
            label.addName(target);
        }
        return true;
    }

    public void i2c() throws IOException
    {
        out.writeOpCode(I2C);
    }

    /**
     * eq succeeds if and only if value == 0
     * @param target
     * @throws IOException
     */
    public void ifeq(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IFEQ);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(IFEQ);
            out.writeShort(branch);
        }
    }

    /**
     * ne succeeds if and only if value != 0
     * @param target
     * @throws IOException
     */
    public void ifne(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IFNE);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(IFNE);
            out.writeShort(branch);
        }
    }

    /**
     * lt succeeds if and only if value &lt; 0
     * @param target
     * @throws IOException
     */
    public void iflt(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IFLT);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(IFLT);
            out.writeShort(branch);
        }
    }

    /**
     * ge succeeds if and only if value &gt;= 0
     * @param target
     * @throws IOException
     */
    public void ifge(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IFGE);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(IFGE);
            out.writeShort(branch);
        }
    }

    /**
     * gt succeeds if and only if value &gt; 0
     * @param target
     * @throws IOException
     */
    public void ifgt(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IFGT);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(IFGT);
            out.writeShort(branch);
        }
    }

    /**
     * le succeeds if and only if value &lt;= 0
     * @param target
     * @throws IOException
     */
    public void ifle(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IFLE);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(IFLE);
            out.writeShort(branch);
        }
    }

    public void ifnonnull(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IFNONNULL);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(IFNONNULL);
            out.writeShort(branch);
        }
    }

    public void ifnull(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IFNULL);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(IFNULL);
            out.writeShort(branch);
        }
    }

    public void instanceo(int index) throws IOException
    {
        out.writeOpCode(INSTANCEOF);
        out.writeShort(index);
    }

    public void invokeinterface(int index, int count) throws IOException
    {
        out.writeOpCode(INVOKEINTERFACE);
        out.writeShort(index);
        out.writeByte(count);
        out.writeByte(0);
    }

    public void invokespecial(int index) throws IOException
    {
        out.writeOpCode(INVOKESPECIAL);
        out.writeShort(index);
    }

    public void invokestatic(int index) throws IOException
    {
        out.writeOpCode(INVOKESTATIC);
        out.writeShort(index);
    }

    public void invokevirtual(int index) throws IOException
    {
        out.writeOpCode(INVOKEVIRTUAL);
        out.writeShort(index);
    }

    public void ior() throws IOException
    {
        out.writeOpCode(IOR);
    }

    public void irem() throws IOException
    {
        out.writeOpCode(IREM);
    }

    public void ireturn() throws IOException
    {
        out.writeOpCode(IRETURN);
    }

    public void ishl() throws IOException
    {
        out.writeOpCode(ISHL);
    }

    public void ishr() throws IOException
    {
        out.writeOpCode(ISHR);
    }

    public void istore(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeOpCode(ISTORE_0);
                break;
            case 1:
                out.writeOpCode(ISTORE_1);
                break;
            case 2:
                out.writeOpCode(ISTORE_2);
                break;
            case 3:
                out.writeOpCode(ISTORE_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeOpCode(ISTORE);
                    out.writeByte(index);
                }
                else
                {
                    out.writeOpCode(WIDE);
                    out.writeOpCode(ISTORE);
                    out.writeShort(index);
                }
                break;
        }
    }

    public void isub() throws IOException
    {
        out.writeOpCode(ISUB);
    }

    public void iushr() throws IOException
    {
        out.writeOpCode(IUSHR);
    }

    public void ixor() throws IOException
    {
        out.writeOpCode(IXOR);
    }

    public void jsr(String target) throws IOException
    {
        if (wideIndex)
        {
            jsr_w(target);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeOpCode(JSR);
            out.writeShort(branch);
        }
    }

    public void jsr_w(String target) throws IOException
    {
        Branch branch = createBranch(target);
        out.writeOpCode(JSR_W);
        out.writeInt(branch);
    }

    public void ldc(int index) throws IOException
    {
        if (index < 256)
        {
            out.writeOpCode(LDC);
            out.writeByte(index);
        }
        else
        {
            ldc_w(index);
        }
    }

    public void ldc_w(int index) throws IOException
    {
        out.writeOpCode(LDC_W);
        out.writeShort(index);
    }

    public void ldc2_w(int index) throws IOException
    {
        out.writeOpCode(LDC2_W);
        out.writeShort(index);
    }

    public void lookupswitch(String def, LookupList list) throws IOException
    {
        Collections.sort(list);
        Branch[] ba = new Branch[list.size()];
        Branch defOffset = createBranch(def);
        int ii = 0;
        for (LookupPair lup : list)
        {
            ba[ii++] = createBranch(lup.getTarget());
        }

        out.writeOpCode(LOOKUPSWITCH);
        while ((position() % 4) != 0)
        {
            out.writeByte(0);
        }
        out.writeInt(defOffset);
        out.writeInt(list.size());
        ii = 0;
        for (LookupPair lup : list)
        {
            out.writeInt(lup.getMatch());
            out.writeInt(ba[ii++]);
        }
    }

    public void monitorenter() throws IOException
    {
        out.writeOpCode(MONITORENTER);
    }

    public void monitorexit() throws IOException
    {
        out.writeOpCode(MONITOREXIT);
    }

    public void multianewarray(int index, int dimensions) throws IOException
    {
        out.writeOpCode(MULTIANEWARRAY);
        out.writeShort(index);
        out.writeByte(dimensions);
    }

    public void anew(int index) throws IOException
    {
        out.writeOpCode(NEW);
        out.writeShort(index);
    }

    public void newarray(int type) throws IOException
    {
        out.writeOpCode(NEWARRAY);
        out.writeByte(type);
    }

    public void nop() throws IOException
    {
        out.writeOpCode(NOP);
    }

    public void pop() throws IOException
    {
        out.writeOpCode(POP);
    }

    public void pop2() throws IOException
    {
        out.writeOpCode(POP2);
    }

    public void putfield(int index) throws IOException
    {
        out.writeOpCode(PUTFIELD);
        out.writeShort(index);
    }

    public void putstatic(int index) throws IOException
    {
        out.writeOpCode(PUTSTATIC);
        out.writeShort(index);
    }

    public void ret(int index) throws IOException
    {
        if (index < 256)
        {
            out.writeOpCode(RET);
            out.writeByte(index);
        }
        else
        {
            out.writeOpCode(WIDE);
            out.writeOpCode(RET);
            out.writeShort(index);
        }
    }

    public void swap() throws IOException
    {
        out.writeOpCode(SWAP);
    }

    public void tableswitch(String def, int low, int high, String... symbols) throws IOException
    {
        if ((high - low + 1) != symbols.length)
        {
            int exp = (high - low + 1);
            throw new IllegalArgumentException("expected number of offsets " + exp + " got " + symbols.length);
        }
        Branch[] ba = new Branch[symbols.length];
        int index = 0;
        for (String symbol : symbols)
        {
            ba[index++] = createBranch(symbol);
        }
        Branch defOffset = createBranch(def);
        out.writeOpCode(TABLESWITCH);
        while ((position() % 4) != 0)
        {
            out.writeByte(0);
        }
        out.writeInt(defOffset);
        out.writeInt(low);
        out.writeInt(high);
        for (Branch branch : ba)
        {
            out.writeInt(branch);
        }
    }

    public void aconst_null() throws IOException
    {
        a.tconst_null();
    }
    /**
     * Push int constant
     * <p>Stack: ...=&gt;..., i
     * @param value int value
     * @throws IOException
     */
    public void iconst(int value) throws IOException
    {
        i.tconst(value);
    }

    /**
     * Push long constant
     * <p>Stack: ...=&gt;..., i
     * @param value int value
     * @throws IOException
     */
    public void lconst(int value) throws IOException
    {
        l.tconst(value);
    }

    /**
     * Push float constant
     * <p>Stack: ...=&gt;..., i
     * @param value int value
     * @throws IOException
     */
    public void fconst(int value) throws IOException
    {
        f.tconst(value);
    }

    /**
     * Push double constant
     * <p>Stack: ...=&gt;..., i
     * @param value int value
     * @throws IOException
     */
    public void dconst(int value) throws IOException
    {
        d.tconst(value);
    }

    /**
     * Push byte constant
     * <p>Stack: ...=&gt;..., i
     * @param value int value
     * @throws IOException
     */
    public void bipush(byte value) throws IOException
    {
        b.tipush(value);
    }

    /**
     * Push short constant
     * <p>Stack: ...=&gt;..., i
     * @param value int value
     * @throws IOException
     */
    public void sipush(short value) throws IOException
    {
        s.tipush(value);
    }

    /**
     * Load int from local variable
     * <p>Stack: ...=&gt;..., value
     * @param index
     * @throws IOException
     */
    public void iload(int index) throws IOException
    {
        i.tload(index);
    }

    /**
     * Load long from local variable
     * <p>Stack: ...=&gt;..., value
     * @param index
     * @throws IOException
     */
    public void lload(int index) throws IOException
    {
        l.tload(index);
    }

    /**
     * Load float from local variable
     * <p>Stack: ...=&gt;..., value
     * @param index
     * @throws IOException
     */
    public void fload(int index) throws IOException
    {
        f.tload(index);
    }

    /**
     * Load double from local variable
     * <p>Stack: ...=&gt;..., value
     * @param index
     * @throws IOException
     */
    public void dload(int index) throws IOException
    {
        d.tload(index);
    }

    /**
     * Load Object from local variable
     * <p>Stack: ...=&gt;..., value
     * @param index
     * @throws IOException
     */
    public void aload(int index) throws IOException
    {
        a.tload(index);
    }

    /**
     * Load int from array
     * <p>Stack: ..., arrayref, index=&gt;..., value
     * @throws IOException
     */
    public void iaload() throws IOException
    {
        i.taload();
    }

    /**
     * Load long from array
     * <p>Stack: ..., arrayref, index=&gt;..., value
     * @throws IOException
     */
    public void laload() throws IOException
    {
        l.taload();
    }

    /**
     * Load float from array
     * <p>Stack: ..., arrayref, index=&gt;..., value
     * @throws IOException
     */
    public void faload() throws IOException
    {
        f.taload();
    }

    /**
     * Load double from array
     * <p>Stack: ..., arrayref, index=&gt;..., value
     * @throws IOException
     */
    public void daload() throws IOException
    {
        d.taload();
    }

    /**
     * Load Object from array
     * <p>Stack: ..., arrayref, index=&gt;..., value
     * @throws IOException
     */
    public void aaload() throws IOException
    {
        a.taload();
    }

    /**
     * Load byte from array
     * <p>Stack: ..., arrayref, index=&gt;..., value
     * @throws IOException
     */
    public void baload() throws IOException
    {
        b.taload();
    }

    /**
     * Load char from array
     * <p>Stack: ..., arrayref, index=&gt;..., value
     * @throws IOException
     */
    public void caload() throws IOException
    {
        c.taload();
    }

    /**
     * Load short from array
     * <p>Stack: ..., arrayref, index=&gt;..., value
     * @throws IOException
     */
    public void saload() throws IOException
    {
        s.taload();
    }

    /**
     * Store long into local variable
     * <p>Stack: ..., value=&gt;...
     * @param index
     * @throws IOException
     */
    public void lstore(int index) throws IOException
    {
        l.tstore(index);
    }

    /**
     * Store float into local variable
     * <p>Stack: ..., value=&gt;...
     * @param index
     * @throws IOException
     */
    public void fstore(int index) throws IOException
    {
        f.tstore(index);
    }

    /**
     * Store double into local variable
     * <p>Stack: ..., value=&gt;...
     * @param index
     * @throws IOException
     */
    public void dstore(int index) throws IOException
    {
        d.tstore(index);
    }

    /**
     * Store Object into local variable
     * <p>Stack: ..., value=&gt;...
     * @param index
     * @throws IOException
     */
    public void astore(int index) throws IOException
    {
        a.tstore(index);
    }

    /**
     * Store into int array
     * <p>Stack: ..., arrayref, index, value=&gt;...
     * @throws IOException
     */
    public void iastore() throws IOException
    {
        i.tastore();
    }

    /**
     * Store into long array
     * <p>Stack: ..., arrayref, index, value=&gt;...
     * @throws IOException
     */
    public void lastore() throws IOException
    {
        l.tastore();
    }

    /**
     * Store into float array
     * <p>Stack: ..., arrayref, index, value=&gt;...
     * @throws IOException
     */
    public void fastore() throws IOException
    {
        f.tastore();
    }

    /**
     * Store into double array
     * <p>Stack: ..., arrayref, index, value=&gt;...
     * @throws IOException
     */
    public void dastore() throws IOException
    {
        d.tastore();
    }

    /**
     * Store into Object array
     * <p>Stack: ..., arrayref, index, value=&gt;...
     * @throws IOException
     */
    public void aastore() throws IOException
    {
        a.tastore();
    }

    /**
     * Store into byte array
     * <p>Stack: ..., arrayref, index, value=&gt;...
     * @throws IOException
     */
    public void bastore() throws IOException
    {
        b.tastore();
    }

    /**
     * Store into char array
     * <p>Stack: ..., arrayref, index, value=&gt;...
     * @throws IOException
     */
    public void castore() throws IOException
    {
        c.tastore();
    }

    /**
     * Store into short array
     * <p>Stack: ..., arrayref, index, value=&gt;...
     * @throws IOException
     */
    public void sastore() throws IOException
    {
        s.tastore();
    }

    /**
     * Add int
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void iadd() throws IOException
    {
        i.tadd();
    }

    /**
     * Add long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void ladd() throws IOException
    {
        l.tadd();
    }

    /**
     * Add float
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void fadd() throws IOException
    {
        f.tadd();
    }

    /**
     * Add double
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void dadd() throws IOException
    {
        d.tadd();
    }

    /**
     * Subtract long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void lsub() throws IOException
    {
        l.tsub();
    }

    /**
     * Subtract float
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void fsub() throws IOException
    {
        f.tsub();
    }

    /**
     * Subtract double
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void dsub() throws IOException
    {
        d.tsub();
    }

    /**
     * Multiply int
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void imul() throws IOException
    {
        i.tmul();
    }

    /**
     * Multiply long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void lmul() throws IOException
    {
        l.tmul();
    }

    /**
     * Multiply float
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void fmul() throws IOException
    {
        f.tmul();
    }

    /**
     * Multiply double
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void dmul() throws IOException
    {
        d.tmul();
    }

    /**
     * Divide int
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void idiv() throws IOException
    {
        i.tdiv();
    }

    /**
     * Divide long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void ldiv() throws IOException
    {
        l.tdiv();
    }

    /**
     * Divide float
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void fdiv() throws IOException
    {
        f.tdiv();
    }

    /**
     * Divide double
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void ddiv() throws IOException
    {
        d.tdiv();
    }

    /**
     * Remainder long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void lrem() throws IOException
    {
        l.trem();
    }

    /**
     * Remainder float
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void frem() throws IOException
    {
        f.trem();
    }

    /**
     * Negate long
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void lneg() throws IOException
    {
        l.tneg();
    }

    /**
     * Negate float
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void fneg() throws IOException
    {
        f.tneg();
    }

    /**
     * Negate double
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void dneg() throws IOException
    {
        d.tneg();
    }

    /**
     * Shift left long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void lshl() throws IOException
    {
        l.tshl();
    }

    /**
     * Arithmetic shift right long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void lshr() throws IOException
    {
        l.tshr();
    }

    /**
     * Logical shift right long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void lushr() throws IOException
    {
        l.tushr();
    }

    /**
     * Boolean AND int
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void iand() throws IOException
    {
        i.tand();
    }

    /**
     * Boolean AND long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void land() throws IOException
    {
        l.tand();
    }

    /**
     * Boolean OR long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void lor() throws IOException
    {
        l.tor();
    }

    /**
     * Boolean XOR long
     * <p>Stack: ..., value1, value2=&gt;..., result
     * @throws IOException
     */
    public void lxor() throws IOException
    {
        l.txor();
    }

    /**
     * Increment local variable by constant
     * <p>Stack: No change
     * @param index into local variable
     * @param con constant value
     * @throws IOException
     */
    public void iinc(int index, int con) throws IOException
    {
        i.tinc(index, con);
    }

    /**
     * Convert int to long
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void i2l() throws IOException
    {
        l.i2t();
    }

    /**
     * Convert int to float
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void i2f() throws IOException
    {
        f.i2t();
    }

    /**
     * Convert int to double
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void i2d() throws IOException
    {
        d.i2t();
    }

    /**
     * Convert long to int
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void l2i() throws IOException
    {
        i.l2t();
    }

    /**
     * Convert long to float
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void l2f() throws IOException
    {
        f.l2t();
    }

    /**
     * Convert long to double
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void l2d() throws IOException
    {
        d.l2t();
    }

    /**
     * Convert float to int
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void f2i() throws IOException
    {
        i.f2t();
    }

    /**
     * Convert float to long
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void f2l() throws IOException
    {
        l.f2t();
    }

    /**
     * Convert float to double
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void f2d() throws IOException
    {
        d.f2t();
    }

    /**
     * Convert double to int
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void d2i() throws IOException
    {
        i.d2t();
    }

    /**
     * Convert double to long
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void d2l() throws IOException
    {
        l.d2t();
    }

    /**
     * Convert double to float
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void d2f() throws IOException
    {
        f.d2t();
    }

    /**
     * Convert int to byte
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void i2b() throws IOException
    {
        b.i2t();
    }

    /**
     * Convert int to short
     * <p>Stack: ..., value=&gt;..., result
     * @throws IOException
     */
    public void i2s() throws IOException
    {
        s.i2t();
    }

    /**
     * Return a unique branch name
     * @return 
     */
    public String createBranch()
    {
        nextBranch++;
        return "branch"+nextBranch;
    }

    public Block startBlock() throws IOException
    {
        return new Block(position());
    }
    
    public void endBlock(Block block) throws IOException
    {
        block.setEnd(position());
    }
    
    public void convert(TypeMirror from, TypeMirror to) throws IOException
    {
        if (!Typ.isSameType(from, to))
        {
            switch (from.getKind())
            {
                case INT:
                    switch (to.getKind())
                    {
                        case INT:
                            break;
                        case LONG:
                            i2l();
                            break;
                        case FLOAT:
                            i2f();
                            break;
                        case DOUBLE:
                            i2d();
                            break;
                        default:
                            throw new IllegalArgumentException(from+" to "+to+" conversion not supported");
                    }
                    break;
                case LONG:
                    switch (to.getKind())
                    {
                        case INT:
                            l2i();
                            break;
                        case LONG:
                            break;
                        case FLOAT:
                            l2f();
                            break;
                        case DOUBLE:
                            l2d();
                            break;
                        default:
                            throw new IllegalArgumentException(from+" to "+to+" conversion not supported");
                    }
                    break;
                case FLOAT:
                    switch (to.getKind())
                    {
                        case INT:
                            f2i();
                            break;
                        case LONG:
                            f2l();
                            break;
                        case FLOAT:
                            break;
                        case DOUBLE:
                            f2d();
                            break;
                        default:
                            throw new IllegalArgumentException(from+" to "+to+" conversion not supported");
                    }
                    break;
                case DOUBLE:
                    switch (to.getKind())
                    {
                        case INT:
                            d2i();
                            break;
                        case LONG:
                            d2l();
                            break;
                        case FLOAT:
                            d2f();
                            break;
                        case DOUBLE:
                            break;
                        default:
                            throw new IllegalArgumentException(from+" to "+to+" conversion not supported");
                    }
                    break;
                default:
                    throw new IllegalArgumentException(from+" to "+to+" conversion not supported");
            }
        }
    }
}
