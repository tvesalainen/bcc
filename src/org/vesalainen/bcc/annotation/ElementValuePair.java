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
import org.vesalainen.bcc.ClassFile;
import org.vesalainen.bcc.Writable;

/**
 * @author Timo Vesalainen
 */
public class ElementValuePair implements Writable 
{
    private int elementNameIndex;
    private ElementValue elementValue;
    private final ClassFile classFile;

    public ElementValuePair(ClassFile classFile, DataInput in) throws IOException
    {
        this.classFile = classFile;
        elementNameIndex = in.readUnsignedShort();
        elementValue = ElementValue.newInstance(classFile, in);
    }
    public int getLength()
    {
        return 2 + elementValue.getLength();
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(elementNameIndex);
        elementValue.write(out);
    }

    public String getName()
    {
        return classFile.getString(elementNameIndex);
    }

    public ElementValue getValue()
    {
        return elementValue;
    }

    @Override
    public String toString()
    {
        return getName()+"="+getValue();
    }
    
}
