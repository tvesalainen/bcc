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

package org.vesalainen.bcc.annotation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import org.vesalainen.bcc.ClassFile;
import org.vesalainen.bcc.Writable;
import org.vesalainen.bcc.model.E;

/**
 * @author Timo Vesalainen
 */
public class AnnotationWrapper implements AnnotationMirror, Writable, InvocationHandler  
{
    private ClassFile classFile;
    private int typeIndex;
    private DeclaredType annotationType;
    private Map<ExecutableElement, AnnotationValue> elementValues = new HashMap<>();
    private Map<? extends ExecutableElement, ? extends AnnotationValue> elementValuesWithDefaults;
    private List<ElementValuePair> elementValuePairs = new ArrayList<>();
    private final TypeElement typeElement;

    public AnnotationWrapper(ClassFile classFile, DataInput in) throws IOException
    {
        this.classFile = classFile;
        typeIndex = in.readUnsignedShort();
        typeElement = E.fromDescriptor(getDescriptor());
        annotationType = (DeclaredType) typeElement.asType();
        int numElementValuePairs = in.readUnsignedShort();
        for (int ii = 0; ii < numElementValuePairs; ii++)
        {
            ElementValuePair elementValuePair = new ElementValuePair(classFile, in);
            ExecutableElement executableElement = getExecutableElement(elementValuePair.getName());
            elementValues.put(executableElement, elementValuePair.getValue());
            elementValuePairs.add(elementValuePair);
        }
    }

    private ExecutableElement getExecutableElement(String name)
    {
        for (ExecutableElement ee : ElementFilter.methodsIn(typeElement.getEnclosedElements()))
        {
            if (ee.getSimpleName().contentEquals(name))
            {
                return ee;
            }
        }
        throw new IllegalArgumentException(name+" not found from "+typeElement);
    }
    @Override
    public DeclaredType getAnnotationType()
    {
        return annotationType;
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues()
    {
        return elementValues;
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        if (elementValuesWithDefaults == null)
        {
            elementValuesWithDefaults = E.getElementValuesWithDefaults(this);
        }
        return (A) Proxy.newProxyInstance(
                annotationType.getClassLoader(), 
                new Class<?>[] { annotationType}, 
                this
                );
    }
    String getDescriptor()
    {
        return classFile.getString(typeIndex);
    }
    /**
     * @deprecated 
     * @param name
     * @return 
     */
    public ElementValue getElement(String name)
    {
        for (ElementValuePair evp : elementValuePairs)
        {
            if (name.equals(evp.getName()))
            {
                return evp.getValue();
            }
        }
        return null;
    }
    
    public int getLength()
    {
        int length = 4;
        for (ElementValuePair evp : elementValuePairs)
        {
            length += evp.getLength();
        }
        return length;
    }
    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(typeIndex);
        out.writeShort(elementValuePairs.size());
        for (ElementValuePair ev : elementValuePairs)
        {
            ev.write(out);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("@");
        sb.append(getDescriptor());
        sb.append("(");
        for (ElementValuePair ev : elementValuePairs)
        {
            sb.append(ev);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        ExecutableElement executableElement = getExecutableElement(method.getName());
        if (executableElement == null)
        {
            throw new IllegalArgumentException(method+" not found");
        }
        return elementValuesWithDefaults.get(executableElement);
    }

    
}
