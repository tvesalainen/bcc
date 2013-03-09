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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * @author Timo Vesalainen
 */
public class ElementFactory 
{
    public static final Elements Elements = new ElementsImpl();
    
    private static Map<Class<?>,TypeElement> classMap = new HashMap<>();
    private static Map<Field,VariableElement> fieldMap = new HashMap<>();
    private static Map<Constructor,ExecutableElement> constructorMap = new HashMap<>();
    private static Map<Method,ExecutableElement> methodMap = new HashMap<>();
    private static Map<Package,PackageElement> packageMap = new HashMap<>();
    private static Map<TypeVariable,TypeParameterElement> typeParameterMap = new HashMap<>();
    private static Map<Annotation,AnnotationMirror> annotationMap = new HashMap<>();
    private static Map<Object,AnnotationValue> annotationValueMap = new HashMap<>();
    private static Map<Type,VariableElement> variableElementMap = new HashMap<>();
    private static Map<Enum,VariableElement> variableElementEnumMap = new HashMap<>();
    
    public static Element get(Object ob)
    {
        if (ob instanceof Class)
        {
            Class<?> cls = (Class<?>) ob;
            return get(cls);
        }
        if (ob instanceof Field)
        {
            Field f = (Field) ob;
            return get(f);
        }
        if (ob instanceof Constructor)
        {
            Constructor c = (Constructor) ob;
            return get(c);
        }
        if (ob instanceof Method)
        {
            Method m = (Method) ob;
            return get(m);
        }
        if (ob instanceof Package)
        {
            Package p = (Package) ob;
            return getPackageElement(p);
        }
        if (ob instanceof Annotation)
        {
            Annotation a = (Annotation) ob;
            return get(a);
        }
        throw new UnsupportedOperationException(ob+" not supported");
    }
    public static TypeElement get(Class<?> type)
    {
        TypeElement te = classMap.get(type);
        if (te == null)
        {
            TypeElementImpl tei = new TypeElementImpl(type);
            classMap.put(type, tei); // try to prevent loops
            tei.init(type);
            te = tei;
        }
        return te;
    }

    public static VariableElement get(Field field)
    {
        VariableElement ve = fieldMap.get(field);
        if (ve == null)
        {
            if (field.getName().endsWith("field1"))
            {
                System.err.println();
            }
            ve = new VariableElementImpl(field);
            fieldMap.put(field, ve);
        }
        return ve;
    }
    public static ExecutableElement get(Constructor constructor)
    {
        ExecutableElement ee = constructorMap.get(constructor);
        if (ee == null)
        {
            ExecutableElementImpl eei = new ExecutableElementImpl(constructor);
            constructorMap.put(constructor, eei);
            eei.init(constructor);
            ee = eei;
        }
        return ee;
    }
    public static ExecutableElement get(Method method)
    {
        ExecutableElement ee = methodMap.get(method);
        if (ee == null)
        {
            ExecutableElementImpl eei = new ExecutableElementImpl(method);
            methodMap.put(method, eei);
            eei.init(method);
            ee = eei;
        }
        return ee;
    }
    public static TypeElement get(Annotation annotation)
    {
        return get(annotation.annotationType());    // TODO does this work???
    }
    public static PackageElement getPackageElement(Package pkg)
    {
        PackageElement pe = packageMap.get(pkg);
        if (pe == null)
        {
            pe = new PackageElementImpl(pkg);
            packageMap.put(pkg, pe);
        }
        return pe;
    }
    public static TypeParameterElement getTypeParameterElement(TypeVariable typeVariable)
    {
        TypeParameterElement tpe = typeParameterMap.get(typeVariable);
        if (tpe == null)
        {
            TypeParameterElementImpl tpei = new TypeParameterElementImpl(typeVariable);
            typeParameterMap.put(typeVariable, tpei);
            tpei.init(typeVariable);
            tpe = tpei;
        }
        return tpe;
    }

    public static AnnotationMirror getAnnotationMirror(Annotation annotation)
    {
        AnnotationMirror am = annotationMap.get(annotation);
        if (am == null)
        {
            am = new AnnotationMirrorImpl(annotation);
            annotationMap.put(annotation, am);
        }
        return am;
    }

    public static AnnotationValue getAnnotationValue(Object value)
    {
        AnnotationValue av = annotationValueMap.get(value);
        if (av == null)
        {
            av = new AnnotationValueImpl(value);
            annotationValueMap.put(value, av);
        }
        return av;
    }

    public static VariableElement getVariableElement(Type param, Annotation[] annotation)
    {
        VariableElement ve = variableElementMap.get(param);
        if (ve == null)
        {
            ve = new VariableElementImpl(param, annotation);
            variableElementMap.put(param, ve);
        }
        return ve;
    }
    public static Element get(GenericDeclaration genericDeclaration)
    {
        if (genericDeclaration instanceof Class)
        {
            Class<?> cls = (Class<?>) genericDeclaration;
            return get(cls);
        }
        if (genericDeclaration instanceof Constructor)
        {
            Constructor constructor = (Constructor) genericDeclaration;
            return get(constructor);
        }
        if (genericDeclaration instanceof Method)
        {
            Method method = (Method) genericDeclaration;
            return get(method);
        }
        throw new UnsupportedOperationException(genericDeclaration+" Not implemented");
    }

    public static VariableElement getVariableElement(Enum en)
    {
        VariableElement ve = variableElementEnumMap.get(en);
        if (ve == null)
        {
            ve = new VariableElementImpl(en);
            variableElementEnumMap.put(en, ve);
        }
        return ve;
    }
    private static Map<Type[],TypeElement> intersectionTypeElementMap = new HashMap<>();
    public static TypeElement getIntersectionTypeElement(Type[] bounds)
    {
        TypeElement te = intersectionTypeElementMap.get(bounds);
        if (te == null)
        {
            TypeElementImpl tei = new TypeElementImpl();
            intersectionTypeElementMap.put(bounds, tei);
            tei.init(bounds);
            te = tei;
        }
        return te;
    }

}
