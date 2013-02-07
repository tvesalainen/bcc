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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.vesalainen.bcc.MethodCompiler;
import org.vesalainen.bcc.MethodImplementor;
import org.vesalainen.bcc.SubClass;

/**
 *
 * @author tkv
 */
public class MethodWrapper implements AnnotatedElement, GenericDeclaration, Member

{
    private int modifiers;
    private Type declaringType;
    private String name;
    protected TypeVariable<?>[] typeParameters;
    private Type returnType;
    private Type[] parameters;
    private Type genericReturnType;
    private Type[] genericParameters;
    private Type[] genericExceptionTypes = new Type[]{};
    private boolean interf;
    protected boolean exists;
    private Annotation[] annotations;
    private Annotation[][] parameterAnnotations;
    private MethodImplementor implementor;
    private boolean wideIndex;
    private List<TypeVariable> typeParameterList;
    /**
     * @param method 
     */
    public MethodWrapper(Method method)
    {
        this(
            method.getModifiers(),
            method.getTypeParameters(),
            method.getDeclaringClass(),
            method.getName(),
            method.getDeclaringClass().isInterface(),
            method.getAnnotations(),
            method.getParameterAnnotations(),
            method.getGenericReturnType(),
            method.getGenericParameterTypes()
            );
        exists = true;
    }

    public MethodWrapper(Type declaringClass, String name, Type returnType, Type... parameters)
    {
        this(
            0,
            new TypeVariable<?>[]{},
            declaringClass,
            name,
            false,
            new Annotation[]{},
            null,
            returnType,
            parameters
            );
    }

    public MethodWrapper(int modifiers, Type declaringClass, String name, boolean interf, Type returnType, Type... parameters)
    {
        this(
            modifiers,
            new TypeVariable<?>[]{},
            declaringClass,
            name,
            interf,
            new Annotation[]{},
            null,
            returnType,
            parameters
            );
    }

    public MethodWrapper(Type declaringClass, String name, String descriptor, boolean interf)
    {
        this(
            0,
            new TypeVariable<?>[]{},
            declaringClass,
            name,
            interf,
            new Annotation[]{},
            null,
            Descriptor.getReturnType(descriptor),
            Descriptor.getParameters(descriptor)
            );
    }

    public MethodWrapper(int modifiers, Type declaringClass, String name, Type returnType, Type... parameters)
    {
        this(
            modifiers,
            new TypeVariable<?>[]{},
            declaringClass,
            name,
            false,
            new Annotation[]{},
            null,
            returnType,
            parameters
            );
    }

    public MethodWrapper(
            int modifiers,
            TypeVariable<?>[] typeParameters,
            Type declaringClass,
            String name,
            boolean interf,
            Annotation[] annotations,
            Annotation[][] parameterAnnotations,
            Type returnType,
            Type... parameters)
    {
        if (!isJavaIdentifier(name))
        {
            throw new IllegalArgumentException(name+" not java identifier");
        }
        this.modifiers = modifiers;
        this.typeParameters = typeParameters;
        this.declaringType = declaringClass;
        assert name.indexOf('.') == -1;
        this.name = name;
        this.interf = interf;
        this.annotations = annotations;
        if (parameterAnnotations != null)
        {
            this.parameterAnnotations = parameterAnnotations;
        }
        else
        {
            this.parameterAnnotations = emptyParameterAnnotations(parameters.length);
        }
        if (returnType instanceof Class<?>)
        {
            this.returnType = returnType;
            this.genericReturnType = returnType;
        }
        else
        {
            if (returnType instanceof ClassWrapper)
            {
                this.returnType = returnType;
                this.genericReturnType = returnType;
            }
            else
            {
                if (returnType instanceof TypeVariable)
                {
                    TypeVariable tv = (TypeVariable) returnType;
                    this.returnType = (Type) tv.getGenericDeclaration();
                    this.genericReturnType = returnType;
                }
                else
                {
                    if (returnType instanceof ParameterizedType)
                    {
                        ParameterizedType pt = (ParameterizedType) returnType;
                        this.returnType = (Type) pt.getRawType();
                        this.genericReturnType = returnType;
                    }
                    else
                    {
                        throw new IllegalArgumentException(returnType+" not valid");
                    }
                }
            }
        }
        this.parameters = new Type[parameters.length];
        this.genericParameters = new Type[parameters.length];
        int index = 0;
        for (Type param : parameters)
        {
            if (param instanceof Class<?>)
            {
                this.parameters[index] = param;
                this.genericParameters[index] = param;
            }
            else
            {
                if (param instanceof ClassWrapper)
                {
                    this.parameters[index] = param;
                    this.genericParameters[index] = param;
                }
                else
                {
                    if (param instanceof TypeVariable)
                    {
                        TypeVariable tv = (TypeVariable) param;
                        this.parameters[index] = (Type) tv.getGenericDeclaration();
                        this.genericParameters[index] = param;
                    }
                    else
                    {
                        if (param instanceof ParameterizedType)
                        {
                            ParameterizedType pt = (ParameterizedType) param;
                            this.parameters[index] = (Type) pt.getRawType();
                            this.genericParameters[index] = param;
                        }
                        else
                        {
                            throw new IllegalArgumentException(param+" not valid");
                        }
                    }
                }
            }
            index++;
        }
    }
    public void setExceptions(Type... exceptions)
    {
        if (exceptions != null)
        {
            for (Type e : exceptions)
            {
                if (e instanceof Class)
                {
                    Class cls = (Class) e;
                    if (!Throwable.class.isAssignableFrom(cls))
                    {
                        throw new IllegalArgumentException(e+" not exception type");
                    }
                }
                else
                {
                    throw new IllegalArgumentException(e+" not valid exception type");
                }
            }
        }
        genericExceptionTypes = exceptions;
    }
    public void addParameterAnnotation(int index, Annotation annotation)
    {
        parameterAnnotations[index] = Arrays.copyOf(parameterAnnotations[index], parameterAnnotations[index].length+1);
        parameterAnnotations[index][parameterAnnotations[index].length-1] = annotation;
    }
    
