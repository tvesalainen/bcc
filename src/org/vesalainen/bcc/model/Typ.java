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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;
import org.vesalainen.bcc.ReturnAddress;

/**
 * @author Timo Vesalainen
 */
public class Typ 
{
    public static NullType Null;
    public static NoType Void;
    public static NoType Package;
    public static NoType None;

    public static PrimitiveType Byte;
    public static PrimitiveType Boolean;
    public static PrimitiveType Char;
    public static PrimitiveType Short;
    public static PrimitiveType Int;
    public static PrimitiveType Long;
    public static PrimitiveType Float;
    public static PrimitiveType Double;
    public static ArrayType ByteA;
    public static ArrayType BooleanA;
    public static ArrayType CharA;
    public static ArrayType ShortA;
    public static ArrayType IntA;
    public static ArrayType LongA;
    public static ArrayType FloatA;
    public static ArrayType DoubleA;
    public static ArrayType StringA;
    public static ArrayType ObjectA;
    public static DeclaredType String;
    public static DeclaredType Object;
    public static TypeMirror ReturnAddress;
    
    private static Types types;

    static
    {
        setTypes(new TypesImpl());
    }
    
    public static void setTypes(Types types)
    {
        Typ.types = types;
        Null = types.getNullType();
        Void = types.getNoType(TypeKind.VOID);
        Package = types.getNoType(TypeKind.PACKAGE);
        None = types.getNoType(TypeKind.NONE);
        Byte = types.getPrimitiveType(TypeKind.BYTE);
        Boolean = types.getPrimitiveType(TypeKind.BOOLEAN);
        Char = types.getPrimitiveType(TypeKind.CHAR);
        Short = types.getPrimitiveType(TypeKind.SHORT);
        Int = types.getPrimitiveType(TypeKind.INT);
        Long = types.getPrimitiveType(TypeKind.LONG);
        Float = types.getPrimitiveType(TypeKind.FLOAT);
        Double = types.getPrimitiveType(TypeKind.DOUBLE);
        String = (DeclaredType) getTypeFor(String.class);
        Object = (DeclaredType) getTypeFor(Object.class);
        ByteA = types.getArrayType(Byte);
        BooleanA = types.getArrayType(Boolean);
        CharA = types.getArrayType(Char);
        ShortA = types.getArrayType(Short);
        IntA = types.getArrayType(Int);
        LongA = types.getArrayType(Long);
        FloatA = types.getArrayType(Float);
        DoubleA = types.getArrayType(Double);
        StringA = types.getArrayType(String);
        ObjectA = types.getArrayType(Object);
        ReturnAddress = new ReturnAddress();
    }

