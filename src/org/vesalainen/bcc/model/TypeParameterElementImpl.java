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
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
class TypeParameterElementImpl implements TypeParameterElement 
{
    private GenericDeclaration genericDeclaration;
    private TypeVariable typeVariable;
    private List<TypeMirror> bounds;
    public TypeParameterElementImpl(GenericDeclaration genericDeclaration, TypeVariable typeVariable)
    {
        this.genericDeclaration = genericDeclaration;
        this.typeVariable = typeVariable;
    }

    @Override
    public TypeMirror asType()
    {
        return TypeMirrorFactory.get(typeVariable);
    }

    @Override
    public ElementKind getKind()
    {
        return ElementKind.TYPE_PARAMETER;
    }

    @Override
    public Element getEnclosingElement()
    {
        return ElementFactory.get(genericDeclaration);
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
        return v.visitTypeParameter(this, p);
    }

    @Override
    public Element getGenericElement()
    {
        return ElementFactory.get(typeVariable.getGenericDeclaration());
    }

    @Override
    public List<? extends TypeMirror> getBounds()
    {
        if (bounds == null)
        {
            bounds = new ArrayList<>();
            for (Type b : typeVariable.getBounds())
            {
                bounds.add(TypeMirrorFactory.get(b));
            }
        }
        return bounds;
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
        return new NameImpl(typeVariable.getName());
    }

}
