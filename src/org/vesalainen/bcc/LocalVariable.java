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

package org.vesalainen.bcc;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.E;

/**
 * @author Timo Vesalainen
 */
public class LocalVariable implements VariableElement 
{
    private ExecutableElement enclosingElement;
    private TypeMirror type;
    private ElementKind kind;
    private Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    private Name simpleName;

    public LocalVariable(ExecutableElement enclosingElement, TypeMirror type)
    {
        this(enclosingElement, type, ElementKind.LOCAL_VARIABLE, "");
    }

    public LocalVariable(ExecutableElement enclosingElement, TypeMirror type, CharSequence simpleName)
    {
        this(enclosingElement, type, ElementKind.LOCAL_VARIABLE, simpleName);
    }

    protected LocalVariable(ExecutableElement enclosingElement, TypeMirror type, ElementKind kind, CharSequence simpleName)
    {
        this.enclosingElement = enclosingElement;
        this.type = type;
        this.kind = kind;
        this.simpleName = E.getName(simpleName);
    }

    public void setSimpleName(CharSequence simpleName)
    {
        this.simpleName = E.getName(simpleName);
    }

    @Override
    public Object getConstantValue()
    {
        return null;
    }

    @Override
    public TypeMirror asType()
    {
        return type;
    }

    @Override
    public ElementKind getKind()
    {
        return kind;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public Element getEnclosingElement()
    {
        return enclosingElement;
    }

    @Override
    @SuppressWarnings(value = "unchecked")
    public List<? extends Element> getEnclosedElements()
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitVariable(this, p);
    }

}
