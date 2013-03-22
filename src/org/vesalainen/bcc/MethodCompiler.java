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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.E;
import org.vesalainen.bcc.model.ElementFactory;
import org.vesalainen.bcc.model.T;
import org.vesalainen.bcc.model.UpdateableElement;

/**
 *
 * @author tkv
 */
public class MethodCompiler extends Assembler
{
    public static final String SUBROUTINERETURNADDRESSNAME = "$subroutineReturnAddressName";
    private SubClass subClass;
    private CodeAttribute code;
    private List<VariableElement> localVariables = new ArrayList<>();
    private ExecutableElement debugMethod;
    private boolean compiled;
    private String subroutine;
    private boolean optimize = true;
    private boolean dump;
    private List<ExceptionTable> exceptionTableList = new ArrayList<>();
    private MethodInfo methodInfo;
    private ExecutableElement executableElement;
    private ExecutableType executableType;

    MethodCompiler(SubClass subClass, MethodInfo methodInfo)
    {
        this.subClass = subClass;
        this.methodInfo = methodInfo;
        this.executableElement = methodInfo.getExecutableElement();
        executableType = (ExecutableType) executableElement.asType();
        this.code = methodInfo.getCodeAttribute();
        localVariables.add(new LocalVariable(executableElement, subClass.asType(), "this"));
        for (VariableElement ve : executableElement.getParameters())
        {
            if (ve instanceof UpdateableElement)
            {
                localVariables.add(ve);
            }
            else
            {
                localVariables.add(E.createUpdateableElement(ve));
            }
        }
        methodInfo.setMc(this);
    }

    public ExecutableElement getExecutableElement()
    {
        return executableElement;
    }

    public SubClass getSubClass()
    {
        return subClass;
    }

    public String getModifiers()
    {
        return methodInfo.getModifiersAsString();
    }
    /**
     * Set the compiler optimize flag. Affects switches
     * @param optimize
     */
    public void setOptimize(boolean optimize)
    {
        this.optimize = optimize;
    }

    public String getMethodDescription()
    {
        StringWriter sw = new StringWriter();
        E.printElements(sw, executableElement);
        return sw.toString();
    }

    public List<? extends TypeMirror> getParameters()
    {
        return executableType.getParameterTypes();
    }

    public TypeMirror getReturnType()
    {
        return executableType.getReturnType();
    }

    public int localSize()
    {
        return localSize(0, localVariables.size());
    }

    private int localSize(int offset, int length)
    {
        int size = 0;
        for (int ii=offset;ii<length;ii++)
        {
            TypeMirror type = localVariables.get(ii).asType();
            if (T.isCategory2(type))
            {
                size += 2;
            }
            else
            {
                size++;
            }
        }
        return size;
    }
    /**
     * Names an argument
     * @param name Argument name
     * @param index First argument is 1, this = 0
     */
    public final void nameArgument(String name, int index)
    {
        VariableElement lv = getLocalVariable(index);
        if (lv instanceof UpdateableElement)
        {
            UpdateableElement ue = (UpdateableElement) lv;
            ue.setSimpleName(E.getName(name));
            return;
        }
        else
        {
            throw new IllegalArgumentException("local variable at index "+index+" cannot be named");
        }
    }
    /**
     * Returns local variable at index. Note! long and double take two positions.
     * @param index
     * @return 
     */
    public VariableElement getLocalVariable(int index)
    {
        int idx = 0;
        for (VariableElement lv : localVariables)
        {
            if (idx == index)
            {
                return lv;
            }
            if (T.isCategory2(lv.asType()))
            {
                idx += 2;
            }
            else
            {
                idx++;
            }
        }
        throw new IllegalArgumentException("local variable at index "+index+" not found");
    }
    public VariableElement getLocalVariable(String name)
    {
        for (VariableElement lv : localVariables)
        {
            if (name.contentEquals(lv.getSimpleName()))
            {
                return lv;
            }
        }
        throw new IllegalArgumentException("local variable for name "+name+" not found");
    }
    public int getLocalVariableIndex(String name)
    {
        int idx = 0;
        for (VariableElement lv : localVariables)
        {
            if (name.contentEquals(lv.getSimpleName()))
            {
                return idx;
            }
            if (T.isCategory2(lv.asType()))
            {
                idx += 2;
            }
            else
            {
                idx++;
            }
        }
        throw new IllegalArgumentException("local variable for name "+name+" not found");
    }
    public void nameArguments(String[] args)
    {
        for (int index=0;index<args.length;index++)
        {
            nameArgument(args[index], index);
        }
    }

