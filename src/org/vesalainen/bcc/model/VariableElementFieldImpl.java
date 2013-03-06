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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public class VariableElementFieldImpl extends AbstractSymbol implements VariableElement
{
    private Field field;
    public VariableElementFieldImpl(Field field)
    {
        super(field, field.getModifiers(), field.getName());
        this.field = field;
    }

    @Override
    public TypeMirror asType()
    {
        return TypeMirrorFactory.get(field.getType());
    }

    @Override
    public ElementKind getKind()
    {
        return ElementKind.FIELD;
    }

    @Override
    public Element getEnclosingElement()
    {
        return ElementFactory.get(field.getDeclaringClass());
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
        if (
                Modifier.isFinal(field.getModifiers()) &&
                Modifier.isStatic(field.getModifiers()) 
                )
        {
            try
            {
                return field.get(null);
            }
            catch (IllegalArgumentException | IllegalAccessException ex)
            {
            }
        }
        return null;
    }

}
