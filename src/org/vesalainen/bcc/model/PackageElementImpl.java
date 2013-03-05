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

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public class PackageElementImpl extends AbstractSymbol implements PackageElement
{
    private Package pkg;
    public PackageElementImpl(Package pkg)
    {
        super(pkg, 0, pkg.getName());
    }

    @Override
    public TypeMirror asType()
    {
        return new NoTypeImpl(TypeKind.NONE);
    }

    @Override
    public ElementKind getKind()
    {
        return ElementKind.PACKAGE;
    }

    @Override
    public Element getEnclosingElement()
    {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends Element> getEnclosedElements()
    {
        return Collections.EMPTY_LIST;  // no support in reflection
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitPackage(this, p);
    }

    @Override
    public Name getQualifiedName()
    {
        return new NameImpl(pkg.getName());
    }

    @Override
    public boolean isUnnamed()
    {
        return pkg.getName().isEmpty();
    }

}
