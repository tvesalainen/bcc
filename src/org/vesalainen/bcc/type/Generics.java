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
package org.vesalainen.bcc.type;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.vesalainen.bcc.MethodImplementor;
import org.vesalainen.bcc.SubClass;

/**
 * @author Timo Vesalainen
 * @deprecated 
 */
public class Generics
{
    public static boolean isAssignableFrom(Type to, Type from)
    {
        if (to.equals(from))
        {
            return true;
        }
        if ((to instanceof Class) && (from instanceof Class))
        {
            Class<?> clsTo = (Class<?>) to;
            Class<?> clsFrom = (Class<?>) from;
            return clsTo.isAssignableFrom(clsFrom);
        }
        if (Object[].class.equals(to) && !Generics.isPrimitive(Generics.getComponentType(from)))
        {
            return true;
        }
        if (Object.class.equals(to) && !Generics.isPrimitive(from))
        {
            return true;
        }
        if (!isPrimitive(to))
        {
            Type superClass = from;
            while (superClass != null)
            {
                if (to.equals(superClass))
                {
                    return true;
                }
                for (Type intf : getInterfaces(superClass))
                {
                    Type superIntf = intf;
                    while (superIntf != null)
                    {
                        if (to.equals(superIntf))
                        {
                            return true;
                        }
                        superIntf = getSuperclass(superIntf);
                    }
                }
                superClass = getSuperclass(superClass);
            }
        }
        return false;
    }
    /**
     * Internal form is like Ljava/lang.String or Z or [J
     * @param type
     * @return 
     */
    public static String getInternalForm(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> clazz = (Class<?>) type;
            return getInternalForm(clazz);
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getInternalForm();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }
    /**
     * Internal form is like Ljava/lang.String or Z or [J
     * @param clazz
     * @return 
     */
    public static String getInternalForm(Class<?> clazz)
    {
        if (clazz.isArray())
        {
            Class<?> component = clazz.getComponentType();
            if (component.isPrimitive() || component.isArray())
            {
                return '['+getInternalForm(component);
            }
            else
            {
                return "[L"+getInternalForm(component)+';';
            }
        }
        if (boolean.class.equals(clazz)) return "Z";
        if (byte.class.equals(clazz)) return "B";
        if (char.class.equals(clazz)) return "C";
        if (short.class.equals(clazz)) return "S";
        if (int.class.equals(clazz)) return "I";
        if (long.class.equals(clazz)) return "J";
        if (float.class.equals(clazz)) return "F";
        if (double.class.equals(clazz)) return "D";
        if (void.class.equals(clazz)) return "V";
        
        return clazz.getCanonicalName().replace('.', '/');
    }
    public static String getFullyQualifiedForm(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getCanonicalName();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getFullyQualifiedForm();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static Type[] getParameterTypes(Member method)
    {
        if (method instanceof Method)
        {
            Method m = (Method) method;
            return m.getParameterTypes();
        }
        else
        {
            if (method instanceof Constructor)
            {
                Constructor c = (Constructor) method;
                return c.getParameterTypes();
            }
            else
            {
                if (method instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) method;
                    return m.getParameterTypes();
                }
                else
                {
                    throw new IllegalArgumentException(method+" not class type");
                }
            }
        }
    }

