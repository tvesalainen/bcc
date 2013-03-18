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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @deprecated 
 *
 * @author tkv
 */
public class ODescriptor
{
    /**
     * Returns effective returntype. With effective we mean that if type is one 
     * of char, boolean, byte or short we return int.
     * @param methodDescriptor
     * @return 
     */
    public static Type getReturnType(String methodDescriptor)
    {
        int idx = methodDescriptor.lastIndexOf(')');
        String rt = methodDescriptor.substring(idx + 1);
        Type type = ClassWrapper.getType(rt);
        if (Generics.isInteger(type))
        {
            return int.class;
        }
        else
        {
            return type;
        }
    }
    
    public static Type[] getParameters(String methodDescriptor)
    {
        List<Type> list = new ArrayList<>();
        StringIterator si = new StringIterator(methodDescriptor.substring(1));
        while (si.hasNext())
        {
            String s = parseNext(si);
            if (s == null)
            {
                break;
            }
            list.add(ClassWrapper.getType(s));
        }
        return list.toArray(new Type[list.size()]);
    }

    private static String parseNext(StringIterator si)
    {
        char cc = si.next();
        switch (cc)
        {
            case 'Z':
            case 'B':
            case 'C':
            case 'S':
            case 'I':
            case 'J':
            case 'F':
            case 'D':
                return String.valueOf(cc);
            case 'L':
                return "L" + parseObject(si) + ";";
            case '[':
                return "[" + parseNext(si);
            case ')':
                return null;
            default:
                throw new IllegalArgumentException("illegal descriptor format ");
        }
    }

    private static String parseObject(StringIterator si)
    {
        StringBuilder sb = new StringBuilder();
        while (si.hasNext())
        {
            char cc = si.next();
            if (cc == ';')
            {
                return sb.toString();
            }
            sb.append(cc);
        }
        throw new IllegalArgumentException("illegal descriptor format ");
    }
    
    public static String getFieldDesriptor(Member member)
    {
        StringBuilder sb = new StringBuilder();
        if (member instanceof FieldWrapper)
        {
            FieldWrapper fw = (FieldWrapper) member;
            fieldDescriptor(sb, fw.getType());
        }
        else
        {
            Field f = (Field) member;
            fieldDescriptor(sb, f.getType());
        }
        return sb.toString();
    }
    /**
     * 
     */
    public static String getFieldDesriptor(Type type)
    {
        StringBuilder sb = new StringBuilder();
        if (type instanceof ClassWrapper)
        {
            ClassWrapper cw = (ClassWrapper) type;
            fieldDescriptor(sb, cw);
        }
        else
        {
            Class<?> c = (Class<?>) type;
            fieldDescriptor(sb, c);
        }
        return sb.toString();
    }
    /**
     * FieldDescriptor:
     *   FieldType
     * @param sb
     * @param type 
     */
    protected static void fieldDescriptor(StringBuilder sb, Type type)
    {
        fieldType(sb, type);
    }
    /**
     * ComponentType:
     *   FieldType
     * @param sb
     * @param type 
     */
    protected static void componentType(StringBuilder sb, Type type)
    {
        fieldType(sb, type);
    }
    /**
     * FieldType:
     *   BaseType
     *   ObjectType
     *   ArrayType
     * @param sb
     * @param type 
     */
    private static void fieldType(StringBuilder sb, Type type)
    {
        if (isBaseType(type))
        {
            baseType(sb, type);
        }
        else
        {
            if (type instanceof Class<?>)
            {
                Class<?> cls = (Class<?>) type;
                if (cls.isArray())
                {
                    sb.append('[');
                    componentType(sb, cls.getComponentType());
                }
                else
                {
                    sb.append('L');
                    sb.append(cls.getName().replace('.', '/'));
                    sb.append(';');
                }
            }
            else
            {
                if (type instanceof ClassWrapper)
                {
                    ClassWrapper cls = (ClassWrapper) type;
                    if (cls.isArray())
                    {
                        sb.append('[');
                        componentType(sb, cls.getComponentType());
                    }
                    else
                    {
                        sb.append('L');
                        sb.append(cls.getName().replace('.', '/'));
                        sb.append(';');
                    }
                }
                else
                {
                    if (type instanceof TypeVariable)
                    {
                        TypeVariable tv = (TypeVariable) type;
                        sb.append('L');
                        sb.append(Generics.getName(tv.getGenericDeclaration()).replace('.', '/'));
                        sb.append(';');
                    }
                    else
                    {
                        if (type instanceof GenericArrayType)
                        {
                            GenericArrayType gat = (GenericArrayType) type;
                            sb.append('[');
                            fieldType(sb, gat.getGenericComponentType());
                        }
                        else
                        {
                            throw new IllegalArgumentException(type+" not valid");
                        }
                    }
                }
            }
        }
    }
    public static String getMethodDesriptor(Member method)
    {
        return getMethodDesriptor(Generics.getReturnType(method), Generics.getParameterTypes(method));
    }
    public static String getMethodDesriptor(Type returnType, Type... parameterTypes)
    {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (Type param : parameterTypes)
        {
            parameterDescriptor(sb, param);
        }
        sb.append(')');
        returnDescriptor(sb, returnType);
        return sb.toString();
    }
    private static void parameterDescriptor(StringBuilder sb, Type type)
    {
        fieldType(sb, type);
    }
    private static void returnDescriptor(StringBuilder sb, Type type)
    {
        if (void.class.equals(type))
        {
            sb.append('V');
        }
        else
        {
            fieldType(sb, type);
        }
    }
    protected static boolean isBaseType(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.isPrimitive() && !void.class.equals(cls);
        }
        return false;
    }

    protected static void baseType(StringBuilder sb, Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> clazz = (Class<?>) type;
            if (boolean.class.equals(clazz)) sb.append('Z');
            else
            if (byte.class.equals(clazz)) sb.append('B');
            else
            if (char.class.equals(clazz)) sb.append('C');
            else
            if (short.class.equals(clazz)) sb.append('S');
            else
            if (int.class.equals(clazz)) sb.append('I');
            else
            if (long.class.equals(clazz)) sb.append('J');
            else
            if (float.class.equals(clazz)) sb.append('F');
            else
            if (double.class.equals(clazz)) sb.append('D');
            else
            throw new IllegalArgumentException("unknown primitive type "+clazz);
        }
        else
        {
            throw new IllegalArgumentException("unknown primitive type "+type);
        }
    }
    protected static boolean isClass(Type type)
    {
        return (type instanceof Class<?>) || (type instanceof ClassWrapper);
    }

    private static class StringIterator implements Iterator<Character>
    {

        private String text;
        private int pos;

        public StringIterator(String text)
        {
            this.text = text;
        }

        public boolean hasNext()
        {
            return pos < text.length();
        }

        public Character next()
        {
            return text.charAt(pos++);
        }

        public void remove()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
