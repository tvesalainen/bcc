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
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
class ExecutableTypeImpl implements ExecutableType 
{
    private List<TypeVariable> typeVariables = new ArrayList<>();
    private TypeMirror returnType;
    private List<TypeMirror> parameterTypes = new ArrayList<>();
    private List<TypeMirror> thrownTypes = new ArrayList<>();
    public ExecutableTypeImpl(Constructor constructor)
    {
        returnType = T.getNullType();
        for (Class<?> type : constructor.getParameterTypes())
        {
            parameterTypes.add(TypeMirrorFactory.getClassType(type));
        }
        for (java.lang.reflect.TypeVariable<?> tv : constructor.getTypeParameters())
        {
            typeVariables.add(TypeMirrorFactory.getTypeVariable(tv));
        }
        for (Class<?> type : constructor.getExceptionTypes())
        {
            thrownTypes.add(TypeMirrorFactory.getClassType(type));
        }
    }

    public ExecutableTypeImpl(Method method)
    {
        returnType = TypeMirrorFactory.get(method.getReturnType());
        for (Class<?> type : method.getParameterTypes())
        {
            parameterTypes.add(TypeMirrorFactory.getClassType(type));
        }
        for (java.lang.reflect.TypeVariable<Method> tv : method.getTypeParameters())
        {
            typeVariables.add(TypeMirrorFactory.getTypeVariable(tv));
        }
        for (Class<?> type : method.getExceptionTypes())
        {
            thrownTypes.add(TypeMirrorFactory.getClassType(type));
        }
    }

    @Override
    public List<? extends TypeVariable> getTypeVariables()
    {
        return typeVariables;
    }

    @Override
    public TypeMirror getReturnType()
    {
        return returnType;
    }

    @Override
    public List<? extends TypeMirror> getParameterTypes()
    {
        return parameterTypes;
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes()
    {
        return thrownTypes;
    }

    @Override
    public TypeKind getKind()
    {
        return TypeKind.EXECUTABLE;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p)
    {
        return v.visitExecutable(this, p);
    }

}