    public static boolean isJavaConstant(Object value)
    {
        return 
                (value instanceof Boolean) ||
                (value instanceof Character) ||
                (value instanceof Byte) ||
                (value instanceof Short) ||
                (value instanceof Integer) ||
                (value instanceof Long) ||
                (value instanceof Float) ||
                (value instanceof Double) ||
                (value instanceof String)
                ;
    }
    public static boolean isPrimitive(TypeMirror type)
    {
        switch (type.getKind())
        {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                return true;
            default:
                return false;
        }
    }
    public static TypeMirror getTypeFor(Class<?> cls)
    {
        if (cls.isPrimitive())
        {
            switch (cls.getSimpleName())
            {
                case "boolean":
                    return Boolean;
                case "char":
                    return Char;
                case "byte":
                    return Byte;
                case "short":
                    return Short;
                case "int":
                    return Int;
                case "long":
                    return Long;
                case "float":
                    return Float;
                case "double":
                    return Double;
                case "void":
                    return Void;
                default:
                    throw new IllegalArgumentException(cls+" of unknown primitive");
            }
        }
        else
        {
            if (cls.isArray())
            {
                return types.getArrayType(getTypeFor(cls.getComponentType()));
            }
            else
            {
                return El.getTypeElement(cls.getCanonicalName()).asType();
            }
        }
    }
    public static int getMaxIndexOf(List<? extends VariableElement> params)
    {
        int index = 0;
        for (VariableElement lv : params)
        {
            if (Typ.isCategory2(lv.asType()))
            {
                index += 2;
            }
            else
            {
                index++;
            }
        }
        return index;
    }
    public static int getIndexOf(List<? extends VariableElement> params, int ind)
    {
        int index = 0;
        for (int ii=0;ii<ind;ii++)
        {
            VariableElement lv = params.get(ii);
            if (Typ.isCategory2(lv.asType()))
            {
                index += 2;
            }
            else
            {
                index++;
            }
        }
        return index;
    }
    public static boolean isCategory2(TypeMirror type)
    {
        TypeKind kind = type.getKind();
        return kind == TypeKind.LONG || kind == TypeKind.DOUBLE;
    }
    public static TypeMirror typeFromDescriptor(String descriptor)
    {
        StringIterator si = new StringIterator(descriptor);
        return parseNext(si);
    }
    public static List<? extends TypeMirror> typesFromDescriptor(String descriptor)
    {
        List<TypeMirror> list = new ArrayList<>();
        StringIterator si = new StringIterator(descriptor);
        TypeMirror t = parseNext(si);
        while ( t != null && si.hasNext())
        {
            list.add(t);
            t = parseNext(si);
        }
        return list;
    }
    private static TypeMirror parseNext(StringIterator si)
    {
        char cc = si.next();
        switch (cc)
        {
            case 'Z':
                return Typ.Boolean;
            case 'B':
                return Typ.Byte;
            case 'C':
                return Typ.Char;
            case 'S':
                return Typ.Short;
            case 'I':
                return Typ.Int;
            case 'J':
                return Typ.Long;
            case 'F':
                return Typ.Float;
            case 'D':
                return Typ.Double;
            case 'L':
                return El.fromDescriptor("L" + parseObject(si) + ";").asType();
            case '[':
                return Typ.getArrayType(parseNext(si));
            case '(':
                return parseNext(si);
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
    
    public static Element asElement(TypeMirror t)
    {
        return types.asElement(t);
    }

    public static boolean isSameType(TypeMirror t1, TypeMirror t2)
    {
        return types.isSameType(t1, t2);
    }

    public static boolean isSubtype(TypeMirror t1, TypeMirror t2)
    {
        return types.isSubtype(t1, t2);
    }

    public static boolean isAssignable(TypeMirror t1, TypeMirror t2)
    {
        return types.isAssignable(t1, t2);
    }

    public static boolean contains(TypeMirror t1, TypeMirror t2)
    {
        return types.contains(t1, t2);
    }

    public static boolean isSubsignature(ExecutableType m1, ExecutableType m2)
    {
        return types.isSubsignature(m1, m2);
    }

    public static List<? extends TypeMirror> directSupertypes(TypeMirror t)
    {
        return types.directSupertypes(t);
    }

    public static TypeMirror erasure(TypeMirror t)
    {
        return types.erasure(t);
    }

    public static TypeElement boxedClass(PrimitiveType p)
    {
        return types.boxedClass(p);
    }

    public static PrimitiveType unboxedType(TypeMirror t)
    {
        return types.unboxedType(t);
    }

    public static TypeMirror capture(TypeMirror t)
    {
        return types.capture(t);
    }

    public static PrimitiveType getPrimitiveType(TypeKind kind)
    {
        return types.getPrimitiveType(kind);
    }

    public static NullType getNullType()
    {
        return types.getNullType();
    }

    public static NoType getNoType(TypeKind kind)
    {
        return types.getNoType(kind);
    }

    public static ArrayType getArrayType(TypeMirror componentType)
    {
        return types.getArrayType(componentType);
    }

    public static WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound)
    {
        return types.getWildcardType(extendsBound, superBound);
    }

    public static DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs)
    {
        return types.getDeclaredType(typeElem, typeArgs);
    }

    public static DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs)
    {
        return types.getDeclaredType(containing, typeElem, typeArgs);
    }

    public static TypeMirror asMemberOf(DeclaredType containing, Element element)
    {
        return types.asMemberOf(containing, element);
    }

    public static TypeMirror typeFromSignature(String string)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static boolean isInteger(TypeMirror type)
    {
        switch (type.getKind())
        {
            case INT:
            case SHORT:
            case BYTE:
            case BOOLEAN:
                return true;
            default:
                return false;
        }
    }

    public static boolean isJavaConstantClass(TypeMirror type)
    {
        switch (type.getKind())
        {
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return true;
            case DECLARED:
                DeclaredType dt = (DeclaredType) type;
                TypeElement te = (TypeElement) dt.asElement();
                return "java.lang.String".contentEquals(te.getQualifiedName());
            default:
                return false;
        }
    }

    public static java.lang.String getInternalForm(TypeMirror type)
    {
        throw new UnsupportedOperationException("Not yet implemented");
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
