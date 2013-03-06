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
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
public class TypeVariableImpl implements TypeVariable
{
    private java.lang.reflect.TypeVariable typeVariable;

    public TypeVariableImpl(java.lang.reflect.TypeVariable typeVariable)
    {
        this.typeVariable = typeVariable;
    }
    
    @Override
    public Element asElement()
    {
        return ElementFactory.get(typeVariable.getGenericDeclaration());
    }

    @Override
    public TypeMirror getUpperBound()
    {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0)
        {
            return TypeMirrorFactory.getClassType(Object.class);
        }
        else
        {
            return TypeMirrorFactory.getIntersectionType(bounds);
        }
    }

    @Override
    public TypeMirror getLowerBound()
    {
        return TypeMirrorFactory.Null;
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

}
