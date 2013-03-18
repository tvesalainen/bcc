/*
 * Copyright (C) 2012 Timo Vesalainen
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
package org.vesalainen.bcc.type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * @author tkv
 * @deprecated 
 */
public class ConstructorWrapper extends MethodWrapper
{

    public ConstructorWrapper(Constructor method)
    {
        super(
            method.getModifiers(),
            method.getTypeParameters(),
            method.getDeclaringClass(),
            method.getName(),
            false,
            method.getAnnotations(),
            method.getParameterAnnotations(),
            void.class,
            method.getGenericParameterTypes()
            );
        exists = true;
    }
    public ConstructorWrapper(Type declaringClass)
    {
        super(Modifier.PUBLIC, declaringClass, "<init>", void.class);
    }
}
