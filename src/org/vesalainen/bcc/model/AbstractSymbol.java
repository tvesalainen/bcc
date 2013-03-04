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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 * @author Timo Vesalainen
 */
public abstract class AbstractSymbol implements Element
{
    protected AnnotatedElement element;
    private int modifier;
    private Name name;
    
    protected Set<Modifier> modifiers;
    protected List<AnnotationMirror> annotationMirrors;

    public AbstractSymbol(AnnotatedElement element, int modifier, String name)
    {
        this.element = element;
        this.modifier = modifier;
        this.name = new NameImpl(name);
    }

    @Override
    public Set<Modifier> getModifiers()
    {
        if (modifiers == null)
        {
            modifiers = EnumSet.noneOf(Modifier.class);
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
        return modifiers;
    }

    @Override
    public Name getSimpleName()
    {
        return name;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        return element.getAnnotation(annotationType);
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        if (annotationMirrors == null)
        {
            annotationMirrors = new ArrayList<>();
            for (Annotation annotation : element.getDeclaredAnnotations())
            {
                annotationMirrors.add(ElementFactory.getAnnotationMirror(annotation));
            }
        }
        return annotationMirrors;
    }

}
