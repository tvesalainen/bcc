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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

/**
 * @author Timo Vesalainen
 */
public class AnnotationMirrorImpl implements AnnotationMirror
{
    private Map<ExecutableElement,AnnotationValue> elementValues = new HashMap<>();
    private DeclaredType declaredType;
    public AnnotationMirrorImpl(Annotation annotation)
    {
        for (Method method : annotation.annotationType().getDeclaredMethods())
        {
            elementValues.put(ElementFactory.get(method), new AnnotationValueImpl(method.getDefaultValue()));
        }
        declaredType = TypeMirrorFactory.get(annotation);
    }
    
    @Override
    public DeclaredType getAnnotationType()
    {
        return declaredType;
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues()
    {
        return elementValues;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
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
        final AnnotationMirrorImpl other = (AnnotationMirrorImpl) obj;
        if (!Objects.equals(this.elementValues, other.elementValues))
        {
            return false;
        }
        if (!Objects.equals(this.declaredType, other.declaredType))
        {
            return false;
        }
        return true;
    }

}
