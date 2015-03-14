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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
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
            if (t1.getKind() == t2.getKind())
            {
                switch (t1.getKind())
                {
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case DOUBLE:
                    case FLOAT:
                    case INT:
                    case LONG:
                    case SHORT:
                    case VOID:
                    case OTHER:
                    case NULL:
                        return true;
                    case ARRAY:
                        ArrayType at1 = (ArrayType) t1;
                        ArrayType at2 = (ArrayType) t2;
                        return isSameType(at1.getComponentType(), at2.getComponentType());
                    case DECLARED:
                        DeclaredType dt1 = (DeclaredType) t1;
                        DeclaredType dt2 = (DeclaredType) t2;
                        TypeElement te1 = (TypeElement) dt1.asElement();
                        TypeElement te2 = (TypeElement) dt2.asElement();
                        return te1.getQualifiedName().contentEquals(te2.getQualifiedName());
                    case TYPEVAR:
                        TypeVariable tv1 = (TypeVariable) t1;
                        TypeVariable tv2 = (TypeVariable) t2;
                        return isSameType(tv1.getUpperBound(), tv2.getUpperBound()) && isSameType(tv1.getLowerBound(), tv2.getLowerBound());
                    default:
                        throw new IllegalArgumentException(t1+" "+t1.getKind()+" is unsuitable type");
                }
            }
            else
            {
                return false;
            }
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
                DeclaredType dt = (DeclaredType) sup;
                TypeElement te = (TypeElement) dt.asElement();
                boolean isObject = te.getQualifiedName().contentEquals("java.lang.Object");
                switch (sub.getKind())
                {
                    case DECLARED:
                        for (TypeMirror t : allSupertypes(sub))
                        {
                            if (isSameType(t, sup))
                            {
                                return true;
                            }
                        }
                        return isObject;    // TODO
                    case TYPEVAR:
                        TypeVariable tv = (TypeVariable) sub;
                        return isSubtype(tv.getUpperBound(), sup);
                    case ARRAY:
                        return isObject;
                    case NULL:
                        return true;
                    default:
                        return false;
                }
            case TYPEVAR:
                TypeVariable tv = (TypeVariable) sup;
                return isSubtype(sub, tv.getUpperBound());
            case ARRAY:
            case BOOLEAN:
            case VOID:
                return false;
            default:
                throw new IllegalArgumentException(sup+" is unsuitable type");
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
            case VOID:
                return false;
            case DECLARED:
                switch (t1.getKind())
                {
                    case NULL:
                        return true;
                    case ARRAY:
                        DeclaredType dt = (DeclaredType) t2;
                        TypeElement te = (TypeElement) dt.asElement();
                        Name n = te.getQualifiedName();
                        return n.contentEquals("java.lang.Object");
                    default:
                    return false;
                }
            case NULL:
                switch (t1.getKind())
                {
                    case DECLARED:
                    case ARRAY:
                        return true;
                    default:
                    return false;
                }
            case ARRAY:
                switch (t1.getKind())
                {
                    case NULL:
                        return true;
                    case ARRAY:
                        ArrayType at1 = (ArrayType) t1;
                        ArrayType at2 = (ArrayType) t2;
                        return isAssignable(at1.getComponentType(), at2.getComponentType());
                    default:
                        return false;
                }
            case TYPEVAR:
                TypeVariable tv = (TypeVariable) t2;
                if (isSubtype(t1, tv.getUpperBound()))
                {
                    if (tv.getLowerBound().getKind() != TypeKind.NULL)
                    {
                        return isSubtype(tv.getLowerBound(), t1);
                    }
                    else
                    {
                        return true;
                    }
                }
                else
                {
                    return false;
                }
            default:
                throw new IllegalArgumentException(t2+" not suitable type");
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
    /**
     * The direct superclass is the class from whose implementation the 
     * implementation of the current class is derived.
     * @param t
     * @return 
     */
    @Override
    public List<? extends TypeMirror> directSupertypes(TypeMirror t)
    {
        switch (t.getKind())
        {
            case DECLARED:
                DeclaredType dt = (DeclaredType) t;
                TypeElement te = (TypeElement) dt.asElement();
                List<TypeMirror> list = new ArrayList<>();
                TypeElement superclass = (TypeElement) asElement(te.getSuperclass());
                if (superclass != null)
                {
                    list.add(0, superclass.asType());
                }
                list.addAll(te.getInterfaces());
                return list;
            case EXECUTABLE:
            case PACKAGE:
                throw new IllegalArgumentException(t.getKind().name());
            default:
                throw new UnsupportedOperationException(t.getKind().name()+" not supported yet");
        }
    }

    private Set<? extends TypeMirror> allSupertypes(TypeMirror t)
    {
        switch (t.getKind())
        {
            case DECLARED:
                DeclaredType dt = (DeclaredType) t;
                TypeElement te = (TypeElement) dt.asElement();
                Set<TypeMirror> set = new HashSet<>();
                addInterfaces(set, te);
                TypeElement superclass = (TypeElement) asElement(te.getSuperclass());
                while (superclass != null)
                {
                    set.add(superclass.asType());
                    addInterfaces(set, superclass);
                    superclass = (TypeElement) asElement(superclass.getSuperclass());
                }
                return set;
            case EXECUTABLE:
            case PACKAGE:
                throw new IllegalArgumentException(t.getKind().name());
            default:
                throw new UnsupportedOperationException(t.getKind().name()+" not supported yet");
        }
    }
    private void addInterfaces(Set<TypeMirror> set, TypeElement type)
    {
        for (TypeMirror tm : type.getInterfaces())
        {
            set.add(tm);
            addInterfaces(set, (TypeElement) asElement(tm));
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
                case "java.lang.Byte":
                    return Byte;
                case "java.lang.Boolean":
                    return Boolean;
                case "java.lang.Character":
                    return Char;
                case "java.lang.Short":
                    return Short;
                case "java.lang.Integer":
                    return Int;
                case "java.lang.Long":
                    return Long;
                case "java.lang.Float":
                    return Float;
                case "java.lang.Double":
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
        return new DeclaredTypeImpl(typeElem, Arrays.asList(typeArgs));
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
