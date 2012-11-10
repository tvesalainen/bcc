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
 * @author Timo Vesalainen
 */
public class LocalVariableTypeTable extends AttributeInfo
{
    private List<LocalTypeVariable> localTypeVariables = new ArrayList<>();
    private int length;
    
    public LocalVariableTypeTable(int attribute_name_index, int codelength)
    {
        super(attribute_name_index, 0);
        this.length = codelength;
    }

    LocalVariableTypeTable(int attribute_name_index, int attribute_length, DataInput in)
    {
        super(attribute_name_index, attribute_length);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public boolean isEmpty()
    {
        return localTypeVariables.isEmpty();
    }
    
    public void addLocalTypeVariable(int nameIndex, int signatureIndex, int index)
    {
        localTypeVariables.add(new LocalTypeVariable(nameIndex, signatureIndex, index));
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        assert length > 0;
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
        private int nameIndex;
        private int signatureIndex;
        private int index;

        public LocalTypeVariable(int nameIndex, int signatureIndex, int index)
        {
            this.nameIndex = nameIndex;
            this.signatureIndex = signatureIndex;
            this.index = index;
        }

        @Override
        public void write(DataOutput out) throws IOException
        {
            out.writeShort(0);
            out.writeShort(length);
            out.writeShort(nameIndex);
            out.writeShort(signatureIndex);
            out.writeShort(index);
        }
    }
}
