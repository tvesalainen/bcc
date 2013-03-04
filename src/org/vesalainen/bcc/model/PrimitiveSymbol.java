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

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

/**
 * @author Timo Vesalainen
 */
public class PrimitiveSymbol implements PrimitiveType
{
    private Class<?> cls;

    public PrimitiveSymbol(Class<?> cls)
    {
        assert cls.isPrimitive();
        this.cls = cls;
    }
    
    @Override
    public TypeKind getKind()
    {
        return TypeKind.valueOf(cls.getSimpleName().toUpperCase());
    }

    @Override
    public <R, P> R accept(TypeVisitor<R, P> v, P p)
    {
        return v.visitPrimitive(this, p);
    }

}
