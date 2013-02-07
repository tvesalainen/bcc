/*
 * Copyright (C) 2013 Timo Vesalainen
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
 * @author Timo Vesalainen
 */
public class ExceptionsAttribute extends AttributeInfo
{
    private int[] exceptionIndexes;
    
    public ExceptionsAttribute(ClassFile classFile, int attribute_name_index, int... exceptionIndexes)
    {
        super(classFile, attribute_name_index, 0);
        this.exceptionIndexes = exceptionIndexes;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(attribute_name_index);
        out.writeInt(2+2*exceptionIndexes.length);
        out.writeShort(exceptionIndexes.length);
        for (int ei : exceptionIndexes)
        {
            out.writeShort(ei);
        }
    }

}