    /**
     * Returns the name of local variable at index
     * @param index
     * @return
     */
    public String getLocalName(int index)
    {
        VariableElement lv = getLocalVariable(index);
        return lv.getSimpleName().toString();
    }
    /**
     * Returns the type of local variable at index
     * @param index
     * @return
     */
    public TypeMirror getLocalType(int index)
    {
        VariableElement lv = getLocalVariable(index);
        return lv.asType();
    }
    /**
     * returns the type of local variable named name.
     * @param name
     * @return
     */
    public TypeMirror getLocalType(String name)
    {
        VariableElement lv = getLocalVariable(name);
        return lv.asType();
    }
    /**
     * Return true if named variable is added.
     * @param name
     * @return 
     */
    public boolean hasLocalVariable(String name)
    {
        try
        {
            getLocalVariable(name);
            return true;
        }
        catch (IllegalArgumentException ex)
        {
            return false;
        }
    }
    /**
     * return a descriptive text about local variable named name.
     * @param index
     * @return
     */
    public String getLocalDescription(int index)
    {
        StringWriter sw = new StringWriter();
        E.printElements(sw, getLocalVariable(index));
        return sw.toString();
    }
    /**
     * Add new local variable
     * @param name
     * @param type
     */
    public void addVariable(String name, Class<?> type)
    {
        addVariable(name, T.getTypeFor(type));
    }
    /**
     * Add new local variable
     * @param name
     * @param type
     */
    public void addVariable(String name, TypeMirror type)
    {
        localVariables.add(new LocalVariable(executableElement, type, name));
    }
    /**
     * assign default type for local variable depending on type. (false, 0, null)
     * @param name
     * @throws IOException
     */
    public void assignDefault(String name) throws IOException
    {
        TypeMirror t = getLocalType(name);
        if (T.isPrimitive(t))
        {
            tconst(t, 0);
        }
        else
        {
            aconst_null();
        }
        tstore(name);
    }
    /**
     * Load default value to stack depending on type
     * @param type
     * @throws IOException
     */
    public void loadDefault(Class<?> type) throws IOException
    {
        loadDefault(T.getTypeFor(type));
    }
    /**
     * Load default value to stack depending on type
     * @param type
     * @throws IOException
     */
    public void loadDefault(TypeMirror type) throws IOException
    {
        if (type.getKind() != TypeKind.VOID)
        {
            if (T.isPrimitive(type))
            {
                tconst(type, 0);
            }
            else
            {
                aconst_null();
            }
        }
    }
    /**
     * Compiles the subroutine start. Use endSubroutine to end that subroutine.
     * @param target
     * @throws IOException
     */
    public void startSubroutine(String target) throws IOException
    {
        if (subroutine != null)
        {
            throw new IllegalStateException("subroutine "+subroutine+" not ended when "+target+ "started");
        }
        subroutine = target;
        if (!hasLocalVariable(SUBROUTINERETURNADDRESSNAME))
        {
            addVariable(SUBROUTINERETURNADDRESSNAME, T.ReturnAddress);
        }
        fixAddress(target);
        tstore(SUBROUTINERETURNADDRESSNAME);
    }

    public void endSubroutine() throws IOException
    {
        if (subroutine == null)
        {
            throw new IllegalStateException("ending subroutine that is not started");
        }
        int index = getLocalVariableIndex(SUBROUTINERETURNADDRESSNAME);
        ret(index);
        subroutine = null;
    }
    /**
     * Reset a started subroutine
     */
    public void resetSubroutine()
    {
        subroutine = null;
    }

