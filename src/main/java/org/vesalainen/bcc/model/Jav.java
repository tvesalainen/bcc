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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Timo Vesalainen
 */
public class Jav 
{
    private final Map<String,String> map = new HashMap<>();
    
    public String makeJavaIdentifier(String id)
    {
        if (id.isEmpty())
        {
            throw new IllegalArgumentException("cannot convert empty string to java identifier");
        }
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        int index = 0;
        while (!Character.isJavaIdentifierStart(id.charAt(index)))
        {
            index++;
        }
        sb.append(Character.toLowerCase(id.charAt(index)));
        for (int ii=index+1;ii<id.length();ii++)
        {
            if (Character.isJavaIdentifierPart(id.charAt(ii)))
            {
                if (upper)
                {
                    sb.append(Character.toUpperCase(id.charAt(ii)));
                    upper = false;
                }
                else
                {
                    sb.append(id.charAt(ii));
                }
            }
            else
            {
                upper = true;
            }
        }
        if (sb.length() == 0)
        {
            throw new IllegalArgumentException("couldn't convert '"+id+"' to java identifier");
        }
        String jid = sb.toString();
        String old = map.put(jid, id);
        if (old != null)
        {
            throw new IllegalArgumentException("both "+old+" and "+id+" creates same java id "+jid);
        }
        return jid;
    }

    public String makeJavaClassname(String id)
    {
        if (id.isEmpty())
        {
            throw new IllegalArgumentException("cannot convert empty string to java identifier");
        }
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        int index = 0;
        while (!Character.isJavaIdentifierStart(id.charAt(index)))
        {
            index++;
        }
        sb.append(Character.toUpperCase(id.charAt(index)));
        for (int ii=index+1;ii<id.length();ii++)
        {
            if (Character.isJavaIdentifierPart(id.charAt(ii)))
            {
                if (upper)
                {
                    sb.append(Character.toUpperCase(id.charAt(ii)));
                    upper = false;
                }
                else
                {
                    sb.append(id.charAt(ii));
                }
            }
            else
            {
                upper = true;
            }
        }
        if (sb.length() == 0)
        {
            throw new IllegalArgumentException("couldn't convert '"+id+"' to java identifier");
        }
        return sb.toString();
    }


}
