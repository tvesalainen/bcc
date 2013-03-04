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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.TypeParameterElement;

/**
 * @author Timo Vesalainen
 */
public abstract class AbstractParameterizableSymbol extends AbstractSymbol implements Parameterizable
{
    private GenericDeclaration genericDeclaration;
    protected List<TypeParameterElement> typeParameters;

    public AbstractParameterizableSymbol(Class<?> type)
    {
        super(type, type.getModifiers(), type.getSimpleName());
        this.genericDeclaration = type;
    }

    public AbstractParameterizableSymbol(Constructor constructor)
    {
        super(constructor, constructor.getModifiers(), "<init>");
        this.genericDeclaration = constructor;
    }

    public AbstractParameterizableSymbol(Method method)
    {
        super(method, method.getModifiers(), method.getName());
        this.genericDeclaration = method;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters()
    {
        if (typeParameters == null)
        {
            typeParameters = new ArrayList<>();
            for (TypeVariable param : genericDeclaration.getTypeParameters())
            {
                typeParameters.add(ElementFactory.get(genericDeclaration, param));
            }
        }
        return typeParameters;
    }

}
