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

import org.vesalainen.bcc.annotation.RuntimeVisibleAnnotations;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author tkv
 */
public class AttributeInfo implements Writable
{
    protected ClassFile classFile;
    protected int attribute_name_index;
    protected int attribute_length;
    private byte info[];

    public static AttributeInfo getInstance(ClassFile cf, DataInput in) throws IOException
    {
        int attribute_name_index = in.readUnsignedShort();
        int attribute_length = in.readInt();
        String name = cf.getString(attribute_name_index);
        switch (name)
        {
            case "Code":
                return new CodeAttribute(cf, attribute_name_index, attribute_length, in);
            case "LocalVariableTable":
                return new LocalVariableTable(cf, attribute_name_index, attribute_length, in);
            case "LocalVariableTypeTable":
                return new LocalVariableTypeTable(cf, attribute_name_index, attribute_length, in);
            case "Exceptions":
                return new ExceptionsAttribute(cf, attribute_name_index, attribute_length, in);
            case "RuntimeVisibleAnnotations":
                return new RuntimeVisibleAnnotations(cf, attribute_name_index, attribute_length, in);
            default:
                return new AttributeInfo(cf, attribute_name_index, attribute_length, in);
        }
    }
    
    public static boolean isOfType(ClassFile cf, String type, AttributeInfo ai)
    {
        String name = cf.getString(ai.attribute_name_index);
        return type.equals(name);
    }

    public AttributeInfo(SubClass classFile, String attributeName)
    {
        this.classFile = classFile;
        this.attribute_name_index = classFile.resolveNameIndex(attributeName);
    }

    public AttributeInfo(ClassFile classFile, int attribute_name_index, int attribute_length)
    {
        this.classFile = classFile;
        this.attribute_name_index = attribute_name_index;
        this.attribute_length = attribute_length;
    }

    public AttributeInfo(SubClass classFile, String attributeName, int attribute_length)
    {
        this.classFile = classFile;
        this.attribute_name_index = classFile.resolveNameIndex(attributeName);
        this.attribute_length = attribute_length;
    }

    public AttributeInfo(ClassFile classFile, DataInput in) throws IOException
    {
        this.classFile = classFile;
        attribute_name_index = in.readUnsignedShort();
        attribute_length = in.readInt();
        info = new byte[attribute_length];
        in.readFully(info);
    }

    private AttributeInfo(ClassFile classFile, int attribute_name_index, int attribute_length, DataInput in) throws IOException
    {
        this.classFile = classFile;
        this.attribute_name_index = attribute_name_index;
        this.attribute_length = attribute_length;
        info = new byte[attribute_length];
        in.readFully(info);
    }
    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(attribute_name_index);
        out.writeInt(attribute_length);
        out.write(info);
    }

    public int getAttributeSize()
    {
        return attribute_length+6;
    }

}