    /**
     * Create new array
     * <p>Stack: ..., count =&gt; ..., arrayref
     * @param type array class eg. String[].class
     * @param count array count
     * @throws IOException
     */
    public void newarray(Class<?> type, int count) throws IOException
    {
        newarray(T.getTypeFor(type), count);
    }
    /**
     * Create new array
     * <p>Stack: ..., count =&gt; ..., arrayref
     * @param type array class eg. String[].class
     * @param count array count
     * @throws IOException
     */
    public void newarray(TypeMirror type, int count) throws IOException
    {
        if (type.getKind() != TypeKind.ARRAY)
        {
            throw new IllegalArgumentException(type+" is not array");
        }
        ArrayType at = (ArrayType) type;
        iconst(count);
        TypeMirror ct = at.getComponentType();
        switch (ct.getKind())
        {
            case BOOLEAN:
                newarray(T_BOOLEAN);
                break;
            case CHAR:
                newarray(T_CHAR);
                break;
            case FLOAT:
                newarray(T_FLOAT);
                break;
            case DOUBLE:
                newarray(T_DOUBLE);
                break;
            case BYTE:
                newarray(T_BYTE);
                break;
            case SHORT:
                newarray(T_SHORT);
                break;
            case INT:
                newarray(T_INT);
                break;
            case LONG:
                newarray(T_LONG);
                break;
            case DECLARED:
            {
                DeclaredType dt = (DeclaredType) ct;
                int index = subClass.resolveClassIndex((TypeElement)dt.asElement());
                anewarray(index);
            }
                break;
            default:
                throw new IllegalArgumentException(type+" not supported");
        }
    }
    /**
     * Return a integral class able to support count values
     * @param count
     * @return
     */
    public TypeMirror typeForCount(int count)
    {
        if (count <= Byte.MAX_VALUE)
        {
            return T.Byte;
        }
        if (count <= Short.MAX_VALUE)
        {
            return T.Short;
        }
        if (count <= Integer.MAX_VALUE)
        {
            return T.Int;
        }
        return T.Long;
    }
    /**
     * Load from local variable
     * <p>Stack: ... =&gt; ..., value
     * @param name local variable name
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     */
    public void tload(String name) throws IOException
    {
        TypeMirror cn = getLocalType(name);
        int index = getLocalVariableIndex(name);
        if (T.isPrimitive(cn))
        {
            super.tload(cn, index);
        }
        else
        {
            super.aload(index);
        }
    }
    /**
     * Store into local variable
     * <p>Stack: ..., value =&gt; ...
     * @param name local variable name
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     */
    public void tstore(String name) throws IOException
    {
        TypeMirror cn = getLocalType(name);
        int index = getLocalVariableIndex(name);
        if (T.isPrimitive(cn))
        {
            tstore(cn, index);
        }
        else
        {
            astore(index);
        }
    }
    /**
     * Increment local variable by constant
     * <p>Stack: No change
     * @param name local variable name
     * @param con signed byte
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     */
    public void tinc(String name, int con) throws IOException
    {
        TypeMirror cn = getLocalType(name);
        int index = getLocalVariableIndex(name);
        tinc(cn, index, con);
    }
    /**
     * Return. If return type is other than void the value at stack is returned
     * <p>Stack: ..., result =&gt; ...
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     */
    public void treturn() throws IOException
    {
        treturn(executableElement.getReturnType());
    }

    public void setDebug(ExecutableElement debug)
    {
        this.debugMethod = debug;
    }
    /**
     * Labels a current position
     * @param name
     * @throws IOException
     */
    @Override
    public void fixAddress(String name) throws IOException
    {
        super.fixAddress(name);
        if (debugMethod != null)
        {
            int position = position();
            tload("this");
            ldc(position);
            ldc(name.toString());
            invokevirtual(debugMethod);
        }
    }
    /**
     * Create new object
     * <p>Stack: ... =&gt; ..., objectref
     * @param type class eg. String.class
     * @throws IOException
     */
    public void anew(Class<?> clazz) throws IOException
    {
        anew(E.getTypeElement(clazz.getCanonicalName()));
    }
    /**
     * Create new object
     * <p>Stack: ... =&gt; ..., objectref
     * @param type class eg. String.class
     * @throws IOException
     */
    public void anew(TypeElement clazz) throws IOException
    {
        int index = subClass.resolveClassIndex(clazz);
        anew(index);
    }

