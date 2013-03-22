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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import javax.annotation.processing.Filer;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.vesalainen.annotation.dump.Descriptor;
import org.vesalainen.bcc.AccessFlags.MethodFlags;
import org.vesalainen.bcc.ConstantInfo.Clazz;
import org.vesalainen.bcc.ConstantInfo.ConstantDouble;
import org.vesalainen.bcc.ConstantInfo.ConstantFloat;
import org.vesalainen.bcc.ConstantInfo.ConstantInteger;
import org.vesalainen.bcc.ConstantInfo.ConstantLong;
import org.vesalainen.bcc.ConstantInfo.ConstantString;
import org.vesalainen.bcc.ConstantInfo.Fieldref;
import org.vesalainen.bcc.ConstantInfo.InterfaceMethodref;
import org.vesalainen.bcc.ConstantInfo.Methodref;
import org.vesalainen.bcc.ConstantInfo.NameAndType;
import org.vesalainen.bcc.ConstantInfo.Utf8;
import org.vesalainen.bcc.model.E;
import org.vesalainen.bcc.model.ExecutableElementImpl.ConstructorBuilder;
import org.vesalainen.bcc.model.ExecutableElementImpl.MethodBuilder;
import org.vesalainen.bcc.model.T;
import org.vesalainen.bcc.model.UpdateableElement;
import org.vesalainen.bcc.model.VariableElementImpl.VariableBuilder;

/**
 *
 * @author tkv
 */
public class SubClass extends ClassFile
{
    private ReentrantLock intfLock = new ReentrantLock();
    private ReentrantLock fieldLock = new ReentrantLock();
    private ReentrantLock attrLock = new ReentrantLock();
    private ReentrantLock methodLock = new ReentrantLock();
    private ReentrantReadWriteLock constantLock = new ReentrantReadWriteLock();
    private ReadLock constantReadLock = constantLock.readLock();
    private WriteLock constantWriteLock = constantLock.writeLock();

    public SubClass(TypeElement superClass, String qualifiedName, Modifier... modifiers) throws IOException
    {
        super(superClass, qualifiedName, modifiers);
        
        magic = 0xcafebabe;
        minor_version = 0;
        major_version = 49;
        this_class = resolveClassIndex(this);
        super_class = resolveClassIndex(superClass);

    }

    @Override
    public final void addAttribute(AttributeInfo ai)
    {
        attrLock.lock();
        try
        {
            super.addAttribute(ai);
        }
        finally
        {
            attrLock.unlock();
        }
    }

    @Override
    protected int addConstantInfo(ConstantInfo ci, int fromIndex)
    {
        constantWriteLock.lock();
        try
        {
            return super.addConstantInfo(ci, fromIndex);
        }
        finally
        {
            constantWriteLock.unlock();
        }
    }

    @Override
    public void addFieldInfo(FieldInfo fieldInfo)
    {
        fieldLock.lock();
        try
        {
            super.addFieldInfo(fieldInfo);
        }
        finally
        {
            fieldLock.unlock();
        }
    }

    @Override
    public void addInterface(short intf)
    {
        intfLock.lock();
        try
        {
            super.addInterface(intf);
        }
        finally
        {
            intfLock.unlock();
        }
    }

    @Override
    public void addMethodInfo(MethodInfo methodInfo)
    {
        methodLock.lock();
        try
        {
            super.addMethodInfo(methodInfo);
        }
        finally
        {
            methodLock.unlock();
        }
    }
    
