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

package org.vesalainen.bcc.annotation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.AttributeInfo;
import org.vesalainen.bcc.model.E;
import org.vesalainen.bcc.model.T;

/**
 * @author Timo Vesalainen
 */
public class ModelUtil 
{
    public static void setModifiers(Set<Modifier> modifiers, int modifier)
    {
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
    public static List<? extends AnnotationMirror> getAnnotationMirrors(Collection<AttributeInfo> attributes)
    {
        List<AnnotationMirror> annotationMirrors = new ArrayList<>();
        for (AttributeInfo at : attributes)
        {
            if (at instanceof RuntimeVisibleAnnotations)
            {
                RuntimeVisibleAnnotations rva = (RuntimeVisibleAnnotations) at;
                for (AnnotationWrapper aw : rva.getAnnotations())
                {
                    annotationMirrors.add(aw);
                }
            }
        }
        return annotationMirrors;
    }

    public static <A extends Annotation> A getAnnotation(Collection<AttributeInfo> attributes, Class<A> annotationType)
    {
        TypeElement typeElement = E.getTypeElement(annotationType.getCanonicalName());
        TypeMirror type = typeElement.asType();
        for (AnnotationMirror am : getAnnotationMirrors(attributes))
        {
            if (T.isSameType(type, am.getAnnotationType()))
            {
                AnnotationWrapper aw = (AnnotationWrapper) am;
                return aw.getAnnotation(annotationType);
            }
        }
        return null;
    }

}
