/*
 * Copyright (C) 2014 Timo Vesalainen
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

import javax.lang.model.type.TypeMirror;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen
 */
public class TypesImplTest
{
    private final TypesImpl types = new TypesImpl();
    public TypesImplTest()
    {
    }

    /**
     * Test of asElement method, of class TypesImpl.
     */
    @Test
    public void testAsElement()
    {
    }

    /**
     * Test of isSameType method, of class TypesImpl.
     */
    @Test
    public void testIsSameType()
    {
    }

    /**
     * Test of isSubtype method, of class TypesImpl.
     */
    @Test
    public void testIsSubtype()
    {
    }

    /**
     * Test of isAssignable method, of class TypesImpl.
     */
    @Test
    public void testIsAssignable1()
    {
        TypeMirror stringType = Typ.getTypeFor(String.class);
        TypeMirror objectType = Typ.getTypeFor(Object.class);
        assertFalse(types.isAssignable(Typ.CharA, stringType));
        assertTrue(types.isAssignable(Typ.CharA, objectType));
    }

    @Test
    public void testIsAssignable2()
    {
        TypeMirror stringType = Typ.getTypeFor(String.class);
        TypeMirror charSequenceType = Typ.getTypeFor(CharSequence.class);
        assertTrue(types.isAssignable(stringType, charSequenceType));
    }

    /**
     * Test of contains method, of class TypesImpl.
     */
    @Test
    public void testContains()
    {
    }

    /**
     * Test of isSubsignature method, of class TypesImpl.
     */
    @Test
    public void testIsSubsignature()
    {
    }

    /**
     * Test of directSupertypes method, of class TypesImpl.
     */
    @Test
    public void testDirectSupertypes()
    {
    }

    /**
     * Test of erasure method, of class TypesImpl.
     */
    @Test
    public void testErasure()
    {
    }

    /**
     * Test of boxedClass method, of class TypesImpl.
     */
    @Test
    public void testBoxedClass()
    {
    }

    /**
     * Test of unboxedType method, of class TypesImpl.
     */
    @Test
    public void testUnboxedType()
    {
    }

    /**
     * Test of capture method, of class TypesImpl.
     */
    @Test
    public void testCapture()
    {
    }

    /**
     * Test of getPrimitiveType method, of class TypesImpl.
     */
    @Test
    public void testGetPrimitiveType()
    {
    }

    /**
     * Test of getNullType method, of class TypesImpl.
     */
    @Test
    public void testGetNullType()
    {
    }

    /**
     * Test of getNoType method, of class TypesImpl.
     */
    @Test
    public void testGetNoType()
    {
    }

    /**
     * Test of getArrayType method, of class TypesImpl.
     */
    @Test
    public void testGetArrayType()
    {
    }

    /**
     * Test of getWildcardType method, of class TypesImpl.
     */
    @Test
    public void testGetWildcardType()
    {
    }

    /**
     * Test of getDeclaredType method, of class TypesImpl.
     */
    @Test
    public void testGetDeclaredType_TypeElement_TypeMirrorArr()
    {
    }

    /**
     * Test of getDeclaredType method, of class TypesImpl.
     */
    @Test
    public void testGetDeclaredType_3args()
    {
    }

    /**
     * Test of asMemberOf method, of class TypesImpl.
     */
    @Test
    public void testAsMemberOf()
    {
    }
    
}