    public static Type[] getGenericParameterTypes(Member method)
    {
        if (method instanceof Method)
        {
            Method m = (Method) method;
            return m.getGenericParameterTypes();
        }
        else
        {
            if (method instanceof Constructor)
            {
                Constructor c = (Constructor) method;
                return c.getGenericParameterTypes();
            }
            else
            {
                if (method instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) method;
                    return m.getGenericParameterTypes();
                }
                else
                {
                    throw new IllegalArgumentException(method+" not class type");
                }
            }
        }
    }

    public static Type[] getGenericExceptionTypes(Member method)
    {
        if (method instanceof Method)
        {
            Method m = (Method) method;
            return m.getGenericExceptionTypes();
        }
        else
        {
            if (method instanceof Constructor)
            {
                Constructor c = (Constructor) method;
                return c.getGenericExceptionTypes();
            }
            else
            {
                if (method instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) method;
                    return m.getGenericExceptionTypes();
                }
                else
                {
                    throw new IllegalArgumentException(method+" not class type");
                }
            }
        }
    }

    public static Type getReturnType(Member method)
    {
        if (method instanceof Method)
        {
            Method m = (Method) method;
            return m.getReturnType();
        }
        else
        {
            if (method instanceof Constructor)
            {
                return void.class;
            }
            else
            {
                if (method instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) method;
                    return m.getReturnType();
                }
                else
                {
                    throw new IllegalArgumentException(method+" not method type");
                }
            }
        }
    }

    public static Type getGenericReturnType(Member method)
    {
        if (method instanceof Method)
        {
            Method m = (Method) method;
            return m.getGenericReturnType();
        }
        else
        {
            if (method instanceof Constructor)
            {
                return void.class;
            }
            else
            {
                if (method instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) method;
                    return m.getGenericReturnType();
                }
                else
                {
                    throw new IllegalArgumentException(method+" not method type");
                }
            }
        }
    }

    public static boolean isCategory2(Type type)
    {
        if (type == null)
        {
            return false;
        }
        return type.equals(long.class) || type.equals(double.class);
    }

    public static boolean isPrimitive(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.isPrimitive();
        }
        else
        {
            return false;
        }
    }
    public static Type getPrimitiveType(Type type)
    {
        if (isPrimitive(type))
        {
            return type;
        }
        else
        {
            if (type instanceof Class<?>)
            {
                Class<?> cls = (Class<?>) type;
                if (Boolean.class.equals(cls))
                {
                    return boolean.class;
                }
                if (Byte.class.equals(cls))
                {
                    return byte.class;
                }
                if (Character.class.equals(cls))
                {
                    return char.class;
                }
                if (Short.class.equals(cls))
                {
                    return short.class;
                }
                if (Integer.class.equals(cls))
                {
                    return int.class;
                }
                if (Long.class.equals(cls))
                {
                    return long.class;
                }
                if (Float.class.equals(cls))
                {
                    return float.class;
                }
                if (Double.class.equals(cls))
                {
                    return double.class;
                }
            }
        }
        throw new IllegalArgumentException(type+" not primitive type or wrapper");
    }

    public static String getName(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getName();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getName();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }
    
    public static String getName(GenericDeclaration type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getName();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getName();
            }
            else
            {
                throw new IllegalArgumentException(type+" not GenericDeclaration type");
            }
        }
    }
    
    public static Type getType(Member field)
    {
        if (field instanceof Field)
        {
            Field f = (Field) field;
            return f.getType();
        }
        else
        {
            if (field instanceof FieldWrapper)
            {
                FieldWrapper f = (FieldWrapper) field;
                return f.getType();
            }
            else
            {
                throw new IllegalArgumentException(field+" not class type");
            }
        }
    }

    public static Constructor getConstructor(Type type, Type... parameters) throws NoSuchMethodException
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            Class<?>[] params = new Class<?>[parameters.length];
            for (int ii=0;ii<params.length;ii++)
            {
                params[ii] = (Class<?>) parameters[ii];
            }
            return cls.getConstructor(params);
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                throw new NoSuchMethodException("Not yet implemented");
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static boolean isVoid(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return void.class.equals(cls);
        }
        else
        {
            return false;
        }
    }

    public static boolean isInterfaceMethod(Member method)
    {
        if (method instanceof Method)
        {
            Method m = (Method) method;
            return m.getDeclaringClass().isInterface();
        }
        else
        {
            if (method instanceof Constructor)
            {
                return false;
            }
            else
            {
                if (method instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) method;
                    return Generics.isInterface(m.getDeclaringType());
                }
                else
                {
                    throw new IllegalArgumentException(method+" not class type");
                }
            }
        }
    }

    public static Member getDeclaredMethod(Type superClass, String name, Type[] parameters) throws NoSuchMethodException
    {
        if (superClass instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) superClass;
            for (Method method : cls.getDeclaredMethods())
            {
                if (name.equals(method.getName()) && isAssignableFrom(method.getParameterTypes(), parameters))
                {
                    return method;
                }
            }
            throw new NoSuchMethodException(name);
        }
        else
        {
            throw new IllegalArgumentException(superClass+" not class type");
        }
    }

    private static boolean isAssignableFrom(Type[] assignee, Type[] assignable)
    {
        if (assignee.length == assignable.length)
        {
            for (int ii=0;ii<assignee.length;ii++)
            {
                if (!isAssignableFrom(assignee[ii], assignable[ii]))
                {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    public static Type getSuperclass(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getSuperclass();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getSuperclass();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }
    /**
     * return true if type is one of char, boolean, byte, short or int.
     * @param type
     * @return 
     */
    public static boolean isInteger(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return (
                    char.class.equals(cls) ||
                    boolean.class.equals(cls) ||
                    byte.class.equals(cls) ||
                    short.class.equals(cls) ||
                    int.class.equals(cls)
                    );
        }
        else
        {
            return false;
        }
    }

    public static Type getComponentType(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getComponentType();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getComponentType();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static boolean isArray(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.isArray();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.isArray();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static boolean isReference(Type type)
    {
        return !isPrimitive(type) && !isVoid(type);
    }

    public static Member getMethod(Type type, String str, Type... parameters) throws NoSuchMethodException
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            Class<?>[] params = new Class<?>[parameters.length];
            for (int ii=0;ii<params.length;ii++)
            {
                params[ii] = (Class<?>) parameters[ii];
            }
            return cls.getMethod(str, params);
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                throw new NoSuchMethodException("Not yet implemented");
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static int getModifiers(Member member)
    {
        if (member instanceof Method)
        {
            Method m = (Method) member;
            return m.getModifiers();
        }
        else
        {
            if (member instanceof Constructor)
            {
                Constructor c = (Constructor) member;
                return c.getModifiers();
            }
            else
            {
                if (member instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) member;
                    return m.getModifiers();
                }
                else
                {
                    throw new IllegalArgumentException(member+" not class type");
                }
            }
        }
    }

    public static int getModifiers(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getModifiers();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getModifiers();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }
    public static Member[] getMethods(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getMethods();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                throw new UnsupportedOperationException("Not yet implemented");
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static Constructor<?>[] getDeclaredConstructors(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getDeclaredConstructors();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                throw new UnsupportedOperationException("Not yet implemented");
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static String getSourceName(Type type)
    {
        String intForm = getInternalForm(type);
        int idx = intForm.lastIndexOf('/');
        if (idx != -1)
        {
            intForm = intForm.substring(idx+1);
        }
        return intForm+".jasm";
    }
    /**
     * Return class file.
     * @param type
     * @param filer
     * @return 
     */
    public static JavaFileObject createClassFile(Type type, Filer filer) throws IOException
    {
        return filer.createClassFile(getFullyQualifiedForm(type));
    }

    public static FileObject createSourceFile(Type type, Filer filer) throws IOException
    {
        return filer.createSourceFile(getFullyQualifiedForm(type));
    }

    /**
     * Creates a File object for Class
     * @param type Class
     * @param filer source directory
     * @param suffix File suffix.
     * @return 
     */
    public static FileObject createFileForClass(Type type, Filer filer, String suffix) throws IOException
    {
        return filer.createResource(StandardLocation.SOURCE_OUTPUT, getPackage(type), Generics.getSimpleName(type)+suffix);
    }

    public static String getPackage(Type type)
    {
        String fqForm = getFullyQualifiedForm(type);
        String pkg = "";
        int idx = fqForm.lastIndexOf('.');
        if (idx != -1)
        {
            pkg = fqForm.substring(0, idx);
        }
        return pkg;
    }
    public static ClassLoader getClassLoader(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getClassLoader();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getClass().getClassLoader();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }
    private static Type[] getInterfaces(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getInterfaces();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getInterfaces();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static Annotation[][] getParameterAnnotations(Member member)
    {
        if (member instanceof Method)
        {
            Method m = (Method) member;
            return m.getParameterAnnotations();
        }
        else
        {
            if (member instanceof Constructor)
            {
                Constructor c = (Constructor) member;
                return c.getParameterAnnotations();
            }
            else
            {
                if (member instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) member;
                    return m.getParameterAnnotations();
                }
                else
                {
                    throw new IllegalArgumentException(member+" not class type");
                }
            }
        }
    }

    public static MethodImplementor getImplementor(Member method)
    {
        if (method instanceof MethodWrapper)
        {
            MethodWrapper m = (MethodWrapper) method;
            return m.getImplementor();
        }
        else
        {
            throw new IllegalArgumentException("cannot get implementor for "+method);
        }
    }

    public static void implement(Member method, SubClass subClass) throws IOException
    {
        if (method instanceof MethodWrapper)
        {
            MethodWrapper m = (MethodWrapper) method;
            m.implement(subClass);
        }
        else
        {
            throw new IllegalArgumentException("cannot implement "+method);
        }
    }

    public static boolean needsImplementation(Member method)
    {
        if (method instanceof MethodWrapper)
        {
            MethodWrapper m = (MethodWrapper) method;
            return m.needsImplementation();
        }
        else
        {
            return false;
        }
    }

    public static Type getDeclaringClass(Member member)
    {
        if (member instanceof Method)
        {
            Method m = (Method) member;
            return m.getDeclaringClass();
        }
        else
        {
            if (member instanceof Constructor)
            {
                Constructor c = (Constructor) member;
                return c.getDeclaringClass();
            }
            else
            {
                if (member instanceof Field)
                {
                    Field f = (Field) member;
                    return f.getDeclaringClass();
                }
                else
                {
                    if (member instanceof MethodWrapper)
                    {
                        MethodWrapper m = (MethodWrapper) member;
                        return m.getDeclaringType();
                    }
                    else
                    {
                        throw new IllegalArgumentException(member+" not class type");
                    }
                }
            }
        }
    }

    public static boolean isInterface(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.isInterface();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.isInterface();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }

    public static boolean isConstructor(Object member)
    {
        return (member instanceof Constructor) || (member instanceof ConstructorWrapper);
    }

    public static String getInternalForm(String name)
    {
        return name.replace('.', '/');
    }

    public static String getName(Member member)
    {
        if (member instanceof Method)
        {
            Method m = (Method) member;
            return m.getName();
        }
        else
        {
            if (member instanceof Constructor)
            {
                Constructor c = (Constructor) member;
                return "<init>";
            }
            else
            {
                if (member instanceof Field)
                {
                    Field f = (Field) member;
                    return f.getName();
                }
                else
                {
                    if (member instanceof MethodWrapper)
                    {
                        MethodWrapper m = (MethodWrapper) member;
                        return m.getName();
                    }
                    else
                    {
                        throw new IllegalArgumentException(member+" not class type");
                    }
                }
            }
        }
    }

    public static boolean isConstantClass(Class<?> cls)
    {
        return
            cls.isPrimitive() ||
            String.class.equals(cls) ||
            Boolean.class.equals(cls) ||
            Byte.class.equals(cls) ||
            Character.class.equals(cls) ||
            Short.class.equals(cls) ||
            Integer.class.equals(cls) ||
            Long.class.equals(cls) ||
            Float.class.equals(cls) ||
            Double.class.equals(cls);
    }
    public static TypeVariable<?>[] getTypeParameters(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> m = (Class<?>) type;
            return m.getTypeParameters();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper m = (ClassWrapper) type;
                return m.getTypeParameters();
            }
            else
            {
                throw new IllegalArgumentException(type+" not GenericDeclaration type");
            }
        }
    }

    public static TypeVariable<?>[] getTypeParameters(Member member)
    {
        if (member instanceof Method)
        {
            Method m = (Method) member;
            return m.getTypeParameters();
        }
        else
        {
            if (member instanceof Constructor)
            {
                Constructor c = (Constructor) member;
                return c.getTypeParameters();
            }
            else
            {
                if (member instanceof MethodWrapper)
                {
                    MethodWrapper m = (MethodWrapper) member;
                    return m.getTypeParameters();
                }
                else
                {
                    throw new IllegalArgumentException(member+" not GenericDeclaration type");
                }
            }
        }
    }

    public static String getSimpleName(Type type)
    {
        if (type instanceof Class<?>)
        {
            Class<?> cls = (Class<?>) type;
            return cls.getSimpleName();
        }
        else
        {
            if (type instanceof ClassWrapper)
            {
                ClassWrapper cls = (ClassWrapper) type;
                return cls.getSimpleName();
            }
            else
            {
                throw new IllegalArgumentException(type+" not class type");
            }
        }
    }
}
