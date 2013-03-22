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

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author tkv
 */
public class Dependencies
{
    private ClassFinder finder;
    private String include;

    public Dependencies(String include, File... classPath)
    {
        this.include = include.replace('.', '/');
        finder = new ClassFinder(classPath);
    }

    public SortedMap<String,Boolean> dependenciesFor(Class<?>... classes) throws IOException
    {
        String[] sa = new String[classes.length];
        int index = 0;
        for (Class<?> cls : classes)
        {
            sa[index++] = cls.getName().replace('.', '/');
        }
        return dependenciesFor(sa);
    }
    public SortedMap<String,Boolean> dependenciesFor(String... classNames) throws IOException
    {
        SortedMap<String,Boolean> map = new TreeMap<String,Boolean>();
        Deque<String> deque = new ArrayDeque<String>();
        for (String cn : classNames)
        {
            cn = cn.trim();
            if (!cn.isEmpty())
            {
                cn = cn.replace('.', '/');
                deque.add(cn);
            }
        }
        while (!deque.isEmpty())
        {
            String classname = deque.removeFirst();
            ClassFile cf = finder.find(classname);
            map.put(classname, cf.isSynthetic());
            for (String rn : cf.getReferencedClassnames())
            {
                if (rn.startsWith(include))
                {
                    if (!map.containsKey(rn))
                    {
                        deque.add(rn);
                    }
                }
            }
        }
        return map;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            File p1 = new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\RegexParser\\dist\\RegexParser.jar");
            File p2 = new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\JavaLPG\\dist\\JavaLPG.jar");
            File p3 = new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\ByteCodeCompiler\\dist\\ByteCodeCompiler.jar");
            Dependencies d = new Dependencies("org.vesalainen", p1, p2, p3);
            Map<String,Boolean> s = d.dependenciesFor("org.vesalainen.regex.Regex");
            System.err.println(s);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
