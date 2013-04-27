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
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.vesalainen.bcc.AccessFlags.ClassFlags;
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
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.ExecutableElementImpl.ConstructorBuilder;
import org.vesalainen.bcc.model.ExecutableElementImpl.MethodBuilder;
import org.vesalainen.bcc.model.Typ;
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

    public SubClass(Class<?> superClass, String qualifiedName, Modifier... modifiers) throws IOException
    {
        this(El.getTypeElement(superClass.getCanonicalName()), qualifiedName, modifiers);
    }
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
    final int resolveNameIndex(CharSequence name)
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
        return resolveClassIndex(El.getTypeElement(type.getCanonicalName()));
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
            String name = El.getInternalForm(type);
            int nameIndex = resolveNameIndex(name);
            index = addConstantInfo(new Clazz(nameIndex), size);
        }
        addIndexedElement(index, type);
        return index;
    }
    /**
     * Returns the constant map index to class
     * If entry doesn't exist it is created.
     * @param arrayType
     * @return
     */
    public final int resolveClassIndex(ArrayType arrayType)
    {
        int size = 0;
        int index = 0;
        constantReadLock.lock();
        try
        {
            size = getConstantPoolSize();
            index = getClassIndex(arrayType);
        }
        finally
        {
            constantReadLock.unlock();
        }
        if (index == -1)
        {
            String name = Descriptor.getFieldDesriptor(arrayType);
            int nameIndex = resolveNameIndex(name);
            index = addConstantInfo(new Clazz(nameIndex), size);
        }
        addIndexedElement(index, arrayType);
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

    public void codeDefaultConstructor(final FieldInitializer... fis) throws IOException
    {
        for (final ExecutableElement constructor : ElementFilter.constructorsIn(superClass.getEnclosedElements()))
        {
            if (!constructor.getModifiers().contains(Modifier.PRIVATE))
            {
                MethodCompiler c = new MethodCompiler()
                {
                    @Override
                    protected void implement() throws IOException
                    {
                        aload(0);
                        int index = 1;
                        for (VariableElement param : constructor.getParameters())
                        {
                            tload(param.asType(), index++);
                        }
                        invokespecial(constructor);
                        for (FieldInitializer fi : fis)
                        {
                            fi.init(this);
                        }
                        treturn();
                    }
                };
                overrideMethod(c, constructor, constructor.getModifiers());
            }
        }
    }
    public void codeStaticInitializer(final FieldInitializer... fis) throws IOException
    {
        if (fis.length != 0)
        {
            MethodCompiler c = new MethodCompiler()
            {
                @Override
                protected void implement() throws IOException
                {
                    for (FieldInitializer fi : fis)
                    {
                        fi.init(this);
                    }
                    treturn();
                }
            };
            createStaticInitializer(c);
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
        defineField(modifier, fieldName, Typ.getTypeFor(type));
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
        fieldInfo.readyToWrite();
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
        builder.setType(Typ.IntA);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
        fieldInfo.readyToWrite();
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
        builder.setType(Typ.Long);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
        fieldInfo.readyToWrite();
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
        builder.setType(Typ.Float);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
        fieldInfo.readyToWrite();
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
        builder.setType(Typ.Double);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
        fieldInfo.readyToWrite();
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
        builder.setType(Typ.String);
        FieldInfo fieldInfo = new FieldInfo(this, builder.getVariableElement(), new ConstantValue(this, constant));
        addFieldInfo(fieldInfo);
        fieldInfo.readyToWrite();
    }

    public void createStaticInitializer(MethodCompiler mc) throws IOException
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
        mc.startImplement(this, methodInfo);
        methodInfo.readyToWrite();
    }
    public MethodBuilder buildMethod(String methodName)
    {
        DeclaredType dt = (DeclaredType)asType();
        return new MethodBuilder(
                this, 
                methodName, 
                dt.getTypeArguments(), 
                typeParameterMap
                );
    }
    public void defineMethod(MethodCompiler mc, int modifier, String methodName, Class<?> returnType, Class<?>... parameters) throws IOException
    {
        defineMethod(mc, modifier, methodName, returnType, null, parameters);
    }
    public void defineMethod(MethodCompiler mc, int modifier, String methodName, Class<?> returnType, Class<?>[] exceptionTypes, Class<?>... parameters) throws IOException
    {
        MethodBuilder builder = buildMethod(methodName);
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
            builder.addParameter("").setType(p);
        }
        defineMethod(mc, builder.getExecutableElement());
    }

    public void overrideConstructor(MethodCompiler mc, int modifier, Class<?>... parameters) throws IOException
    {
        ExecutableElement method = El.getConstructor(superClass, El.getParams(parameters));
        Set<Modifier> mod = EnumSet.noneOf(Modifier.class);
        MethodFlags.setModifiers(mod, modifier);
        overrideMethod(mc, method, modifiers);
    }

    public void overrideMethod(MethodCompiler mc, int modifier, String methodName, Class<?>... parameters) throws IOException
    {
        ExecutableElement method = El.getMethod(superClass, methodName, El.getParams(parameters));
        Set<Modifier> mod = EnumSet.noneOf(Modifier.class);
        MethodFlags.setModifiers(mod, modifier);
        overrideMethod(mc, method, modifiers);
    }

    public void overrideMethod(MethodCompiler mc, ExecutableElement method, Modifier... modifiers) throws IOException
    {
        overrideMethod(mc, method, MethodFlags.getModifiers(modifiers));
    }
    public void overrideMethod(MethodCompiler mc, ExecutableElement method, Set<Modifier> modifiers) throws IOException
    {
        Method m = new Method(method);
        m.setModifiers(modifiers);
        defineMethod(mc, m);
    }

    public void defineMethod(MethodCompiler mc, ExecutableElement method) throws IOException
    {
        defineMethod(mc, new Method(method));
    }
    private void defineMethod(MethodCompiler mc, Method method) throws IOException
    {
        MethodInfo methodInfo = new MethodInfo(this, method);
        addMethodInfo(methodInfo);
        mc.startImplement(this, methodInfo);
        methodInfo.readyToWrite();
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
        FileObject sourceFile = filer.createResource(
                StandardLocation.SOURCE_OUTPUT, 
                El.getPackageOf(this).getQualifiedName(), 
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

    /**
     * Writes the class
     * @param out
     * @throws IOException
     */
    @Override
    public void write(DataOutput out) throws IOException
    {
        addSignatureIfNeed();
        out.writeInt(magic);
        out.writeShort(minor_version);
        out.writeShort(major_version);
        out.writeShort(constant_pool.size()+1);
        for (ConstantInfo ci : constant_pool)
        {
            ci.write(out);
        }
        int modifier = ClassFlags.getModifier(getModifiers());
        modifier |= ClassFlags.ACC_SYNTHETIC | ClassFlags.ACC_PUBLIC | ClassFlags.ACC_SUPER;
        out.writeShort(modifier);
        out.writeShort(this_class);
        out.writeShort(super_class);
        out.writeShort(interfaces.size());
        for (int ii : interfaces)
        {
            out.writeShort(ii);
        }
        out.writeShort(fields.size());
        for (FieldInfo fi : fields)
        {
            fi.write(out);
        }
        out.writeShort(methods.size());
        for (MethodInfo mi : methods)
        {
            mi.write(out);
        }
        out.writeShort(attributes.size());
        for (AttributeInfo ai : attributes)
        {
            ai.write(out);
        }

    }

    private void addSignatureIfNeed()
    {
        String signature = Signature.getSignature(this);
        if (!signature.isEmpty())
        {
            attributes.add(new SignatureAttribute(this, signature));
        }
    }

    VariableElement getVariableFor(VariableElement ve)
    {
        if (hasTypeParameters(ve))
        {
            DeclaredType parameterizedType = (DeclaredType) ve.asType();
            TypeMirror actualType = getActualType(parameterizedType);
            return new Variable(ve, actualType);
        }
        return ve;
    }
    private boolean hasTypeParameters(VariableElement ve)
    {
        return hasTypeParameters(ve.asType());
    }
    private boolean hasTypeParameters(TypeMirror type)
    {
        if (type.getKind() == TypeKind.DECLARED)
        {
            DeclaredType dt = (DeclaredType) type;
            for (TypeMirror ta : dt.getTypeArguments())
            {
                switch (ta.getKind())
                {
                    case DECLARED:
                        if (hasTypeParameters(ta))
                        {
                            return true;
                        }
                        break;
                    case TYPEVAR:
                        return true;
                }
            }
        }
        return false;
    }
    private TypeMirror getActualType(TypeMirror type)
    {
        if (hasTypeParameters(type))
        {
            DeclaredType parameterizedType = (DeclaredType) type;
            TypeElement typeElement = (TypeElement) parameterizedType.asElement();
            TypeMirror[] typeArguments = new TypeMirror[parameterizedType.getTypeArguments().size()];
            int index = 0;
            for (TypeMirror typeArg : parameterizedType.getTypeArguments())
            {
                if (typeArg.getKind() == TypeKind.TYPEVAR)
                {
                    TypeParameterElement typeParameter = typeElement.getTypeParameters().get(index);
                    typeArguments[index] = getActualType(getActualTypeFor(typeParameter));
                }
                else
                {
                    typeArguments[index] = getActualType(typeArg);
                }
                index++;
            }
            return Typ.getDeclaredType(typeElement, typeArguments);
        }
        else
        {
            return type;
        }
    }
    private TypeMirror getActualTypeFor(TypeParameterElement typeParameter)
    {
        TypeMirror type = asType();
        while (type.getKind() == TypeKind.DECLARED)
        {
            DeclaredType dt = (DeclaredType) type;
            TypeElement te = (TypeElement) dt.asElement();
            Name name = typeParameter.getSimpleName();
            int index = 0;
            for (TypeParameterElement tpe : te.getTypeParameters())
            {
                if (name.contentEquals(tpe.getSimpleName()))
                {
                    TypeMirror actualType = dt.getTypeArguments().get(index);
                    return actualType;
                }
                index++;
            }
            type = te.getSuperclass();
        }
        throw new IllegalArgumentException("no actual type for type parameter "+typeParameter+" "+typeParameter.getEnclosingElement());
    }

    public class Method implements ExecutableElement, UpdateableElement
    {
        private ExecutableElement parent;
        private Name name;
        private Set<Modifier> modifiers;
        private List<VariableElement> parameters = new ArrayList<>();
        private TypeMirror returnType;

        public Method(ExecutableElement parent)
        {
            this.parent = parent;
            this.name = parent.getSimpleName();
            this.modifiers = parent.getModifiers();
            for (VariableElement ve : parent.getParameters())
            {
                parameters.add(getVariableFor(ve));
            }
            returnType = getActualType(parent.getReturnType());
        }

        @Override
        public List<? extends TypeParameterElement> getTypeParameters()
        {
            return parent.getTypeParameters();
        }

        @Override
        public TypeMirror getReturnType()
        {
            return returnType;
        }

        @Override
        public List<? extends VariableElement> getParameters()
        {
            return parameters;
        }

        @Override
        public boolean isVarArgs()
        {
            return parent.isVarArgs();
        }

        @Override
        public List<? extends TypeMirror> getThrownTypes()
        {
            return parent.getThrownTypes();
        }

        @Override
        public AnnotationValue getDefaultValue()
        {
            return parent.getDefaultValue();
        }

        @Override
        public Name getSimpleName()
        {
            return name;
        }

        @Override
        public TypeMirror asType()
        {
            return parent.asType();
        }

        @Override
        public ElementKind getKind()
        {
            return parent.getKind();
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors()
        {
            return parent.getAnnotationMirrors();
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType)
        {
            return parent.getAnnotation(annotationType);
        }

        @Override
        public Set<Modifier> getModifiers()
        {
            return modifiers;
        }

        @Override
        public Element getEnclosingElement()
        {
            return SubClass.this;
        }

        @Override
        public List<? extends Element> getEnclosedElements()
        {
            return parent.getEnclosedElements();
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p)
        {
            return parent.accept(v, p);
        }
        
        @Override
        public void setEnclosingElement(Element enclosingElement)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setModifiers(Set<Modifier> modifiers)
        {
            this.modifiers = modifiers;
        }

        @Override
        public void setSimpleName(Name name)
        {
            this.name = name;
        }
        
    }
    public class Variable implements VariableElement, UpdateableElement
    {
        private VariableElement parent;
        private TypeMirror type;
        private Element enclosingElement;
        private Name name;
        private Set<Modifier> modifiers;

        public Variable(VariableElement parent, TypeMirror type)
        {
            this.parent = parent;
            this.enclosingElement = parent.getEnclosingElement();
            this.name = parent.getSimpleName();
            this.modifiers = parent.getModifiers();
            this.type = type;
        }

        @Override
        public Object getConstantValue()
        {
            return parent.getConstantValue();
        }

        @Override
        public TypeMirror asType()
        {
            return type;
        }

        @Override
        public ElementKind getKind()
        {
            return parent.getKind();
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors()
        {
            return parent.getAnnotationMirrors();
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType)
        {
            return parent.getAnnotation(annotationType);
        }

        @Override
        public Set<Modifier> getModifiers()
        {
            return modifiers;
        }

        @Override
        public Name getSimpleName()
        {
            return name;
        }

        @Override
        public Element getEnclosingElement()
        {
            return enclosingElement;
        }

        @Override
        public List<? extends Element> getEnclosedElements()
        {
            return parent.getEnclosedElements();
        }

        @Override
        public <R, P> R accept(ElementVisitor<R, P> v, P p)
        {
            return parent.accept(v, p);
        }

        @Override
        public void setEnclosingElement(Element enclosingElement)
        {
            this.enclosingElement = enclosingElement;
        }

        @Override
        public void setModifiers(Set<Modifier> modifiers)
        {
            this.modifiers = modifiers;
        }

        @Override
        public void setSimpleName(Name name)
        {
            this.name = name;
        }
        
    }
}
