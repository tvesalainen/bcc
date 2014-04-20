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
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.Typ;

/**
 * @author Timo Vesalainen
 */
public class LocalVariableTypeTable extends AttributeInfo
{
    private List<LocalTypeVariable> localTypeVariables = new ArrayList<>();
    private int codeLength;
    
    public LocalVariableTypeTable(SubClass subClass, int codelength)
    {
        super(subClass, "LocalVariableTypeTable");
        this.codeLength = codelength;
    }

    LocalVariableTypeTable(ClassFile classFile, int attribute_name_index, int attribute_length, DataInput in) throws IOException
    {
        super(classFile, attribute_name_index, attribute_length);
        int localTypeVariablesCount = in.readShort();
        for (int ii=0;ii<localTypeVariablesCount;ii++)
        {
            localTypeVariables.add(new LocalTypeVariable(in));
        }
    }

    public boolean isEmpty()
    {
        return localTypeVariables.isEmpty();
    }
    /**
     * Adds a entry into LocalTypeVariableTable if variable is of generic type
     * @param ve
     * @param index 
     */
    public void addLocalTypeVariable(VariableElement ve, String signature, int index)
    {
        localTypeVariables.add(new LocalTypeVariable(ve, signature, index));
    }
    /**
     * @deprecated 
     * @param nameIndex
     * @param signatureIndex
     * @param index 
     */
    public void addLocalTypeVariable(int nameIndex, int signatureIndex, int index)
    {
        localTypeVariables.add(new LocalTypeVariable(nameIndex, signatureIndex, index));
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        assert codeLength > 0;
        out.writeShort(attribute_name_index);
        out.writeInt(localTypeVariables.size()*10+2);
        out.writeShort(localTypeVariables.size());
        for (LocalTypeVariable ltv : localTypeVariables)
        {
            ltv.write(out);
        }
    }

    public int getAttributeSize()
    {
        return localTypeVariables.size()*10+2+6;
    }

    private class LocalTypeVariable implements Writable
    {
        private int startPc;
        private int length = codeLength;
        private int nameIndex;
        private int signatureIndex;
        private int index;

        private LocalTypeVariable(VariableElement ve, String signature, int index)
        {
            SubClass subClass = (SubClass) classFile;
            this.nameIndex = subClass.resolveNameIndex(ve.getSimpleName().toString());
            this.signatureIndex = subClass.resolveNameIndex(signature);
            this.index = index;
        }

        public LocalTypeVariable(int nameIndex, int signatureIndex, int index)
        {
            this.nameIndex = nameIndex;
            this.signatureIndex = signatureIndex;
            this.index = index;
        }

        private LocalTypeVariable(DataInput in) throws IOException
        {
            startPc = in.readUnsignedShort();
            length = in.readUnsignedShort();
            nameIndex = in.readUnsignedShort();
            signatureIndex = in.readUnsignedShort();
            index = in.readUnsignedShort();
        }

        @Override
        public void write(DataOutput out) throws IOException
        {
            out.writeShort(startPc);
            out.writeShort(length);
            out.writeShort(nameIndex);
            out.writeShort(signatureIndex);
            out.writeShort(index);
        }
        
        public TypeMirror getType()
        {
            return Typ.typeFromSignature(classFile.getString(signatureIndex));
        }

        public int getIndex()
        {
            return index;
        }

        public String getSimpleName()
        {
            return classFile.getString(nameIndex);
        }

    }
}
