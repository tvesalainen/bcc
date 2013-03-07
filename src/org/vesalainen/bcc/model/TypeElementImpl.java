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
public class TypeElementImpl extends AbstractParameterizableSymbol implements TypeElement
{
    private TypeMirror type;
    private List<Element> enclosedElements = new ArrayList<>();
    private List<TypeMirror> interfaces = new ArrayList<>();
    private NestingKind nestingKind;
    private Name qualifiedName;
    private TypeMirror superclass;
    private Element enclosingElement;
    private ElementKind kind;

    public TypeElementImpl(Class<?> cls)
    {
        super(cls);
        type = TypeMirrorFactory.get(cls);
        qualifiedName = ElementFactory.Elements.getName(cls.getName());
        superclass = TypeMirrorFactory.get(cls.getSuperclass());
        for (Field field : cls.getDeclaredFields())
        {
            enclosedElements.add(ElementFactory.get(field));
        }
        for (Constructor constructor : cls.getDeclaredConstructors())
        {
            enclosedElements.add(ElementFactory.get(constructor));
        }
        for (Method method : cls.getDeclaredMethods())
        {
            enclosedElements.add(ElementFactory.get(method));
        }
        for (Annotation annotation : cls.getDeclaredAnnotations())
        {
            enclosedElements.add(ElementFactory.get(annotation));
        }
        for (Class<?> c : cls.getDeclaredClasses())
        {
            enclosedElements.add(ElementFactory.get(c));
        }
        if (cls.isAnonymousClass())
        {
            nestingKind = NestingKind.ANONYMOUS;
        }
        else
        {
            if (cls.isLocalClass())
            {
                nestingKind = NestingKind.LOCAL;
            }
            else
            {
                if (cls.isMemberClass())
                {
                    nestingKind = NestingKind.MEMBER;
                }
                else
                {
                    nestingKind = NestingKind.TOP_LEVEL;
                }
            }
        }
        for (Type intf : cls.getGenericInterfaces())
        {
            interfaces.add(TypeMirrorFactory.get(intf));
        }
        Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass != null)
        {
            enclosingElement = ElementFactory.get(enclosingClass);
        }
        else
        {
            enclosingElement = ElementFactory.get(cls.getPackage());
        }
        if (cls.isAnnotation())
        {
            kind = ElementKind.ANNOTATION_TYPE;
        }
        else
        {
            if (cls.isEnum())
            {
                kind = ElementKind.ENUM;
            }
            else
            {
                if (cls.isInterface())
                {
                    kind = ElementKind.INTERFACE;
                }
                else
                {
                    kind = ElementKind.CLASS;
                }
            }
        }
    }
    
    @Override
    public List<? extends Element> getEnclosedElements()
    {
        return enclosedElements;
    }

    @Override
    public NestingKind getNestingKind()
    {
        return nestingKind;
    }

    @Override
    public Name getQualifiedName()
    {
        return qualifiedName;
    }

    @Override
    public TypeMirror getSuperclass()
    {
        return superclass;
    }

    @Override
    public List<? extends TypeMirror> getInterfaces()
    {
        return interfaces;
    }

    @Override
    public Element getEnclosingElement()
    {
        return enclosingElement;
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
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitType(this, p);
    }

}
