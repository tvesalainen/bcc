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
import java.io.IOException;
import org.vesalainen.bcc.ClassFile;
import org.vesalainen.bcc.Writable;

/**
 * @author Timo Vesalainen
 */
public abstract class ElementValue implements Writable 
{
    protected int tag;
    protected ClassFile classFile;

    protected ElementValue(ClassFile classFile, int tag)
    {
        this.classFile = classFile;
        this.tag = tag;
    }

    public static ElementValue newInstance(ClassFile classFile, DataInput in) throws IOException
    {
        int tag = in.readUnsignedByte();
        switch (tag)
        {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                return new ConstValueIndex(classFile, tag, in);
            case 'e':
                return new EnumConstValue(classFile, tag, in);
            case 'c':
                return new ClassInfoIndex(classFile, tag, in);
            case '@':
                return new AnnotationValue(classFile, tag, in);
            case '[':
                return new ArrayValue(classFile, tag, in);
            default:
                throw new IllegalArgumentException("unknown element value tag " + tag);
        }
    }
    public abstract int getLength();


}
