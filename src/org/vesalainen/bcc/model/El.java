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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.vesalainen.bcc.Descriptor;

/**
 * @author Timo Vesalainen
 */
public class El 
{
    private static Elements elements = new ElementsImpl();

    public static <E extends Element> E createUpdateableElement(E element)
    {
        UpdateableElementImpl<E> uei = new UpdateableElementImpl<>();
        return uei.getUpdateableElement(element);
    }
    public static VariableElement getField(Class<?> cls, String name)
    {
        return getField(El.getTypeElement(cls.getCanonicalName()), name);
    }
    public static VariableElement getField(TypeElement typeElement, String name)
    {
        for (VariableElement field : ElementFilter.fieldsIn(El.getAllMembers(typeElement)))
        {
            if (name.contentEquals(field.getSimpleName()))
            {
                return field;
            }
        }
        return null;
    }
    public static ExecutableElement getMethod(Class<?> cls, String name, Class<?>... parameters)
    {
        return getMethod(
                El.getTypeElement(cls.getCanonicalName()), 
                name, 
                getParams(parameters)
                );
    }
    public static ExecutableElement getConstructor(Class<?> cls, Class<?>... parameters)
    {
        return getConstructor(
                El.getTypeElement(cls.getCanonicalName()), 
                getParams(parameters)
                );
    }
    public static TypeMirror[] getParams(Class<?>... parameters)
    {
        TypeMirror[] params = new TypeMirror[parameters.length];
        for (int ii=0;ii<params.length;ii++)
        {
            params[ii] = Typ.getTypeFor(parameters[ii]);
        }
        return params;
    }
    public static ExecutableElement getMethod(TypeElement typeElement, String name, TypeMirror... parameters)
    {
        for (ExecutableElement method : ElementFilter.methodsIn(El.getAllMembers(typeElement)))
        {
            if (name.contentEquals(method.getSimpleName()))
            {
                if (parameters.length == method.getParameters().size())
                {
                    boolean ok = true;
                    List<? extends VariableElement> calleeParams = method.getParameters();
                    for (int ii=0;ii<parameters.length;ii++)
                    {
                        if (!Typ.isSameType(parameters[ii], calleeParams.get(ii).asType()))
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
        return null;
    }

    public static ExecutableElement getConstructor(TypeElement typeElement, TypeMirror... parameters)
    {
        for (ExecutableElement method : ElementFilter.constructorsIn(typeElement.getEnclosedElements()))
        {
            if (parameters.length == method.getParameters().size())
            {
                boolean ok = true;
                List<? extends VariableElement> calleeParams = method.getParameters();
                for (int ii=0;ii<parameters.length;ii++)
                {
                    if (!Typ.isSameType(parameters[ii], calleeParams.get(ii).asType()))
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
        return null;
    }

    public static TypeElement fromDescriptor(String fieldDescriptor)
    {
        return getTypeElement(Descriptor.getFullyQualifiedForm(fieldDescriptor));
    }
    public static void setElements(Elements elements)
    {
        El.elements = elements;
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
        El.elements.printElements(w, elements);
    }

    public static Name getName(CharSequence cs)
    {
        return elements.getName(cs);
    }

    public static String getInternalForm(TypeElement type)
    {
        return type.getQualifiedName().toString().replace('.', '/');
    }

    /**
     * Return effective methods for a class. All methods accessible at class 
     * are returned. That includes superclass methods which are not override.
     * @param cls
     * @return 
     */
    public static List<? extends ExecutableElement> getEffectiveMethods(TypeElement cls)
    {
        List<ExecutableElement> list = new ArrayList<>();
        while (cls != null)
        {
            for (ExecutableElement method : ElementFilter.methodsIn(cls.getEnclosedElements()))
            {
                if (!overrides(list, method))
                {
                    list.add(method);
                }
            }
            cls = (TypeElement) Typ.asElement(cls.getSuperclass());
        }
        return list;
    }
    private static boolean overrides(Collection<ExecutableElement> methods, ExecutableElement method)
    {
        for (ExecutableElement m : methods)
        {
            if (El.overrides(m, method, (TypeElement)m.getEnclosingElement()))
            {
                return true;
            }
        }
        return false;
    }
}
