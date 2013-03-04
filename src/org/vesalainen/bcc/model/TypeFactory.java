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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public class TypeFactory 
{

    static TypeMirror get(Type type)
    {
        if (type instanceof GenericArrayType)
        {
            GenericArrayType gat = (GenericArrayType) type;
            return getGenericArrayType(gat);
        }
        if (type instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) type;
            return getParameterizedType(pt);
        }
        if (type instanceof TypeVariable)
        {
            TypeVariable tv = (TypeVariable) type;
            return getTypeVariable(tv);
        }
        if (type instanceof WildcardType)
        {
            WildcardType wt = (WildcardType) type;
            return getWildcardType(wt);
        }
        if (type instanceof Class)
        {
            Class<?> cls = (Class<?>) type;
            return getClassType(cls);
        }
        throw new UnsupportedOperationException(type+" Not implemented");
    }

    static DeclaredType get(Annotation annotation)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    static DeclaredType get(Enum en)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static TypeMirror getGenericArrayType(GenericArrayType gat)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static TypeMirror getParameterizedType(ParameterizedType pt)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static TypeMirror getTypeVariable(TypeVariable pt)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static TypeMirror getWildcardType(WildcardType wt)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static TypeMirror getClassType(Class<?> cls)
    {
        if (cls.isArray())
        {
            return new ArraySymbol(cls);
        }
        if (cls.isPrimitive())
        {
            return new PrimitiveSymbol(cls);
        }
        return new DeclaredSymbol(cls);
    }

}
