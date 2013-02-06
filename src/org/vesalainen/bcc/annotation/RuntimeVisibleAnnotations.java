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
import java.util.ArrayList;
import java.util.List;
import org.vesalainen.bcc.AttributeInfo;
import org.vesalainen.bcc.ClassFile;

/**
 * @author Timo Vesalainen
 */
public class RuntimeVisibleAnnotations extends AttributeInfo 
{
    private List<AnnotationWrapper> annotations = new ArrayList<>();
    
    public RuntimeVisibleAnnotations(ClassFile classFile, int attribute_name_index, int attribute_length, DataInput in) throws IOException
    {
        super(classFile, attribute_name_index, attribute_length);
        int numAnnotations = in.readUnsignedShort();
        for (int ii=0;ii<numAnnotations;ii++)
        {
            annotations.add(new AnnotationWrapper(classFile, in));
        }
        assert attribute_length == getLength();
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(attribute_name_index);
        attribute_length = getLength();
        out.writeInt(attribute_length);
        for (AnnotationWrapper annotation : annotations)
        {
            annotation.write(out);
        }
    }

    public List<AnnotationWrapper> getAnnotations()
    {
        return annotations;
    }
    
    private int getLength()
    {
        int length = 2;
        for (AnnotationWrapper annotation : annotations)
        {
            length += annotation.getLength();
        }
        return length;
    }
}
