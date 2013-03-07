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

import java.lang.reflect.Constructor;
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
    private List<TypeParameterElement> typeParameters = new ArrayList<>();

    public AbstractParameterizableSymbol(Class<?> type)
    {
        super(type, type.getModifiers(), type.getSimpleName());
        for (TypeVariable param : type.getTypeParameters())
        {
            typeParameters.add(ElementFactory.get(type, param));
        }
    }

    public AbstractParameterizableSymbol(Constructor constructor)
    {
        super(constructor, constructor.getModifiers(), "<init>");
        for (TypeVariable param : constructor.getTypeParameters())
        {
            typeParameters.add(ElementFactory.get(constructor, param));
        }
    }

    public AbstractParameterizableSymbol(Method method)
    {
        super(method, method.getModifiers(), method.getName());
        for (TypeVariable param : method.getTypeParameters())
        {
            typeParameters.add(ElementFactory.get(method, param));
        }
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters()
    {
        return typeParameters;
    }

}
