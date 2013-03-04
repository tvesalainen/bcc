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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;

/**
 * @author Timo Vesalainen
 */
class AnnotationValueSymbol implements AnnotationValue 
{
    private Object value;
    public AnnotationValueSymbol(Object value)
    {
        this.value = value;
    }

    @Override
    public Object getValue()
    {
        if (value == null)
        {
            return null;
        }
        Class<?> returnType = value.getClass();
        if (
                returnType.isPrimitive() ||
                String.class.equals(returnType)
                )
        {
            return value;
        }
        if (Class.class.equals(returnType))
        {
            return TypeFactory.get((Type)value);
        }
        if (returnType.isEnum())
        {
            Enum en = (Enum) value;
            return ElementFactory.getVariableElement(en);
        }
        if (returnType.isAnnotation())
        {
            Annotation annotation = (Annotation) value;
            return ElementFactory.getAnnotationMirror(annotation);
        }
        if (returnType.isArray())
        {
            Object[] ar = (Object[]) value;
            List<AnnotationValue> list = new ArrayList<>();
            for (Object v : ar)
            {
                list.add(ElementFactory.getAnnotationValue(v));
            }
            return list;
        }
        throw new UnsupportedOperationException(value+" Not supported as annotation value.");
    }

    @Override
    public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p)
    {
        return v.visit(this, p);
    }

}