    public void tconst(Object value) throws IOException
    {
        if (value instanceof Integer)
        {
            iconst((Integer)value);
        }
        else
        if (value instanceof Boolean)
        {
            iconst((Boolean)value);
        }
        else
        if (value instanceof Character)
        {
            iconst((Character)value);
        }
        else
        if (value instanceof Byte)
        {
            iconst((Byte)value);
        }
        else
        if (value instanceof Long)
        {
            lconst((Long)value);
        }
        else
        if (value instanceof Float)
        {
            fconst((Float)value);
        }
        else
        if (value instanceof Double)
        {
            dconst((Double)value);
        }
        else
        if (value instanceof String)
        {
            ldc((String)value);
        }
        else
        {
            throw new IllegalArgumentException(value+" is not a java constant type");
        }
    }
    public void tconst(int value) throws IOException
    {
        iconst(value);
    }
    public void tconst(boolean value) throws IOException
    {
        iconst(value);
    }
    public void tconst(char value) throws IOException
    {
        iconst(value);
    }
    public void tconst(byte value) throws IOException
    {
        iconst(value);
    }
    public void tconst(long value) throws IOException
    {
        lconst(value);
    }
    public void tconst(float value) throws IOException
    {
        fconst(value);
    }
    public void tconst(double value) throws IOException
    {
        dconst(value);
    }
    public void tconst(String value) throws IOException
    {
        ldc(value);
    }
    /**
     * Push item from runtime constant pool.
     * Creates new constant if needed
     * <p>Stack: ... =&gt; ..., value
     * @param constant int constant
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     * @throws IOException
     */
    @Override
    public void ldc(int constant) throws IOException
    {
        int index = subClass.resolveConstantIndex(constant);
        super.ldc(index);
    }
    /**
     * Push item from runtime constant pool.
     * Creates new constant if needed
     * <p>Stack: ... =&gt; ..., value
     * @param constant float constant
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     * @throws IOException
     */
    public void ldc(float constant) throws IOException
    {
        int index = subClass.resolveConstantIndex(constant);
        super.ldc(index);
    }
    /**
     * Push item from runtime constant pool.
     * Creates new constant if needed
     * <p>Stack: ... =&gt; ..., value
     * @param constant long constant
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     * @throws IOException
     */
    public void ldc(long constant) throws IOException
    {
        int index = subClass.resolveConstantIndex(constant);
        super.ldc2_w(index);
    }
    /**
     * Push item from runtime constant pool.
     * Creates new constant if needed
     * <p>Stack: ... =&gt; ..., value
     * @param constant double constant
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     * @throws IOException
     */
    public void ldc(double constant) throws IOException
    {
        int index = subClass.resolveConstantIndex(constant);
        super.ldc2_w(index);
    }
    /**
     * Push item from runtime constant pool.
     * Creates new constant if needed
     * <p>Stack: ... =&gt; ..., value
     * @param constant String constant
     * @throws IOException
     * @see nameArgument
     * @see addVariable
     * @throws IOException
     */
    public void ldc(String constant) throws IOException
    {
        int index = subClass.resolveConstantIndex(constant);
        super.ldc(index);
    }
    /**
     * Fetch field from object
     * <p>Stack: ..., objectref =&gt; ..., value
     * @param field
     * @throws IOException
     */
    public void getField(VariableElement field) throws IOException
    {
        if (field.getModifiers().contains(Modifier.STATIC))
        {
            throw new IllegalArgumentException(field+" is static");
        }
        int index = subClass.resolveFieldIndex(field);
        getfield(index);
    }
    /**
     * Fetch field from object
     * <p>Stack: ..., objectref =&gt; ..., value
     * @param field
     * @throws IOException
     */
    public void getStaticField(VariableElement field) throws IOException
    {
        if (!field.getModifiers().contains(Modifier.STATIC))
        {
            throw new IllegalArgumentException(field+" is not static");
        }
        int index = subClass.resolveFieldIndex(field);
        getstatic(index);
    }
    /**
     * Get field from class
     * <p>Stack: ..., =&gt; ..., value
     * @param field
     * @throws IOException
     */
    public void getField(Class<?> cls, String name) throws IOException
    {
        getField(E.getField(cls, name));
    }
    /**
     * Get field from class
     * <p>Stack: ..., =&gt; ..., value
     * @param field
     * @throws IOException
     */
    public void getStaticField(Class<?> cls, String name) throws IOException
    {
        getStaticField(E.getField(cls, name));
    }
    /**
     * Set field in object
     * <p>Stack: ..., objectref, value =&gt; ...
     */
    public void putField(VariableElement field) throws IOException
    {
        if (field.getModifiers().contains(Modifier.STATIC))
        {
            throw new IllegalArgumentException(field+" is static");
        }
        int index = subClass.resolveFieldIndex(field);
        putfield(index);
    }
    /**
     * Set static field in object
     * <p>Stack: ..., objectref, value =&gt; ...
     */
    public void putStaticField(VariableElement field) throws IOException
    {
        if (!field.getModifiers().contains(Modifier.STATIC))
        {
            throw new IllegalArgumentException(field+" is not static");
        }
        int index = subClass.resolveFieldIndex(field);
        putstatic(index);
    }
    /**
     * Set field in class
     * <p>Stack: ..., value =&gt; ...
     * @param field
     * @throws IOException
     */
    public void putField(Class<?> cls, String name) throws IOException
    {
        putField(E.getField(cls, name));
    }
    /**
     * Set field in class
     * <p>Stack: ..., value =&gt; ...
     * @param field
     * @throws IOException
     */
    public void putStaticField(Class<?> cls, String name) throws IOException
    {
        putStaticField(E.getField(cls, name));
    }
    /**
     * @param constructor
     * @throws IOException 
     */
    public void invokeConstructor(Class<?> cls, Class<?>... parameters) throws IOException
    {
        invoke(E.getConstructor(cls, parameters));
    }
    /**
     * @param method
     * @throws IOException 
     */
    public void invokeMethod(Class<?> cls, String name, Class<?>... parameters) throws IOException
    {
        invoke(E.getMethod(cls, name, parameters));
    }
    /**
     * @param method
     * @throws IOException 
     */
    public void invoke(ExecutableElement method) throws IOException
    {
        switch (method.getKind())
        {
            case CONSTRUCTOR:
                invokespecial(method);
                break;
            case METHOD:
                if (method.getModifiers().contains(Modifier.PRIVATE))
                {
                    invokespecial(method);
                }
                else
                {
                    if (method.getModifiers().contains(Modifier.STATIC))
                    {
                        invokestatic(method);
                    }
                    else
                    {
                        invokevirtual(method);
                    }
                }
                break;
        }
    }
    /**
     * Invoke instance method; special handling for superclass, private, and instance initialization method invocations
     * <p>Stack: ..., objectref, [arg1, [arg2 ...]] =&gt; ...
     * @param fullyQualifiedName in fully qualified form
     * @param methodName
     * @param isInterface
     * @param returnType
     * @param parameters
     * @throws IOException
     */
    public void invokespecial(Class<?> cls, String name, Class<?>... parameters) throws IOException
    {
        invokespecial(E.getMethod(cls, name, parameters));
    }

