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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Timo Vesalainen
 */
@AssertSignature("<T::Ljava/util/Set<Ljava/lang/Integer;>;:Ljava/io/Serializable;U::Ljava/util/Map$Entry<Ljava/lang/Boolean;Ljava/lang/String;>;>Ljava/lang/Object;")
public class T4<T extends Set<Integer> & Serializable,U extends Map.Entry<Boolean,String>>
{
    @AssertSignature("<U:Ljava/lang/Long;>Ljava/lang/Object;Ljava/lang/Cloneable;")
    public class Inner<U extends Long> implements Cloneable
    {
        public U f;
        public void m(U u)
        {
            
        }
    }
}
