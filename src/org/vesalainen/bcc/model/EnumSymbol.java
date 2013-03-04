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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
class EnumSymbol implements VariableElement
{
    private Enum en;
    public EnumSymbol(Enum en)
    {
        this.en = en;
    }

    @Override
    public TypeMirror asType()
    {
        return TypeFactory.get(en);
    }

    @Override
    public ElementKind getKind()
    {
        return ElementKind.ENUM_CONSTANT;
    }

    @Override
    public Element getEnclosingElement()
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
        return en;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<Modifier> getModifiers()
    {
        return Collections.EMPTY_SET;
    }

    @Override
    public Name getSimpleName()
    {
        return new NameImpl(en.name());
    }

}
