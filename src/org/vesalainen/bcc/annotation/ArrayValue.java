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

package org.vesalainen.bcc.annotation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.bcc.ClassFile;

/**
 * @author Timo Vesalainen
 */
public class ArrayValue extends ElementValue 
{
    private List<ElementValue> values = new ArrayList<>();

    public ArrayValue(ClassFile classFile, int tag, DataInput in) throws IOException
    {
        super(classFile, tag);
        int numValues = in.readUnsignedShort();
        for (int ii = 0; ii < numValues; ii++)
        {
            values.add(ElementValue.newInstance(classFile, in));
        }
    }

    public List<ElementValue> getValues()
    {
        return values;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeByte(tag);
        out.writeShort(values.size());
        for (ElementValue ev : values)
        {
            ev.write(out);
        }
    }

    @Override
    public int getLength()
    {
        int length = 3;
        for (ElementValue ev : values)
        {
            length += ev.getLength();
        }
        return length;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (ElementValue ev : values)
        {
            sb.append(ev);
        }
        sb.append("]");
        return sb.toString();
    }
}
