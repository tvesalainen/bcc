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
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;

/**
 * @author Timo Vesalainen
 */
public class TypesImpl implements Types
{
    public static final NullType Null = new NullTypeImpl();
    public static final NoType Void = new NoTypeImpl(TypeKind.VOID);
    public static final NoType Package = new NoTypeImpl(TypeKind.PACKAGE);
    public static final NoType None = new NoTypeImpl(TypeKind.NONE);

    public static final PrimitiveType Byte = new PrimitiveTypeImpl(TypeKind.BYTE);
    public static final PrimitiveType Boolean = new PrimitiveTypeImpl(TypeKind.BOOLEAN);
    public static final PrimitiveType Char = new PrimitiveTypeImpl(TypeKind.CHAR);
    public static final PrimitiveType Short = new PrimitiveTypeImpl(TypeKind.SHORT);
    public static final PrimitiveType Int = new PrimitiveTypeImpl(TypeKind.INT);
    public static final PrimitiveType Long = new PrimitiveTypeImpl(TypeKind.LONG);
    public static final PrimitiveType Float = new PrimitiveTypeImpl(TypeKind.FLOAT);
    public static final PrimitiveType Double = new PrimitiveTypeImpl(TypeKind.DOUBLE);
    
    @Override
    public Element asElement(TypeMirror t)
    {
        switch (t.getKind())
        {
            case DECLARED:
                DeclaredType dt = (DeclaredType) t;
                return dt.asElement();
            case TYPEVAR:
                TypeVariable tv = (TypeVariable) t;
                return tv.asElement();
            default:
                return null;
        }
    }

    @Override
    public boolean isSameType(TypeMirror t1, TypeMirror t2)
    {
        if (t1.getKind() == TypeKind.WILDCARD || t2.getKind() == TypeKind.WILDCARD)
        {
            return false;
        }
        else
        {
            return t1.getKind() == t2.getKind();
        }
    }

    @Override
    public boolean isSubtype(TypeMirror sub, TypeMirror sup)
    {
        if (isSameType(sub, sup))
        {
            return true;
        }
        switch (sup.getKind())
        {
            case DOUBLE:
                switch (sub.getKind())
                {
                    case FLOAT:
                    case LONG:
                    case INT:
                    case CHAR:
                    case SHORT:
                    case BYTE:
                        return true;
                    default:
                        return false;
                }
            case FLOAT:
                switch (sub.getKind())
                {
                    case LONG:
                    case INT:
                    case CHAR:
                    case SHORT:
                    case BYTE:
                        return true;
                    default:
                        return false;
                }
            case LONG:
                switch (sub.getKind())
                {
                    case INT:
                    case CHAR:
                    case SHORT:
                    case BYTE:
                        return true;
                    default:
                        return false;
                }
            case INT:
                switch (sub.getKind())
                {
                    case CHAR:
                    case SHORT:
                    case BYTE:
                        return true;
                    default:
                        return false;
                }
            case SHORT:
                switch (sub.getKind())
                {
                    case BYTE:
                        return true;
                    default:
                        return false;
                }
            case DECLARED:
                switch (sub.getKind())
                {
                    case DECLARED:
                        for (TypeMirror t : directSupertypes(sub))
                        {
                            if (t.equals(sup))
                            {
                                return true;
                            }
                        }
                        return false;
                    case NULL:
                        return true;
                    default:
                        return false;
                }
            case EXECUTABLE:
            case PACKAGE:
                throw new IllegalArgumentException(sup+" is unsuitable type");
            default:
                return false;
        }
    }

    @Override
    public boolean isAssignable(TypeMirror t1, TypeMirror t2)
    {
        if (isSubtype(t1, t2))
        {
            return true;
        }
        switch (t2.getKind())
        {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
            case DECLARED:
                return false;
            case NULL:
            case ARRAY:
            default:
                throw new IllegalArgumentException(t2+" not suitable type");
            case EXECUTABLE:
            case PACKAGE:
                throw new IllegalArgumentException(t2+" is unsuitable type");
        }
    }

    @Override
    public boolean contains(TypeMirror t1, TypeMirror t2)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSubsignature(ExecutableType m1, ExecutableType m2)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<? extends TypeMirror> directSupertypes(TypeMirror t)
    {
        switch (t.getKind())
        {
            case DECLARED:
                DeclaredType dt = (DeclaredType) t;
                TypeElement te = (TypeElement) dt.asElement();
                List<TypeMirror> list = new ArrayList<>();
                TypeMirror superclass = te.getSuperclass();
                while (TypeKind.NONE != superclass.getKind())
                {
                    list.add(0, superclass);
                    list.addAll(te.getInterfaces());
                    TypeElement sc = (TypeElement) superclass;
                    superclass = sc.getSuperclass();
                }
                return list;
            case EXECUTABLE:
            case PACKAGE:
                throw new IllegalArgumentException(t.getKind().name());
            default:
                throw new UnsupportedOperationException(t.getKind().name()+" not supported yet");
        }
    }

    @Override
    public TypeMirror erasure(TypeMirror t)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TypeElement boxedClass(PrimitiveType p)
    {
        switch (p.getKind())
        {
            case BYTE:
                return ElementFactory.get(Byte.class);
            case BOOLEAN:
                return ElementFactory.get(Boolean.class);
            case CHAR:
                return ElementFactory.get(Character.class);
            case SHORT:
                return ElementFactory.get(Short.class);
            case INT:
                return ElementFactory.get(Integer.class);
            case LONG:
                return ElementFactory.get(Long.class);
            case FLOAT:
                return ElementFactory.get(Float.class);
            case DOUBLE:
                return ElementFactory.get(Double.class);
            case VOID:
                return ElementFactory.get(Void.class);
            default:
                throw new IllegalArgumentException(p+" is not primitive");
        }
    }

    @Override
    public PrimitiveType unboxedType(TypeMirror t)
    {
        if (TypeKind.DECLARED == t.getKind())
        {
            DeclaredType dt = (DeclaredType) t;
            TypeElement te = (TypeElement) dt.asElement();
            switch (te.getQualifiedName().toString())
            {
                case "byte":
                    return Byte;
                case "boolean":
                    return Boolean;
                case "char":
                    return Char;
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
            }
        }
        throw new IllegalArgumentException(t+" cannot be unboxed");
    }

    @Override
    public TypeMirror capture(TypeMirror t)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PrimitiveType getPrimitiveType(TypeKind kind)
    {
        switch (kind)
        {
            case BYTE:
                return Byte;
            case BOOLEAN:
                return Boolean;
            case CHAR:
                return Char;
            case SHORT:
                return Short;
            case INT:
                return Int;
            case LONG:
                return Long;
            case FLOAT:
                return Float;
            case DOUBLE:
                return Double;
            default:
                throw new IllegalArgumentException(kind+" is not primitive");
        }
    }

    @Override
    public NullType getNullType()
    {
        return Null;
    }

    @Override
    public NoType getNoType(TypeKind kind)
    {
        switch (kind)
        {
            case NONE:
                return None;
            case VOID:
                return Void;
            case PACKAGE:
                return Package;
            default:
                throw new IllegalArgumentException(kind+" no no-type");
        }
    }

    @Override
    public ArrayType getArrayType(TypeMirror componentType)
    {
        return new ArrayTypeImpl(componentType);
    }

    @Override
    public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TypeMirror asMemberOf(DeclaredType containing, Element element)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
