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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.AccessFlags;
import org.vesalainen.bcc.AccessFlags.MethodFlags;
import org.vesalainen.bcc.model.VariableElementImpl.VariableBuilder;

/**
 * @author Timo Vesalainen
 */
public class ExecutableElementImpl extends ElementImpl<ExecutableType> implements ExecutableElement 
{
    private List<TypeParameterElement> typeParameters = new ArrayList<>();
    private TypeMirror returnType = Typ.getNoType(TypeKind.VOID);
    private List<VariableElement> parameters = new ArrayList<>();
    private boolean varArgs;
    private List<TypeMirror> thrownTypes = new ArrayList<>();
    private AnnotationValue defaultValue;
    
    public static class MethodBuilder extends ConstructorBuilder
    {
        public MethodBuilder(TypeElement enclosingElement, String name, List<? extends TypeMirror> classTypeArguments, Map<String, TypeParameterElement> classTypeParameterMap)
        {
            super(enclosingElement, ElementKind.METHOD, name, classTypeArguments, classTypeParameterMap);
        }
        public MethodBuilder setReturnType(Class<?> returnType, String... typeArgs)
        {
            return setReturnType(returnType.getCanonicalName(), typeArgs);
        }
        public MethodBuilder setReturnType(String returnType, String... typeArgs)
        {
            Element element = resolvElement(returnType);
            switch (element.getKind())
            {
                case CLASS:
                case INTERFACE:
                    TypeElement te = (TypeElement) element;
                    List<TypeMirror> args = new ArrayList<>();
                    for (String a : typeArgs)
                    {
                        args.add(resolvType(a));
                    }
                    return setReturnType(new DeclaredTypeImpl(te, args));
                default:
                    if (typeArgs.length > 0)
                    {
                        throw new IllegalArgumentException("type args not used");
                    }
                    return setReturnType(element.asType());
            }
        }
        public MethodBuilder setReturnType(TypeMirror returnType)
        {
            exe.returnType = returnType;
            return this;
        }
    }
    public static class ConstructorBuilder
    {
        protected ExecutableElementImpl exe;
        private TypeParameterBuilder<ConstructorBuilder> typeParamBuilder;

        public ConstructorBuilder(TypeElement enclosingElement, List<? extends TypeMirror> classTypeArguments, Map<String, TypeParameterElement> classTypeParameterMap)
        {
            this(enclosingElement, ElementKind.CONSTRUCTOR, "<init>", classTypeArguments, classTypeParameterMap);
        }
        public ConstructorBuilder(TypeElement enclosingElement, ElementKind kind, String name, List<? extends TypeMirror> classTypeArguments, Map<String, TypeParameterElement> classTypeParameterMap)
        {
            exe = new ExecutableElementImpl(enclosingElement, kind, name);
            typeParamBuilder = new TypeParameterBuilder<>(this, enclosingElement, exe.typeParameters, classTypeArguments, classTypeParameterMap);
        }

        public ExecutableElement getExecutableElement()
        {
            exe.type = new ExecutableTypeImpl(exe);
            return exe;
        }
        
        public ConstructorBuilder addModifier(Modifier modifier)
        {
            exe.modifiers.add(modifier);
            return this;
        }
        public ConstructorBuilder addModifiers(int modifier)
        {
            MethodFlags.setModifiers(exe.modifiers, modifier);
            return this;
        }
        public VariableBuilder addParameter(String name)
        {
            VariableBuilder variableBuilder = new VariableElementImpl.VariableBuilder(exe, name, typeParamBuilder);
            exe.parameters.add(variableBuilder.getVariableElement());
            return variableBuilder;
        }
        public ConstructorBuilder addParameter(VariableElement param)
        {
            exe.parameters.add(param);
            return this;
        }
        public ConstructorBuilder addThrownType(Class<?> thrownType)
        {
            return addThrownType(resolvType(thrownType.getCanonicalName()));
        }
        public ConstructorBuilder addThrownType(String thrownType)
        {
            return addThrownType(resolvType(thrownType));
        }
        public ConstructorBuilder addThrownType(TypeMirror thrownType)
        {
            exe.thrownTypes.add(thrownType);
            return this;
        }

        public List<TypeMirror> getTypeArguments()
        {
            return typeParamBuilder.getTypeArguments();
        }

