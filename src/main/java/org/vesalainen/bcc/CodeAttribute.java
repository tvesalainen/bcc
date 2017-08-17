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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import org.vesalainen.bcc.model.Typ;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CodeAttribute extends AttributeInfo
{
    private int max_stack;
    private int max_locals;
    private byte code[];
    private ExceptionTable[] exception_table;
    private List<AttributeInfo> attributes = new ArrayList<>();

    public CodeAttribute(SubClass cf)
    {
        super(cf, "Code");
    }

    public CodeAttribute(ClassFile cf, int attribute_name_index, int attribute_length, DataInput in) throws IOException
    {
        super(cf, attribute_name_index, attribute_length);
        max_stack = in.readUnsignedShort();
        max_locals = in.readUnsignedShort();
        int code_length = in.readInt();
        code = new byte[code_length];
        in.readFully(code);
        int exception_table_length = in.readUnsignedShort();
        exception_table = new ExceptionTable[exception_table_length];
        for (int ii=0;ii<exception_table_length;ii++)
        {
            exception_table[ii] = new ExceptionTable(in);
        }
        int attributes_count = in.readUnsignedShort();
        for (int ii=0;ii<attributes_count;ii++)
        {
            attributes.add(AttributeInfo.getInstance(cf, in));
        }
    }

    public void addLocalVariables(List<? extends VariableElement> localVariables)
    {
        if (code == null)
        {
            throw new IllegalStateException("code is not set yet");
        }
        SubClass subClass = (SubClass) classFile;
        LocalVariableTable lvt = new LocalVariableTable(subClass, code.length);
        attributes.add(lvt);
        LocalVariableTypeTable lvtt = null;
        int index = 0;
        for (VariableElement lv : localVariables)
        {
            if (lv.asType().getKind() != TypeKind.OTHER)    // skipping returnAddress types which are coded as OTHER
            {
                lvt.addLocalVariable(lv, index);
                String signature = Signature.getSignature(lv);
                if (!signature.isEmpty())
                {
                    if (lvtt == null)
                    {
                        lvtt = new LocalVariableTypeTable(subClass, code.length);
                        attributes.add(lvtt);
                    }
                    lvtt.addLocalTypeVariable(lv, signature, index);
                }
            }
            if (Typ.isCategory2(lv.asType()))
            {
                index += 2;
            }
            else
            {
                index++;
            }
        }
    }
    public void addLineNumberTable(LineNumberTable lnt)
    {
        attributes.add(lnt);
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        if (code == null)
        {
            throw new IllegalArgumentException("code missing. Possibly because not calling end() method.");
        }
        out.writeShort(attribute_name_index );
        out.writeInt(attributeLength());
        out.writeShort(max_stack);
        out.writeShort(max_locals);
        out.writeInt(code.length);
        out.write(code);
        out.writeShort(exception_table.length);
        for (int ii=0;ii<exception_table.length;ii++)
        {
            exception_table[ii].write(out);
        }
        out.writeShort(attributes.size());
        for (AttributeInfo ai : attributes)
        {
            ai.write(out);
        }
    }

    public byte[] getCode()
    {
        return code;
    }

    public void setCode(byte[] code, ExceptionTable... exception_table)
    {
        this.code = code;
        this.exception_table = exception_table;
    }

    public int getMax_locals()
    {
        return max_locals;
    }

    public void setMax_locals(int max_locals)
    {
        this.max_locals = max_locals;
    }

    public int getMax_stack()
    {
        return max_stack;
    }

    public void setMax_stack(int max_stack)
    {
        this.max_stack = max_stack;
    }

    public List<? extends AttributeInfo> getAttributes()
    {
        return attributes;
    }

    private int attributeLength()
    {
        int length = 0;
        length += 2;    // max_stack
        length += 2;    // max_locals
        length += 4;    // code.length
        length += code.length;
        length += 2;    // exception_table.length
        length += exception_table.length*8;    // exception_table.length
        length += 2;    // attributes.length
        for (AttributeInfo ai : attributes)
        {
            length += ai.getAttributeSize();
        }
        return length;
    }
}
