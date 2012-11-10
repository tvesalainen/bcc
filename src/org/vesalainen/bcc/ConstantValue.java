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

import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Timo Vesalainen
 */
public class ConstantValue extends AttributeInfo
{
    private int constantvalue_index;
    public ConstantValue(int attribute_name_index, int constantvalue_index)
    {
        super(attribute_name_index, 2);
        this.constantvalue_index = constantvalue_index;
    }
    
    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(attribute_name_index );
        out.writeInt(2);
        out.writeShort(constantvalue_index);
    }
}
