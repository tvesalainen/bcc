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
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
public class PrimitiveTypeImpl implements PrimitiveType
{
    private TypeKind kind;
    PrimitiveTypeImpl(Class<?> cls)
    {
        this(TypeKind.valueOf(cls.getSimpleName().toUpperCase()));
    }

    PrimitiveTypeImpl(TypeKind kind)
    {
        this.kind = kind;
    }
    
    @Override
    public TypeKind getKind()
    {
        return kind;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p)
    {
        return v.visitPrimitive(this, p);
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 29 * hash + (this.kind != null ? this.kind.hashCode() : 0);
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
        final PrimitiveTypeImpl other = (PrimitiveTypeImpl) obj;
        if (this.kind != other.kind)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PrimitiveTypeImpl{" + "kind=" + kind + '}';
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
