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

package org.vesalainen.bcc.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.LogLevel;
import org.apache.tools.ant.types.Path;
import org.vesalainen.bcc.ClassFile;

/**
 * @author Timo Vesalainen
 */
public class RuntimeJar extends Task
{
    private String mainClass;
    private List<File> classPath = new ArrayList<>();
    private File mainJar;
    private File targetJar;
    private Set<String> include;
    private Set<String> exclude = new HashSet<>();

    public RuntimeJar()
    {
        exclude.add("java");
        exclude.add("org/ietf/jgss");
        exclude.add("org/omg");
        exclude.add("org/w3c");
        exclude.add("org/xml");
    }

    public void setMainClass(String mainClass)
    {
        this.mainClass = mainClass.replace(".class", "");
    }

    public void setTargetJar(File targetJar)
    {
        this.targetJar = targetJar;
    }

    public void setMainJar(File mainJar)
    {
        this.mainJar = mainJar;
    }

    public void setClasspath(Path path)
    {
        String[] list = path.list();
        for (int ii=0;ii<list.length;ii++)
        {
            classPath.add(new File(list[ii]));
        }
    }

    public void setInclude(String path)
    {
        include = new HashSet<>();
        for (String e : path.split(","))
        {
            include.add(e.replace('.', '/'));
        }
        log("include="+include, LogLevel.DEBUG.getLevel());
    }
    public void setExclude(String path)
    {
        for (String e : path.split(","))
        {
            exclude.add(e.replace('.', '/'));
        }
        log("exclude="+include, LogLevel.DEBUG.getLevel());
    }
    @Override
    public void execute() throws BuildException
    {
        try
        {
            if (mainJar == null)
            {
                throw new BuildException("mainjar is missing", getLocation());
            }
            if (targetJar == null)
            {
                throw new BuildException("targetjar is missing", getLocation());
            }
            JarFile main = new JarFile(mainJar);
            Manifest manifest = main.getManifest();
            if (mainClass != null)
            {
                manifest.getMainAttributes().put(Name.MAIN_CLASS, mainClass);
            }
            else
            {
                mainClass = manifest.getMainAttributes().getValue(Name.MAIN_CLASS);
            }
            if (mainClass == null)
            {
                throw new BuildException("mainclass is missing", getLocation());
            }
            String cp = manifest.getMainAttributes().getValue(Name.CLASS_PATH);
            File dir = mainJar.getParentFile();
            if (cp != null)
            {
                String[] cpStr = cp.split(" ");
                for (String jf : cpStr)
                {
                    File f = new File(dir, jf);
                    if (!f.exists())
                    {
                        throw new BuildException(f+" not found", getLocation());
                    }
                    if (!classPath.contains(f))
                    {
                        classPath.add(f);
                    }
                }
            }
            manifest.getMainAttributes().remove(Name.CLASS_PATH);
            byte[] buffer = new byte[0x1000000];
            classPath.add(0, mainJar);
            Map<String,byte[]> byteMap = new HashMap<>();
            Map<String,byte[]> jarMap = new TreeMap<>();
            for (File jar : classPath)
            {
                log("reading "+jar, LogLevel.VERBOSE.getLevel());
                try (JarInputStream mainIn = new JarInputStream(new FileInputStream(jar)))
                {
                    JarEntry jarEntry = mainIn.getNextJarEntry();
                    while (jarEntry != null)
                    {
                        if (
                                !jarEntry.isDirectory() && 
                                !isExcluded(jarEntry.getName()) &&
                                !jarEntry.getName().startsWith("META")
                                )
                        {
                            log("include="+jarEntry.getName(), LogLevel.DEBUG.getLevel());
                            int read = 0;
                            while (true)
                            {
                                if (read == buffer.length)
                                {
                                    throw new BuildException("buffer too small", getLocation());
                                }
                                int rc = mainIn.read(buffer, read, buffer.length-read);
                                if (rc == -1)
                                {
                                    break;
                                }
                                read += rc;
                            }
                            byte[] bytes = Arrays.copyOf(buffer, read);
                            if (jarEntry.getName().endsWith(".class"))
                            {
                                byte[] old = byteMap.put(jarEntry.getName(), bytes);
                                if (old != null)
                                {
                                    if (!Arrays.equals(old, bytes))
                                    {
                                        throw new BuildException(jarEntry.getName()+" in "+jar+" is duplicate", getLocation());
                                    }
                                }
                            }
                            else
                            {
                                byte[] old = jarMap.put(jarEntry.getName(), bytes);
                                if (old != null)
                                {
                                    if (!Arrays.equals(old, bytes))
                                    {
                                        throw new BuildException(jarEntry.getName()+" in "+jar+" is duplicate", getLocation());
                                    }
                                }
                            }
                        }
                        else
                        {
                            log("exclude="+jarEntry.getName(), LogLevel.DEBUG.getLevel());
                        }
                        jarEntry = mainIn.getNextJarEntry();
                    }
                }
            }
            Deque<String> unresolved = new ArrayDeque<>();
            unresolved.add(mainClass.replace('.', '/') +".class");
            while (!unresolved.isEmpty())
            {
                String classname = unresolved.removeFirst();
                byte[] bytes = byteMap.get(classname);
                if (bytes == null)
                {
                    throw new BuildException(classname+" not found", getLocation());
                }
                jarMap.put(classname, bytes);
                System.err.println(classname);
                ClassFile cf = new ClassFile(bytes);
                for (String cn : cf.getReferencedClassnames())
                {
                    cn = cn+".class";
                    if (
                        !isExcluded(cn) &&
                        !jarMap.containsKey(cn) &&
                        !unresolved.contains(cn)
                        )
                    {
                        unresolved.add(cn);
                    }
                }
            }
            try (JarOutputStream out = new JarOutputStream(new FileOutputStream(targetJar), manifest))
            {
                log("created "+targetJar, LogLevel.VERBOSE.getLevel());
                for (String entry : jarMap.keySet())
                {
                    JarEntry je = new JarEntry(entry);
                    out.putNextEntry(je);
                    byte[] bytes = jarMap.get(entry);
                    out.write(bytes);
                }
            }
            
        }
        catch (IOException ex)
        {
            throw new BuildException(ex, getLocation());
        }
    }
    
    private boolean isExcluded(String classname)
    {
        if (include != null)
        {
            for (String prefix : include)
            {
                if (classname.startsWith(prefix))
                {
                    return false;
                }
            }
            return true;
        }
        for (String prefix : exclude)
        {
            if (classname.startsWith(prefix))
            {
                return true;
            }
        }
        return false;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            RuntimeJar rtj = new RuntimeJar();
            rtj.setMainJar(new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\HoskiAdmin\\dist\\HoskiAdmin.jar"));
            rtj.setTargetJar(new File("c:\\temp\\target.jar"));
            rtj.execute();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
