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

package org.vesalainen.bcc.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;

/**
 * @author Timo Vesalainen
 */
class AnnotationValueImpl implements AnnotationValue 
{
    private Object value;
    public AnnotationValueImpl(Object value)
    {
        if (value == null)
        {
            this.value = null;
        }
        else
        {
            Class<?> returnType = value.getClass();
            if (
                    returnType.isPrimitive() ||
                    String.class.equals(returnType)
                    )
            {
                this.value = value;
            }
            else
            {
                if (Class.class.equals(returnType))
                {
                    this.value = TypeMirrorFactory.get((Type)value);
                }
                else
                {
                    if (returnType.isEnum())
                    {
                        Enum en = (Enum) value;
                        this.value = ElementFactory.getVariableElement(en);
                    }
                    else
                    {
                        if (returnType.isAnnotation())
                        {
                            Annotation annotation = (Annotation) value;
                            this.value = ElementFactory.getAnnotationMirror(annotation);
                        }
                        else
                        {
                            if (returnType.isArray())
                            {
                                Object[] ar = (Object[]) value;
                                List<AnnotationValue> list = new ArrayList<>();
                                for (Object v : ar)
                                {
                                    list.add(ElementFactory.getAnnotationValue(v));
                                }
                                this.value = list;
                            }
                            else
                            {
                                throw new UnsupportedOperationException(value+" Not supported as annotation value.");
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public Object getValue()
    {
        return value;
    }

    @Override
    public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p)
    {
        return v.visit(this, p);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
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
        final AnnotationValueImpl other = (AnnotationValueImpl) obj;
        if (!Objects.equals(this.value, other.value))
        {
            return false;
        }
        return true;
    }

}
