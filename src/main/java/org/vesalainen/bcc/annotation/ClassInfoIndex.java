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
import javax.lang.model.element.AnnotationValueVisitor;
import org.vesalainen.bcc.ClassFile;

/**
 * @author Timo Vesalainen
 */
public class ClassInfoIndex extends ElementValue 
{
    private final int classInfoIndex;

    public ClassInfoIndex(ClassFile classFile, int tag, DataInput in) throws IOException
    {
        super(classFile, tag);
        classInfoIndex = in.readUnsignedShort();
    }

    public String getReturnDescriptor()
    {
        return classFile.getString(classInfoIndex);
    }
    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeByte(tag);
        out.writeShort(classInfoIndex);
    }

    @Override
    public int getLength()
    {
        return 3;
    }

    @Override
    public Object getValue()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p)
    {
        return v.visit(this, p);
    }

}