    private static Annotation[][] emptyParameterAnnotations(int length)
    {
        Annotation[][] a = new Annotation[length][];
        for (int ii=0;ii<length;ii++)
        {
            a[ii] = new Annotation[]{};
        }
        return a;
    }
    public void addType(String name, Type... bounds)
    {
        typeParameters = null;
        if (typeParameterList == null)
        {
            typeParameterList = new ArrayList<>();
        }
        typeParameterList.add(TypeFactory.createTypeVariable(this, name, bounds));
    }

    public static MethodWrapper wrap(Member member)
    {
        if (member instanceof MethodWrapper)
        {
            return (MethodWrapper) member;
        }
        else
        {
            if (member instanceof Method)
            {
                return new MethodWrapper((Method)member);
            }
            else
            {
                throw new IllegalArgumentException(member+" not suitable for MethodWrapper");
            }
        }
    }
    public void setWideIndex(boolean wideIndex)
    {
        this.wideIndex = wideIndex;
    }

    public MethodImplementor getImplementor()
    {
        return implementor;
    }

    public boolean needsImplementation()
    {
        return implementor != null;
    }

    public void implement(SubClass subClass) throws IOException
    {
        if (implementor == null)
        {
            subClass.defineMethod(Modifier.ABSTRACT | modifiers, name, returnType, genericExceptionTypes, parameters);
        }
        else
        {
            MethodCompiler mc = subClass.defineMethod(modifiers & ~Modifier.ABSTRACT, name, returnType, genericExceptionTypes, parameters);
            mc.setWideIndex(wideIndex);
            implementor.implement(mc, this);
        }
    }

    public void setImplementor(MethodImplementor implementor)
    {
        this.implementor = implementor;
    }
    
    public int getModifiers()
    {
        return modifiers;
    }
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        for (Annotation a : annotations)
        {
            if (annotationClass.equals(a.annotationType()))
            {
                return (T) a;
            }
        }
        return null;
    }

    public Annotation[][] getParameterAnnotations()
    {
        return parameterAnnotations;
    }

    public boolean isInterface()
    {
        return interf;
    }

    public String getName()
    {
        return name;
    }

    public Type getReturnType()
    {
        return (Type) returnType;
    }

    public static Type getReturnType(String descriptor)
    {
        return Descriptor.getReturnType(descriptor);
    }

    public Type[] getParameterTypes()
    {
        return (Type[]) parameters;
    }

    public String getDescriptor()
    {
        return Descriptor.getMethodDesriptor(this);
    }

    @Override
    public String toString()
    {
        return declaringType + "." + name + getDescriptor();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final MethodWrapper other = (MethodWrapper) obj;
        if (this.modifiers != other.modifiers)
        {
            return false;
        }
        if (this.declaringType != other.declaringType && (this.declaringType == null || !this.declaringType.equals(other.declaringType)))
        {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
        {
            return false;
        }
        if (this.returnType != other.returnType && (this.returnType == null || !this.returnType.equals(other.returnType)))
        {
            return false;
        }
        if (!Arrays.deepEquals(this.parameters, other.parameters))
        {
            return false;
        }
        if (this.interf != other.interf)
        {
            return false;
        }
        if (this.exists != other.exists)
        {
            return false;
        }
        if (!Arrays.deepEquals(this.parameterAnnotations, other.parameterAnnotations))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 23 * hash + Arrays.deepHashCode(this.parameters);
        return hash;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
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

    @Override
    public boolean isSynthetic()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    /**
     * @deprecated Use getDeclaringType()
     * @return 
     */
    @Override
    public Class<?> getDeclaringClass()
    {
        throw new UnsupportedOperationException("@deprecated Use getDeclaringType()");
    }

    public Type getDeclaringType()
    {
        return declaringType;
    }

    public Type[] getGenericParameterTypes()
    {
        return genericParameters;
    }

    public Type getGenericReturnType()
    {
        return genericReturnType;
    }

    public Type[] getGenericExceptionTypes()
    {
        return genericExceptionTypes;
    }

    public static boolean isJavaIdentifier(String id)
    {
        if (id.isEmpty())
        {
            return false;
        }
        if ("<init>".equals(id) || "<clinit>".equals(id))
        {
            return true;
        }
        if (!Character.isJavaIdentifierStart(id.charAt(0)))
        {
            return false;
        }
        for (int ii=1;ii<id.length();ii++)
        {
            if (!Character.isJavaIdentifierPart(id.charAt(ii)))
            {
                return false;
            }
        }
        return true;
    }

    public static String makeJavaIdentifier(String id)
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
        return sb.toString();
    }

    public static String makeJavaClassname(String id)
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
