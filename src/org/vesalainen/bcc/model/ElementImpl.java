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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.annotation.ModelUtil;

/**
 * @author Timo Vesalainen
 */
public abstract class ElementImpl<T extends TypeMirror> implements Element, UpdateableElement
{
    protected T type;
    protected Element enclosingElement;
    protected ElementKind kind;
    protected Name simpleName;
    protected Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    protected Annotation[] annotations;
    protected List<AnnotationMirror> annotationMirrors = new ArrayList<>();

    ElementImpl(ElementKind kind, String name)
    {
        this.kind = kind;
        this.simpleName = El.getName(name);
        annotations = new Annotation[] {};
    }
    ElementImpl(ElementKind kind, Annotation[] annotations, String name)
    {
        this.kind = kind;
        this.simpleName = El.getName(name);
        this.annotations = annotations;
    }
    ElementImpl(ElementKind kind, AnnotatedElement element, int modifier, String name)
    {
        this.kind = kind;
        this.simpleName = El.getName(name);
        annotations = element.getAnnotations();
        for (Annotation annotation : element.getDeclaredAnnotations())
        {
            annotationMirrors.add(ElementFactory.getAnnotationMirror(annotation));
        }
        ModelUtil.setModifiers(modifiers, modifier);
    }

    @Override
    public Element getEnclosingElement()
    {
        return enclosingElement;
    }

    @Override
    public void setEnclosingElement(Element enclosingElement)
    {
        this.enclosingElement = enclosingElement;
    }

    @Override
    public void setModifiers(Set<Modifier> modifiers)
    {
        this.modifiers = modifiers;
    }

    @Override
    public void setSimpleName(Name simpleName)
    {
        this.simpleName = simpleName;
    }

    @Override
    public Set<Modifier> getModifiers()
    {
        return modifiers;
    }

    @Override
    public Name getSimpleName()
    {
        return simpleName;
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
        hash = 83 * hash + Objects.hashCode(this.simpleName);
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
        if (!Objects.equals(this.simpleName, other.simpleName))
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
        return "ElementImpl{" + "name=" + simpleName + '}';
    }

}
