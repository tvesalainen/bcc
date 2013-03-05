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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
class ExecutableElementConstructorImpl extends AbstractParameterizableSymbol implements ExecutableElement 
{
    private Constructor constructor;
    private List<VariableElement> parameters;
    private List<TypeMirror> thrownTypes;
    public ExecutableElementConstructorImpl(Constructor constructor)
    {
        super(constructor);
        this.constructor = constructor;
    }

    @Override
    public TypeMirror asType()
    {
        return new NoTypeImpl(TypeKind.VOID);
    }

    @Override
    public ElementKind getKind()
    {
        return ElementKind.CONSTRUCTOR;
    }

    @Override
    public Element getEnclosingElement()
    {
        return ElementFactory.get(constructor.getDeclaringClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends Element> getEnclosedElements()
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitExecutable(this, p);
    }

    @Override
    public TypeMirror getReturnType()
    {
        return new NoTypeImpl(TypeKind.VOID);
    }

    @Override
    public List<? extends VariableElement> getParameters()
    {
        if (parameters == null)
        {
            parameters = new ArrayList<>();
            Type[] genericParameterTypes = constructor.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            int index = 0;
            for (Type param : genericParameterTypes)
            {
                parameters.add(ElementFactory.getVariableElement(param, parameterAnnotations[index++]));
            }
        }
        return parameters;
    }

    @Override
    public boolean isVarArgs()
    {
        return constructor.isVarArgs();
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes()
    {
        if (thrownTypes == null)
        {
            thrownTypes = new ArrayList<>();
            for (Type type : constructor.getGenericExceptionTypes())
            {
                thrownTypes.add(TypeMirrorFactory.get(type));
            }
        }
        return thrownTypes;
    }

    @Override
    public AnnotationValue getDefaultValue()
    {
        return null;
    }

}
