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
import org.vesalainen.annotation.dump.Descriptor;
import org.vesalainen.annotation.dump.Signature;

/**
 *
 * @author tkv
 */
public class FieldInfo implements Writable, VariableElement
{
    /**
     * Declared public; may be accessed from outside its package.
     */
    public static final int ACC_PUBLIC = 0x0001;
    /**
     * Declared private; accessible only within the defining class.
     */
    public static final int ACC_PRIVATE = 0x0002;
    /**
     * Declared protected; may be accessed within subclasses.
     */
    public static final int ACC_PROTECTED = 0x0004;
    /**
     * Declared static.
     */
    public static final int ACC_STATIC = 0x0008;
    /**
     * Declared final; no subclasses allowed.
     */
    public static final int ACC_FINAL = 0x0010;
    /**
     * Declared volatile; cannot be cached.
     */
    public static final int ACC_VOLATILE = 0x0040;
    /**
     * Declared transient; not written or read by a persistent object manager.
     */
    public static final int ACC_TRANSIENT = 0x0080;
    /**
     * Declared synthetic; Not present in the source code.
     */
    public static final int ACC_SYNTHETIC = 0x1000;
    /**
     * Declared as an element of an enum.
     */
    public static final int ACC_ENUM = 0x4000;
    
    private ClassFile classFile;
    private VariableElement variableElement;
    private List<AttributeInfo> attributes = new ArrayList<>();
    private boolean synthetic;

    public FieldInfo(ClassFile classFile, VariableElement variableElement)
    {
        this.classFile = classFile;
        this.variableElement = variableElement;
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

    public void write(DataOutput out) throws IOException
    {
        addSignatureIfNeed();
        out.writeShort(addModifiers());
        out.writeShort(classFile.getNameIndex(getSimpleName()));
        out.writeShort(classFile.getNameIndex(Descriptor.getDesriptor(this)));
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
            attributes.add(new SignatureAttribute(classFile, signature));
        }
    }
    
    private int addModifiers()
    {
        int flags = 0;
        if (synthetic)
        {
            flags |= ACC_SYNTHETIC;
        }
        for (Modifier m : getModifiers())
        {
            switch (m)
            {
                case PUBLIC:
                    flags |= ACC_PUBLIC;
                    break;
                case PRIVATE:
                    flags |= ACC_PRIVATE;
                    break;
                case PROTECTED:
                    flags |= ACC_PROTECTED;
                    break;
                case STATIC:
                    flags |= ACC_STATIC;
                    break;
                case FINAL:
                    flags |= ACC_FINAL;
                    break;
                case VOLATILE:
                    flags |= ACC_VOLATILE;
                    break;
                case TRANSIENT:
                    flags |= ACC_TRANSIENT;
                    break;
                default:
                    throw new IllegalArgumentException(m+" is not valid method flag");
            }
        }
        return flags;
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
