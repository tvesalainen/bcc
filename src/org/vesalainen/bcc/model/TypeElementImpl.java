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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.VariableElementImpl.FieldBuilder;

/**
 * @author Timo Vesalainen
 */
public class TypeElementImpl extends ElementImpl<DeclaredType> implements TypeElement
{
    private List<Element> enclosedElements = new ArrayList<>();
    private List<TypeMirror> interfaces = new ArrayList<>();
    private NestingKind nestingKind = NestingKind.TOP_LEVEL;
    private Name qualifiedName;
    private TypeMirror superclass = T.getNoType(TypeKind.NONE);
    private Element enclosingElement;
    private List<TypeParameterElement> typeParameters = new ArrayList<>();

    public static class ClassBuilder
    {
        private TypeElementImpl element = new TypeElementImpl();
        private TypeParameterBuilder<ClassBuilder> typeParamBuilder;
        public ClassBuilder()
        {
            typeParamBuilder = new TypeParameterBuilder<>(this, element, element.typeParameters);
        }
        
        public TypeElementImpl getTypeElement()
        {
            if (element.enclosingElement == null)
            {
                throw new IllegalArgumentException("enclosingElement not set");
            }
            if (element.type == null)
            {
                element.type = new DeclaredTypeImpl(element, typeParamBuilder.getTypeArguments());
            }
            if (
                    ElementKind.CLASS == element.getKind() && 
                    !"java.lang.Object".contentEquals(element.qualifiedName) &&
                    element.superclass == null
                    )
            {
                throw new IllegalArgumentException("superclass not set");
            }
            return element;
        }

        public ClassBuilder addTypeParameter(String name, Class<?>... bounds)
        {
            return typeParamBuilder.addTypeParameter(name, bounds);
        }

        public ClassBuilder addTypeParameter(String name, CharSequence... bounds)
        {
            return typeParamBuilder.addTypeParameter(name, bounds);
        }

        public ClassBuilder addTypeParameter(String name, TypeElement... bounds)
        {
            return typeParamBuilder.addTypeParameter(name, bounds);
        }

        public ClassBuilder addTypeParameter(String name, TypeMirror... bounds)
        {
            return typeParamBuilder.addTypeParameter(name, bounds);
        }

        public ClassBuilder addTypeParameter(TypeParameterElement param)
        {
            return typeParamBuilder.addTypeParameter(param);
        }

        public FieldBuilder addField(String name, String type)
        {
            FieldBuilder fieldBuilder = new VariableElementImpl.FieldBuilder(element, typeParamBuilder.resolvType(type), name);
            addEnclosedElement(fieldBuilder.getVariableElement());
            return fieldBuilder;
        }
        public ClassBuilder addEnclosedElement(Element el)
        {
            element.enclosedElements.add(el);
            return this;
        }
        public ClassBuilder addInterface(Class<?> element, String... typeArguments)
        {
            List<TypeMirror> ta = new ArrayList<>();
            for (String a : typeArguments)
            {
                ta.add(typeParamBuilder.resolvType(a));
            }
            return addInterface(TypeMirrorFactory.getDeclaredType(E.getTypeElement(element.getCanonicalName()), ta));
        }
        public ClassBuilder addInterface(Class<?> element, Class<?>... typeArguments)
        {
            return addInterface(TypeMirrorFactory.getDeclaredType(element, typeArguments));
        }
        public ClassBuilder addInterface(Class<?> intf)
        {
            if (!intf.isInterface())
            {
                throw new IllegalArgumentException(intf+" is not interface");
            }
            return addInterface(E.getTypeElement(intf.getCanonicalName()));
        }
        public ClassBuilder addInterface(CharSequence intf)
        {
            return addInterface(E.getTypeElement(intf));
        }
        public ClassBuilder addInterface(TypeElement intf)
        {
            return addInterface(intf.asType());
        }
        public ClassBuilder addInterface(TypeMirror intf)
        {
            element.interfaces.add(intf);
            return this;
        }
        public ClassBuilder setNestingKind(NestingKind nestingKind)
        {
            element.nestingKind = nestingKind;
            return this;
        }

        public ClassBuilder setSuperclass(Class<?> superclass)
        {
            return setSuperclass(E.getTypeElement(superclass.getName()));
        }

        public ClassBuilder setSuperclass(CharSequence superclass)
        {
            return setSuperclass(E.getTypeElement(superclass));
        }

        public ClassBuilder setSuperclass(TypeElement superclass)
        {
            return setSuperclass(superclass.asType());
        }

        public ClassBuilder setSuperclass(TypeMirror superclass)
        {
            element.superclass = superclass;
            return this;
        }

        public ClassBuilder setEnclosingElement(Element enclosingElement)
        {
            element.enclosingElement = enclosingElement;
            return this;
        }

