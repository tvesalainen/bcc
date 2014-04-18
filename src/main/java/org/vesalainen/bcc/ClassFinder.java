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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * @author tkv
 */
public class ClassFinder
{
    private File[] classPath;

    public ClassFinder(File... classPath)
    {
        this.classPath = classPath;
    }

    public ClassFile find(Class<?> clazz) throws IOException
    {
        return find(clazz.getName());
    }

    public ClassFile find(String name) throws IOException
    {
        name = name.replace('.', '/')+".class";
        for (File entry : classPath)
        {
            ClassFile is = findIn(entry, name);
            if (is != null)
            {
                return is;
            }
        }
        throw new IllegalArgumentException(name+" not found");
    }

    private ClassFile findIn(File file, String name) throws IOException
    {
        if (file.isDirectory())
        {
            File ff = new File(file, name);
            if (ff.exists())
            {
                return new ClassFile(new FileInputStream(ff));
            }
            else
            {
                return null;
            }
        }
        else
        {
            JarFile jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry(name);
            if (entry != null)
            {
                return new ClassFile(jar.getInputStream(entry));
            }
            else
            {
                return null;
            }
        }
    }
}
