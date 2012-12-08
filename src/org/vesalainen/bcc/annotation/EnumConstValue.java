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

/**
 * @author Timo Vesalainen
 */
public class EnumConstValue extends ElementValue 
{
    private final int typeNameIndex;
    private final int constNameIndex;

    public EnumConstValue(ClassFile classFile, int tag, DataInput in) throws IOException
    {
        super(classFile, tag);
        typeNameIndex = in.readUnsignedShort();
        constNameIndex = in.readUnsignedShort();
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeByte(tag);
        out.writeShort(typeNameIndex);
        out.writeShort(constNameIndex);
    }

    @Override
    public int getLength()
    {
        return 5;
    }

    public String getDescriptor()
    {
        return classFile.getString(typeNameIndex);
    }
    public String getName()
    {
        return classFile.getString(constNameIndex);
    }
    
    @Override
    public String toString()
    {
        return getDescriptor()+"."+getName();
    }
    
}
