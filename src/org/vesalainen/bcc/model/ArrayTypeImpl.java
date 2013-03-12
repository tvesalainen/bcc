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

import java.util.Objects;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
public class ArrayTypeImpl implements ArrayType
{
    private TypeMirror componentType;
    ArrayTypeImpl(Class<?> cls)
    {
        assert cls.isArray();
        this.componentType = TypeMirrorFactory.get(cls.getComponentType());
    }

    ArrayTypeImpl(java.lang.reflect.GenericArrayType gat)
    {
        this.componentType = TypeMirrorFactory.get(gat.getGenericComponentType());
    }

    ArrayTypeImpl(TypeMirror componentType)
    {
        this.componentType = componentType;
    }
    
    @Override
    public TypeMirror getComponentType()
    {
        return componentType;
    }

    @Override
    public TypeKind getKind()
    {
        return TypeKind.ARRAY;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p)
    {
        return v.visitArray(this, p);
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
        final ArrayTypeImpl other = (ArrayTypeImpl) obj;
        if (!Objects.equals(this.componentType, other.componentType))
        {
            return false;
        }
        return true;
    }

}
