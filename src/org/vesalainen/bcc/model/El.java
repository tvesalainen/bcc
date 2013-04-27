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
import java.lang.reflect.Method;
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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import org.vesalainen.bcc.Descriptor;

/**
 * @author Timo Vesalainen
 */
public class El 
{
    private static final Elements myElements = new ElementsImpl();
    private static Elements elements = myElements;
    /**
     * Returns method in text form. Format is
     * &lt;canonical name of method class&gt; ' ' &lt;method name&gt; '(' arguments ')'
     * 
     * Arguments is a comma separated list of argument type names. Argument type name
     * is canonical name of argument class. Arrays however are printed with leading '['.
     * Example T0.class.getDeclaredMethod("m1", String.class, int.class, long[].class) 
     * produces "org.vesalainen.bcc.T0 m1(java.lang.String,int,[long)"
     * 
     * @param method
     * @return 
     */
    public static String getExecutableString(Method method)
    {
        if (method == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getCanonicalName());
        sb.append(' ');
        sb.append(method.getName());
        sb.append('(');
        boolean f = true;
        for (Class<?> p : method.getParameterTypes())
        {
            if (f)
            {
                f = false;
            }
            else
            {
                sb.append(',');
            }
            aName(sb, p);
        }
        sb.append(')');
        return sb.toString();
    }
    private static void aName(StringBuilder sb, Class<?> c)
    {
        if (c.isArray())
        {
            sb.append('[');
            aName(sb, c.getComponentType());
        }
        else
        {
            sb.append(c.getCanonicalName());
        }
    }
    /**
     * Returns ExecutableElement from text form. Format is
     * &lt;canonical name of method class&gt; ' ' &lt;method name&gt; '(' arguments ')'
     * 
     * Arguments is a comma separated list of argument type names. Argument type name
     * is canonical name of argument class. Arrays however are printed with leading '['.
     * Example T0.class.getDeclaredMethod("m1", String.class, int.class, long[].class) 
     * produces "org.vesalainen.bcc.T0 m1(java.lang.String,int,[long)"
     * 
     * @param methodString
     * @return 
     */
    public static ExecutableElement getExecutableElement(String methodString)
    {
        if (methodString == null)
        {
            throw new NullPointerException();
        }
        if (methodString.isEmpty())
        {
            return null;
        }
        int i1 = methodString.indexOf(' ');
        if (i1 == -1)
        {
            throw new IllegalArgumentException(methodString+" illegal");
        }
        TypeElement cls = El.getTypeElement(methodString.substring(0, i1));
        if (cls == null)
        {
            throw new IllegalArgumentException(methodString+" class not found");
        }
        int i2 = methodString.indexOf('(', i1);
        if (i2 == -1)
        {
            throw new IllegalArgumentException(methodString+" illegal");
        }
        String name = methodString.substring(i1+1, i2);
        int i3 = methodString.indexOf(')', i2);
        if (i3 == -1)
        {
            throw new IllegalArgumentException(methodString+" illegal");
        }
        if (i3-i2 > 1)
        {
            String[] args = methodString.substring(i2+1, methodString.length()-1).split(",");
            TypeMirror[] params = new TypeMirror[args.length];
            for (int ii=0;ii<args.length;ii++)
            {
                params[ii] = Typ.getTypeFor(args[ii]);
            }
            return getMethod(cls, name, params);
        }
        else
        {
            return getMethod(cls, name);
        }
    }
    /**
     * Returns method in text form. Format is
     * &lt;canonical name of method class&gt; ' ' &lt;method name&gt; '(' arguments ')'
     * 
     * Arguments is a comma separated list of argument type names. Argument type name
     * is canonical name of argument class. Arrays however are printed with leading '['.
     * Example T0.class.getDeclaredMethod("m1", String.class, int.class, long[].class) 
     * produces "org.vesalainen.bcc.T0 m1(java.lang.String,int,[long)"
     * 
     * @param method
     * @return 
     */
    public static String getExecutableString(ExecutableElement method)
    {
        if (method == null)
        {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        TypeElement te = (TypeElement) method.getEnclosingElement();
        sb.append(te.getQualifiedName());
        sb.append(' ');
        sb.append(method.getSimpleName());
        sb.append('(');
        boolean f = true;
        for (VariableElement p : method.getParameters())
        {
            if (f)
            {
                f = false;
            }
            else
            {
                sb.append(',');
            }
            addTypeName(sb, p.asType());
        }
        sb.append(')');
        return sb.toString();
    }
    private static void addTypeName(StringBuilder sb, TypeMirror type)
    {
        switch (type.getKind())
        {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
                sb.append(type.getKind().name().toLowerCase());
                break;
            case DECLARED:
                DeclaredType dt = (DeclaredType) type;
                TypeElement te = (TypeElement) dt.asElement();
                sb.append(te.getQualifiedName());
                break;
            case ARRAY:
                ArrayType at = (ArrayType) type;
                sb.append('[');
                addTypeName(sb, at.getComponentType());
                break;
            case TYPEVAR:
                TypeVariable tv = (TypeVariable) type;
                addTypeName(sb, tv.getUpperBound());
                break;
            default:
                throw new UnsupportedOperationException(type+" "+type.getKind()+" not supported");
        }
    }
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
                        if (!Typ.isAssignable(parameters[ii], calleeParams.get(ii).asType()))
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
                    if (!Typ.isAssignable(parameters[ii], calleeParams.get(ii).asType()))
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
        TypeElement te = elements.getTypeElement(name);
        if (te == null)
        {
            throw new IllegalArgumentException(name+" not TypeElement");
        }
        return te;
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
        return myElements.getPackageOf(type);
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
        myElements.printElements(w, elements);
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
