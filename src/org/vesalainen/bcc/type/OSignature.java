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

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * @deprecated 
 * @author Timo Vesalainen
 */
public class OSignature extends ODescriptor
{
    /**
     * Return true if type needs a signature
     * @param type
     * @return 
     */
    public static boolean needsSignature(Type type)
    {
        Type superClass = Generics.getSuperclass(type);
        if (superClass != null && needsSignature(superClass))
        {
            return true;
        }
        if (type instanceof Class<?>)
        {
            Class<?> clazz = (Class<?>) type;
            return clazz.getTypeParameters().length > 0;
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper clazz = (ClassWrapper) type;
                return clazz.getTypeParameters().length > 0;
            }
            else
            {
                throw new IllegalArgumentException(type+" not valid");
            }
        }
    }
    /**
     * Return true if member needs a signature
     * @param member
     * @return 
     */
    public static boolean needsSignature(Member member)
    {
        if (member instanceof Field)
        {
            Field field = (Field) member;
            return !(field.getType().equals(field.getGenericType()));
        }
        else
        {
            if (member instanceof FieldWrapper)
            {
                FieldWrapper field = (FieldWrapper) member;
                return !(field.getType().equals(field.getGenericType()));
            }
            else
            {
                if (member instanceof Method)
                {
                    Method method = (Method) member;
                    if (method.getTypeParameters().length > 0)
                    {
                        return true;
                    }
                    Class<?>[] params = method.getParameterTypes();
                    Type[] paramTypes = method.getGenericParameterTypes();
                    for (int ii=0;ii<params.length;ii++)
                    {
                        if (!params[ii].equals(paramTypes[ii]))
                        {
                            return true;
                        }
                    }
                    return !(method.getReturnType().equals(method.getGenericReturnType()));
                }
                else
                {
                    if (member instanceof MethodWrapper)
                    {
                        MethodWrapper method = (MethodWrapper) member;
                        if (method.getTypeParameters().length > 0)
                        {
                            return true;
                        }
                        Type[] params = method.getParameterTypes();
                        Type[] paramTypes = method.getGenericParameterTypes();
                        for (int ii=0;ii<params.length;ii++)
                        {
                            if (!params[ii].equals(paramTypes[ii]))
                            {
                                return true;
                            }
                        }
                        return !(method.getReturnType().equals(method.getGenericReturnType()));
                    }
                    else
                    {
                        throw new IllegalArgumentException(member+" not valid");
                    }
                }
            }
        }
    }

    /**
     * ClassSignature:
     *  FormalTypeParametersopt SuperclassSignature SuperinterfaceSignature*
     * 
     * <U:Ljava/lang/Integer;:Ljava/io/Serializable;>Lorg/vesalainen/bcc/Test;Ljava/lang/Cloneable;;
     * 
     * @param clazz
     * @return 
     */
    public static String getClassSignature(Type type)
    {
        StringBuilder sb = new StringBuilder();
        if (type instanceof Class<?>)
        {
            Class<?> clazz = (Class<?>) type;
            formalTypeParameters(sb, (TypeVariable[]) clazz.getTypeParameters());
            Type superClass = clazz.getGenericSuperclass();
            // SuperclassSignature:
            //     ClassTypeSignature
            if (superClass != null)
            {
                classTypeSignature(sb, superClass);
            }
            // SuperinterfaceSignature:
            //    ClassTypeSignature
            for (Type intf : clazz.getGenericInterfaces())
            {
                classTypeSignature(sb, intf);
            }
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper clazz = (ClassWrapper) type;
                formalTypeParameters(sb, (TypeVariable[]) clazz.getTypeParameters());
                Type superClass = clazz.getGenericSuperclass();
                // SuperclassSignature:
                //     ClassTypeSignature
                if (superClass != null)
                {
                    classTypeSignature(sb, superClass);
                }
                // SuperinterfaceSignature:
                //    ClassTypeSignature
                for (Type intf : clazz.getGenericInterfaces())
                {
                    classTypeSignature(sb, intf);
                }
            }
            else
            {
                throw new IllegalArgumentException(type+" not valid");
            }
        }
        return sb.toString();
    }
    /**
     * MethodTypeSignature:
     *  FormalTypeParametersopt (TypeSignature*) ReturnType ThrowsSignature*
     * @param method
     * @return 
     */
    public static String getMethodSignature(Member member)
    {
        StringBuilder sb = new StringBuilder();
        if (member instanceof Method)
        {
            Method method = (Method) member;
            formalTypeParameters(sb, (TypeVariable[]) method.getTypeParameters());
            sb.append('(');
            for (Type param : method.getGenericParameterTypes())
            {
                typeSignature(sb, param);
            }
            sb.append(')');
            // ReturnType:
            //    TypeSignature
            //    VoidDescriptor
            Type returnType = method.getGenericReturnType();
            if (void.class.equals(returnType))
            {
                sb.append('V');
            }
            else
            {
                typeSignature(sb, returnType);
            }

            for (Type param : method.getGenericExceptionTypes())
            {
                getThrowsSignature(sb, param);
            }
        }
        else
        {
            if (member instanceof MethodWrapper)
            {
                MethodWrapper method = (MethodWrapper) member;
                formalTypeParameters(sb, (TypeVariable[]) method.getTypeParameters());
                sb.append('(');
                for (Type param : method.getGenericParameterTypes())
                {
                    typeSignature(sb, param);
                }
                sb.append(')');
                // ReturnType:
                //    TypeSignature
                //    VoidDescriptor
                Type returnType = method.getGenericReturnType();
                if (void.class.equals(returnType))
                {
                    sb.append('V');
                }
                else
                {
                    typeSignature(sb, returnType);
                }

                for (Type param : method.getGenericExceptionTypes())
                {
                    getThrowsSignature(sb, param);
                }
            }
            else
            {
                throw new IllegalArgumentException(member+" not valid");
            }
        }
        return sb.toString();
    }
    /**
     * 
     * @param field
     * @return 
     */
    public static String getFieldSignature(Member member)
    {
        StringBuilder sb = new StringBuilder();
        if (member instanceof Field)
        {
            Field field = (Field) member;
            fieldTypeSignature(sb, field.getGenericType());
        }
        else
        {
            if (member instanceof FieldWrapper)
            {
                FieldWrapper field = (FieldWrapper) member;
                fieldTypeSignature(sb, field.getGenericType());
            }
            else
            {
                throw new IllegalArgumentException(member+" not valid");
            }
        }
        return sb.toString();
    }
    /**
     * Return field signature
     * @param type Fields type
     * @return 
     */
    public static String getFieldSignature(Type type)
    {
        StringBuilder sb = new StringBuilder();
        fieldTypeSignature(sb, type);
        return sb.toString();
    }

    /**
     * FormalTypeParameters:
     *   < FormalTypeParameter+ >
     * @param clazz
     * @return 
     */
    @SuppressWarnings("unchecked")
    private static void formalTypeParameters(StringBuilder sb, TypeVariable[] typeParameters)
    {
        if (typeParameters.length > 0)
        {
            sb.append('<');
            for (TypeVariable typeVariable : typeParameters)
            {
                formalTypeParameter(sb, typeVariable);
            }
            sb.append('>');
        }
    }
    /**
     * FormalTypeParameter:
     *   Identifier ClassBound InterfaceBound*
     * @param typeVariable
     * @return 
     */
    private static void formalTypeParameter(StringBuilder sb, TypeVariable<Class<?>> typeVariable)
    {
        sb.append(typeVariable.getName());
        Type[] types = typeVariable.getBounds();
        if (types.length > 0 && !isClass(types[0]))
        {
            sb.append(':');
        }
        for (Type type : types)
        {
            sb.append(':');
            fieldTypeSignature(sb, type);
        }
    }
    /**
     * FieldTypeSignature:
     *   ClassTypeSignature
     *   ArrayTypeSignature
     *   TypeVariableSignature
     * @param type
     * @return 
     */
    private static void fieldTypeSignature(StringBuilder sb, Type type)
    {
        if (type instanceof TypeVariable)
        {
            typeVariableSignature(sb, (TypeVariable)type);
        }
        else
        {
            if (type instanceof GenericArrayType)
            {
                arrayTypeSignature(sb, (GenericArrayType)type);
            }
            else
            {
                if ((type instanceof ParameterizedType) || isClass(type))
                {
                    classTypeSignature(sb, type);
                }
                else
                {
                    throw new IllegalArgumentException(type+" not valid type for FieldTypeSignature");
                }
            }
        }
    }
    /**
     * TypeVariableSignature:
     *   T Identifier ;
     * @param sb
     * @param typeVariable 
     */
    private static void typeVariableSignature(StringBuilder sb, TypeVariable typeVariable)
    {
        sb.append('T');
        sb.append(typeVariable.getName());
        sb.append(';');
    }
    /**
     * ArrayTypeSignature:
     *   [ TypeSignature
     * @param sb
     * @param genericArrayType 
     */
    private static void arrayTypeSignature(StringBuilder sb, GenericArrayType genericArrayType)
    {
        sb.append('[');
        typeSignature(sb, genericArrayType.getGenericComponentType());
    }
    /**
     * TypeSignature:
     *   FieldTypeSignature
     *   BaseType
     * @param sb
     * @param type 
     */
    private static void typeSignature(StringBuilder sb, Type type)
    {
        if (isBaseType(type))
        {
            baseType(sb, type);
        }
        else
        {
            fieldTypeSignature(sb, type);
        }
    }
    /**
     * ClassTypeSignature:
     *   L PackageSpecifieropt SimpleClassTypeSignature ClassTypeSignatureSuffix* ;
     * @param sb
     * @param type 
     */
    private static void classTypeSignature(StringBuilder sb, Type type)
    {
        sb.append('L');
        packageSpecifier(sb, type);
        simpleClassTypeSignature(sb, type);
        classTypeSignatureSuffix(sb, type);
        sb.append(';');
    }
    /**
     * PackageSpecifier:
     *   Identifier / PackageSpecifier*
     * @param sb
     * @param type 
     */
    private static void packageSpecifier(StringBuilder sb, Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            Package pkg = cls.getPackage();
            sb.append(pkg.getName().replace('.', '/'));
            sb.append('/');
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                String pkg = cls.getPackageName().replace('.', '/');
                sb.append(pkg);
                sb.append('/');
            }
            else
            {
                if (type instanceof ParameterizedType)
                {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    packageSpecifier(sb, parameterizedType.getRawType());
                }
                else
                {
                    throw new IllegalArgumentException(type+ "not supported");
                }
            }
        }
    }
    /**
     * SimpleClassTypeSignature:
     *   Identifier TypeArgumentsopt
     * @param type 
     */
    private static void simpleClassTypeSignature(StringBuilder sb, Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            String name = cls.getName();
            int idx = name.lastIndexOf('.');
            if (idx != -1)
            {
                name = name.substring(idx+1);
            }
            sb.append(name);
            typeArguments(sb, cls.getTypeParameters());
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                String name = cls.getName();
                int idx = name.lastIndexOf('.');
                if (idx != -1)
                {
                    name = name.substring(idx+1);
                }
                sb.append(name);
                typeArguments(sb, cls.getTypeParameters());
            }
            else
            {
                if (type instanceof ParameterizedType)
                {
                    ParameterizedType parameterizedType = (ParameterizedType) type;
                    Class<?> raw = (Class<?>) parameterizedType.getRawType();
                    String name = raw.getName();
                    int idx = name.lastIndexOf('.');
                    if (idx != -1)
                    {
                        name = name.substring(idx+1);
                    }
                    sb.append(name);
                    typeArguments(sb, parameterizedType.getActualTypeArguments());
                }
                else
                {
                    throw new IllegalArgumentException(type+ "not supported");
                }
            }
        }
    }

    private static void classTypeSignatureSuffix(StringBuilder sb, Type type)
    {
        // TODO couldn't find any test case for this!!!!
        //sb.append('.');
        //simpleClassTypeSignature(sb, type);
    }

    private static void classTypeSignature(StringBuilder sb, ParameterizedType parameterizedType)
    {
        sb.append('L');
        Class<?> raw = (Class<?>) parameterizedType.getRawType();
        sb.append(raw.getName().replace('.', '/'));
        typeArguments(sb, parameterizedType.getActualTypeArguments());
        sb.append(';');
    }
    /**
     * TypeArguments:
     *   < TypeArgument+ >
     * @param sb
     * @param types 
     */
    @SuppressWarnings("unchecked")
    private static void typeArguments(StringBuilder sb, Type[] types)
    {
        if (types.length > 0)
        {
            sb.append('<');
            for (Type type : types)
            {
                if (type instanceof ParameterizedType)
                {
                    typeArgument(sb, (ParameterizedType)type);
                }
                else
                {
                    if (type instanceof TypeVariable)
                    {
                        typeArgument(sb, (TypeVariable)type);
                    }
                    else
                    {
                        if (type instanceof WildcardType)
                        {
                            typeArgument(sb, (WildcardType)type);
                        }
                        else
                        {
                            typeArgument(sb, (Class<?>)type);
                        }
                    }
                }
            }
            sb.append('>');
        }
    }
    /**
     * TypeArgument:
     *   WildcardIndicatoropt FieldTypeSignature*
     * @param sb
     * @param cls 
     */
    private static void typeArgument(StringBuilder sb, Class<?> cls)
    {
        fieldTypeSignature(sb, cls);
    }
    private static void typeArgument(StringBuilder sb, ParameterizedType parameterizedType)
    {
        classTypeSignature(sb, parameterizedType);
    }
    private static void typeArgument(StringBuilder sb, WildcardType wildcardType)
    {
        for (Type type : wildcardType.getUpperBounds())
        {
            if (!Object.class.equals(type))
            {
                sb.append('+');
                fieldTypeSignature(sb, type);
            }
        }
        for (Type type : wildcardType.getLowerBounds())
        {
            sb.append('-');
            fieldTypeSignature(sb, type);
        }
    }
    private static void typeArgument(StringBuilder sb, TypeVariable<Class<?>> typeVariable)
    {
        fieldTypeSignature(sb, typeVariable);
    }

    private static void getThrowsSignature(StringBuilder sb, Type param)
    {
        // If the throws clause of a method or constructor does not involve type variables, the ThowsSignature may be elided from the MethodTypeSignature.
    }

    
}
