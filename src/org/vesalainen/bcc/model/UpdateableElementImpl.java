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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;

/**
 * @author Timo Vesalainen
 */
public class UpdateableElementImpl<E extends Element> implements InvocationHandler
{
    private static final InterfaceComp IComp = new InterfaceComp();
    
    private E element;
    private Element enclosingElement;
    private Set<Modifier> modifier;
    private Name name;
    
    public E getUpdateableElement(E element)
    {
        this.element = element;
        return (E) Proxy.newProxyInstance(
                element.getClass().getClassLoader(), 
                new Class<?>[] {extractInterface(element), UpdateableElement.class }, 
                this
                );
    }
    private Class<?> extractInterface(E element)
    {
        Class<? extends Element> aClass = element.getClass();
        Class<?>[] interfaces = aClass.getInterfaces();
        switch (interfaces.length)
        {
            case 0:
                throw new IllegalArgumentException(element+" doesn't have any interface");
            case 1:
                return interfaces[0];
            default:
                Arrays.sort(interfaces, IComp);
                return interfaces[0];
        }
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        switch (method.getName())
        {
            case "setEnclosingElement":
                enclosingElement = (Element) args[0];
                return null;
            case "setModifiers":
                modifier = (Set<Modifier>) args[0];
                return null;
            case "setSimpleName":
                name = (Name) args[0];
                return null;
            case "getEnclosingElement":
                if (enclosingElement != null)
                {
                    return enclosingElement;
                }
                else
                {
                    return method.invoke(element, args);
                }
            case "getModifiers":
                if (modifier != null)
                {
                    return modifier;
                }
                else
                {
                    return method.invoke(element, args);
                }
            case "getSimpleName":
                if (name != null)
                {
                    return name;
                }
                else
                {
                    return method.invoke(element, args);
                }
            default:
                return method.invoke(element, args);
        }
    }

    private static class InterfaceComp implements Comparator<Class<?>>
    {

        @Override
        public int compare(Class<?> o1, Class<?> o2)
        {
            if (contains(o1.getInterfaces(), o2))
            {
                return 1;
            }
            else
            {
                if (contains(o2.getInterfaces(), o1))
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        }
        private boolean contains(Class<?>[] arr, Class<?> i)
        {
            for (Class<?> a : arr)
            {
                if (a.equals(i))
                {
                    return true;
                }
            }
            return false;
        }
    }
}