    /**
     * Invoke instance method; special handling for superclass, private, and instance initialization method invocations
     * <p>Stack: ..., objectref, [arg1, [arg2 ...]] =&gt; ...
     * @param fullyQualifiedName in fully qualified form
     * @param methodName
     * @param isInterface
     * @param returnType
     * @param parameters
     * @throws IOException
     */
    public void invokespecial(ExecutableElement method) throws IOException
    {
        int index = subClass.resolveMethodIndex(method);
        invokespecial(index);
    }

    /**
     * Invoke instance method; dispatch based on classIf method is interface
     * calls invokeinterface otherwise calls invokevirtual.
     * <p>Stack: ..., objectref, [arg1, [arg2 ...]] =&gt; ...
     * @param fullyQualifiedName
     * @param methodName
     * @param isInterface
     * @param returnType
     * @param parameters
     * @throws IOException
     */
    public void invokevirtual(Class<?> cls, String name, Class<?>... parameters) throws IOException
    {
        invokevirtual(E.getMethod(cls, name, parameters));
    }
    /**
     * Invoke instance method; dispatch based on classIf method is interface
     * calls invokeinterface otherwise calls invokevirtual.
     * <p>Stack: ..., objectref, [arg1, [arg2 ...]] =&gt; ...
     * @param fullyQualifiedName
     * @param methodName
     * @param isInterface
     * @param returnType
     * @param parameters
     * @throws IOException
     */
    public void invokevirtual(ExecutableElement method) throws IOException
    {
        int index = subClass.resolveMethodIndex(method);
        if (method.getEnclosingElement().getKind() == ElementKind.INTERFACE)
        {
            invokeinterface(index, argumentCount(method.getParameters()));
        }
        else
        {
            invokevirtual(index);
        }
    }
    /**
     * Invoke a class (static) method
     * <p>Stack: ..., [arg1, [arg2 ...]] =&gt; ...
     * @param method
     * @throws IOException
     */
    public void invokestatic(Class<?> cls, String name, Class<?>... parameters) throws IOException
    {
        invokestatic(E.getMethod(cls, name, parameters));
    }

