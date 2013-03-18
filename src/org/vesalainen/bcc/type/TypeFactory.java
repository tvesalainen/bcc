/*
 * Copyright (C) 2012 Timo Vesalainen
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
package org.vesalainen.bcc.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * @deprecated 
 * @author Timo Vesalainen
 */
public class TypeFactory
{
    /**
     * Creates a type variable. E.g List<T extends Number> = createTypeVariable(List.class, "T", Number.class)
     * @param <D>
     * @param genericDeclaration
     * @param name
     * @param bounds
     * @return 
     */
    public static <D extends GenericDeclaration> TypeVariable<D> createTypeVariable(D genericDeclaration, String name, Type... bounds)
    {
        return new TypeVariableImpl<>(genericDeclaration, name, bounds);
    }
    /**
     * Creates parameterized type. E.g List<Integer> = createParameterizedType(List.class, null, Integer.class)
     * @param rawType
     * @param ownerType
     * @param actualTypeArguments
     * @return 
     */
    public static ParameterizedType createParameterizedType(GenericDeclaration rawType, GenericDeclaration ownerType, Type... actualTypeArguments)
    {
        return new ParameterizedTypeImpl(rawType, ownerType, actualTypeArguments);
    }
    
    public static GenericArrayType createArrayType(Type type)
    {
        return new GenericArrayTypeImpl(type);
    }
    
    public static WildcardType createUpperBounds(Type... types)
    {
        return new WildcardTypeImpl(types, new Type[]{});
    }
    public static WildcardType createLowerBounds(Type... types)
    {
        return new WildcardTypeImpl(new Type[]{Object.class}, types);
    }
    public static WildcardType createBounds(Type[] upperBounds, Type[] lowerBounds)
    {
        return new WildcardTypeImpl(upperBounds, lowerBounds);
    }
    private static class GenericArrayTypeImpl implements GenericArrayType
    {
        private Type genericComponentType;

        public GenericArrayTypeImpl(Type genericComponentType)
        {
            this.genericComponentType = genericComponentType;
        }

        public Type getGenericComponentType()
        {
            return genericComponentType;
        }
        
    }
    private static class TypeVariableImpl<D extends GenericDeclaration> implements TypeVariable<D>
    {
        private D genericDeclaration;
        private String name;
        private Type[] bounds;

        public TypeVariableImpl(D genericDeclaration, String name, Type[] bounds)
        {
            this.genericDeclaration = genericDeclaration;
            this.name = name;
            this.bounds = bounds;
        }

        public Type[] getBounds()
        {
            return bounds;
        }

        public D getGenericDeclaration()
        {
            return genericDeclaration;
        }

        public String getName()
        {
            return name;
        }
        
    }
    private static class ParameterizedTypeImpl implements ParameterizedType
    {
        private GenericDeclaration rawType;
        private GenericDeclaration ownerType;
        private Type[] actualTypeArguments;

        public ParameterizedTypeImpl(GenericDeclaration rawType, GenericDeclaration ownerType, Type... actualTypeArguments)
        {
            if (rawType.getTypeParameters().length != actualTypeArguments.length)
            {
                throw new IllegalArgumentException("TypeParameters count != actualTypeArguments count");
            }
            this.rawType = rawType;
            this.ownerType = ownerType;
            this.actualTypeArguments = actualTypeArguments;
        }

        public Type[] getActualTypeArguments()
        {
            return actualTypeArguments;
        }

        public Type getOwnerType()
        {
            return (Type) ownerType;
        }

        public Type getRawType()
        {
            return (Type) rawType;
        }

    }
    private static class WildcardTypeImpl implements WildcardType
    {
        private Type[] upperBounds;
        private Type[] lowerBounds;

        public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds)
        {
            this.upperBounds = upperBounds;
            this.lowerBounds = lowerBounds;
        }

        public Type[] getLowerBounds()
        {
            return lowerBounds;
        }

        public Type[] getUpperBounds()
        {
            return upperBounds;
        }

        
    }
}