        public ClassBuilder setQualifiedName(String name)
        {
            element.qualifiedName = E.getName(name);
            int idx = name.lastIndexOf('.');
            if (idx != -1)
            {
                element.simpleName = E.getName(name.substring(idx+1));
                element.enclosingElement = new PackageElementImpl(name.substring(0, idx));
            }
            else
            {
                element.simpleName = element.qualifiedName;
                element.enclosingElement = new PackageElementImpl("");
            }
            return this;
        }
        public ClassBuilder setType(DeclaredType type)
        {
            element.type = type;
            return this;
        }
        public ClassBuilder addModifier(Modifier modifier)
        {
            element.modifiers.add(modifier);
            return this;
        }
    }
    TypeElementImpl()
    {
        this("");
    }

    TypeElementImpl(TypeMirror[] bounds)
    {
        this("");
        assert bounds.length > 0;
        superclass = bounds[0];
        for (int ii=1;ii<bounds.length;ii++)
        {
            interfaces.add(bounds[ii]);
        }
    }

    TypeElementImpl(String name)
    {
        super(ElementKind.CLASS, name);
    }

    TypeElementImpl(Class<?> cls)
    {
        super(detectKind(cls), cls, cls.getModifiers(), cls.getSimpleName());
    }
    void init(Class<?> cls)
    {
        type = (DeclaredType) TypeMirrorFactory.getClassType(cls);
        qualifiedName = E.getName(cls.getName());
        if (cls.getSuperclass() != null)
        {
            superclass = TypeMirrorFactory.get(cls.getGenericSuperclass());
        }
        else
        {
            superclass = T.getNoType(TypeKind.NONE);
        }
        for (TypeVariable param : cls.getTypeParameters())
        {
            typeParameters.add(ElementFactory.getTypeParameterElement(param));
        }
        if (cls.isAnonymousClass())
        {
            nestingKind = NestingKind.ANONYMOUS;
        }
        else
        {
            if (cls.isLocalClass())
            {
                nestingKind = NestingKind.LOCAL;
            }
            else
            {
                if (cls.isMemberClass())
                {
                    nestingKind = NestingKind.MEMBER;
                }
                else
                {
                    nestingKind = NestingKind.TOP_LEVEL;
                }
            }
        }
        Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass != null)
        {
            enclosingElement = ElementFactory.get(enclosingClass);
        }
        else
        {
            enclosingElement = ElementFactory.getPackageElement(cls.getPackage());
        }
        if (cls.isAnnotation())
        {
            
        }
        else
        {
            if (cls.isEnum())
            {
                
            }
            else
            {
                for (Field field : cls.getDeclaredFields())
                {
                    enclosedElements.add(ElementFactory.get(field));
                }
                for (Constructor constructor : cls.getDeclaredConstructors())
                {
                    enclosedElements.add(ElementFactory.get(constructor));
                }
                for (Method method : cls.getDeclaredMethods())
                {
                    enclosedElements.add(ElementFactory.get(method));
                }
                for (Class<?> c : cls.getDeclaredClasses())
                {
                    enclosedElements.add(ElementFactory.get(c));
                }
                for (Type intf : cls.getGenericInterfaces())
                {
                    interfaces.add(TypeMirrorFactory.get(intf));
                }
            }
        }
    }

    void init(Type[] bounds)
    {
        assert bounds.length > 0;
        superclass = TypeMirrorFactory.get(bounds[0]);
        for (int ii=1;ii<bounds.length;ii++)
        {
            interfaces.add(TypeMirrorFactory.get(bounds[ii]));
        }
    }

    private static ElementKind detectKind(Class<?> cls)
    {
        if (cls.isAnnotation())
        {
            return ElementKind.ANNOTATION_TYPE;
        }
        else
        {
            if (cls.isEnum())
            {
                return ElementKind.ENUM;
            }
            else
            {
                if (cls.isInterface())
                {
                    return ElementKind.INTERFACE;
                }
                else
                {
                    return ElementKind.CLASS;
                }
            }
        }
    }
    @Override
    public List<? extends TypeParameterElement> getTypeParameters()
    {
        return typeParameters;
    }

    @Override
    public List<? extends Element> getEnclosedElements()
    {
        return enclosedElements;
    }

    @Override
    public NestingKind getNestingKind()
    {
        return nestingKind;
    }

    @Override
    public Name getQualifiedName()
    {
        return qualifiedName;
    }

    @Override
    public TypeMirror getSuperclass()
    {
        return superclass;
    }

    @Override
    public List<? extends TypeMirror> getInterfaces()
    {
        return interfaces;
    }

    @Override
    public Element getEnclosingElement()
    {
        return enclosingElement;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitType(this, p);
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
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
        final TypeElementImpl other = (TypeElementImpl) obj;
        if (!Objects.equals(this.enclosedElements, other.enclosedElements))
        {
            return false;
        }
        if (!Objects.equals(this.interfaces, other.interfaces))
        {
            return false;
        }
        if (this.nestingKind != other.nestingKind)
        {
            return false;
        }
        if (!Objects.equals(this.qualifiedName, other.qualifiedName))
        {
            return false;
        }
        if (!Objects.equals(this.superclass, other.superclass))
        {
            return false;
        }
        if (!Objects.equals(this.enclosingElement, other.enclosingElement))
        {
            return false;
        }
        if (!Objects.equals(this.typeParameters, other.typeParameters))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "TypeElementImpl{" + "qualifiedName=" + qualifiedName + '}';
    }

}
