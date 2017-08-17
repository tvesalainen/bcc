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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.AccessFlags.FieldFlags;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FieldInfo implements Writable, VariableElement
{
    private ClassFile classFile;
    private VariableElement variableElement;
    private List<AttributeInfo> attributes = new ArrayList<>();
    private boolean synthetic = true;
    private boolean readyToWrite;
    private int access_flags;
    private int name_index;
    private int descriptor_index;

    public FieldInfo(ClassFile classFile, VariableElement variableElement)
    {
        this.classFile = classFile;
        this.variableElement = variableElement;
    }

    public FieldInfo(ClassFile classFile, VariableElement variableElement, ConstantValue constant)
    {
        this(classFile, variableElement);
        attributes.add(constant);
    }

    public FieldInfo(DataInput in) throws IOException
    {
        access_flags = in.readUnsignedShort();
        name_index = in.readUnsignedShort();
        descriptor_index = in.readUnsignedShort();
        int attributes_count = in.readUnsignedShort();
        for (int ii=0;ii<attributes_count;ii++)
        {
            attributes.add(new AttributeInfo(classFile, in));
        }
    }

    /**
     * Call to this method tells that the Attribute is ready writing. This method 
     * must be called before constant pool is written.
     */
    public void readyToWrite()
    {
        addSignatureIfNeed();
        this.name_index = ((SubClass)classFile).resolveNameIndex(getSimpleName());
        this.descriptor_index = ((SubClass)classFile).resolveNameIndex(Descriptor.getDesriptor(this));
        readyToWrite = true;
    }
    
    @Override
    public void write(DataOutput out) throws IOException
    {
        if (!readyToWrite)
        {
            throw new IllegalStateException("not ready to write");
        }
        int modifier = FieldFlags.getModifier(getModifiers());
        modifier |= FieldFlags.ACC_SYNTHETIC;
        out.writeShort(modifier);
        out.writeShort(name_index);
        out.writeShort(descriptor_index);
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
            SubClass subClass = (SubClass) classFile;
            attributes.add(new SignatureAttribute(subClass, signature));
        }
    }
    
    @Override
    public Object getConstantValue()
    {
        return variableElement.getConstantValue();
    }

    @Override
    public TypeMirror asType()
    {
        return variableElement.asType();
    }

    @Override
    public ElementKind getKind()
    {
        return variableElement.getKind();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        return variableElement.getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        return variableElement.getAnnotation(annotationType);
    }

    @Override
    public Set<Modifier> getModifiers()
    {
        return variableElement.getModifiers();
    }

    @Override
    public Name getSimpleName()
    {
        return variableElement.getSimpleName();
    }

    @Override
    public Element getEnclosingElement()
    {
        return variableElement.getEnclosingElement();
    }

    @Override
    public List<? extends Element> getEnclosedElements()
    {
        return variableElement.getEnclosedElements();
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return variableElement.accept(v, p);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
