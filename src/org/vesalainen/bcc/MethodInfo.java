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
public class MethodInfo implements Writable
{
    private int access_flags;
    private int name_index;
    private int descriptor_index;
    private AttributeInfo[] attributes;
    private MethodCompiler mc;
    /**
     * 
     * @param access_flags
     * @param name_index
     * @param descriptor_index
     * @param attributes If array member is null it is removed.
     */
    public MethodInfo(int access_flags, int name_index, int descriptor_index, AttributeInfo... attributes)
    {
        this.access_flags = access_flags;
        this.name_index = name_index;
        this.descriptor_index = descriptor_index;
        this.attributes = filterNulls(attributes);
    }

    public MethodInfo(ClassFile cf, DataInput in) throws IOException
    {
        access_flags = in.readUnsignedShort();
        name_index = in.readUnsignedShort();
        descriptor_index = in.readUnsignedShort();
        int attributes_count = in.readUnsignedShort();
        attributes = new AttributeInfo[attributes_count];
        for (int ii=0;ii<attributes_count;ii++)
        {
            attributes[ii] = AttributeInfo.getInstance(cf, in);
        }
    }

    public MethodCompiler getMc()
    {
        return mc;
    }

    public void setMc(MethodCompiler mc)
    {
        this.mc = mc;
    }

    public CodeAttribute getCodeAttribute()
    {
        for (AttributeInfo ai : attributes)
        {
            if (ai instanceof CodeAttribute)
            {
                return (CodeAttribute) ai;
            }
        }
        return null;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(access_flags);
        out.writeShort(name_index);
        out.writeShort(descriptor_index);
        out.writeShort(attributes.length);
        for (int ii=0;ii<attributes.length;ii++)
        {
            attributes[ii].write(out);
        }
    }

    public int getAccess_flags()
    {
        return access_flags;
    }

    public int getDescriptor_index()
    {
        return descriptor_index;
    }

    public int getName_index()
    {
        return name_index;
    }

    public AttributeInfo[] getAttributes()
    {
        return attributes;
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
        final MethodInfo other = (MethodInfo) obj;
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
        int hash = 3;
        hash = 61 * hash + this.name_index;
        hash = 61 * hash + this.descriptor_index;
        return hash;
    }

    private AttributeInfo[] filterNulls(AttributeInfo[] attributes)
    {
        int len = 0;
        for (AttributeInfo ai : attributes)
        {
            if (ai != null)
            {
                len++;
            }
        }
        if (len == attributes.length)
        {
            return attributes;
        }
        AttributeInfo[] aia = new AttributeInfo[len] ;
        int index = 0;
        for (AttributeInfo ai : attributes)
        {
            if (ai != null)
            {
                aia[index++] = ai;
            }
        }
        return aia;
    }
    
}
