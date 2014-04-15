/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.vesalainen.bcc;

import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import org.vesalainen.bcc.model.El;

/**
 * @author Timo Vesalainen
 */
public class Signature extends Descriptor
{
    
    public static String getSignature(Element element)
    {
        try
        {
            switch (element.getKind())
            {
                case CLASS:
                    TypeElement typeElement = (TypeElement) element;
                    return getClassSignature(typeElement);
                case FIELD:
                case PARAMETER:
                case LOCAL_VARIABLE:
                    VariableElement variableElement = (VariableElement) element;
                    return getFieldSignature(variableElement);
                case CONSTRUCTOR:
                case STATIC_INIT:
                case METHOD:
                    ExecutableElement executableElement = (ExecutableElement) element;
                    return getMethodSignature(executableElement);
                default:
                    throw new UnsupportedOperationException(element.getKind()+" unsupported");
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    /**
     * ClassSignature:
     *  FormalTypeParametersopt SuperclassSignature SuperinterfaceSignature*
     * 
     * <U:Ljava/lang/Integer;:Ljava/io/Serializable;>Lorg/vesalainen/bcc/Test;Ljava/lang/Cloneable;;
     * 
     * @param clazz
     * @return 
     */
    private static String getClassSignature(TypeElement element) throws IOException
    {
        Result sb = new Result();
        formalTypeParameters(sb, element.getTypeParameters());
        TypeMirror superclass = element.getSuperclass();
        // SuperclassSignature:
        //     ClassTypeSignature
        if (superclass != null)
        {
            classTypeSignature(sb, superclass);
        }
        // SuperinterfaceSignature:
        //    ClassTypeSignature
        for (TypeMirror intf : element.getInterfaces())
        {
            classTypeSignature(sb, intf);
        }
        return sb.toString();
    }
    /**
     * FormalTypeParameters:
     *   < FormalTypeParameter+ >
     * @param clazz
     * @return 
     */
    private static void formalTypeParameters(Result sb, List<? extends TypeParameterElement> typeParameters) throws IOException
    {
        if (!typeParameters.isEmpty())
        {
            sb.setNeedsSignature(true);
            sb.append('<');
            for (TypeParameterElement typeParameter : typeParameters)
            {
                formalTypeParameter(sb, typeParameter);
            }
            sb.append('>');
        }
    }

    /**
     * FormalTypeParameter:
     *   Identifier ClassBound InterfaceBound*
     * @param typeVariable
     * @return 
     */
    private static void formalTypeParameter(Result sb, TypeParameterElement typeParameter) throws IOException
    {
        sb.append(typeParameter.getSimpleName());
        List<? extends TypeMirror> bounds = typeParameter.getBounds();
        if (!bounds.isEmpty())
        {
            TypeMirror tm = bounds.get(0);
            if (TypeKind.DECLARED == tm.getKind())
            {
                DeclaredType dt = (DeclaredType) tm;
                if (ElementKind.CLASS != dt.asElement().getKind())
                {
                    sb.append(':');
                }
            }
        }
        for (TypeMirror bound : bounds)
        {
            sb.append(':');
            fieldTypeSignature(sb, bound);
        }
    }
    /**
     * ClassTypeSignature:
     *   L PackageSpecifieropt SimpleClassTypeSignature ClassTypeSignatureSuffix* ;
     * @param sb
     * @param type 
     */
    private static void classTypeSignature(Result sb, TypeMirror type) throws IOException
    {
        sb.append('L');
        packageSpecifier(sb, type);
        simpleClassTypeSignature(sb, type);
        classTypeSignatureSuffix(sb, type);
        sb.append(';');
    }

    /**
     * PackageSpecifier:
     *   Identifier / PackageSpecifier*
     * @param sb
     * @param type 
     */
    private static void packageSpecifier(Result sb, TypeMirror type) throws IOException
    {
        switch (type.getKind())
        {
            case DECLARED:
                DeclaredType dt = (DeclaredType) type;
                PackageElement pkg = El.getPackageOf(dt.asElement());
                sb.append(pkg.getQualifiedName().toString().replace('.', '/'));
                sb.append('/');
                break;
            default:
                throw new UnsupportedOperationException(type.getKind()+" unsupported");
        }
    }

    /**
     * SimpleClassTypeSignature:
     *   Identifier TypeArgumentsopt
     * @param type 
     */
    private static void simpleClassTypeSignature(Result sb, TypeMirror type) throws IOException
    {
        switch (type.getKind())
        {
            case DECLARED:
                DeclaredType dt = (DeclaredType) type;
                TypeElement te = (TypeElement) dt.asElement();
                nestedSimpleName(sb, te);
                typeArguments(sb, dt.getTypeArguments());
                break;
            default:
                throw new UnsupportedOperationException(type.getKind()+" unsupported");
        }
    }

    private static void typeArguments(Result sb, List<? extends TypeMirror> typeArguments) throws IOException
    {
        if (!typeArguments.isEmpty())
        {
            sb.append('<');
            sb.setNeedsSignature(true);
            for (TypeMirror ta : typeArguments)
            {
                typeArgument(sb, ta);
            }
            sb.append('>');
        }
    }
    private static void typeArgument(Result sb, TypeMirror typeArgument) throws IOException
    {
        switch (typeArgument.getKind())
        {
            case DECLARED:
                fieldTypeSignature(sb, typeArgument);
                break;
            case TYPEVAR:
                fieldTypeSignature(sb, typeArgument);
                break;
            case WILDCARD:
                WildcardType wildcardType = (WildcardType) typeArgument;
                TypeMirror extendsBound = wildcardType.getExtendsBound();
                if (extendsBound != null)
                {
                    sb.append('+');
                    fieldTypeSignature(sb, extendsBound);
                }
                TypeMirror superBound = wildcardType.getSuperBound();
                if (superBound != null)
                {
                    sb.append('-');
                    fieldTypeSignature(sb, superBound);
                }
                break;
            default:
                throw new UnsupportedOperationException(typeArgument.getKind()+" unsupported");
        }
    }
    private static void classTypeSignatureSuffix(Result sb, TypeMirror type)
    {
    }
    /**
     * FieldTypeSignature:
     *   ClassTypeSignature
     *   ArrayTypeSignature
     *   TypeVariableSignature
     * @param type
     * @return 
     */
    private static void fieldTypeSignature(Result sb, TypeMirror type) throws IOException
    {
        switch (type.getKind())
        {
            case DECLARED:
                classTypeSignature(sb, type);
                break;
            case TYPEVAR:
                typeVariableSignature(sb, (TypeVariable)type);
                break;
            case ARRAY:
                arrayTypeSignature(sb, (ArrayType)type);
                break;
            default:
                //throw new UnsupportedOperationException(type.getKind()+" unsupported");
        }
    }

    private static void typeVariableSignature(Result sb, TypeVariable type) throws IOException
    {
        sb.setNeedsSignature(true);
        sb.append('T');
        TypeParameterElement tpe = (TypeParameterElement) type.asElement();
        sb.append(tpe.getSimpleName());
        sb.append(';');
    }

    private static String getFieldSignature(VariableElement variableElement) throws IOException
    {
        Result sb = new Result();
        fieldTypeSignature(sb, variableElement.asType());
        return sb.toString();
    }

    private static void arrayTypeSignature(Result sb, ArrayType type) throws IOException
    {
        sb.append('[');
        typeSignature(sb, type.getComponentType());
    }

    private static void typeSignature(Result sb, TypeMirror type) throws IOException
    {
        switch (type.getKind())
        {
            case BYTE:
                sb.append('B');
                break;
            case CHAR:
                sb.append('C');
                break;
            case DOUBLE:
                sb.append('D');
                break;
            case FLOAT:
                sb.append('F');
                break;
            case INT:
                sb.append('I');
                break;
            case LONG:
                sb.append('J');
                break;
            case SHORT:
                sb.append('S');
                break;
            case BOOLEAN:
                sb.append('Z');
                break;
            case VOID:
                sb.append('V');
                break;
            default:
                fieldTypeSignature(sb, type);
        }
    }
    /**
     * MethodTypeSignature:
     *  FormalTypeParametersopt (TypeSignature*) ReturnType ThrowsSignature*
     * @param method
     * @return 
     */
    private static String getMethodSignature(ExecutableElement executableElement) throws IOException
    {
        Result sb = new Result();
        formalTypeParameters(sb, executableElement.getTypeParameters());
        sb.append('(');
        for (VariableElement param : executableElement.getParameters())
        {
            typeSignature(sb, param.asType());
        }
        sb.append(')');
        // ReturnType:
        //    TypeSignature
        //    VoidDescriptor
        TypeMirror returnType = executableElement.getReturnType();
        if (TypeKind.VOID.equals(returnType.getKind()))
        {
            sb.append('V');
        }
        else
        {
            typeSignature(sb, returnType);
        }

        for (TypeMirror param : executableElement.getThrownTypes())
        {
            getThrowsSignature(sb, param);
        }
        return sb.toString();
    }

    private static void getThrowsSignature(Result sb, TypeMirror param)
    {
        // If the throws clause of a method or constructor does not involve type variables, the ThowsSignature may be elided from the MethodTypeSignature.
    }

    private static class Result implements Appendable
    {
        private StringBuilder sb = new StringBuilder();
        private boolean needsSignature;

        private void setNeedsSignature(boolean needsSignature)
        {
            this.needsSignature = needsSignature;
        }
        
        @Override
        public Appendable append(CharSequence csq) throws IOException
        {
            return sb.append(csq);
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException
        {
            return sb.append(csq, start, end);
        }

        @Override
        public Appendable append(char c) throws IOException
        {
            return sb.append(c);
        }

        @Override
        public String toString()
        {
            if (needsSignature)
            {
                return sb.toString();
            }
            else
            {
                return "";
            }
        }
        
    }
}
