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
import java.lang.reflect.Type;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.WildcardType;

/**
 * @author Timo Vesalainen
 */
class WildcardTypeImpl implements WildcardType 
{
    private TypeMirror extendsBound;
    private TypeMirror superBound;

    WildcardTypeImpl(TypeMirror extendsBound, TypeMirror superBound)
    {
        if (extendsBound != null && superBound != null)
        {
            throw new IllegalArgumentException("extends & super at the same time");
        }
        this.extendsBound = extendsBound;
        this.superBound = superBound;
    }
    
    WildcardTypeImpl(java.lang.reflect.WildcardType rwt)
    {
        Type[] upperBounds = rwt.getUpperBounds();
        switch (upperBounds.length)
        {
            case 0:
                break;
            case 1:
                if (Object.class != upperBounds[0])
                {
                    extendsBound = TypeMirrorFactory.get(upperBounds[0]);
                }
                break;
            default:
                throw new UnsupportedOperationException(rwt+" more than 1 upperbounds not supported");
        }
        Type[] lowerBounds = rwt.getLowerBounds();
        switch (lowerBounds.length)
        {
            case 0:
                break;
            case 1:
                superBound = TypeMirrorFactory.get(lowerBounds[0]);
                break;
            default:
                throw new UnsupportedOperationException(rwt+" more than 1 lowerbounds not supported");
        }
        assert !(extendsBound != null && superBound != null);
    }

    @Override
    public TypeMirror getExtendsBound()
    {
        return extendsBound;
    }

    @Override
    public TypeMirror getSuperBound()
    {
        return superBound;
    }

    @Override
    public TypeKind getKind()
    {
        return TypeKind.WILDCARD;
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p)
    {
        return v.visitWildcard(this, p);
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
