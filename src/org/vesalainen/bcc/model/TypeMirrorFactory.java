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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
public class TypeMirrorFactory 
{
    public static final NullType Null = new NullTypeImpl();
    public static final NoType Void = new NoTypeImpl(TypeKind.VOID);
    public static final NoType Package = new NoTypeImpl(TypeKind.PACKAGE);
    public static final NoType None = new NoTypeImpl(TypeKind.NONE);
    
    public static TypeMirror get(Type type)
    {
        if (type instanceof java.lang.reflect.GenericArrayType)
        {
            java.lang.reflect.GenericArrayType gat = (java.lang.reflect.GenericArrayType) type;
            return getGenericArrayType(gat);
        }
        if (type instanceof java.lang.reflect.ParameterizedType)
        {
            java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;
            return getParameterizedType(pt);
        }
        if (type instanceof java.lang.reflect.TypeVariable)
        {
            java.lang.reflect.TypeVariable tv = (java.lang.reflect.TypeVariable) type;
            return getTypeVariable(tv);
        }
        if (type instanceof java.lang.reflect.WildcardType)
        {
            java.lang.reflect.WildcardType wt = (java.lang.reflect.WildcardType) type;
            return getWildcardType(wt);
        }
        if (type instanceof Class)
        {
            Class<?> cls = (Class<?>) type;
            return getClassType(cls);
        }
        throw new UnsupportedOperationException(type+" Not implemented");
    }

    public static DeclaredType get(Annotation annotation)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static DeclaredType get(Enum en)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static TypeMirror getGenericArrayType(java.lang.reflect.GenericArrayType gat)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static TypeMirror getParameterizedType(java.lang.reflect.ParameterizedType pt)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static TypeVariable getTypeVariable(java.lang.reflect.TypeVariable tv)
    {
        return new TypeVariableImpl(tv);
    }

    public static TypeMirror getWildcardType(java.lang.reflect.WildcardType wt)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static TypeMirror getClassType(Class<?> cls)
    {
        if (cls.isArray())
        {
            return new ArrayTypeImpl(cls);
        }
        if (cls.isPrimitive())
        {
            return new PrimitiveTypeImpl(cls);
        }
        return new DeclaredTypeImpl(cls);
    }

    static TypeMirror getIntersectionType(Type[] bounds)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static class NullTypeImpl implements NullType
    {

        private NullTypeImpl()
        {
        }

        @Override
        public TypeKind getKind()
        {
            return TypeKind.NULL;
        }

        @Override
        public <R, P> R accept(TypeVisitor<R, P> v, P p)
        {
            return v.visitNull(this, p);
        }

    }
    public static class NoTypeImpl implements NoType 
    {
        private TypeKind typeKind;
        private NoTypeImpl(TypeKind typeKind)
        {
            this.typeKind = typeKind;
        }

        @Override
        public TypeKind getKind()
        {
            return typeKind;
        }

        @Override
        public <R, P> R accept(TypeVisitor<R, P> v, P p)
        {
            return v.visitNoType(this, p);
        }

    }
}
