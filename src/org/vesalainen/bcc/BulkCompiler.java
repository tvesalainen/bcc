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

/**
 * @author Timo Vesalainen
 */
public class BulkCompiler
{
    private static File classes;
    private static File src;

    public static void reset()
    {
        classes = null;
        src = null;
    }
    public static File getClasses()
    {
        return classes;
    }

    public static void setClasses(File classes)
    {
        BulkCompiler.classes = classes;
    }

    public static File getSrc()
    {
        return src;
    }

    public static void setSrc(File src)
    {
        BulkCompiler.src = src;
    }
    
    public static void compile(ClassCompiler compiler) throws IOException, ReflectiveOperationException
    {
        compiler.setClassDir(classes);
        compiler.setSrcDir(src);
        compiler.compile();
        if (classes == null)
        {
            System.err.println("warning! classes directory not set");
        }
        else
        {
            compiler.saveClass();
        }
    }
}
