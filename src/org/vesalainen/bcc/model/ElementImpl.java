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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public abstract class ElementImpl<T extends TypeMirror> implements Element
{
    protected T type;
    private ElementKind kind;
    private Name name;
    private Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);;
    private Annotation[] annotations;
    private List<AnnotationMirror> annotationMirrors = new ArrayList<>();

    public ElementImpl(ElementKind kind, String name)
    {
        this.kind = kind;
        this.name = E.getName(name);
        annotations = new Annotation[] {};
        annotationMirrors = new ArrayList<>();
        for (Annotation annotation : annotations)
        {
            annotationMirrors.add(ElementFactory.getAnnotationMirror(annotation));
        }
    }
    public ElementImpl(ElementKind kind, Annotation[] annotations, String name)
    {
        this.kind = kind;
        this.name = E.getName(name);
        this.annotations = annotations;
        annotationMirrors = new ArrayList<>();
    }
    public ElementImpl(ElementKind kind, AnnotatedElement element, int modifier, String name)
    {
        this.kind = kind;
        this.name = E.getName(name);
        annotations = element.getAnnotations();
        annotationMirrors = new ArrayList<>();
        for (Annotation annotation : element.getDeclaredAnnotations())
        {
            annotationMirrors.add(ElementFactory.getAnnotationMirror(annotation));
        }
        if (java.lang.reflect.Modifier.isAbstract(modifier))
        {
            modifiers.add(Modifier.ABSTRACT);
        }
        if (java.lang.reflect.Modifier.isFinal(modifier))
        {
            modifiers.add(Modifier.FINAL);
        }
        if (java.lang.reflect.Modifier.isNative(modifier))
        {
            modifiers.add(Modifier.NATIVE);
        }
        if (java.lang.reflect.Modifier.isPrivate(modifier))
        {
            modifiers.add(Modifier.PRIVATE);
        }
        if (java.lang.reflect.Modifier.isProtected(modifier))
        {
            modifiers.add(Modifier.PROTECTED);
        }
        if (java.lang.reflect.Modifier.isPublic(modifier))
        {
            modifiers.add(Modifier.PUBLIC);
        }
        if (java.lang.reflect.Modifier.isStatic(modifier))
        {
            modifiers.add(Modifier.STATIC);
        }
        if (java.lang.reflect.Modifier.isStrict(modifier))
        {
            modifiers.add(Modifier.STRICTFP);
        }
        if (java.lang.reflect.Modifier.isSynchronized(modifier))
        {
            modifiers.add(Modifier.SYNCHRONIZED);
        }
        if (java.lang.reflect.Modifier.isTransient(modifier))
        {
            modifiers.add(Modifier.TRANSIENT);
        }
        if (java.lang.reflect.Modifier.isVolatile(modifier))
        {
            modifiers.add(Modifier.VOLATILE);
        }
    }

    @Override
    public Set<Modifier> getModifiers()
    {
        return modifiers;
    }

    @Override
    public Name getSimpleName()
    {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        for (Annotation a : annotations)
        {
            if (annotationType.isInstance(a))
            {
                return (A) a;
            }
        }
        return null;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        return annotationMirrors;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.name);
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
        @SuppressWarnings("unchecked")
        final ElementImpl<T> other = (ElementImpl<T>) obj;
        if (!Objects.equals(this.type, other.type))
        {
            return false;
        }
        if (this.kind != other.kind)
        {
            return false;
        }
        if (!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if (!Objects.equals(this.modifiers, other.modifiers))
        {
            return false;
        }
        if (!Arrays.deepEquals(this.annotations, other.annotations))
        {
            return false;
        }
        if (!Objects.equals(this.annotationMirrors, other.annotationMirrors))
        {
            return false;
        }
        return true;
    }

    @Override
    public T asType()
    {
        return type;
    }

    @Override
    public ElementKind getKind()
    {
        return kind;
    }

    @Override
    public String toString()
    {
        return "ElementImpl{" + "name=" + name + '}';
    }

}