    /**
     * Invoke a class (static) method
     * <p>Stack: ..., [arg1, [arg2 ...]] =&gt; ...
     * @param method
     * @throws IOException
     */
    public void invokestatic(ExecutableElement method) throws IOException
    {
        int index = subClass.resolveMethodIndex(method);
        invokestatic(index);
    }

    private int argumentCount(List<? extends VariableElement> parameters)
    {
        int result = 0;
        for (VariableElement parameter : parameters)
        {
            if (T.isCategory2(parameter.asType()))
            {
                result += 2;
            }
            else
            {
                result++;
            }
        }
        return result;
    }
    /**
     * Ends the compilation. This must be the last compile method for a MethodCompiler.
     * @throws IOException
     */
    public void end() throws IOException
    {
        assert !compiled;
        compiled =true;
        byte[] bb = getCode();
        if (bb.length > 0xfffe)
        {
            throw new IllegalArgumentException("code size "+bb.length+" > 65534");
        }
        try
        {
            fixLabels(bb);
        }
        catch (BranchException ex)
        {
            ByteCodeDump bcd = new ByteCodeDump(bb, subClass);
            bcd.print(this);
            throw ex;
        }
        if (dump)
        {
            ByteCodeDump bcd = new ByteCodeDump(bb, subClass);
            bcd.print(this);
        }
        ExceptionTable[] exceptionTable = exceptionTableList.toArray(new ExceptionTable[exceptionTableList.size()]);
        ByteCodeVerifier ver = new ByteCodeVerifier(bb, exceptionTable, subClass, this);
        ver.verify();
        code.setCode(bb, exceptionTable);
        code.setMax_locals(localVariables.size()+1);
        code.setMax_stack(ver.getMaxStack());
        code.addLocalVariables(localVariables);
    }
    @Override
    public String toString()
    {
        try
        {
            return "line=" + position();
        }
        catch (IOException ex)
        {
            return ex.getMessage();
        }
    }
    /**
     * Push int constant
     * <p>Stack: ..., =&gt; ..., i
     * @param value
     * @throws IOException
     */
    public void iconst(boolean value) throws IOException
    {
        if (value)
        {
            super.iconst(1);
        }
        else
        {
            super.iconst(0);
        }
    }
    /**
     * Push int constant. Uses either iconst or ldc.
     * <p>Stack: ..., =&gt; ..., i
     * @param value
     * @throws IOException
     */
    @Override
    public void iconst(int value) throws IOException
    {
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE)
        {
            super.iconst(value);
        }
        else
        {
            ldc(value);
        }
    }
    /**
     * Push float constant. Uses either fconst or ldc.
     * <p>Stack: ..., =&gt; ..., f
     * @param value
     * @throws IOException
     */
    public void fconst(float value) throws IOException
    {
        if (value == 0 || value == 1 || value == 2)
        {
            super.fconst((int) value);
        }
        else
        {
            ldc(value);
        }
    }
    /**
     * Push long constant. Uses either lconst or ldc.
     * <p>Stack: ..., =&gt; ..., l
     * @param value
     * @throws IOException
     */
    public void lconst(long value) throws IOException
    {
        if (value == 0 || value == 1)
        {
            super.lconst((int) value);
        }
        else
        {
            ldc(value);
        }
    }
    /**
     * Push double constant. Use either dconst or ldc.
     * <p>Stack: ..., =&gt; ..., d
     * @param value
     * @throws IOException
     */
    public void dconst(double value) throws IOException
    {
        if (value == 0 || value == 1)
        {
            super.dconst((int) value);
        }
        else
        {
            ldc(value);
        }
    }
    /**
     * Compiles a tableswitch, lookupswitch or if/goto depending on contents of
     * LookupList.
     * <p>Stack: ..., index =&gt; ...,
     * <p>throws SwitchException if target illegal.
     * @param list
     * @throws IOException
     * @throws NoSuchMethodException
     */
    public void optimizedSwitch(LookupList list) throws IOException, NoSuchMethodException
    {
        String def = createBranch();
        optimizedSwitch(def, list);
        fixAddress(def);
        anew(SwitchException.class);
        dup();
        invokeConstructor(SwitchException.class);
        athrow();
    }
    /**
     * Compiles a tableswitch, lookupswitch or if/goto depending on contents of
     * LookupList.
     * <p>Stack: ..., index =&gt; ...,
     * @param def
     * @param list
     * @throws IOException
     * @throws NoSuchMethodException
     */
    public void optimizedSwitch(String def, LookupList list) throws IOException, NoSuchMethodException
    {
        if (list.isEmpty())
        {
            throw new IllegalArgumentException("empty lookuplist");
        }
        if (list.size() == 1)
        {
            for (LookupPair lp : list)
            {
                iconst(lp.getMatch());
                if_icmpeq(lp.getTarget());
            }
            goto_n(def);
        }
        else
        {
            if (optimize && list.canOptimize())
            {
                for (LookupPair lp : list)
                {
                    String next = createBranch();
                    dup();
                    iconst(lp.getMatch());
                    if_icmpne(next);
                    pop();
                    goto_n(lp.getTarget());
                    fixAddress(next);
                }
                pop();
                goto_n(def);
            }
            else
            {
                if (list.isContiguous())    // note! list is sorted at this point
                {
                    String[] symbols = new String[list.size()];
                    int index = 0;
                    for (LookupPair lp : list)
                    {
                        symbols[index++] = lp.getTarget();
                    }
                    tableswitch(def, list.get(0).getMatch(), list.get(list.size()-1).getMatch(), symbols);
                }
                else
                {
                    lookupswitch(def, list);
                }
            }
        }
    }
    /**
     * Compiles lookupswitch
     * <p>Stack: ..., index =&gt; ...,
     * @param list
     * @throws IOException
     * @throws NoSuchMethodException
     */
    public void lookupswitch(LookupList list) throws IOException, NoSuchMethodException
    {
        String def = createBranch();
        lookupswitch(def, list);
        fixAddress(def);
        anew(SwitchException.class);
        dup();
        invokeConstructor(SwitchException.class);
        athrow();
    }
    /**
     * Compiles tableswitch
     * <p>Stack: ..., index =&gt; ...,
     * @param low
     * @param high
     * @param symbols
     * @throws IOException
     * @throws NoSuchMethodException
     */
    public void tableswitch(int low, int high, List<String> symbols) throws IOException, NoSuchMethodException
    {
        tableswitch(low, high, symbols.toArray(new String[symbols.size()]));
    }
    /**
     * Compiles tableswitch
     * <p>Stack: ..., index =&gt; ...,
     * @param low
     * @param high
     * @param symbols
     * @throws IOException
     * @throws NoSuchMethodException
     */
    public void tableswitch(int low, int high, String... symbols) throws IOException, NoSuchMethodException
    {
        String def = createBranch();
        tableswitch(def, low, high, symbols);
        fixAddress(def);
        anew(SwitchException.class);
        dup();
        invokeConstructor(SwitchException.class);
        athrow();
    }
    /**
     * Create new array
     * <p>Stack: ..., count =&gt; ..., arrayref
     * @param name
     * @param aClass
     * @param size
     * @throws IOException
     */
    public void addNewArray(String name, Class<?> aClass, int size) throws IOException
    {
        addNewArray(name, T.getTypeFor(aClass), size);
    }
    /**
     * Create new array
     * <p>Stack: ..., count =&gt; ..., arrayref
     * @param name
     * @param type
     * @param size
     * @throws IOException
     */
    public void addNewArray(String name, TypeMirror type, int size) throws IOException
    {
        if (type.getKind() != TypeKind.ARRAY)
        {
            throw new IllegalArgumentException(type+" is not array");
        }
        ArrayType at = (ArrayType) type;
        addVariable(name, at);
        newarray(type, size);
        if (!T.isPrimitive(at.getComponentType()))
        {
            checkcast(type);
        }
        tstore(name);
    }
    /**
     * Check whether object is of given type
     * <p>Stack: ..., objectref =&gt; ..., objectref
     * @param objectref
     * @throws IOException
     */
    public void checkcast(Class<?> objectref) throws IOException
    {
        checkcast(T.getTypeFor(objectref));
    }
    /**
     * Check whether object is of given type
     * <p>Stack: ..., objectref =&gt; ..., objectref
     * @param objectref
     * @throws IOException
     */
    public void checkcast(TypeMirror objectref) throws IOException
    {
        if (objectref.getKind() != TypeKind.DECLARED)
        {
            throw new IllegalArgumentException("checkcast type must be objectref");
        }
        DeclaredType dt = (DeclaredType) objectref;
        int index = subClass.resolveClassIndex((TypeElement)dt.asElement());
        checkcast(index);
    }

    /**
     * @deprecated This is not tested!
     * Convert a local variable to given type. (if it is possible)
     * <p>Stack: ..., =&gt; ..., objectref
     * @param fromVariable
     * @param to
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    public void convert(String fromVariable, TypeMirror to) throws IOException, NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, IllegalConversionException
    {
        TypeMirror from = getLocalType(fromVariable);
        if (T.isSameType(from, to))
        {
            tload(fromVariable);
            return;
        }
        if (to.getKind() == TypeKind.DECLARED)
        {
            DeclaredType dt = (DeclaredType) to;
            TypeElement te = (TypeElement) dt.asElement();
            ExecutableElement c = E.getConstructor(te, from);
            if (c != null)
            {
                anew(te);
                dup();
                tload(fromVariable);
                invokespecial(c);
                return;
            }
            ExecutableElement m = E.getMethod(te, "valueOf", from);
            if (m != null && m.getModifiers().contains(Modifier.STATIC) && T.isAssignable(m.getReturnType(), to))
            {
                tload(fromVariable);
                invokestatic(m);
                return;
            }
        }
        if (from.getKind() == TypeKind.DECLARED && T.isPrimitive(to))
        {
            DeclaredType dt = (DeclaredType) from;
            TypeElement te = (TypeElement) dt.asElement();
            String str = to.getKind().name().toLowerCase() + "Value";
            ExecutableElement m = E.getMethod(te, str);
            // xxxValue()
            // xxxValue()
            if (m != null && T.isAssignable(m.getReturnType(), to))
            {
                tload(fromVariable);
                invokevirtual(m);
                return;
            }
        }
        if (T.isPrimitive(from))
        {
            switch (from.getKind())
            {
                case BYTE:
                case CHAR:
                case SHORT:
                case INT:
                    switch (to.getKind())
                    {
                        case BYTE:
                        case CHAR:
                        case SHORT:
                        case INT:
                        case LONG:
                        case FLOAT:
                        case DOUBLE:
                            tload(fromVariable);
                            i2t();
                            break;
                        default:
                            throw new IllegalConversionException("Cannot convert from " + from + " to " + to);
                    }
                    break;
                case LONG:
                    switch (to.getKind())
                    {
                        case BYTE:
                        case CHAR:
                        case SHORT:
                        case INT:
                            tload(fromVariable);
                            l2t();
                            break;
                        case FLOAT:
                        case DOUBLE:
                            tload(fromVariable);
                            l2t();
                            break;
                        default:
                            throw new IllegalConversionException("Cannot convert from " + from + " to " + to);
                    }
                    break;
                case FLOAT:
                    switch (to.getKind())
                    {
                        case BYTE:
                        case CHAR:
                        case SHORT:
                        case INT:
                            tload(fromVariable);
                            f2t();
                            break;
                        case LONG:
                        case DOUBLE:
                            tload(fromVariable);
                            f2t();
                            break;
                        default:
                            throw new IllegalConversionException("Cannot convert from " + from + " to " + to);
                    }
                    break;
                case DOUBLE:
                    switch (to.getKind())
                    {
                        case BYTE:
                        case CHAR:
                        case SHORT:
                        case INT:
                            tload(fromVariable);
                            d2t();
                            break;
                        case LONG:
                        case FLOAT:
                            tload(fromVariable);
                            d2t();
                            break;
                        default:
                            throw new IllegalConversionException("Cannot convert from " + from + " to " + to);
                    }
                    break;
                default:
                    throw new IllegalConversionException("Cannot convert from " + from + " to " + to);
            }
        }
    }

    /**
     * Adds an exception handler. If one of catchTypes is thrown inside block the
     * execution continues at handler address. Thrown object is pushed in stack.
     * 
     * @param block 
     * @param handler
     * @param catchTypes Throwable objects which are caught. If none is present then 
     * all throwables are caught. Note! This is not the same as finally!
     */
    public void addExceptionHandler(Block block, String handler, Class<? extends Throwable> catchType)
    {
        if (catchType != null)
        {
            exceptionTableList.add(new ExceptionTable(block, getLabel(handler), subClass.resolveClassIndex(catchType)));
        }
        else
        {
            exceptionTableList.add(new ExceptionTable(block, getLabel(handler), 0));
        }
    }
}
