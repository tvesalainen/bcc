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

import org.vesalainen.bcc.type.Descriptor;
import org.vesalainen.bcc.type.ClassWrapper;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
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
import org.vesalainen.bcc.type.Generics;
import org.vesalainen.bcc.type.MethodWrapper;
import org.vesalainen.bcc.type.Signature;

/**
 *
 * @author tkv
 */
public class SubClass extends ClassFile
{
    private ClassFile superClassFile;
    private ReentrantLock intfLock = new ReentrantLock();
    private ReentrantLock fieldLock = new ReentrantLock();
    private ReentrantLock attrLock = new ReentrantLock();
    private ReentrantLock methodLock = new ReentrantLock();
    private ReentrantReadWriteLock constantLock = new ReentrantReadWriteLock();
    private ReadLock constantReadLock = constantLock.readLock();
    private WriteLock constantWriteLock = constantLock.writeLock();

    public SubClass(ClassWrapper thisClass) throws IOException
    {
        this.thisClass = thisClass;
        this.superClass = thisClass.getSuperclass();
        if (superClass instanceof Class<?>)
        {
            Class<?> sCls = (Class<?>) superClass;
            this.superClassFile = new ClassFile(sCls);
        }
        magic = 0xcafebabe;
        minor_version = 0;
        major_version = 49;
        access_flags = ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC;
        this_class = resolveClassIndex(thisClass);
        super_class = resolveClassIndex(superClass);

        if (Signature.needsSignature(thisClass))
        {
            int nameIndex = resolveNameIndex("Signature");
            String signature = Signature.getClassSignature(thisClass);
            int signatureIndex = resolveNameIndex(signature);
            SignatureAttribute sa = new SignatureAttribute(this, nameIndex, signatureIndex);
            addAttribute(sa);
        }
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
     * Returns the constant map index to constructor
     * If entry doesn't exist it is created.
     * @param constructor
     * @return
     */
    public int resolveConstructorIndex(Member constructor)
    {
        return resolveMethodIndex(constructor);
    }
    
    /**
     * Returns the constant map index to field.
     * If entry doesn't exist it is created.
     * @param field
     * @return
     */
    public int resolveFieldIndex(Member field)
    {
        Type declaringClass = field.getDeclaringClass();
        String descriptor = Descriptor.getFieldDesriptor(field);
        int index = resolveFieldIndex(declaringClass, Generics.getName(field), descriptor);
        addWrapper(index, field);
        return index;
    }
    /**
     * Returns the constant map index to field
     * If entry doesn't exist it is created.
     * @param declaringClass
     * @param name
     * @param type
     * @return 
     */
    public int resolveFieldIndex(Type declaringClass, String name, Type type)
    {
        return resolveFieldIndex(declaringClass, name, Descriptor.getFieldDesriptor(type));
    }
    /**
     * Returns the constant map index to field
     * If entry doesn't exist it is created.
     * @param declaringClass
     * @param name
     * @param descriptor
     * @return
     */
    public int resolveFieldIndex(Type declaringClass, String name, String descriptor)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getRefIndex(Fieldref.class, Generics.getFullyQualifiedForm(declaringClass), name, descriptor);
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
    public int resolveMethodIndex(Type declaringClass, String methodName, String methodDescription, boolean b)
    {
        MethodWrapper method = new MethodWrapper(declaringClass, methodName, methodDescription, b);
        return resolveMethodIndex(method);
    }
    /**
     * Returns the constant map index to method
     * If entry doesn't exist it is created.
     * @param clazz
     * @param methodName
     * @param descriptor
     * @param interf
     * @return
     */
    public int resolveMethodIndex(Member method)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        Type declaringClass = Generics.getDeclaringClass(method);
        String declaringClassname = Generics.getFullyQualifiedForm(declaringClass);
        String descriptor = Descriptor.getMethodDesriptor(method);
        try
        {
            size = getConstantPoolSize();
            index = getRefIndex(Methodref.class, declaringClassname, Generics.getName(method), descriptor);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index == -1)
        {
            // add entry to constant pool
            int ci = resolveClassIndex(declaringClass);
            int nati = resolveNameAndTypeIndex(Generics.getName(method), descriptor);
            if (Generics.isInterfaceMethod(method))
            {
                index = addConstantInfo(new InterfaceMethodref(ci, nati), size);
            }
            else
            {
                index = addConstantInfo(new Methodref(ci, nati), size);
            }
        }
        addWrapper(index, method);
        return index;
    }
    /**
     * Returns the constant map index to name
     * If entry doesn't exist it is created.
     * @param name
     * @return
     */
    public final int resolveNameIndex(String name)
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
    public int resolveNameAndTypeIndex(String name, String descriptor)
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
     * @param cls
     * @return
     */
    public final int resolveClassIndex(Type type)
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
            String name = Generics.getInternalForm(type);
            int nameIndex = resolveNameIndex(name);
            index = addConstantInfo(new Clazz(nameIndex), size);
        }
        addWrapper(index, type);
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

    public int getThisIndex()
    {
        return this_class;
    }

    public int getSuperIndex()
    {
        return super_class;
    }

    public int getThisDescriptorIndex()
    {
        return resolveNameIndex(Descriptor.getFieldDesriptor(thisClass));
    }
    
    public void implement(Member method) throws IOException
    {
        if (!isImplemented(method))
        {
            Generics.implement(method, this);
        }
        // TODO implement concurrent queue here
    }
    
    public void codeDefaultConstructor(FieldInitializer... fis) throws IOException
    {
        Constructor<?>[] clist = Generics.getDeclaredConstructors(superClass);
        for (Constructor constructor : clist)
        {
            if ((constructor.getModifiers() & Modifier.PRIVATE) == 0)
            {
                MethodCompiler c = override(constructor.getModifiers(), constructor);
                c.aload(0);
                int index = 1;
                for (Class<?> param : constructor.getParameterTypes())
                {
                    c.nameArgument("arg"+index, index);
                    c.tload(param, index++);
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

    public MethodCompiler createStaticInitializer()
    {
        String methodName = "<clinit>";
        int nameIndex = resolveNameIndex(methodName);
        String methodDescription = "()V";
        int descriptorIndex = resolveNameIndex(methodDescription);
        int codeIndex = resolveNameIndex("Code");
        CodeAttribute code = new CodeAttribute(this, codeIndex);
        MethodInfo methodInfo = new MethodInfo(Modifier.STATIC, nameIndex, descriptorIndex, code);
        addMethodInfo(methodInfo);
        MethodCompiler mc = new MethodCompiler(this, Modifier.STATIC, code, new Type[]{}, void.class, methodName);
        methodInfo.setMc(mc);
        return mc;
    }
    /**
     * Define a new field.
     * @param modifier
     * @param fieldName
     * @param type 
     */
    public void defineField(int modifier, String fieldName, Type type)
    {
        if (Signature.needsSignature(type))
        {
            int nameIndex = resolveNameIndex("Signature");
            String signature = Signature.getFieldSignature(type);
            int signatureIndex = resolveNameIndex(signature);
            SignatureAttribute sa = new SignatureAttribute(this, nameIndex, signatureIndex);
            defineField(modifier, fieldName, Descriptor.getFieldDesriptor(type), sa);
        }
        else
        {
            defineField(modifier, fieldName, Descriptor.getFieldDesriptor(type));
        }
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, int constant)
    {
        if (!Modifier.isStatic(modifier))
        {
            throw new IllegalArgumentException("cannot define non-static field "+thisClass+"."+fieldName+" as constant");
        }
        String descriptor = Descriptor.getFieldDesriptor(int.class);
        int attribute_name_index = resolveNameIndex("ConstantValue");
        int constantvalue_index = resolveConstantIndex(constant);
        ConstantValue constantValue = new ConstantValue(this, attribute_name_index, constantvalue_index);
        defineField(modifier, fieldName, descriptor, constantValue);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, long constant)
    {
        if (!Modifier.isStatic(modifier))
        {
            throw new IllegalArgumentException("cannot define non-static field "+thisClass+"."+fieldName+" as constant");
        }
        String descriptor = Descriptor.getFieldDesriptor(long.class);
        int attribute_name_index = resolveNameIndex("ConstantValue");
        int constantvalue_index = resolveConstantIndex(constant);
        ConstantValue constantValue = new ConstantValue(this, attribute_name_index, constantvalue_index);
        defineField(modifier, fieldName, descriptor, constantValue);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, float constant)
    {
        if (!Modifier.isStatic(modifier))
        {
            throw new IllegalArgumentException("cannot define non-static field "+thisClass+"."+fieldName+" as constant");
        }
        String descriptor = Descriptor.getFieldDesriptor(float.class);
        int attribute_name_index = resolveNameIndex("ConstantValue");
        int constantvalue_index = resolveConstantIndex(constant);
        ConstantValue constantValue = new ConstantValue(this, attribute_name_index, constantvalue_index);
        defineField(modifier, fieldName, descriptor, constantValue);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, double constant)
    {
        if (!Modifier.isStatic(modifier))
        {
            throw new IllegalArgumentException("cannot define non-static field "+thisClass+"."+fieldName+" as constant");
        }
        String descriptor = Descriptor.getFieldDesriptor(double.class);
        int attribute_name_index = resolveNameIndex("ConstantValue");
        int constantvalue_index = resolveConstantIndex(constant);
        ConstantValue constantValue = new ConstantValue(this, attribute_name_index, constantvalue_index);
        defineField(modifier, fieldName, descriptor, constantValue);
    }
    /**
     * Define constant field and set the constant value.
     * @param modifier
     * @param fieldName
     * @param constant 
     */
    public void defineConstantField(int modifier, String fieldName, String constant)
    {
        if (!Modifier.isStatic(modifier))
        {
            throw new IllegalArgumentException("cannot define non-static field "+thisClass+"."+fieldName+" as constant");
        }
        String descriptor = Descriptor.getFieldDesriptor(String.class);
        int attribute_name_index = resolveNameIndex("ConstantValue");
        int constantvalue_index = resolveConstantIndex(constant);
        ConstantValue constantValue = new ConstantValue(this, attribute_name_index, constantvalue_index);
        defineField(modifier, fieldName, descriptor, constantValue);
    }
    private void defineField(int modifier, String fieldName, String descriptor, AttributeInfo... attributes)
    {
        int nameIndex = resolveNameIndex(fieldName);
        int descriptorIndex = resolveNameIndex(descriptor);
        int fieldIndex = resolveFieldIndex(thisClass, fieldName, descriptor);
        FieldInfo fieldInfo = new FieldInfo(this, modifier, nameIndex, descriptorIndex, attributes);
        addFieldInfo(fieldInfo);
    }

    public MethodCompiler defineMethod(int modifier, String methodName, Type returnType, Type... parameters)
    {
        return defineMethod(modifier, methodName, returnType, null, parameters);
    }
    public MethodCompiler defineMethod(int modifier, String methodName, Type returnType, Type[] exceptionTypes, Type... parameters)
    {
        MethodInfo methodInfo = null;
        SignatureAttribute sa = null;
        ExceptionsAttribute ea = createExceptionsAttribute(exceptionTypes);
        MethodWrapper mw = new MethodWrapper(modifier, superClass, methodName, returnType, parameters);
        mw.setExceptions(exceptionTypes);
        if (Signature.needsSignature(mw))
        {
            int nameIndex2 = resolveNameIndex("Signature");
            String signature = Signature.getMethodSignature(mw);
            int signatureIndex = resolveNameIndex(signature);
            sa = new SignatureAttribute(this, nameIndex2, signatureIndex);
        }
        
        int nameIndex = resolveNameIndex(methodName);
        String methodDescription = Descriptor.getMethodDesriptor(returnType, parameters);
        int descriptorIndex = resolveNameIndex(methodDescription);
        int methodIndex = resolveMethodIndex(thisClass, methodName, methodDescription, false);
        if (Modifier.isAbstract(modifier))
        {
            methodInfo = new MethodInfo(modifier, nameIndex, descriptorIndex, sa, ea);
            addMethodInfo(methodInfo);
            return null;
        }
        else
        {
            int codeIndex = resolveNameIndex("Code");
            CodeAttribute code = new CodeAttribute(this, codeIndex);
            methodInfo = new MethodInfo(modifier, nameIndex, descriptorIndex, code, sa, ea);
            addMethodInfo(methodInfo);
            MethodCompiler mc = new MethodCompiler(this, modifier, code, parameters, returnType, methodName);
            methodInfo.setMc(mc);
            return mc;
        }
    }

    public MethodCompiler override(int modifier, Member method)
    {
        if (Generics.isConstructor(method))
        {
            return overrideConstructor(modifier, method);
        }
        else
        {
            return overrideMethod(modifier, method);
        }
    }
    private MethodCompiler overrideConstructor(int modifier, Member constructor)
    {
        String methodName = "<init>";
        int nameIndex = resolveNameIndex(methodName);
        String methodDescription = Descriptor.getMethodDesriptor(constructor);
        int descriptorIndex = resolveNameIndex(methodDescription);
        int codeIndex = resolveNameIndex("Code");
        CodeAttribute code = new CodeAttribute(this, codeIndex);
        ExceptionsAttribute ea = createExceptionsAttribute(Generics.getGenericExceptionTypes(constructor));
        MethodInfo methodInfo = new MethodInfo(modifier, nameIndex, descriptorIndex, code, ea);
        addMethodInfo(methodInfo);
        int methodIndex = resolveMethodIndex(constructor);
        Type[] parameters = Generics.getParameterTypes(constructor);
        MethodCompiler mc = new MethodCompiler(this, modifier, code, parameters, void.class, methodName);
        methodInfo.setMc(mc);
        if (superClassFile != null)
        {
            String[] args = superClassFile.getArgNames(constructor);
            if (args != null)
            {
                mc.nameArguments(args);
            }
        }
        return mc;
    }

    private MethodCompiler overrideMethod(int modifier, Member method)
    {
        String methodName = Generics.getName(method);
        int nameIndex = resolveNameIndex(methodName);
        String methodDescription = Descriptor.getMethodDesriptor(method);
        int descriptorIndex = resolveNameIndex(methodDescription);
        int codeIndex = resolveNameIndex("Code");
        CodeAttribute code = new CodeAttribute(this, codeIndex);
        ExceptionsAttribute ea = createExceptionsAttribute(Generics.getGenericExceptionTypes(method));
        MethodInfo methodInfo = new MethodInfo(modifier, nameIndex, descriptorIndex, code, ea);
        addMethodInfo(methodInfo);
        int methodIndex = resolveMethodIndex(method);   //thisClass, methodName, methodDescription, false);
        Type[] parameters = Generics.getParameterTypes(method);
        Type returnType = Generics.getReturnType(method);
        MethodCompiler mc = new MethodCompiler(this, modifier, code, parameters, returnType, Generics.getName(method));
        methodInfo.setMc(mc);
        if (superClassFile != null)
        {
            String[] args = superClassFile.getArgNames(method);
            if (args != null)
            {
                mc.nameArguments(args);
            }
        }
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
        GenClassLoader cl = new GenClassLoader(Generics.getClassLoader(superClass));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        write(dos);
        dos.close();
//File f = new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\LPG\\build\\classes");
//save(f);

        try
        {
            Class<?> cls = cl.load(Generics.getFullyQualifiedForm(thisClass), baos.toByteArray());
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
        FileObject sourceFile = Generics.createFileForClass(thisClass, filer, ".jasm");
        int ani;
        try (LineNumberPrintStream out = new LineNumberPrintStream(sourceFile.openOutputStream()))
        {
            ani = resolveNameIndex("LineNumberTable");
            for (MethodInfo mi : getMethodInfos())
            {
                CodeAttribute code = mi.getCodeAttribute();
                LineNumberTable lnt = new LineNumberTable(this, ani);
                ByteCodeDump dump = new ByteCodeDump(code.getCode(), this, out);
                dump.print(mi.getMc(), lnt);
                mi.getCodeAttribute().addAttribute(lnt);
            }
            //out.close();
        }
        ani = resolveNameIndex("SourceFile");
        int sfi = resolveNameIndex(Generics.getSourceName(thisClass));
        SourceFileAttribute sfa = new SourceFileAttribute(this, ani, sfi);
        addAttribute(sfa);
    }
    public void dump() throws IOException
    {
        int ani;
        LineNumberPrintStream out = new LineNumberPrintStream(System.err);
        ani = resolveNameIndex("LineNumberTable");
        for (MethodInfo mi : getMethodInfos())
        {
            CodeAttribute code = mi.getCodeAttribute();
            LineNumberTable lnt = new LineNumberTable(this, ani);
            ByteCodeDump dump = new ByteCodeDump(code.getCode(), this, out);
            dump.print(mi.getMc(), lnt);
            mi.getCodeAttribute().addAttribute(lnt);
        }
        ani = resolveNameIndex("SourceFile");
        int sfi = resolveNameIndex(Generics.getSourceName(thisClass));
        SourceFileAttribute sfa = new SourceFileAttribute(this, ani, sfi);
        addAttribute(sfa);
    }
    /**
     * Saves subclass as class file
     * @param classpath
     * @throws IOException
     */
    public void save(Filer filer) throws IOException
    {
        JavaFileObject sourceFile = Generics.createClassFile(thisClass, filer);
        BufferedOutputStream bos = new BufferedOutputStream(sourceFile.openOutputStream());
        DataOutputStream dos = new DataOutputStream(bos);
        write(dos);
        dos.close();
    }

    public Type getSuperClass()
    {
        return superClass;
    }

    private ExceptionsAttribute createExceptionsAttribute(Type[] exceptionTypes)
    {
        if (exceptionTypes == null || exceptionTypes.length == 0)
        {
            return null;
        }
        int[] indexes = new int[exceptionTypes.length];
        int index = 0;
        for (Type t : exceptionTypes)
        {
            indexes[index++] = resolveClassIndex(t);
        }
        int ni = this.resolveNameIndex("Exceptions");
        return new ExceptionsAttribute(this, ni, indexes);
    }

}