    /**
     * Returns the constant map index to field.
     * If entry doesn't exist it is created.
     * @param field
     * @return
     */
    int resolveFieldIndex(VariableElement field)
    {
        TypeElement declaringClass = (TypeElement) field.getEnclosingElement();
        String descriptor = Descriptor.getDesriptor(field);
        int index = resolveFieldIndex(declaringClass, field.getSimpleName().toString(), descriptor);
        addIndexedElement(index, field);
        return index;
    }
    /**
     * Returns the constant map index to field
     * If entry doesn't exist it is created.
     * @param declaringClass
     * @param name
     * @param descriptor
     * @return
     */
    private int resolveFieldIndex(TypeElement declaringClass, String name, String descriptor)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getRefIndex(Fieldref.class, declaringClass.getQualifiedName().toString(), name, descriptor);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index == -1)
        {
            // add entry to constant pool
            int ci = resolveClassIndex(declaringClass);
            int nati = resolveNameAndTypeIndex(name, descriptor);
            return addConstantInfo(new Fieldref(ci, nati), size);
        }
        return index;
    }
    /**
     * Returns the constant map index to method
     * If entry doesn't exist it is created.
     * @param method
     * @return
     */
    int resolveMethodIndex(ExecutableElement method)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        TypeElement declaringClass = (TypeElement) method.getEnclosingElement();
        String declaringClassname = declaringClass.getQualifiedName().toString();
        String descriptor = Descriptor.getDesriptor(method);
        String name = method.getSimpleName().toString();
        try
        {
            size = getConstantPoolSize();
            index = getRefIndex(Methodref.class, declaringClassname, name, descriptor);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index == -1)
        {
            // add entry to constant pool
            int ci = resolveClassIndex(declaringClass);
            int nati = resolveNameAndTypeIndex(name, descriptor);
            if (ElementKind.INTERFACE == method.getKind())
            {
                index = addConstantInfo(new InterfaceMethodref(ci, nati), size);
            }
            else
            {
                index = addConstantInfo(new Methodref(ci, nati), size);
            }
        }
        addIndexedElement(index, method);
        return index;
    }
    /**
     * Returns the constant map index to name
     * If entry doesn't exist it is created.
     * @param name
     * @return
     */
    final int resolveNameIndex(String name)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getNameIndex(name);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index != -1)
        {
            return index;
        }
        else
        {
            return addConstantInfo(new Utf8(name), size);
        }
    }
    /**
     * Returns the constant map index to name and type
     * If entry doesn't exist it is created.
     * @param name
     * @param descriptor
     * @return
     */
    int resolveNameAndTypeIndex(String name, String descriptor)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getNameAndTypeIndex(name, descriptor);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index != -1)
        {
            return index;
        }
        else
        {
            int nameIndex = resolveNameIndex(name);
            int typeIndex = resolveNameIndex(descriptor);
            return addConstantInfo(new NameAndType(nameIndex, typeIndex), size);
        }
    }
    /**
     * Returns the constant map index to class
     * If entry doesn't exist it is created.
     * @param type
     * @return
     */
    public final int resolveClassIndex(Class<?> type)
    {
        return resolveClassIndex(E.getTypeElement(type.getCanonicalName()));
    }
    /**
     * Returns the constant map index to class
     * If entry doesn't exist it is created.
     * @param type
     * @return
     */
    public final int resolveClassIndex(TypeElement type)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getClassIndex(type);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index == -1)
        {
            String name = E.getInternalForm(type);
            int nameIndex = resolveNameIndex(name);
            index = addConstantInfo(new Clazz(nameIndex), size);
        }
        addIndexedElement(index, type);
        return index;
    }
    /**
     * Returns the constant map index to constant
     * If entry doesn't exist it is created.
     * @param constant
     * @return
     */
    public final int resolveConstantIndex(int constant)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getConstantIndex(constant);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index != -1)
        {
            return index;
        }
        return addConstantInfo(new ConstantInteger(constant), size);
    }
    /**
     * Returns the constant map index to constant
     * If entry doesn't exist it is created.
     * @param constant
     * @return
     */
    public final int resolveConstantIndex(float constant)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getConstantIndex(constant);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index != -1)
        {
            return index;
        }
        return addConstantInfo(new ConstantFloat(constant), size);
    }
    /**
     * Returns the constant map index to constant
     * If entry doesn't exist it is created.
     * @param constant
     * @return
     */
    public final int resolveConstantIndex(double constant)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getConstantIndex(constant);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index != -1)
        {
            return index;
        }
        return addConstantInfo(new ConstantDouble(constant), size);
    }
    /**
     * Returns the constant map index to constant
     * If entry doesn't exist it is created.
     * @param constant
     * @return
     */
    public final int resolveConstantIndex(long constant)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getConstantIndex(constant);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index != -1)
        {
            return index;
        }
        return addConstantInfo(new ConstantLong(constant), size);
    }
    /**
     * Returns the constant map index to constant
     * If entry doesn't exist it is created.
     * @param constant
     * @return
     */
    public final int resolveConstantIndex(String constant)
    {
        int size = 0;
        int index = 0;
        int nameIndex = resolveNameIndex(constant);
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getConstantIndex(constant);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index != -1)
        {
            return index;
        }
        return addConstantInfo(new ConstantString(nameIndex), size);
    }

    public void implement(ExecutableElement method) throws IOException
    {
        throw new UnsupportedOperationException();
    }
    
    public void codeDefaultConstructor(FieldInitializer... fis) throws IOException
    {
        for (ExecutableElement constructor : ElementFilter.constructorsIn(superClass.getEnclosedElements()))
        {
            if (!constructor.getModifiers().contains(Modifier.PRIVATE))
            {
                MethodCompiler c = overrideMethod(constructor.getModifiers(), constructor);
                c.aload(0);
                int index = 1;
                for (VariableElement param : constructor.getParameters())
                {
                    c.tload(param.asType(), index++);
                }
                c.invokespecial(constructor);
                for (FieldInitializer fi : fis)
                {
                    fi.init(c);
                }
                c.treturn();
                c.end();
            }
        }
    }
    public void codeStaticInitializer(FieldInitializer... fis) throws IOException
    {
        if (fis.length != 0)
        {
            MethodCompiler c = createStaticInitializer();
            for (FieldInitializer fi : fis)
            {
                fi.init(c);
            }
            c.treturn();
            c.end();
        }
    }

    /**
     * Define a new field.
     * @param modifier
     * @param fieldName
     * @param type 
     */
    public void defineField(int modifier, String fieldName, Class<?> type)
    {
        defineField(modifier, fieldName, T.getTypeFor(type));
    }
    /**
     * Define a new field.
     * @param modifier
     * @param fieldName
     * @param type 
     */
    public void defineField(int modifier, String fieldName, TypeMirror type)
    {
        DeclaredType dt = (DeclaredType)asType();
        VariableBuilder builder = new VariableBuilder(this, fieldName, dt.getTypeArguments(), typeParameterMap);
        builder.addModifiers(modifier);
        builder.setType(type);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement());
        addFieldInfo(fieldInfo);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, int constant)
    {
        DeclaredType dt = (DeclaredType)asType();
        VariableBuilder builder = new VariableBuilder(this, fieldName, dt.getTypeArguments(), typeParameterMap);
        builder.addModifiers(modifier);
        builder.addModifier(Modifier.STATIC);
        builder.setType(T.IntA);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, long constant)
    {
        DeclaredType dt = (DeclaredType)asType();
        VariableBuilder builder = new VariableBuilder(this, fieldName, dt.getTypeArguments(), typeParameterMap);
        builder.addModifiers(modifier);
        builder.addModifier(Modifier.STATIC);
        builder.setType(T.Long);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, float constant)
    {
        DeclaredType dt = (DeclaredType)asType();
        VariableBuilder builder = new VariableBuilder(this, fieldName, dt.getTypeArguments(), typeParameterMap);
        builder.addModifiers(modifier);
        builder.addModifier(Modifier.STATIC);
        builder.setType(T.Float);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, double constant)
    {
        DeclaredType dt = (DeclaredType)asType();
        VariableBuilder builder = new VariableBuilder(this, fieldName, dt.getTypeArguments(), typeParameterMap);
        builder.addModifiers(modifier);
        builder.addModifier(Modifier.STATIC);
        builder.setType(T.Double);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, String constant)
    {
        DeclaredType dt = (DeclaredType)asType();
        VariableBuilder builder = new VariableBuilder(this, fieldName, dt.getTypeArguments(), typeParameterMap);
        builder.addModifiers(modifier);
        builder.addModifier(Modifier.STATIC);
        builder.setType(T.String);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
    }

    public MethodCompiler createStaticInitializer()
    {
        DeclaredType dt = (DeclaredType)asType();
        ConstructorBuilder builder = new ConstructorBuilder(
                this, 
                ElementKind.STATIC_INIT, 
                "<clinit>", 
                dt.getTypeArguments(), 
                typeParameterMap
                );
        builder.addModifier(Modifier.STATIC);
        MethodInfo methodInfo = new MethodInfo(this, builder.getExecutableElement());
        addMethodInfo(methodInfo);
        MethodCompiler mc = new MethodCompiler(this, methodInfo);
        return mc;
    }
    public MethodCompiler defineMethod(int modifier, String methodName, Class<?> returnType, Class<?>... parameters)
    {
        return defineMethod(modifier, methodName, returnType, null, parameters);
    }
    public MethodCompiler defineMethod(int modifier, String methodName, Class<?> returnType, Class<?>[] exceptionTypes, Class<?>... parameters)
    {
        DeclaredType dt = (DeclaredType)asType();
        MethodBuilder builder = new MethodBuilder(
                this, 
                methodName, 
                dt.getTypeArguments(), 
                typeParameterMap
                );
        builder.addModifiers(modifier);
        builder.setReturnType(returnType);
        if (exceptionTypes != null)
        {
            for (Class<?> e : exceptionTypes)
            {
                builder.addThrownType(e);
            }
        }
        for (Class<?> p : parameters)
        {
            builder.addParameter(p);
        }
        return defineMethod(builder.getExecutableElement());
    }

    public MethodCompiler overrideConstructor(int modifier, Class<?>... parameters)
    {
        ExecutableElement method = E.getConstructor(superClass, E.getParams(parameters));
        Set<Modifier> mod = EnumSet.noneOf(Modifier.class);
        MethodFlags.setModifiers(mod, modifier);
        return overrideMethod(modifiers, method);
    }

    public MethodCompiler overrideMethod(int modifier, String methodName, Class<?>... parameters)
    {
        ExecutableElement method = E.getMethod(superClass, methodName, E.getParams(parameters));
        Set<Modifier> mod = EnumSet.noneOf(Modifier.class);
        MethodFlags.setModifiers(mod, modifier);
        return overrideMethod(modifiers, method);
    }

    public MethodCompiler overrideMethod(Set<Modifier> modifiers, ExecutableElement method)
    {
        method = E.createUpdateableElement(method);
        UpdateableElement ue = (UpdateableElement) method;
        ue.setEnclosingElement(this);
        ue.setModifiers(modifiers);
        return defineMethod(method);
    }

    public MethodCompiler defineMethod(ExecutableElement method)
    {
        MethodInfo methodInfo = new MethodInfo(this, method);
        addMethodInfo(methodInfo);
        MethodCompiler mc = new MethodCompiler(this, methodInfo);
        return mc;
    }
    public Object newInstance() throws IOException
    {
        try
        {
            Class<?> c = load();
            return c.newInstance();
        }
        catch (InstantiationException ex)
        {
            throw new IOException(ex);
        }
        catch (IllegalAccessException ex)
        {
            throw new IOException(ex);
        }
    }
    public Class<?> load() throws IOException
    {
        GenClassLoader cl = new GenClassLoader(superClass.getClass().getClassLoader());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        write(dos);
        dos.close();

        try
        {
            Class<?> cls = cl.load(getQualifiedName().toString(), baos.toByteArray());
            return cls;
        }
        catch (Error er)
        {
            dump();
            throw new IOException(er);
        }
    }

    public void createSourceFile(Filer filer) throws IOException
    {
        FileObject sourceFile = filer.getResource(
                StandardLocation.SOURCE_OUTPUT, 
                E.getPackageOf(this).getQualifiedName(), 
                getSimpleName()+".jasm"
                );
        try (LineNumberPrintStream out = new LineNumberPrintStream(sourceFile.openOutputStream()))
        {
            for (MethodInfo mi : getMethodInfos())
            {
                CodeAttribute code = mi.getCodeAttribute();
                LineNumberTable lnt = new LineNumberTable(this);
                ByteCodeDump dump = new ByteCodeDump(code.getCode(), this, out);
                dump.print(mi.getMc(), lnt);
                mi.getCodeAttribute().addLineNumberTable(lnt);
            }
            //out.close();
        }
        SourceFileAttribute sfa = new SourceFileAttribute(this, getSimpleName()+".jasm");
        addAttribute(sfa);
    }
    public void dump() throws IOException
    {
        LineNumberPrintStream out = new LineNumberPrintStream(System.err);
        for (MethodInfo mi : getMethodInfos())
        {
            CodeAttribute code = mi.getCodeAttribute();
            LineNumberTable lnt = new LineNumberTable(this);
            ByteCodeDump dump = new ByteCodeDump(code.getCode(), this, out);
            dump.print(mi.getMc(), lnt);
            mi.getCodeAttribute().addLineNumberTable(lnt);
        }
        SourceFileAttribute sfa = new SourceFileAttribute(this, getSimpleName()+".jasm");
        addAttribute(sfa);
    }
    /**
     * Saves subclass as class file
     * @param classpath
     * @throws IOException
     */
    public void save(Filer filer) throws IOException
    {
        JavaFileObject sourceFile = filer.createClassFile(getQualifiedName(), this);
        BufferedOutputStream bos = new BufferedOutputStream(sourceFile.openOutputStream());
        DataOutputStream dos = new DataOutputStream(bos);
        write(dos);
        dos.close();
    }

}