        protected TypeMirror resolvType(String type)
        {
            return typeParamBuilder.resolvType(type);
        }

        protected Element resolvElement(String type)
        {
            return typeParamBuilder.resolvElement(type);
        }

        public ConstructorBuilder addTypeParameter(String name, Class<?>... bounds)
        {
            return typeParamBuilder.addTypeParameter(name, bounds);
        }

        public ConstructorBuilder addTypeParameter(String name, CharSequence... bounds)
        {
            return typeParamBuilder.addTypeParameter(name, bounds);
        }

        public ConstructorBuilder addTypeParameter(String name, TypeElement... bounds)
        {
            return typeParamBuilder.addTypeParameter(name, bounds);
        }

        public ConstructorBuilder addTypeParameter(String name, TypeMirror... bounds)
        {
            return typeParamBuilder.addTypeParameter(name, bounds);
        }

        public ConstructorBuilder addTypeParameter(TypeParameterElement param)
        {
            return typeParamBuilder.addTypeParameter(param);
        }
        
    }

    public ExecutableElementImpl(Element enclosingElement, ElementKind kind, String name)
    {
        super(kind, name);
        this.enclosingElement = enclosingElement;
    }
    
    ExecutableElementImpl(Constructor constructor)
    {
        super(ElementKind.CONSTRUCTOR, constructor, constructor.getModifiers(), "<init>");
    }
    void init(Constructor constructor)
    {
        for (TypeVariable param : constructor.getTypeParameters())
        {
            typeParameters.add(ElementFactory.getTypeParameterElement(param));
        }
        type = TypeMirrorFactory.get(constructor);
        enclosingElement = ElementFactory.get(constructor.getDeclaringClass());
        returnType = Typ.getNoType(TypeKind.VOID);
        Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
        int index = 0;
        for (Type param : genericParameterTypes)
        {
            parameters.add(ElementFactory.getVariableElement(param, parameterAnnotations[index++]));
        }
        varArgs = constructor.isVarArgs();
        for (Type type : constructor.getGenericExceptionTypes())
        {
            thrownTypes.add(TypeMirrorFactory.get(type));
        }
    }

    ExecutableElementImpl(Method method)
    {
        super(ElementKind.METHOD, method, method.getModifiers(), method.getName());
    }
    void init(Method method)
    {
        for (TypeVariable param : method.getTypeParameters())
        {
            typeParameters.add(ElementFactory.getTypeParameterElement(param));
        }
        type = TypeMirrorFactory.get(method);
        enclosingElement = ElementFactory.get(method.getDeclaringClass());
        returnType = TypeMirrorFactory.get(method.getGenericReturnType());
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        int index = 0;
        for (Type param : genericParameterTypes)
        {
            parameters.add(ElementFactory.getVariableElement(param, parameterAnnotations[index++]));
        }
        varArgs = method.isVarArgs();
        for (Type type : method.getGenericExceptionTypes())
        {
            thrownTypes.add(TypeMirrorFactory.get(type));
        }
        Object defValue = method.getDefaultValue();
        if (defValue != null)
        {
            defaultValue = new AnnotationValueImpl(defValue);
        }
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters()
    {
        return typeParameters;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends Element> getEnclosedElements()
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitExecutable(this, p);
    }

    @Override
    public TypeMirror getReturnType()
    {
        return returnType;
    }

    @Override
    public List<? extends VariableElement> getParameters()
    {
        return parameters;
    }

    @Override
    public boolean isVarArgs()
    {
        return varArgs;
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes()
    {
        return thrownTypes;
    }

    @Override
    public AnnotationValue getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
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
        final ExecutableElementImpl other = (ExecutableElementImpl) obj;
        if (!Objects.equals(this.typeParameters, other.typeParameters))
        {
            return false;
        }
        if (!Objects.equals(this.enclosingElement, other.enclosingElement))
        {
            return false;
        }
        if (!Objects.equals(this.returnType, other.returnType))
        {
            return false;
        }
        if (!Objects.equals(this.parameters, other.parameters))
        {
            return false;
        }
        if (this.varArgs != other.varArgs)
        {
            return false;
        }
        if (!Objects.equals(this.thrownTypes, other.thrownTypes))
        {
            return false;
        }
        if (!Objects.equals(this.defaultValue, other.defaultValue))
        {
            return false;
        }
        return true;
    }


}
