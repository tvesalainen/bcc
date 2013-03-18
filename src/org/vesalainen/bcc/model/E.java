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

import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.vesalainen.annotation.dump.Descriptor;

/**
 * @author Timo Vesalainen
 */
public class E 
{
    private static Elements elements = new ElementsImpl();

    public static TypeElement fromDescriptor(String fieldDescriptor)
    {
        return getTypeElement(Descriptor.getFullyQualifiedForm(fieldDescriptor));
    }
    public static void setElements(Elements elements)
    {
        E.elements = elements;
    }

    public static PackageElement getPackageElement(CharSequence name)
    {
        return elements.getPackageElement(name);
    }

    public static TypeElement getTypeElement(CharSequence name)
    {
        return elements.getTypeElement(name);
    }

    public static Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(AnnotationMirror a)
    {
        return elements.getElementValuesWithDefaults(a);
    }

    public static String getDocComment(Element e)
    {
        return elements.getDocComment(e);
    }

    public static boolean isDeprecated(Element e)
    {
        return elements.isDeprecated(e);
    }

    public static Name getBinaryName(TypeElement type)
    {
        return elements.getBinaryName(type);
    }

    public static PackageElement getPackageOf(Element type)
    {
        return elements.getPackageOf(type);
    }

    public static List<? extends Element> getAllMembers(TypeElement type)
    {
        return elements.getAllMembers(type);
    }

    public static List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e)
    {
        return elements.getAllAnnotationMirrors(e);
    }

    public static boolean hides(Element hider, Element hidden)
    {
        return elements.hides(hider, hidden);
    }

    public static boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type)
    {
        return elements.overrides(overrider, overridden, type);
    }

    public static String getConstantExpression(Object value)
    {
        return elements.getConstantExpression(value);
    }

    public static void printElements(Writer w, Element... elements)
    {
        E.elements.printElements(w, elements);
    }

    public static Name getName(CharSequence cs)
    {
        return elements.getName(cs);
    }

    public static String getInternalForm(TypeElement type)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
}
