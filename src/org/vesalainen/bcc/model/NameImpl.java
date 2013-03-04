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

import javax.lang.model.element.Name;

/**
 * @author Timo Vesalainen
 */
public class NameImpl implements Name
{
    private String name;

    public NameImpl(String name)
    {
        this.name = name;
    }

    @Override
    public int length()
    {
        return name.length();
    }

    @Override
    public char charAt(int index)
    {
        return name.charAt(index);
    }

    @Override
    public boolean equals(Object anObject)
    {
        return name.equals(anObject);
    }

    @Override
    public boolean contentEquals(CharSequence cs)
    {
        return name.contentEquals(cs);
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    @Override
    public CharSequence subSequence(int beginIndex, int endIndex)
    {
        return name.subSequence(beginIndex, endIndex);
    }

    @Override
    public String toString()
    {
        return name.toString();
    }
    
}
