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
import java.util.Objects;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public class TypeElementImpl extends ElementImpl<DeclaredType> implements TypeElement
{
    private List<Element> enclosedElements = new ArrayList<>();
    private List<TypeMirror> interfaces = new ArrayList<>();
    private NestingKind nestingKind;
    private Name qualifiedName;
    private TypeMirror superclass;
    private Element enclosingElement;
    private List<TypeParameterElement> typeParameters = new ArrayList<>();

    TypeElementImpl()
    {
        super(ElementKind.CLASS, "");
    }

    TypeElementImpl(Class<?> cls)
    {
        super(detectKind(cls), cls, cls.getModifiers(), cls.getSimpleName());
    }
    void init(Class<?> cls)
    {
        type = (DeclaredType) TypeMirrorFactory.getClassType(cls);
        qualifiedName = E.getName(cls.getName());
        if (cls.getSuperclass() != null)
        {
            superclass = TypeMirrorFactory.get(cls.getGenericSuperclass());
        }
        else
        {
            superclass = T.getNoType(TypeKind.NONE);
        }
        for (TypeVariable param : cls.getTypeParameters())
        {
            typeParameters.add(ElementFactory.getTypeParameterElement(param));
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
        Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass != null)
        {
            enclosingElement = ElementFactory.get(enclosingClass);
        }
        else
        {
            enclosingElement = ElementFactory.getPackageElement(cls.getPackage());
        }
        if (cls.isAnnotation())
        {
            
        }
        else
        {
            if (cls.isEnum())
            {
                
            }
            else
            {
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
                for (Class<?> c : cls.getDeclaredClasses())
                {
                    enclosedElements.add(ElementFactory.get(c));
                }
                for (Type intf : cls.getGenericInterfaces())
                {
                    interfaces.add(TypeMirrorFactory.get(intf));
                }
            }
        }
    }

    void init(Type[] bounds)
    {
        assert bounds.length > 0;
        superclass = TypeMirrorFactory.get(bounds[0]);
        for (int ii=1;ii<bounds.length;ii++)
        {
            interfaces.add(TypeMirrorFactory.get(bounds[ii]));
        }
    }

    private static ElementKind detectKind(Class<?> cls)
    {
        if (cls.isAnnotation())
        {
            return ElementKind.ANNOTATION_TYPE;
        }
        else
        {
            if (cls.isEnum())
            {
                return ElementKind.ENUM;
            }
            else
            {
                if (cls.isInterface())
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
    public List<? extends TypeParameterElement> getTypeParameters()
    {
        return typeParameters;
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
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitType(this, p);
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
        final TypeElementImpl other = (TypeElementImpl) obj;
        if (!Objects.equals(this.enclosedElements, other.enclosedElements))
        {
            return false;
        }
        if (!Objects.equals(this.interfaces, other.interfaces))
        {
            return false;
        }
        if (this.nestingKind != other.nestingKind)
        {
            return false;
        }
        if (!Objects.equals(this.qualifiedName, other.qualifiedName))
        {
            return false;
        }
        if (!Objects.equals(this.superclass, other.superclass))
        {
            return false;
        }
        if (!Objects.equals(this.enclosingElement, other.enclosingElement))
        {
            return false;
        }
        if (!Objects.equals(this.typeParameters, other.typeParameters))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TypeElementImpl{" + "qualifiedName=" + qualifiedName + '}';
    }

}
