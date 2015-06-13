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
import java.util.Objects;
import java.util.Set;
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
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.AccessFlags.MethodFlags;
import org.vesalainen.bcc.model.Typ;

/**
 *
 * @author tkv
 */
public class MethodInfo implements Writable, ExecutableElement
{
    private ExecutableElement executableElement;
    private ClassFile enclosingElement;
    private int name_index;
    private int descriptor_index;
    private List<AttributeInfo> attributes = new ArrayList<>();
    private MethodCompiler mc;
    private boolean bridge;
    private boolean varArgs;
    private boolean synthetic = true;
    private CodeAttribute code;
    private boolean readyToWrite;
    /**
     * 
     * @param access_flags
     * @param name_index
     * @param descriptor_index
     */
    MethodInfo(SubClass cf, ExecutableElement executableElement)
    {
        this.enclosingElement = cf;
        this.executableElement = executableElement;
        this.name_index = cf.resolveNameIndex(executableElement.getSimpleName());
        this.descriptor_index = cf.resolveNameIndex(Descriptor.getDesriptor(executableElement));
        code = new CodeAttribute(cf);
        attributes.add(code);
    }

    MethodInfo(ClassFile cf, DataInput in) throws IOException
    {
        this.enclosingElement = cf;
        int access_flags = in.readUnsignedShort();
        name_index = in.readUnsignedShort();
        descriptor_index = in.readUnsignedShort();
        int attributes_count = in.readUnsignedShort();
        for (int ii=0;ii<attributes_count;ii++)
        {
            attributes.add(AttributeInfo.getInstance(cf, in));
        }
    }

    public ExecutableElement getExecutableElement()
    {
        return executableElement;
    }

    public String getModifiersAsString()
    {
        StringBuilder sb = new StringBuilder();
        for (Modifier m : getModifiers())
        {
            sb.append(m);
            sb.append(' ');
        }
        if (bridge)
        {
            sb.append("bridge ");
        }
        if (varArgs)
        {
            sb.append("varagrs ");
        }
        if (synthetic)
        {
            sb.append("synthetic ");
        }
        return sb.toString();
    }
    private void addThrowables()
    {
        List<? extends TypeMirror> thrownTypes = getThrownTypes();
        if (!thrownTypes.isEmpty())
        {
            ExceptionsAttribute ea = null;
            for (AttributeInfo ai : attributes)
            {
                if (ai instanceof ExceptionsAttribute)
                {
                    ea = (ExceptionsAttribute) ai;
                    break;
                }
            }
            if (ea == null)
            {
                ea = new ExceptionsAttribute((SubClass)enclosingElement);
                attributes.add(ea);
            }
            for (TypeMirror tt : thrownTypes)
            {
                ea.addThrowable((TypeElement)Typ.asElement(tt));
            }
        }
    }
    
    private void addSignatureIfNeed()
    {
        String signature = Signature.getSignature(this);
        if (!signature.isEmpty())
        {
            attributes.add(new SignatureAttribute((SubClass)enclosingElement, signature));
        }
    }
    @Override
    public boolean isVarArgs()
    {
        return varArgs;
    }

    @Override
    public ClassFile getEnclosingElement()
    {
        return enclosingElement;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitExecutable(this, p);
    }

    public String getDescriptor()
    {
        return enclosingElement.getString(descriptor_index);
    }
    public MethodCompiler getMc()
    {
        return mc;
    }

    public void setMc(MethodCompiler mc)
    {
        this.mc = mc;
    }

    public CodeAttribute getCodeAttribute()
    {
        for (AttributeInfo ai : attributes)
        {
            if (ai instanceof CodeAttribute)
            {
                return (CodeAttribute) ai;
            }
        }
        return null;
    }

    /**
     * Call to this method tells that the Attribute is ready writing. This method 
     * must be called before constant pool is written.
     */
    public void readyToWrite()
    {
        addSignatureIfNeed();
        addThrowables();
        readyToWrite = true;
    }
    
    @Override
    public void write(DataOutput out) throws IOException
    {
        if (!readyToWrite)
        {
            throw new IllegalStateException("not ready to write");
        }
        int modifier = MethodFlags.getModifier(getModifiers());
        modifier |= MethodFlags.ACC_SYNTHETIC;
        out.writeShort(modifier);
        out.writeShort(name_index);
        out.writeShort(descriptor_index);
        out.writeShort(attributes.size());
        for (AttributeInfo ai : attributes)
        {
            ai.write(out);
        }
    }

    public int getDescriptor_index()
    {
        return descriptor_index;
    }

    public int getName_index()
    {
        return name_index;
    }


    @Override
    public Set<Modifier> getModifiers()
    {
        return executableElement.getModifiers();
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes()
    {
        return executableElement.getThrownTypes();
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters()
    {
        return executableElement.getTypeParameters();
    }

    @Override
    public TypeMirror getReturnType()
    {
        return executableElement.getReturnType();
    }

    @Override
    public List<? extends VariableElement> getParameters()
    {
        return executableElement.getParameters();
    }

    @Override
    public AnnotationValue getDefaultValue()
    {
        return executableElement.getDefaultValue();
    }

    @Override
    public Name getSimpleName()
    {
        return executableElement.getSimpleName();
    }

    @Override
    public TypeMirror asType()
    {
        return executableElement.asType();
    }

    @Override
    public ElementKind getKind()
    {
        return executableElement.getKind();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        return executableElement.getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        return executableElement.getAnnotation(annotationType);
    }

    @Override
    public List<? extends Element> getEnclosedElements()
    {
        return executableElement.getEnclosedElements();
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.executableElement);
        return hash;
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
        final MethodInfo other = (MethodInfo) obj;
        if (!Objects.equals(this.executableElement, other.executableElement))
        {
            return false;
        }
        return true;
    }

}
