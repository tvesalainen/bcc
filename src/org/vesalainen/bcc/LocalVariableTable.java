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

/**
 *
 * @author tkv
 */
public class LocalVariableTable extends AttributeInfo
{
    private ClassFile classFile;
    private List<LocalVariable> localVariables = new ArrayList<>();
    private int length;

    public LocalVariableTable(ClassFile classFile, int attribute_name_index, int codelength)
    {
        super(classFile, attribute_name_index, 0);
        this.length = codelength;
    }

    LocalVariableTable(ClassFile classFile, int attribute_name_index, int attribute_length, DataInput in) throws IOException
    {
        super(classFile, attribute_name_index, attribute_length);
        int localVariablesCount = in.readShort();
        for (int ii=0;ii<localVariablesCount;ii++)
        {
            localVariables.add(new LocalVariable(in));
        }
    }

    public void addLocalVariable(int nameIndex, int descriptorIndex, int index)
    {
        localVariables.add(new LocalVariable(nameIndex, descriptorIndex, index));
    }

    public List<LocalVariable> getLocalVariables()
    {
        return localVariables;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        assert length > 0;
        out.writeShort(attribute_name_index);
        out.writeInt(localVariables.size()*10+2);
        out.writeShort(localVariables.size());
        for (LocalVariable lv : localVariables)
        {
            lv.write(out);
        }
    }

    @Override
    public int getAttributeSize()
    {
        return localVariables.size()*10+2+6;
    }

    public class LocalVariable implements Writable
    {
        private int nameIndex;
        private int descriptorIndex;
        private int index;

        public LocalVariable(int nameIndex, int descriptorIndex, int index)
        {
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
            this.index = index;
        }

        private LocalVariable(DataInput in) throws IOException
        {
            int start = in.readShort();
            int end = in.readShort();
            nameIndex = in.readShort();
            descriptorIndex = in.readShort();
            index = in.readShort();
        }

        public void write(DataOutput out) throws IOException
        {
            out.writeShort(0);
            out.writeShort(length);
            out.writeShort(nameIndex);
            out.writeShort(descriptorIndex);
            out.writeShort(index);
        }

        public int getDescriptorIndex()
        {
            return descriptorIndex;
        }

        public int getIndex()
        {
            return index;
        }

        public int getNameIndex()
        {
            return nameIndex;
        }

    }
}
