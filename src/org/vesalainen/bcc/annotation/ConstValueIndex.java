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
import org.vesalainen.bcc.ConstantInfo;
import org.vesalainen.bcc.ConstantInfo.ConstantDouble;
import org.vesalainen.bcc.ConstantInfo.ConstantFloat;
import org.vesalainen.bcc.ConstantInfo.ConstantInteger;
import org.vesalainen.bcc.ConstantInfo.ConstantLong;
import org.vesalainen.bcc.ConstantInfo.ConstantString;
import org.vesalainen.bcc.ConstantInfo.Utf8;

/**
 * @author Timo Vesalainen
 */
public class ConstValueIndex extends ElementValue 
{
    private final int constValueIndex;

    public ConstValueIndex(ClassFile classFile, int tag, DataInput in) throws IOException
    {
        super(classFile, tag);
        constValueIndex = in.readUnsignedShort();
    }

    public ConstantInfo getConstantInfo()
    {
        return classFile.getConstantInfo(constValueIndex);
    }
    public Class<?> getType()
    {
        switch (tag)
        {
            case 'B':
                return byte.class;
            case 'C':
                return char.class;
            case 'D':
                return double.class;
            case 'F':
                return float.class;
            case 'I':
                return int.class;
            case 'J':
                return long.class;
            case 'S':
                return short.class;
            case 'Z':
                return boolean.class;
            case 's':
                return String.class;
            default:
                throw new IllegalArgumentException("unknown element value tag " + tag);
        }
    }
    public Object getValue()
    {
        ConstantInfo constantInfo = getConstantInfo();
        switch (tag)
        {
            case 'B':
            case 'C':
            case 'I':
            case 'S':
                ConstantInteger intConstant = (ConstantInteger) constantInfo;
                return intConstant.getConstant();
            case 'D':
                ConstantDouble doubleConstant = (ConstantDouble) constantInfo;
                return doubleConstant.getConstant();
            case 'F':
                ConstantFloat floatConstant = (ConstantFloat) constantInfo;
                return floatConstant.getConstant();
            case 'J':
                ConstantLong longConstant = (ConstantLong) constantInfo;
                return longConstant.getConstant();
            case 'Z':
                ConstantInteger booleanConstant = (ConstantInteger) constantInfo;
                return booleanConstant.getConstant() != 0;
            case 's':
                Utf8 stringConstant = (Utf8) constantInfo;
                return stringConstant.getString();
            default:
                throw new IllegalArgumentException("unknown element value tag " + tag);
        }
    }
    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeByte(tag);
        out.writeShort(constValueIndex);
    }

    @Override
    public int getLength()
    {
        return 3;
    }

    @Override
    public String toString()
    {
        return getValue().toString();
    }
}
