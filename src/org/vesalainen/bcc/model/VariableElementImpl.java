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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public class VariableElementImpl extends ElementImpl<TypeMirror> implements VariableElement
{
    private Element enclosingElement;
    private Object constantValue;

    public static class FieldBuilder
    {
        private VariableElementImpl var;

        FieldBuilder(Element enclosingElement, TypeMirror type, String name)
        {
            var = new VariableElementImpl(enclosingElement, type, name);
        }
        public FieldBuilder setConstantValue(Object constantValue)
        {
            var.constantValue = constantValue;
            return this;
        }
        public FieldBuilder addModifier(javax.lang.model.element.Modifier modifier)
        {
            var.modifiers.add(modifier);
            return this;
        }
        public VariableElement getVariableElement()
        {
            return var;
        }
    }
    private VariableElementImpl(Element enclosingElement, TypeMirror type, String name)
    {
        super(ElementKind.FIELD, name);
        this.type = type;
        this.enclosingElement = enclosingElement;
    }
    
    VariableElementImpl(Field field)
    {
        super(ElementKind.FIELD, field, field.getModifiers(), field.getName());
        type = TypeMirrorFactory.get(field.getGenericType());
        enclosingElement = ElementFactory.get(field.getDeclaringClass());
        if (
                Modifier.isFinal(field.getModifiers()) &&
                Modifier.isStatic(field.getModifiers()) 
                )
        {
            try
            {
                constantValue = field.get(null);
            }
            catch (IllegalArgumentException | IllegalAccessException ex)
            {
            }
        }
    }

    VariableElementImpl(Enum en)
    {
        super(ElementKind.ENUM_CONSTANT, en.name());
        type = TypeMirrorFactory.get(en);
        enclosingElement = ElementFactory.get(en.getDeclaringClass());
        constantValue = en;
    }

    VariableElementImpl(Type param, Annotation[] annotation)
    {
        super(ElementKind.PARAMETER, annotation, "");
        type = TypeMirrorFactory.get(param);
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
        return v.visitVariable(this, p);
    }

    @Override
    public Object getConstantValue()
    {
        return constantValue;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
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
        final VariableElementImpl other = (VariableElementImpl) obj;
        if (!Objects.equals(this.type, other.type))
        {
            return false;
        }
        if (!Objects.equals(this.enclosingElement, other.enclosingElement))
        {
            return false;
        }
        if (!Objects.equals(this.constantValue, other.constantValue))
        {
            return false;
        }
        return true;
    }

}
