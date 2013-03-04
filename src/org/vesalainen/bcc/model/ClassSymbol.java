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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public class ClassSymbol extends AbstractParameterizableSymbol implements TypeElement
{
    private Class<?> type;
    private List<Element> enclosedElements;
    private List<TypeMirror> interfaces;

    public ClassSymbol(Class<?> type)
    {
        super(type);
        this.type = type;
    }
    
    @Override
    public List<? extends Element> getEnclosedElements()
    {
        if (enclosedElements == null)
        {
            enclosedElements = new ArrayList<>();
            for (Field field : type.getDeclaredFields())
            {
                enclosedElements.add(ElementFactory.get(field));
            }
            for (Constructor constructor : type.getDeclaredConstructors())
            {
                enclosedElements.add(ElementFactory.get(constructor));
            }
            for (Method method : type.getDeclaredMethods())
            {
                enclosedElements.add(ElementFactory.get(method));
            }
            for (Annotation annotation : type.getDeclaredAnnotations())
            {
                enclosedElements.add(ElementFactory.get(annotation));
            }
            for (Class<?> cls : type.getDeclaredClasses())
            {
                enclosedElements.add(ElementFactory.get(cls));
            }
        }
        return enclosedElements;
    }

    @Override
    public NestingKind getNestingKind()
    {
        if (type.isAnonymousClass())
        {
            return NestingKind.ANONYMOUS;
        }
        else
        {
            if (type.isLocalClass())
            {
                return NestingKind.LOCAL;
            }
            else
            {
                if (type.isMemberClass())
                {
                    return NestingKind.MEMBER;
                }
                else
                {
                    return NestingKind.TOP_LEVEL;
                }
            }
        }
    }

    @Override
    public Name getQualifiedName()
    {
        return new NameImpl(type.getName());
    }

    @Override
    public TypeMirror getSuperclass()
    {
        return TypeFactory.get(type.getGenericSuperclass());
    }

    @Override
    public List<? extends TypeMirror> getInterfaces()
    {
        if (interfaces == null)
        {
            interfaces = new ArrayList<>();
            for (Type intf : type.getGenericInterfaces())
            {
                interfaces.add(TypeFactory.get(intf));
            }
        }
        return interfaces;
    }

    @Override
    public Element getEnclosingElement()
    {
        Class<?> enclosingClass = type.getEnclosingClass();
        if (enclosingClass != null)
        {
            return ElementFactory.get(enclosingClass);
        }
        else
        {
            return ElementFactory.get(type.getPackage());
        }
    }

    @Override
    public TypeMirror asType()
    {
        return TypeFactory.get(type);
    }

    @Override
    public ElementKind getKind()
    {
        if (type.isAnnotation())
        {
            return ElementKind.ANNOTATION_TYPE;
        }
        else
        {
            if (type.isEnum())
            {
                return ElementKind.ENUM;
            }
            else
            {
                if (type.isInterface())
                {
                    return ElementKind.INTERFACE;
                }
                else
                {
                    return ElementKind.CLASS;
                }
            }
        }
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitType(this, p);
    }

}
