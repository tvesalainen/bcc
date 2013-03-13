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

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public class DeclaredTypeBuilder 
{
    private List<TypeMirror> typeArguments = new ArrayList<>();
    private DeclaredType declaredType;
    private TypeParameterBuilder<?> typeParamBuilder;

    DeclaredTypeBuilder(TypeElement element, TypeParameterBuilder<?> typeParamBuilder)
    {
        this.declaredType = new DeclaredTypeImpl(element, typeArguments);
        this.typeParamBuilder = typeParamBuilder;
    }
    
    public DeclaredTypeBuilder addNested(Class<?> element)
    {
        return addNested(element.getCanonicalName());
    }
    public DeclaredTypeBuilder addNested(String element)
    {
        return addNested((TypeElement)typeParamBuilder.resolvElement(element));
    }
    public DeclaredTypeBuilder addNested(TypeElement element)
    {
        DeclaredTypeBuilder builder = new DeclaredTypeBuilder(element, typeParamBuilder);
        addType(builder.getDeclaredType());
        return builder;
    }
    public DeclaredTypeBuilder addType(Class<?> type)
    {
        return addType(type.getCanonicalName());
    }
    public DeclaredTypeBuilder addType(String type)
    {
        return addType(typeParamBuilder.resolvType(type));
    }
    public DeclaredTypeBuilder addType(TypeMirror type)
    {
        typeArguments.add(type);
        return this;
    }
    public DeclaredTypeBuilder addExtends(Class<?> bound)
    {
        return addExtends(bound.getCanonicalName());
    }
    public DeclaredTypeBuilder addExtends(String bound)
    {
        return addExtends(typeParamBuilder.resolvType(bound));
    }
    public DeclaredTypeBuilder addExtends(TypeMirror bound)
    {
        typeArguments.add(new WildcardTypeImpl(bound, null));
        return this;
    }
    public DeclaredTypeBuilder addSuper(Class<?> bound)
    {
        return addSuper(bound.getCanonicalName());
    }
    public DeclaredTypeBuilder addSuper(String bound)
    {
        return addSuper(typeParamBuilder.resolvType(bound));
    }
    public DeclaredTypeBuilder addSuper(TypeMirror bound)
    {
        typeArguments.add(new WildcardTypeImpl(null, bound));
        return this;
    }
    public DeclaredType getDeclaredType()
    {
        return declaredType;
    }
}
