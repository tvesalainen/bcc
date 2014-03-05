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
 * @author tkv
 */
public class FieldInfo implements Writable, VariableElement
{
    private ClassFile classFile;
    private VariableElement variableElement;
    private List<AttributeInfo> attributes = new ArrayList<>();
    private boolean synthetic = true;
    private boolean readyToWrite;

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
        int access_flags = in.readUnsignedShort();
        int name_index = in.readUnsignedShort();
        int descriptor_index = in.readUnsignedShort();
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
        readyToWrite = true;
    }
    
    @Override
    public void write(DataOutput out) throws IOException
    {
        if (!readyToWrite)
        {
            throw new IllegalStateException("not ready to write");
        }
        SubClass subClass = (SubClass) classFile;
        int modifier = FieldFlags.getModifier(getModifiers());
        modifier |= FieldFlags.ACC_SYNTHETIC;
        out.writeShort(modifier);
        out.writeShort(subClass.resolveNameIndex(getSimpleName()));
        out.writeShort(subClass.resolveNameIndex(Descriptor.getDesriptor(this)));
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
    
    public Object getConstantValue()
    {
        return variableElement.getConstantValue();
    }

    public TypeMirror asType()
    {
        return variableElement.asType();
    }

    public ElementKind getKind()
    {
        return variableElement.getKind();
    }

    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        return variableElement.getAnnotationMirrors();
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        return variableElement.getAnnotation(annotationType);
    }

    public Set<Modifier> getModifiers()
    {
        return variableElement.getModifiers();
    }

    public Name getSimpleName()
    {
        return variableElement.getSimpleName();
    }

    public Element getEnclosingElement()
    {
        return variableElement.getEnclosingElement();
    }

    public List<? extends Element> getEnclosedElements()
    {
        return variableElement.getEnclosedElements();
    }

    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return variableElement.accept(v, p);
    }


}
