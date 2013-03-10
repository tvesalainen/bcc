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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

/**
 * @author Timo Vesalainen
 */
public class TypeMirrorFactory 
{
    
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
            java.lang.reflect.TypeVariable<? extends java.lang.reflect.GenericDeclaration> tv = (java.lang.reflect.TypeVariable<? extends java.lang.reflect.GenericDeclaration>) type;
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
    private static Map<Annotation,DeclaredType> declaredTypeAnnotationMap = new HashMap<>();
    public static DeclaredType get(Annotation annotation)
    {
        DeclaredType dt = declaredTypeAnnotationMap.get(annotation);
        if (dt == null)
        {
            DeclaredTypeImpl dti = new DeclaredTypeImpl();
            declaredTypeAnnotationMap.put(annotation, dti);
            dti.init(annotation);
            dt = dti;
        }
        return dt;
    }

    public static DeclaredType get(Enum en)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static ExecutableType get(Constructor constructor)
    {
        ExecutableType et = executableTypeMap.get(constructor);
        if (et == null)
        {
            et = new ExecutableTypeImpl(constructor);
            executableTypeMap.put(constructor, et);
        }
        return et;
    }

    private static Map<Member,ExecutableType> executableTypeMap = new HashMap<>();
    public static ExecutableType get(Method method)
    {
        ExecutableType et = executableTypeMap.get(method);
        if (et == null)
        {
            et = new ExecutableTypeImpl(method);
            executableTypeMap.put(method, et);
        }
        return et;
    }
    private static Map<java.lang.reflect.GenericArrayType,ArrayType> arrayTypeMap = new HashMap<>();
    public static ArrayType getGenericArrayType(java.lang.reflect.GenericArrayType gat)
    {
        ArrayType at = arrayTypeMap.get(gat);
        if (at == null)
        {
            at = new ArrayTypeImpl(gat);
            arrayTypeMap.put(gat, at);
        }
        return at;
    }

    private static Map<java.lang.reflect.ParameterizedType,DeclaredType> parameterizedTypeMap = new HashMap<>();
    public static DeclaredType getParameterizedType(java.lang.reflect.ParameterizedType pt)
    {
        DeclaredType dt = parameterizedTypeMap.get(pt);
        if (dt == null)
        {
            DeclaredTypeImpl dti = new DeclaredTypeImpl();
            parameterizedTypeMap.put(pt, dti);
            dti.init(pt);
            dt = dti;
        }
        return dt;
    }
    private static Map<java.lang.reflect.TypeVariable<? extends java.lang.reflect.GenericDeclaration>,TypeVariable> typeVariableMap = new HashMap<>();
    public static <D extends java.lang.reflect.GenericDeclaration> TypeVariable getTypeVariable(java.lang.reflect.TypeVariable<D> typeVar)
    {
        TypeVariable tv = typeVariableMap.get(typeVar);
        if (tv == null)
        {
            tv = new TypeVariableImpl(typeVar);
            typeVariableMap.put(typeVar, tv);
        }
        return tv;
    }
    private static Map<java.lang.reflect.WildcardType,WildcardType> wildcardTypeMap = new HashMap<>();
    public static WildcardType getWildcardType(java.lang.reflect.WildcardType rwt)
    {
        WildcardType wt = wildcardTypeMap.get(rwt);
        if (wt == null)
        {
            wt = new WildcardTypeImpl(rwt);
            wildcardTypeMap.put(rwt, wt);
        }
        return wt;
    }

    private static Map<Class<?>,TypeMirror> classMap = new HashMap<>();
    public static TypeMirror getClassType(Class<?> cls)
    {
        TypeMirror tm = classMap.get(cls);
        if (tm == null)
        {
            if (cls.isArray())
            {
                tm = new ArrayTypeImpl(cls);
                classMap.put(cls, tm);
            }
            else
            {
                if (cls.isPrimitive())
                {
                    tm = new PrimitiveTypeImpl(cls);
                    classMap.put(cls, tm);
                }
                else
                {
                    DeclaredTypeImpl dti = new DeclaredTypeImpl();
                    classMap.put(cls, dti);
                    dti.init(cls);
                    tm = dti;
                }
            }
        }
        return tm;
    }
    private static Map<Type[],DeclaredType> intersectionTypeMap = new HashMap<>();
    static DeclaredType getIntersectionType(Type[] bounds)
    {
        DeclaredType dt = intersectionTypeMap.get(bounds);
        if (dt == null)
        {
            DeclaredTypeImpl dti = new DeclaredTypeImpl();
            intersectionTypeMap.put(bounds, dti);
            dti.init(bounds);
            dt = dti;
        }
        return dt;
    }

}
