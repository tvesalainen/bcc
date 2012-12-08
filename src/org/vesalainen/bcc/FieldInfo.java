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

/**
 *
 * @author tkv
 */
public class FieldInfo implements Writable
{
    private ClassFile classFile;
    private int access_flags;
    private int name_index;
    private int descriptor_index;
    private int attributes_count;
    private AttributeInfo[] attributes;

    public FieldInfo(ClassFile classFile, int access_flags, int name_index, int descriptor_index, AttributeInfo... attributes)
    {
        this.classFile = classFile;
        this.access_flags = access_flags;
        this.name_index = name_index;
        this.descriptor_index = descriptor_index;
        this.attributes_count = attributes.length;
        this.attributes = attributes;
    }

    public FieldInfo(DataInput in) throws IOException
    {
        access_flags = in.readUnsignedShort();
        name_index = in.readUnsignedShort();
        descriptor_index = in.readUnsignedShort();
        attributes_count = in.readUnsignedShort();
        attributes = new AttributeInfo[attributes_count];
        for (int ii=0;ii<attributes_count;ii++)
        {
            attributes[ii] = new AttributeInfo(classFile, in);
        }
    }

    public void write(DataOutput out) throws IOException
    {
        out.writeShort(access_flags);
        out.writeShort(name_index);
        out.writeShort(descriptor_index);
        out.writeShort(attributes.length);
        for (int ii=0;ii<attributes_count;ii++)
        {
            attributes[ii].write(out);
        }
    }

    public int getAccess_flags()
    {
        return access_flags;
    }

    public AttributeInfo[] getAttributes()
    {
        return attributes;
    }

    public int getAttributes_count()
    {
        return attributes_count;
    }

    public int getDescriptor_index()
    {
        return descriptor_index;
    }

    public int getName_index()
    {
        return name_index;
    }

    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final FieldInfo other = (FieldInfo) obj;
        if (this.name_index != other.name_index)
        {
            return false;
        }
        if (this.descriptor_index != other.descriptor_index)
        {
            return false;
        }
        return true;
    }

    public int hashCode()
    {
        int hash = 7;
        hash = 67 * hash + this.name_index;
        hash = 67 * hash + this.descriptor_index;
        return hash;
    }
    
}
