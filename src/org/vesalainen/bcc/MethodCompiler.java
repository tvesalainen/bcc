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
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.vesalainen.bcc.type.Descriptor;
import org.vesalainen.bcc.type.Generics;
import org.vesalainen.bcc.type.Signature;

/**
 *
 * @author tkv
 */
public class MethodCompiler extends Assembler
{
    public static final String SUBROUTINERETURNADDRESSNAME = "$subroutineReturnAddressName";
    private SubClass subClass;
    private CodeAttribute code;
    private List<Type> localVariables = new ArrayList<>();
    private Type[] parameters;
    private Type returnType;
    private String methodName;
    private Map<String,Integer> localIndexMap = new HashMap<>();
    private Map<Integer,String> localNameMap = new HashMap<>();
    private Map<String,Type> localClassMap = new HashMap<>();
    private Method debug;
    private boolean compiled;
    private String subroutine;
    private boolean optimize = true;
    private int modifier;
    private boolean dump;

    public MethodCompiler(SubClass subClass, int modifier, CodeAttribute codeAttribute, Type[] parameters, Type returnType, String name)
    {
        this.subClass = subClass;
        this.modifier = modifier;
        this.code = codeAttribute;
        this.parameters = parameters;
        this.returnType = returnType;
        this.methodName = name;
        localVariables.add(subClass.getClassName());
        nameArgument("this", 0);
        localVariables.addAll(Arrays.asList(parameters));
    }

    public SubClass getSubClass()
    {
        return subClass;
    }

