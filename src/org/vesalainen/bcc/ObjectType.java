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
package org.vesalainen.bcc;

import java.lang.reflect.Type;
import org.vesalainen.bcc.type.ClassWrapper;
import org.vesalainen.bcc.type.Generics;

/**
 * @deprecated 
 * @author tkv
 */
public enum ObjectType
{
    BOOLEAN("Z", boolean.class, boolean[].class),
    BYTE("B", byte.class, byte[].class),
    CHAR("C", char.class, char[].class),
    SHORT("S", short.class, short[].class),
    INT("I", int.class, int[].class),
    LONG("J", long.class, long[].class),
    FLOAT("F", float.class, float[].class),
    DOUBLE("D", double.class, double[].class),
    REF("L", Object.class, Object[].class),
    VOID("V", void.class, null);
    
    private String id;
    private Class<?> cls;
    private Class<?> related;

    private ObjectType(String id, Class<?> cls, Class<?> related)
    {
        this.id = id;
        this.cls = cls;
        this.related = related;
    }

    public Class<?> getObjectClass()
    {
        return cls;
    }
    /**
     * related class is the component type for arrays and array type for others.
     * int -> int[]
     * int[] -> int
     * @return
     */
    public Class<?> getRelatedClass()
    {
        return related;
    }
    public String id()
    {
        return id;
    }

    public static ObjectType valueOf(Type cls)
    {
        if (Generics.isPrimitive(cls))
        {
            return ObjectType.valueOf(Generics.getName(cls).toUpperCase());
        }
        else
        {
            return REF;
        }
    }
}
