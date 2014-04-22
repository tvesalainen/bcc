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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Timo Vesalainen
 */
public class TypeParameterBuilder
{
    private TypeElement element;
    private List<TypeParameterElement> typeParameters = new ArrayList<>();
    private List<TypeMirror> typeArguments = new ArrayList<>();
    private Map<String,TypeParameterElement> typeParameterMap = new HashMap<>();

    public TypeParameterBuilder(TypeElement element, List<TypeParameterElement> typeParameters)
    {
        this.element = element;
        assert typeParameters != null;
        this.typeParameters = typeParameters;
    }

    public TypeParameterBuilder(TypeElement element, List<? extends TypeParameterElement> typeParameters, List<? extends TypeMirror> typeArguments, Map<String, TypeParameterElement> typeParameterMap)
    {
        this.element = element;
        this.typeParameters.addAll(typeParameters);
        this.typeArguments.addAll(typeArguments);
        this.typeParameterMap.putAll(typeParameterMap);
    }

    public Map<String, TypeParameterElement> getTypeParameterMap()
    {
        return typeParameterMap;
    }

    public List<TypeMirror> getTypeArguments()
    {
        return typeArguments;
    }

    protected TypeMirror resolvType(String type)
    {
        if (type == null || type.isEmpty())
        {
            throw new IllegalArgumentException("null or empty type");
        }
        switch (type)
        {
            case "boolean":
                return Typ.Boolean;
            case "byte":
                return Typ.Byte;
            case "char":
                return Typ.Char;
            case "short":
                return Typ.Short;
            case "int":
                return Typ.Int;
            case "long":
                return Typ.Long;
            case "float":
                return Typ.Float;
            case "double":
                return Typ.Double;
            default:
                if (type.charAt(0) == '[')
                {
                    return Typ.getArrayType(resolvType(type.substring(1)));
                }
                else
                {
                    return resolvElement(type).asType();
                }
        }
    }
    private Element resolvElement(String type)
    {
        TypeParameterElement t = typeParameterMap.get(type);
        if (t != null)
        {
            return t;
        }
        else
        {
            TypeElement te = El.getTypeElement(type);
            if (te == null)
            {
                throw new IllegalArgumentException(type+" is not type parameter name or resolvable type name");
            }
            return te;
        }
    }
    public TypeParameterElement addTypeParameter(String name, Class<?>... bounds)
    {
        TypeElement[] types = new TypeElement[bounds.length];
        for (int ii=0;ii<types.length;ii++)
        {
            types[ii] = El.getTypeElement(bounds[ii].getName());
        }
        return addTypeParameter(name, types);
    }
    public TypeParameterElement addTypeParameter(String name, CharSequence... bounds)
    {
        TypeElement[] types = new TypeElement[bounds.length];
        for (int ii=0;ii<types.length;ii++)
        {
            types[ii] = El.getTypeElement(bounds[ii]);
        }
        return addTypeParameter(name, types);
    }
    public TypeParameterElement addTypeParameter(String name, TypeElement... bounds)
    {
        TypeMirror[] types = new TypeMirror[bounds.length];
        for (int ii=0;ii<types.length;ii++)
        {
            types[ii] = bounds[ii].asType();
        }
        return addTypeParameter(name, types);
    }
    public TypeParameterElement addTypeParameter(String name, TypeMirror... bounds)
    {
        return addTypeParameter(new TypeParameterElementImpl(name, element, bounds));
    }
    public TypeParameterElement addTypeParameter(TypeParameterElement param)
    {
        typeParameters.add(param);
        typeArguments.add(param.asType());
        typeParameterMap.put(param.getSimpleName().toString(), param);
        return param;
    }
}
