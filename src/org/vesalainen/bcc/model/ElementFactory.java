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

/**
 * @author Timo Vesalainen
 */
public class ElementFactory 
{
    private static Map<Class<?>,TypeElement> classMap = new HashMap<>();
    private static Map<Field,VariableElement> fieldMap = new HashMap<>();
    private static Map<Constructor,ExecutableElement> constructorMap = new HashMap<>();
    private static Map<Method,ExecutableElement> methodMap = new HashMap<>();
    private static Map<Package,PackageElement> packageMap = new HashMap<>();
    private static Map<TypeVariable,TypeParameterElement> typeParameterMap = new HashMap<>();
    private static Map<Annotation,AnnotationMirror> annotationMap = new HashMap<>();
    private static Map<Object,AnnotationValue> annotationValueMap = new HashMap<>();
    
    public static TypeElement get(Class<?> type)
    {
        TypeElement te = classMap.get(type);
        if (te == null)
        {
            te = new TypeElementImpl(type);
            classMap.put(type, te);
        }
        return te;
    }

    public static VariableElement get(Field field)
    {
        VariableElement ve = fieldMap.get(field);
        if (ve == null)
        {
            ve = new VariableElementFieldImpl(field);
            fieldMap.put(field, ve);
        }
        return ve;
    }
    public static ExecutableElement get(Constructor constructor)
    {
        ExecutableElement ee = constructorMap.get(constructor);
        if (ee == null)
        {
            ee = new ExecutableElementConstructorImpl(constructor);
            constructorMap.put(constructor, ee);
        }
        return ee;
    }
    public static ExecutableElement get(Method method)
    {
        ExecutableElement ee = methodMap.get(method);
        if (ee == null)
        {
            ee = new ExecutableElementMethodImpl(method);
            methodMap.put(method, ee);
        }
        return ee;
    }
    public static TypeElement get(Annotation annotation)
    {
        return get(annotation.annotationType());    // TODO does this work???
    }
    public static PackageElement get(Package pkg)
    {
        PackageElement pe = packageMap.get(pkg);
        if (pe == null)
        {
            pe = new PackageElementImpl(pkg);
            packageMap.put(pkg, pe);
        }
        return pe;
    }
    public static TypeParameterElement get(GenericDeclaration genericDeclaration, TypeVariable typeVariable)
    {
        TypeParameterElement tpe = typeParameterMap.get(typeVariable);
        if (tpe == null)
        {
            tpe = new TypeParameterElementImpl(genericDeclaration, typeVariable);
            typeParameterMap.put(typeVariable, tpe);
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
        return new VariableElementParameterImpl(param, annotation);
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
        return new VariableElementEnumImpl(en);
    }

}
