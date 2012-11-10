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
package org.vesalainen.bcc.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author tkv
 */
public class FieldWrapper implements AnnotatedElement, Member
{
    private int modifiers;
    private String name;
    private GenericDeclaration declaringClass;
    private Type type;
    private Type genericType;

    public FieldWrapper(int modifier, String name, GenericDeclaration declaringClass, Type type)
    {
        this.modifiers = modifier;
        this.name = name;
        this.declaringClass = declaringClass;
        if (type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) type;
            this.type = pt.getRawType();
            this.genericType = pt;
        }
        else
        {
            this.type = type;
            this.genericType = type;
        }
    }
    /**
     * @deprecated 
     * @param field 
     */
    public FieldWrapper(Field field)
    {
        modifiers = field.getModifiers();
        name = field.getName();
        declaringClass = field.getDeclaringClass();
        type = field.getType();
        genericType = field.getGenericType();
    }

    public static FieldWrapper wrap(Member member)
    {
        if (member instanceof FieldWrapper)
        {
            return (FieldWrapper) member;
        }
        else
        {
            if (member instanceof Field)
            {
                return new FieldWrapper((Field)member);
            }
            else
            {
                throw new IllegalArgumentException(member+" not suitable for FieldWrapper");
            }
        }
    }
    
    public int getModifiers()
    {
        return modifiers;
    }

    public String getName()
    {
        return name;
    }

    public Type getType()
    {
        return type;
    }

    public GenericDeclaration getDeclaringClassWrapper()
    {
        return declaringClass;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Annotation[] getAnnotations()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Annotation[] getDeclaredAnnotations()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSynthetic()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Class<?> getDeclaringClass()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Type getGenericType()
    {
        return genericType;
    }

}
