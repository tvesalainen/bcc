/*
 * Copyright (C) 2013 Timo Vesalainen
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

package org.vesalainen.bcc.model;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import org.vesalainen.bcc.annotation.AnnotationWrapper;

/**
 * @author Timo Vesalainen
 */
public class ElementsImpl implements Elements
{

    @Override
    public PackageElement getPackageElement(CharSequence name)
    {
        Package pkg = Package.getPackage(name.toString());
        if (pkg != null)
        {
            return ElementFactory.getPackageElement(pkg);
        }
        else
        {
            return ElementFactory.getPackageElement(name);
        }
    }

    @Override
    public TypeElement getTypeElement(CharSequence name)
    {
        try
        {
            Class<?> cls = Class.forName(name.toString());
            return ElementFactory.get(cls);
        }
        catch (ClassNotFoundException ex)
        {
            return null;
        }
    }

    @Override
    public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a)
    {
        if (a instanceof AnnotationMirrorImpl)
        {
            AnnotationMirrorImpl ami = (AnnotationMirrorImpl) a;
            return ami.getElementValuesWithDefaults();
        }
        if (a instanceof AnnotationWrapper)
        {
            AnnotationWrapper aw = (AnnotationWrapper) a;
            return aw.getElementValuesWithDefaults();
        }
        throw new UnsupportedOperationException("Not supported with "+a.getClass());
    }

    @Override
    public String getDocComment(Element e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isDeprecated(Element e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Name getBinaryName(TypeElement type)
    {
        if (type instanceof QualifiedNameable)
        {
            QualifiedNameable qn = type;
            return qn.getQualifiedName();
        }
        else
        {
            throw new IllegalArgumentException(type+" not QualifiedNameable");
        }
    }

    @Override
    public PackageElement getPackageOf(Element type)
    {
        switch (type.getKind())
        {
            case PACKAGE:
                return (PackageElement)type;
            case CLASS:
            case INTERFACE:
            case ENUM:
                TypeElement te = (TypeElement) type;
                Element ee = te.getEnclosingElement();
                if (ee == null)
                {
                    throw new IllegalArgumentException(te+" doesn't have EnclosingElement");
                }
                return getPackageOf(ee);
            default:
                throw new UnsupportedOperationException(type.getKind()+" not supported");
        }
    }

    @Override
    public List<? extends Element> getAllMembers(TypeElement type)
    {
        List<Element> list = new ArrayList<>();
        while (type != null)
        {
            list.addAll(type.getEnclosedElements());
            type = (TypeElement) Typ.asElement(type.getSuperclass());
        }
        return list;
    }

    @Override
    public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hides(Element hider, Element hidden)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type)
    {
        if (overrider.getSimpleName().contentEquals(overridden.getSimpleName()))
        {
            if (Typ.isAssignable(overrider.getReturnType(), overridden.getReturnType()))
            {
                List<? extends VariableElement> overriderParameters = overrider.getParameters();
                List<? extends VariableElement> overriddenParameters = overridden.getParameters();
                if (overriderParameters.size() == overriddenParameters.size())
                {
                    for (int ii=0;ii<overriderParameters.size();ii++)
                    {
                        if (!Typ.isAssignable(overriderParameters.get(ii).asType(), overriddenParameters.get(ii).asType()))
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getConstantExpression(Object value)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void printElements(Writer w, Element... elements)
    {
        try
        {
            for (Element el : elements)
            {
                w.write(el.toString());
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Name getName(CharSequence cs)
    {
        return new NameImpl(cs);
    }

    @Override
    public boolean isFunctionalInterface(TypeElement type)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public class NameImpl implements Name
    {
        private String name;

        public NameImpl(CharSequence name)
        {
            if (name != null)
            {
                this.name = name.toString();
            }
            else
            {
                this.name = "";
            }
        }

        @Override
        public int length()
        {
            return name.length();
        }

        @Override
        public char charAt(int index)
        {
            return name.charAt(index);
        }

        @Override
        public boolean equals(Object anObject)
        {
            return name.equals(anObject);
        }

        @Override
        public boolean contentEquals(CharSequence cs)
        {
            return name.contentEquals(cs);
        }

        @Override
        public int hashCode()
        {
            return name.hashCode();
        }

        @Override
        public CharSequence subSequence(int beginIndex, int endIndex)
        {
            return name.subSequence(beginIndex, endIndex);
        }

        @Override
        public String toString()
        {
            return name.toString();
        }
    }
}
