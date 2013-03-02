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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ClassWrapper wraps Class except when class doesn't exist.
 * @author tkv
 */
public class ClassWrapper implements Serializable, AnnotatedElement, GenericDeclaration, Type
{
    private static AtomicInteger anonymousId = new AtomicInteger();
    
    // The static initializers and class variable initializers are executed in textual order.
    protected static Map<Class<?>,ClassWrapper> classMap = new HashMap<>();
    protected static Map<String,ClassWrapper> fqnMap = new HashMap<>();

    protected Class<?> clazz;
    protected String fqnClassname;
    protected Type superClass;
    protected Type[] interfaces;
    protected TypeVariable<?>[] typeParameters;
    protected ParameterizedType[] parameterizedTypes;
    protected Type genericSuperClass;
    protected Type[] genericInterfaces;
    private List<TypeVariable> typeParameterList;
    private GenericDeclaration declaringClass;
    private boolean isInterface;
    private int modifiers;
    
    protected ClassWrapper()
    {
    }
    /**
     * @param cls 
     */
    private ClassWrapper(Class<?> cls)
    {
        clazz = cls;
        superClass = cls.getSuperclass();
        interfaces = cls.getInterfaces();
        typeParameters = cls.getTypeParameters();
        genericSuperClass = cls.getGenericSuperclass();
        genericInterfaces = cls.getGenericInterfaces();
        declaringClass = cls.getDeclaringClass();
        fqnClassname = cls.getCanonicalName();
        isInterface = cls.isInterface();
        modifiers = cls.getModifiers();
    }
    private ClassWrapper(String fqnClassname, TypeVariable[] typeParameters, Type superClass, Type... interfaces)
    {
        assert fqnClassname != null || fqnClassname.indexOf('/') == -1;
        assert (superClass instanceof Class<?>) || (superClass instanceof ParameterizedType) || (superClass instanceof ClassWrapper);
        if (isInterface(superClass))
        {
            throw new IllegalArgumentException(superClass+" is interface");
        }
        this.fqnClassname = fqnClassname;
        this.typeParameters = typeParameters;
        if (superClass instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) superClass;
            this.superClass = pt.getRawType();
            this.genericSuperClass = pt;
        }
        else
        {
            this.superClass = superClass;
            this.genericSuperClass = superClass;
        }
        this.interfaces = new Type[interfaces.length];
        this.genericInterfaces = new Type[interfaces.length];
        int index = 0;
        for (Type t : interfaces)
        {
            if (t instanceof ParameterizedType)
            {
                ParameterizedType pt = (ParameterizedType) t;
                this.interfaces[index] = pt;
                this.genericInterfaces[index] = t;
            }
            else
            {
                this.interfaces[index] = t;
                this.genericInterfaces[index] = t;
            }
            index++;
        }
        modifiers = Modifier.PUBLIC;
    }
    /**
     * Wraps a not existing class. Such class is typically a class being generated.
     * Do not use this method for existing classes.
     * @param fullyQualifiedForm
     * @return 
     */
    public static ClassWrapper wrap(String fullyQualifiedForm, Type superClass, Type... interfaces)
    {
        ClassWrapper cw = fqnMap.get(fullyQualifiedForm);
        if (cw == null)
        {
            if (fullyQualifiedForm.endsWith("[]"))
            {
                throw new UnsupportedOperationException(fullyQualifiedForm+" not supported");
            }
            else
            {
                cw = new ClassWrapper(fullyQualifiedForm, Generics.getTypeParameters(superClass), superClass, interfaces);
            }
            fqnMap.put(fullyQualifiedForm, cw);
        }
        return cw;
    }

    public static ClassWrapper wrap(String fullyQualifiedForm, TypeVariable[] typeParameters, Type superClass, Type... interfaces)
    {
        ClassWrapper cw = fqnMap.get(fullyQualifiedForm);
        if (cw == null)
        {
            if (fullyQualifiedForm.endsWith("[]"))
            {
                throw new UnsupportedOperationException(fullyQualifiedForm+" not supported");
            }
            else
            {
                cw = new ClassWrapper(fullyQualifiedForm, typeParameters, superClass, interfaces);
            }
            fqnMap.put(fullyQualifiedForm, cw);
        }
        return cw;
    }
    /**
     * @param cls
     * @return 
     */
    private static ClassWrapper wrapInternal(Class<?> cls)
    {
        ClassWrapper cw = classMap.get(cls);
        if (cw == null)
        {
            cw = new ClassWrapper(cls);
            classMap.put(cls, cw);
            fqnMap.put(cw.getFullyQualifiedForm(), cw);
        }
        return cw;
    }
    /**
     * Return a ClassWrapper overriding superClass. ClassWrapper name is unique
     * @param superClass
     * @return 
     */
    public static ClassWrapper anonymousOverride(Class<?> superClass)
    {
        return ClassWrapper.fromFullyQualifiedForm(superClass.getCanonicalName()+"Impl"+anonymousId.incrementAndGet(), superClass);
    }
    /**
     * Adds a type variable. E.g MyClass<T extends Number> use addTypeVariable("T", Number.class);
     */
    public void addTypeVariable(String name, Type... bounds)
    {
        typeParameters = null;
        if (typeParameterList == null)
        {
            typeParameterList = new ArrayList<>();
        }
        typeParameterList.add(TypeFactory.createTypeVariable(this, name, bounds));
    }
    
    public ParameterizedType createParameterizedType(Type... actualTypeArguments)
    {
        return TypeFactory.createParameterizedType(this, getDeclaringClass(), actualTypeArguments);
    }
    
    public Type[] getInterfaces()
    {
        return interfaces;
    }

    public static ClassWrapper fromInternalForm(String internalForm)
    {
        return ClassWrapper.wrap(getFullyQualifiedForm(internalForm), Object.class);
    }
    public static ClassWrapper fromFullyQualifiedForm(String fullyQualifiedForm)
    {
        return ClassWrapper.wrap(fullyQualifiedForm, Object.class);
    }
    public static ClassWrapper fromInternalForm(String internalForm, Type superClass, Type... interfaces)
    {
        return ClassWrapper.wrap(getFullyQualifiedForm(internalForm), superClass, interfaces);
    }
    public static ClassWrapper fromFullyQualifiedForm(String fullyQualifiedForm, Type superClass, Type... interfaces)
    {
        return ClassWrapper.wrap(fullyQualifiedForm, superClass, interfaces);
    }
    public static Type getType(String internalForm)
    {
        if (
                //internalForm.length() == 1 || 
                internalForm.lastIndexOf('[') == internalForm.length() - 2
                )
        {
            switch (internalForm)
            {
                case "Z":
                    return boolean.class;
                case "B":
                    return byte.class;
                case "C":
                    return char.class;
                case "S":
                    return short.class;
                case "I":
                    return int.class;
                case "J":
                    return long.class;
                case "F":
                    return float.class;
                case "D":
                    return double.class;
                case "V":
                    return void.class;
            }
        }
        if (internalForm.startsWith("["))
        {
            try
            {
                return Class.forName(internalForm.replace('/', '.'));
            }
            catch (ClassNotFoundException ex)
            {
            }
        }
        String fullyQualifiedForm = getFullyQualifiedForm(internalForm);
        // If class is generated class it will be in fqnMap even if it exists
        ClassWrapper cw = fqnMap.get(fullyQualifiedForm);
        if (cw != null)
        {
            return cw;
        }
        else
        {
            try
            {
                return Class.forName(fullyQualifiedForm);
            }
            catch (ClassNotFoundException ex)
            {
                return wrap(fullyQualifiedForm, Object.class);  // we cannot determinate superclass
            }
        }
    }
    public static String getFullyQualifiedForm(String internalForm)
    {
        if (internalForm.length() == 1)
        {
            char cc = internalForm.charAt(0);
            switch (cc)
            {
                case 'Z':
                    return "boolean";
                case 'B':
                    return "byte";
                case 'C':
                    return "char";
                case 'S':
                    return "short";
                case 'I':
                    return "int";
                case 'J':
                    return "long";
                case 'F':
                    return "float";
                case 'D':
                    return "double";
                case 'V':
                    return "void";
                default:
                    return internalForm;
            }
        }
        else
        {
            if (internalForm.startsWith("L"))
            {
                internalForm = internalForm.substring(1, internalForm.length()-1);
            }
            if (internalForm.startsWith("["))
            {
                return getFullyQualifiedForm(internalForm.substring(1))+"[]";
            }
            return internalForm.replace('/', '.');  //.replace('$', '.');
        }
    }
    public static ClassWrapper[] getParameters(Constructor constructor)
    {
        return getParameters(constructor.getParameterTypes());
    }
    public static ClassWrapper[] getParameters(Method method)
    {
        return getParameters(method.getParameterTypes());
    }
    public static ClassWrapper getReturnType(Method method)
    {
        return ClassWrapper.wrapInternal(method.getReturnType());
    }
    public static ClassWrapper[] getParameters(Class<?>... params)
    {
        ClassWrapper[] ar = new ClassWrapper[params.length];
        for (int ii=0;ii<ar.length;ii++)
        {
            ar[ii] = ClassWrapper.wrapInternal(params[ii]);
        }
        return ar;
    }

    ClassWrapper getComponentType()
    {
        if (isArray())
        {
            return ClassWrapper.wrapInternal(clazz.getComponentType());
        }
        else
        {
            return null;
        }
    }
    /**
     * Returns fully qualified form Eg. java.lang.String, int, java.lang.String[],
     * boolean[][]
     * @return
     */
    public String getFullyQualifiedForm()
    {
        return fqnClassname;
    }
    /**
     * Returns internal form Eg. java/lang/String, I, [Ljava/lang/String;, [[Z
     * @return
     */
    String getInternalForm()
    {
        if (clazz == null)
        {
            return fqnClassname.replace('.', '/');
        }
        throw new UnsupportedOperationException("not supported for other than wrapped not existing class");
    }

    @Override
    public String toString()
    {
        if (clazz != null)
        {
            return clazz.toString();
        }
        return fqnClassname;
    }

    boolean isArray()
    {
        if (clazz == null)
        {
            return false;
        }
        return clazz.isArray();
    }

    public String getName()
    {
        if (clazz == null)
        {
            return fqnClassname;
        }
        return clazz.getName();
    }

    public boolean wraps(Class<?> cls)
    {
        if (clazz == null)
        {
            return false;
        }
        return clazz.equals(cls);
    }
    public static String makeClassname(String packageName, String candidate)
    {
        return packageName+'.'+MethodWrapper.makeJavaIdentifier(candidate.substring(0, 1).toUpperCase()+candidate.substring(1));
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Annotation[] getAnnotations()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Annotation[] getDeclaredAnnotations()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TypeVariable<?>[] getTypeParameters()
    {
        if (typeParameters == null)
        {
            typeParameters = (TypeVariable<?>[]) typeParameterList.toArray(new TypeVariable<?>[typeParameterList.size()]);
            typeParameterList = null;
        }
        return typeParameters;
    }

    public Type getGenericSuperclass()
    {
        return genericSuperClass;
    }

    public Type[] getGenericInterfaces()
    {
        return genericInterfaces;
    }
    /**
     * Return package name in fully qualified form form. E.g java.lang.reflect
     * @return 
     */
    String getPackageName()
    {
        int idx = fqnClassname.lastIndexOf('.');
        if (idx != -1)
        {
            return fqnClassname.substring(0, idx);
        }
        else
        {
            return fqnClassname;
        }
    }

    private GenericDeclaration getDeclaringClass()
    {
        return declaringClass;
    }
    private boolean isInterface(Type type)
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
                throw new IllegalArgumentException(type+" can't be interface");
            }
        }
    }

    public boolean isInterface()
    {
        return isInterface;
    }

    public Type getSuperclass()
    {
        return superClass;
    }
    public static String getInternalForm(String fullyQualifiedname)
    {
        return fullyQualifiedname.replace('.', '/');
    }

    public int getModifiers()
    {
        return modifiers;
    }

    public String getSimpleName()
    {
        if (clazz == null)
        {
            int idx = fqnClassname.lastIndexOf('.');
            if (idx != -1)
            {
                return fqnClassname.substring(idx+1);
            }
            else
            {
                return fqnClassname;
            }
        }
        return clazz.getSimpleName();
    }
    
}
