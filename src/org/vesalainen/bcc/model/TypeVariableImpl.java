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

import java.lang.reflect.Type;
import java.util.Objects;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
public class TypeVariableImpl implements TypeVariable
{
    private TypeParameterElement element;
    private TypeMirror upperBound;

    TypeVariableImpl(TypeParameterElement element, TypeMirror... bounds)
    {
        this.element = element;
        switch (bounds.length)
        {
            case 0:
                upperBound = TypeMirrorFactory.getClassType(Object.class);
                break;
            case 1:
                upperBound = bounds[0];
                break;
            default:
                upperBound = new DeclaredTypeImpl(bounds);
        }
    }
    
    <D extends java.lang.reflect.GenericDeclaration> TypeVariableImpl(java.lang.reflect.TypeVariable<D> typeVariable)
    {
        element = ElementFactory.getTypeParameterElement(typeVariable);
        Type[] bounds = typeVariable.getBounds();
        switch (bounds.length)
        {
            case 0:
                upperBound = TypeMirrorFactory.getClassType(Object.class);
                break;
            case 1:
                upperBound = TypeMirrorFactory.get(bounds[0]);
                break;
            default:
                upperBound = TypeMirrorFactory.getIntersectionType(bounds);
        }
    }
    
    @Override
    public Element asElement()
    {
        return element;
    }

    @Override
    public TypeMirror getUpperBound()
    {
        return upperBound;
    }

    @Override
    public TypeMirror getLowerBound()
    {
        return T.getNullType();
    }

    @Override
    public TypeKind getKind()
    {
        return TypeKind.TYPEVAR;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p)
    {
        return v.visitTypeVariable(this, p);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
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
        final TypeVariableImpl other = (TypeVariableImpl) obj;
        if (!Objects.equals(this.element, other.element))
        {
            return false;
        }
        if (!Objects.equals(this.upperBound, other.upperBound))
        {
            return false;
        }
        return true;
    }

}
