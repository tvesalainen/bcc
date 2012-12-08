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
import org.vesalainen.bcc.Writable;

/**
 * @author Timo Vesalainen
 */
public class Annotation implements Writable 
{
    private ClassFile classFile;
    private int typeIndex;
    private List<ElementValuePair> elementValuePairs = new ArrayList<>();

    public Annotation(ClassFile classFile, DataInput in) throws IOException
    {
        this.classFile = classFile;
        typeIndex = in.readUnsignedShort();
        int numElementValuePairs = in.readUnsignedShort();
        for (int ii = 0; ii < numElementValuePairs; ii++)
        {
            elementValuePairs.add(new ElementValuePair(classFile, in));
        }
    }

    public String getType()
    {
        return classFile.getString(typeIndex);
    }
    
    public List<ElementValuePair> getElementValuePairs()
    {
        return elementValuePairs;
    }

    public int getLength()
    {
        int length = 4;
        for (ElementValuePair evp : elementValuePairs)
        {
            length += evp.getLength();
        }
        return length;
    }
    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(typeIndex);
        out.writeShort(elementValuePairs.size());
        for (ElementValuePair ev : elementValuePairs)
        {
            ev.write(out);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("@");
        sb.append(getType());
        sb.append("(");
        for (ElementValuePair ev : elementValuePairs)
        {
            sb.append(ev);
        }
        sb.append(")");
        return sb.toString();
    }

    
}
