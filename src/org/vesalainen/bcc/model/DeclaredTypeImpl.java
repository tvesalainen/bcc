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

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
public class DeclaredTypeImpl implements DeclaredType
{
    private Element element;
    private TypeMirror enclosingType;
    private List<TypeMirror> typeArguments = new ArrayList<>();
    public DeclaredTypeImpl(Class<?> cls)
    {
        this.element = ElementFactory.get(cls);
        Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass == null)
        {
            enclosingType = TypeMirrorFactory.Types.getNoType(TypeKind.NONE);
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

}
