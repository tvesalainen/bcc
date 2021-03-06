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
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
public class DeclaredTypeImpl implements DeclaredType
{
    private TypeElement element;
    private TypeMirror enclosingType = Typ.getNoType(TypeKind.NONE);
    private List<TypeMirror> typeArguments;
    DeclaredTypeImpl()
    {
        this.typeArguments = new ArrayList<>();
    }

    DeclaredTypeImpl(TypeElement element, List<TypeMirror> typeArguments)
    {
        this.element = element;
        this.typeArguments = typeArguments;
    }

    DeclaredTypeImpl(Class<?> element, Class<?>... typeArguments)
    {
        this.element = El.getTypeElement(element.getCanonicalName());
        this.typeArguments = new ArrayList<>();
        for (Class<?> a : typeArguments)
        {
            this.typeArguments.add(Typ.getTypeFor(a));
        }
    }

    DeclaredTypeImpl(TypeMirror[] bounds)
    {
        element = new TypeElementImpl(bounds);
        this.typeArguments = new ArrayList<>();
    }
    void init(Class<?> cls)
    {
        this.element = ElementFactory.get(cls);
        Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass == null)
        {
            enclosingType = Typ.getNoType(TypeKind.NONE);
        }
        else
        {
            enclosingType = TypeMirrorFactory.get(enclosingClass);
        }
        for (TypeVariable tv : cls.getTypeParameters())
        {
            typeArguments.add(TypeMirrorFactory.get(tv));
        }
    }
    
    void init(Annotation annotation)
    {
        this.element = ElementFactory.get(annotation);
        enclosingType = Typ.getNoType(TypeKind.NONE);
    }

    void init(java.lang.reflect.ParameterizedType parameterizedType)
    {
        this.element = (TypeElement) ElementFactory.get(parameterizedType.getRawType());
        Type ownerType = parameterizedType.getOwnerType();
        if (ownerType != null)
        {
            enclosingType = TypeMirrorFactory.get(ownerType);
        }
        for (Type t : parameterizedType.getActualTypeArguments())
        {
            typeArguments.add(TypeMirrorFactory.get(t));
        }
    }
    void init(Type[] bounds)
    {
        element = ElementFactory.getIntersectionTypeElement(bounds);
    }
    
    @Override
    public Element asElement()
    {
        return element;
    }

    @Override
    public TypeMirror getEnclosingType()
    {
        return enclosingType;
    }

    @Override
    public List<? extends TypeMirror> getTypeArguments()
    {
        return typeArguments;
    }

    @Override
    public TypeKind getKind()
    {
        return TypeKind.DECLARED;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p)
    {
        return v.visitDeclared(this, p);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final DeclaredTypeImpl other = (DeclaredTypeImpl) obj;
        if (!Objects.equals(this.element, other.element))
        {
            return false;
        }
        if (!Objects.equals(this.enclosingType, other.enclosingType))
        {
            return false;
        }
        if (!Objects.equals(this.typeArguments, other.typeArguments))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        if (element != null)
        {
            return "DeclaredTypeImpl{" + element.getQualifiedName()+ '}';
        }
        else
        {
            return "DeclaredTypeImpl{ element == null }";
        }
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