    public int getModifier()
    {
        return modifier;
    }
    /**
     * Set the compiler optimize flag. Affects switches
     * @param optimize
     */
    public void setOptimize(boolean optimize)
    {
        this.optimize = optimize;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public Type[] getParameters()
    {
        return parameters;
    }

    public Type getReturnType()
    {
        return returnType;
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
            Type type = localVariables.get(ii);
            if (Generics.isCategory2(type))
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
        if (index < localVariables.size())
        {
            int ls = localSize(0, index);
            String oldName = localNameMap.put(ls, name);
            if (oldName != null)
            {
                localIndexMap.remove(oldName);
                localClassMap.remove(oldName);
            }
            Integer idx = localIndexMap.put(name, ls);
            if (idx != null && idx > 0)
            {
                throw new IllegalArgumentException(name+" has already been set");
            }
            Type cls = localClassMap.put(name, localVariables.get(index));
            assert cls == null || idx == 0;
        }
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
        String name = localNameMap.get(index);
        if (name == null)
        {
            throw new IllegalArgumentException("Variable "+name+" not found");
        }
        return name;
    }
    /**
     * Returns the type of local variable at index
     * @param index
     * @return
     */
    public Type getLocalType(int index)
    {
        String name = getLocalName(index);
        if (name != null)
        {
            return localClassMap.get(name);
        }
        return null;
    }
    /**
     * returns the type of local variable named name.
     * @param name
     * @return
     */
    public Type getLocalType(String name)
    {
        Type type = localClassMap.get(name);
        if (type == null)
        {
            throw new IllegalArgumentException("Variable "+name+" not found");
        }
        return type;
    }
    /**
     * Return true if named variable is added.
     * @param name
     * @return 
     */
    public boolean hasLocalVariable(String name)
    {
        return localClassMap.containsKey(name);
    }
    /**
     * return a descriptive text about local variable named name.
     * @param index
     * @return
     */
    public String getLocalDescription(int index)
    {
        String name = localNameMap.get(index);
        if (name != null)
        {
            Type cls = localClassMap.get(name);
            return name+" "+cls;
        }
        return "?";
    }
    /**
     * Add new local variable
     * @param name
     * @param type
     */
    public void addVariable(String name, Type type)
    {
        if (Generics.isPrimitive(type))
        {
            Type boxed = Generics.getPrimitiveType(type);
            if (boxed != null)
            {
                type = boxed;
            }
        }
        int ls = localSize();
        localIndexMap.put(name, ls);
        localNameMap.put(ls, name);
        localClassMap.put(name, type);
        localVariables.add(type);
    }
    /**
     * assign default type for local variable depending on type. (false, 0, null)
     * @param name
     * @throws IOException
     */
    public void assignDefault(String name) throws IOException
    {
        Type cls = localClassMap.get(name);
        if (Generics.isPrimitive(cls))
        {
            tconst(Generics.getPrimitiveType(cls), 0);
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
    public void loadDefault(Type type) throws IOException
    {
        if (!Generics.isVoid(type))
        {
            if (Generics.isPrimitive(type))
            {
                tconst(Generics.getPrimitiveType(type), 0);
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
            addVariable(SUBROUTINERETURNADDRESSNAME, ReturnAddress.class);
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
        int index = localIndexMap.get(SUBROUTINERETURNADDRESSNAME);
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
    public void newarray(Type type, int count) throws IOException
    {
        if (!Generics.isArray(type))
        {
            throw new IllegalArgumentException(type+" is not array");
        }
        iconst(count);
        ObjectType ot = ObjectType.valueOf(Generics.getComponentType(type));
        switch (ot)
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
            case REF:
            {
                int index = subClass.resolveClassIndex(Generics.getComponentType(type));
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
    public ObjectType typeForCount(int count)
    {
        if (count <= Byte.MAX_VALUE)
        {
            return ObjectType.BYTE;
        }
        if (count <= Short.MAX_VALUE)
        {
            return ObjectType.SHORT;
        }
        if (count <= Integer.MAX_VALUE)
        {
            return ObjectType.INT;
        }
        return ObjectType.LONG;
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
        check(name);
        Type cn = localClassMap.get(name);
        if (Generics.isPrimitive(cn))
        {
            super.tload(Generics.getPrimitiveType(cn), localIndexMap.get(name));
        }
        else
        {
            super.aload(localIndexMap.get(name));
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
        check(name);
        Type cn = localClassMap.get(name);
        if (Generics.isPrimitive(cn))
        {
            tstore(Generics.getPrimitiveType(cn), localIndexMap.get(name));
        }
        else
        {
            astore(localIndexMap.get(name));
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
        check(name);
        tinc(Generics.getPrimitiveType(localClassMap.get(name)), localIndexMap.get(name), con);
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
        treturn(returnType);
    }

    public void setDebug(Method debug)
    {
        this.debug = debug;
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
        if (debug != null)
        {
            int position = position();
            tload("this");
            ldc(position);
            ldc(name.toString());
            invokevirtual(debug);
        }
    }
    /**
     * Create new object
     * <p>Stack: ... =&gt; ..., objectref
     * @param type class eg. String.class
     * @throws IOException
     */
    public void anew(Type clazz) throws IOException
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
    public void getfield(Member field) throws IOException
    {
        int index = subClass.resolveFieldIndex(field);
        getfield(index);
    }
    /**
     * Get static field from class
     * <p>Stack: ..., =&gt; ..., value
     * @param field
     * @throws IOException
     */
    public void getstatic(Member field) throws IOException
    {
        int index = subClass.resolveFieldIndex(field);
        getstatic(index);
    }
    /**
     * Get field from class
     * <p>Stack: ..., =&gt; ..., value
     * @param field
     * @throws IOException
     */
    public void get(Member field) throws IOException
    {
        if (Modifier.isStatic(field.getModifiers()))
        {
            getstatic(field);
        }
        else
        {
            getfield(field);
        }
    }
    /**
     * Set field in object
     * <p>Stack: ..., objectref, value =&gt; ...
     */
    public void putfield(Member field) throws IOException
    {
        int index = subClass.resolveFieldIndex(field);
        putfield(index);
    }
    /**
     * Set static field in class
     * <p>Stack: ..., value =&gt; ...
     * @param field
     * @throws IOException
     */
    public void putstatic(Member field) throws IOException
    {
        int index = subClass.resolveFieldIndex(field);
        putstatic(index);
    }
    /**
     * Set field in class
     * <p>Stack: ..., value =&gt; ...
     * @param field
     * @throws IOException
     */
    public void put(Member field) throws IOException
    {
        if (Modifier.isStatic(field.getModifiers()))
        {
            putstatic(field);
        }
        else
        {
            putfield(field);
        }
    }
    /**
     * If method is ConstructorWrapper calls invokespecial, if method is private 
     * calls invokespecial, if static calls invokestatic, otherwice calls invokevirtual
     * @param method
     * @throws IOException 
     */
    public void invoke(Member method) throws IOException
    {
        if (Generics.isConstructor(method))
        {
            invokespecial(method);
        }
        else
        {
            if (Modifier.isPrivate(method.getModifiers()))
            {
                invokespecial(method);
            }
            else
            {
                if (Modifier.isStatic(method.getModifiers()))
                {
                    invokestatic(method);
                }
                else
                {
                    invokevirtual(method);
                }
            }
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
    public void invokespecial(Member method) throws IOException
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
    public void invokevirtual(Member method) throws IOException
    {
        int index = subClass.resolveMethodIndex(method);
        if (Generics.isInterfaceMethod(method))
        {
            invokeinterface(index, argumentCount(Generics.getParameterTypes(method)));
        }
        else
        {
            invokevirtual(index);
        }
    }
    public void invokevirtual(Type clazz, String name, Type... parameters) throws IOException
    {
        invokevirtual(findMethod(clazz, name, parameters));
    }
    public Member findMethod(Type clazz, String name, Type... parameters) throws IOException
    {
        for (Member method : Generics.getMethods(clazz))
        {
            if (name.equals(Generics.getName(method)))
            {
                Type[] params = Generics.getParameterTypes(method);
                if (params.length == parameters.length)
                {
                    boolean ok = true;
                    for (int ii=0;ii<params.length;ii++)
                    {
                        if (!Generics.isAssignableFrom(params[ii], parameters[ii]))
                        {
                            ok = false;
                            continue;
                        }
                    }
                    if (ok)
                    {
                        return method;
                    }
                }
            }
        }
        throw new IOException("method "+name+" int "+clazz+" not found");
    }

    /**
     * Invoke a class (static) method
     * <p>Stack: ..., [arg1, [arg2 ...]] =&gt; ...
     * @param method
     * @throws IOException
     */
    public void invokestatic(Member method) throws IOException
    {
        int index = subClass.resolveMethodIndex(method);
        invokestatic(index);
    }

    private int argumentCount(Type... parameters)
    {
        int result = 0;
        for (Type parameter : parameters)
        {
            if (category1(parameter))
            {
                result++;
            }
            else
            {
                result += 2;
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
        ByteCodeVerifier ver = new ByteCodeVerifier(bb, subClass, this);
        ver.verify();
        code.setCode(bb);
        code.setMax_locals(localVariables.size()+1);
        code.setMax_stack(ver.getMaxStack());

        int ani = subClass.resolveNameIndex("LocalVariableTable");
        LocalVariableTable lvt = new LocalVariableTable(subClass, ani, bb.length);
        ani = subClass.resolveNameIndex("LocalVariableTypeTable");
        LocalVariableTypeTable lvtt = new LocalVariableTypeTable(subClass, ani, bb.length);
        int ii = 0;
        for (Type type : localVariables)
        {
            if (!(type instanceof ReturnAddress))
            {
                String n = localNameMap.get(ii);
                if (n != null)
                {
                    int index = localIndexMap.get(n);
                    int ni = subClass.resolveNameIndex(n);
                    int di = 0;
                    if (ii == 0)
                    {
                        di = subClass.getThisDescriptorIndex();
                    }
                    else
                    {
                        String descriptor = Descriptor.getFieldDesriptor(type);
                        di = subClass.resolveNameIndex(descriptor);
                    }
                    lvt.addLocalVariable(ni, di, index);
                    if (Signature.needsSignature(type))
                    {
                        String signature = Signature.getFieldSignature(type);
                        int si = subClass.resolveNameIndex(signature);
                        lvtt.addLocalTypeVariable(ni, si, index);
                    }
                }
            }
            ii++;
        }
        code.addAttribute(lvt);
        if (!lvtt.isEmpty())
        {
            code.addAttribute(lvtt);
        }
        //ani = subClass.resolveNameIndex("Synthetic");
        //SyntheticAttribute sa = new SyntheticAttribute(ani);
        //code.addAttribute(sa);
    }
    /**
     * Load a local variable and convert it to reference, or leave as it is if
     * it is not primitive. Eg. int becomes Integer.
     * <p>Stack: ..., =&gt; ..., value
     * @param fromVariable
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    public void convertToReference(String fromVariable) throws IOException, NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, IllegalConversionException
    {
        Type from = this.getLocalType(fromVariable);
        if (Generics.isPrimitive(from))
        {
            ObjectType ot = ObjectType.valueOf(from);
            switch (ot)
            {
                case BOOLEAN:
                    convert(fromVariable, Boolean.class);
                    break;
                case BYTE:
                    convert(fromVariable, Byte.class);
                    break;
                case CHAR:
                    convert(fromVariable, Character.class);
                    break;
                case SHORT:
                    convert(fromVariable, Short.class);
                    break;
                case INT:
                    convert(fromVariable, Integer.class);
                    break;
                case LONG:
                    convert(fromVariable, Long.class);
                    break;
                case FLOAT:
                    convert(fromVariable, Float.class);
                    break;
                case DOUBLE:
                    convert(fromVariable, Double.class);
                    break;
                default:
                    throw new IllegalConversionException("unknown primitive type "+from);
            }
        }
        else
        {
            tload(fromVariable);
        }
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
        invokespecial(SwitchException.class.getConstructor());
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
        invokespecial(SwitchException.class.getConstructor());
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
        invokespecial(SwitchException.class.getConstructor());
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
    public void addNewArray(String name, Type aClass, int size) throws IOException
    {
        addVariable(name, aClass);
        newarray(aClass, size);
        if (!Generics.isPrimitive(Generics.getComponentType(aClass)))
        {
            checkcast(aClass);
        }
        tstore(name);
    }
    /**
     * Check whether object is of given type
     * <p>Stack: ..., objectref =&gt; ..., objectref
     * @param objectref
     * @throws IOException
     */
    public void checkcast(Type objectref) throws IOException
    {
        if (Generics.isPrimitive(objectref))
        {
            throw new IllegalArgumentException("checkcast type must be objectref");
        }
        int index = subClass.resolveClassIndex(objectref);
        checkcast(index);
    }

    /**
     * Convert a local variable to given type. (if it is possible)
     * <p>Stack: ..., =&gt; ..., objectref
     * @param fromVariable
     * @param to
     * @throws IOException
     * @throws NoSuchMethodException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     */
    public void convert(String fromVariable, Type to) throws IOException, NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, IllegalConversionException
    {
        try
        {
            Type from = getLocalType(fromVariable);
            if (from.equals(to))
            {
                tload(fromVariable);
                return;
            }
            try
            {
                Constructor c = Generics.getConstructor(to, from);
                if (c != null)
                {
                    anew(to);
                    dup();
                    tload(fromVariable);
                    invokespecial(c);
                    return;
                }
            }
            catch (NoSuchMethodException ex)
            {
            }
            try
            {
                Member m = Generics.getMethod(to, "valueOf", from);
                if (m != null && Modifier.isStatic(Generics.getModifiers(m)) && to.equals(Generics.getReturnType(m)))
                {
                    tload(fromVariable);
                    invokestatic(m);
                    return;
                }
            }
            catch (NoSuchMethodException ex)
            {
            }
            try
            {
                String str = Generics.getName(to) + "Value";
                Member m = Generics.getMethod(from, str); // xxxValue()
                // xxxValue()
                // xxxValue()
                if (m != null && to.equals(Generics.getReturnType(m)))
                {
                    tload(fromVariable);
                    invokevirtual(m);
                    return;
                }
            }
            catch (NoSuchMethodException ex)
            {
            }
            if (Generics.isPrimitive(from))
            {
                ObjectType fromType = ObjectType.valueOf(from);
                ObjectType toType = ObjectType.valueOf(to);
                switch (fromType)
                {
                    case BYTE:
                    case CHAR:
                    case SHORT:
                    case INT:
                        switch (toType)
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
                        switch (toType)
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
                        switch (toType)
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
                        switch (toType)
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
            else
            {
                if (String.class.equals(to))
                {
                    tload(fromVariable);
                    invokevirtual(Generics.getMethod(from, "toString"));
                    return;
                }
            }
            if (Generics.isPrimitive(to))
            {
                Type toc = null;
                ObjectType ot = ObjectType.valueOf(to);
                switch (ot)
                {
                    case INT:
                        toc = Integer.class;
                        break;
                    case BYTE:
                        toc = Byte.class;
                        break;
                    case BOOLEAN:
                        toc = Boolean.class;
                        break;
                    case CHAR:
                        toc = Character.class;
                        break;
                    case SHORT:
                        toc = Short.class;
                        break;
                    case LONG:
                        toc = Long.class;
                        break;
                    case FLOAT:
                        toc = Float.class;
                        break;
                    case DOUBLE:
                        toc = Double.class;
                        break;
                    default:
                        throw new IllegalConversionException("Cannot convert from " + from + " to " + to);
                }
                try
                {
                    String str = "parse" + Generics.getName(to).substring(0, 1).toUpperCase() + Generics.getName(to).substring(1);
                    Member m = Generics.getMethod(toc, str, from);
                    if (m != null && Modifier.isStatic(m.getModifiers()) && to.equals(Generics.getReturnType(m)))
                    {
                        tload(fromVariable);
                        invokestatic(m);
                        return;
                    }
                }
                catch (NoSuchMethodException ex)
                {
                }
            }
        }
        catch (IOException | SecurityException | IllegalConversionException | NoSuchMethodException ex)
        {
            throw new IllegalConversionException("Cannot convert from " + fromVariable + " to " + to, ex);
        }
    }

    private void check(String name)
    {
        if (!localClassMap.containsKey(name))
        {
            throw new IllegalArgumentException("local variable "+name+" not found");
        }
    }

}
