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

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * @author Timo Vesalainen
 */
public class T 
{
    private static Types types = new TypesImpl();

    public static void setTypes(Types types)
    {
        T.types = types;
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
    
}
