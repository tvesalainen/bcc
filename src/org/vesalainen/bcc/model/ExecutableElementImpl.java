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
import java.lang.reflect.Method;
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
import org.vesalainen.annotation.dump.Dump;

/**
 * @author Timo Vesalainen
 */
class ExecutableElementImpl extends AbstractParameterizableSymbol implements ExecutableElement 
{
    private TypeMirror type;
    private ElementKind kind;
    private Element enclosingElement;
    private TypeMirror returnType;
    private List<VariableElement> parameters = new ArrayList<>();
    private boolean varArgs;
    private List<TypeMirror> thrownTypes = new ArrayList<>();
    private AnnotationValue defaultValue;
    
    public ExecutableElementImpl(Constructor constructor)
    {
        super(constructor);
        type = TypeMirrorFactory.get(constructor);
        kind = ElementKind.CONSTRUCTOR;
        enclosingElement = ElementFactory.get(constructor.getDeclaringClass());
        returnType = TypeMirrorFactory.Types.getNoType(TypeKind.VOID);
        Type[] genericParameterTypes = constructor.getParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        int index = 0;
        for (Type param : genericParameterTypes)
        {
            parameters.add(ElementFactory.getVariableElement(param, parameterAnnotations[index++]));
        }
        varArgs = constructor.isVarArgs();
        for (Type type : constructor.getGenericExceptionTypes())
        {
            thrownTypes.add(TypeMirrorFactory.get(type));
        }
    }

    public ExecutableElementImpl(Method method)
    {
        super(method);
        type = TypeMirrorFactory.get(method);
        kind = ElementKind.METHOD;
        enclosingElement = ElementFactory.get(method.getDeclaringClass());
        returnType = TypeMirrorFactory.get(method.getReturnType());
        Type[] genericParameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        int index = 0;
        for (Type param : genericParameterTypes)
        {
            parameters.add(ElementFactory.getVariableElement(param, parameterAnnotations[index++]));
        }
        varArgs = method.isVarArgs();
        for (Type type : method.getGenericExceptionTypes())
        {
            thrownTypes.add(TypeMirrorFactory.get(type));
        }
        defaultValue = new AnnotationValueImpl(method.getDefaultValue());
    }

    @Override
    public TypeMirror asType()
    {
        return type;
    }

    @Override
    public ElementKind getKind()
    {
        return kind;
    }

    @Override
    public Element getEnclosingElement()
    {
        return enclosingElement;
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
        return returnType;
    }

    @Override
    public List<? extends VariableElement> getParameters()
    {
        return parameters;
    }

    @Override
    public boolean isVarArgs()
    {
        return varArgs;
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes()
    {
        return thrownTypes;
    }

    @Override
    public AnnotationValue getDefaultValue()
    {
        return defaultValue;
    }

}
