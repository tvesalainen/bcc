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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
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
import org.vesalainen.bcc.model.ExecutableElementImpl.ConstructorBuilder;
import org.vesalainen.bcc.model.ExecutableElementImpl.MethodBuilder;
import org.vesalainen.bcc.model.VariableElementImpl.VariableBuilder;

/**
 * @author Timo Vesalainen
 */
public class TypeElementImpl extends ElementImpl implements TypeElement
{
    private DeclaredType type;
    private List<Element> enclosedElements;
    private List<TypeMirror> interfaces = new ArrayList<>();
    private NestingKind nestingKind = NestingKind.TOP_LEVEL;
    private Name qualifiedName;
    private TypeMirror superclass;
    private List<TypeParameterElement> typeParameters = new ArrayList<>();
    private Class<?> cls = null;

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class ClassBuilder
    {
        private TypeElementImpl element = new TypeElementImpl();
        private TypeParameterBuilder typeParamBuilder;
        public ClassBuilder()
        {
            typeParamBuilder = new TypeParameterBuilder(element, element.typeParameters);
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
            // TODO add superclass type parameters!!!
            return element;
        }

        public ClassBuilder addTypeParameter(String name, Class<?>... bounds)
        {
            typeParamBuilder.addTypeParameter(name, bounds);
            return this;
        }

        public ClassBuilder addTypeParameter(String name, CharSequence... bounds)
        {
            typeParamBuilder.addTypeParameter(name, bounds);
            return this;
        }

        public ClassBuilder addTypeParameter(String name, TypeElement... bounds)
        {
            typeParamBuilder.addTypeParameter(name, bounds);
            return this;
        }

        public ClassBuilder addTypeParameter(String name, TypeMirror... bounds)
        {
            typeParamBuilder.addTypeParameter(name, bounds);
            return this;
        }

        public ClassBuilder addTypeParameter(TypeParameterElement param)
        {
            typeParamBuilder.addTypeParameter(param);
            return this;
        }

        public MethodBuilder addMethod(String name)
        {
            MethodBuilder methodBuilder = new ExecutableElementImpl.MethodBuilder(element, name, typeParamBuilder.getTypeArguments(), typeParamBuilder.getTypeParameterMap());
            addEnclosedElement(methodBuilder.getExecutableElement());
            return methodBuilder;
        }
        public ConstructorBuilder addConstructor()
        {
            ConstructorBuilder constructorBuilder = new ExecutableElementImpl.ConstructorBuilder(element, typeParamBuilder.getTypeArguments(), typeParamBuilder.getTypeParameterMap());
            addEnclosedElement(constructorBuilder.getExecutableElement());
            return constructorBuilder;
        }
        public VariableBuilder addField(String name, Class<?> type)
        {
            return addField(name, type.getCanonicalName());
        }
        public VariableBuilder addField(String name, String type)
        {
            VariableBuilder fieldBuilder = addField(name);
            fieldBuilder.setType(typeParamBuilder.resolvType(type));
            return fieldBuilder;
        }
        public VariableBuilder addField(String name)
        {
            VariableBuilder fieldBuilder = new VariableElementImpl.VariableBuilder(element, name, typeParamBuilder);
            addEnclosedElement(fieldBuilder.getVariableElement());
            return fieldBuilder;
        }
        public ClassBuilder addEnclosedElement(Element el)
        {
            if (element.enclosedElements == null)
            {
                element.enclosedElements = new ArrayList<>();
            }
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
            return addInterface(TypeMirrorFactory.getDeclaredType(El.getTypeElement(element.getCanonicalName()), ta));
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
            return addInterface(El.getTypeElement(intf.getCanonicalName()));
        }
        public ClassBuilder addInterface(CharSequence intf)
        {
            return addInterface(El.getTypeElement(intf));
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
            return setSuperclass(El.getTypeElement(superclass.getName()));
        }

        public ClassBuilder setSuperclass(CharSequence superclass)
        {
            return setSuperclass(El.getTypeElement(superclass));
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
            element.qualifiedName = El.getName(name);
            int idx = name.lastIndexOf('.');
            if (idx != -1)
            {
                element.simpleName = El.getName(name.substring(idx+1));
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
        super(ElementKind.CLASS, "");
    }

    TypeElementImpl(TypeMirror[] bounds)
    {
        super(ElementKind.CLASS, "");
        assert bounds.length > 0;
        superclass = bounds[0];
        for (int ii=1;ii<bounds.length;ii++)
        {
            interfaces.add(bounds[ii]);
        }
    }
    /**
     * This is two-phase construction. init is called 
     * @param cls 
     */
   TypeElementImpl(Class<?> cls)
    {
        super(detectKind(cls), cls, cls.getModifiers(), cls.getSimpleName());
        this.cls = cls;
    }
    void init(Class<?> cls)
    {
        this.qualifiedName = El.getName(cls.getCanonicalName());
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
        initTypeParameters();
        for (Type intf : cls.getGenericInterfaces())
        {
            interfaces.add(TypeMirrorFactory.get(intf));
        }
    }

    private void initTypeParameters()
    {
        for (TypeVariable param : cls.getTypeParameters())
        {
            typeParameters.add(ElementFactory.getTypeParameterElement(param));
        }
    }
    @Override
    public TypeMirror asType()
    {
        if (type == null)
        {
            type = (DeclaredType) TypeMirrorFactory.getClassType(cls);
        }
        return type;
    }

    @Override
    public Element getEnclosingElement()
    {
        if (enclosingElement == null)
        {
            Class<?> enclosingClass = cls.getEnclosingClass();
            if (enclosingClass != null)
            {
                enclosingElement = ElementFactory.get(enclosingClass);
            }
            else
            {
                enclosingElement = ElementFactory.getPackageElement(cls.getPackage());
            }
        }
        return enclosingElement;
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
        if (enclosedElements == null)
        {
            enclosedElements = new ArrayList<>();
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
        }
        return enclosedElements;
    }

    @Override
    public NestingKind getNestingKind()
    {
        if (nestingKind == null)
        {
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
        }
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
        if (superclass == null)
        {
            if (cls.getSuperclass() != null)
            {
                superclass = TypeMirrorFactory.get(cls.getGenericSuperclass());
            }
            else
            {
                superclass = Typ.getNoType(TypeKind.NONE);
            }
        }
        return superclass;
    }

    @Override
    public List<? extends TypeMirror> getInterfaces()
    {
        return interfaces;
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
        if (!Objects.equals(this.getQualifiedName(), other.getQualifiedName()))
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
