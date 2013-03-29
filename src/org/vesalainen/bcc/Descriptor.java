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

package org.vesalainen.bcc;

import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;

/**
 * @author Timo Vesalainen
 */
public class Descriptor 
{
    public static String getDesriptor(Element element)
    {
        try
        {
            switch (element.getKind())
            {
                case CLASS:
                case FIELD:
                case PARAMETER:
                case LOCAL_VARIABLE:
                    return getFieldDesriptor(element);
                case CONSTRUCTOR:
                case METHOD:
                    ExecutableElement executableElement = (ExecutableElement) element;
                    return getMethodDesriptor(executableElement);
                default:
                    throw new UnsupportedOperationException(element.getKind()+" unsupported");
            }
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    private static String getFieldDesriptor(Element element) throws IOException
    {
        return getFieldDesriptor(element.asType());
    }
    public static String getFieldDesriptor(TypeMirror type)
    {
        StringBuilder sb = new StringBuilder();
        try
        {
            fieldType(sb, type);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        return sb.toString();
    }
    private static void fieldType(StringBuilder sb, TypeMirror type) throws IOException
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
            case DECLARED:
            {
                sb.append('L');
                DeclaredType dt = (DeclaredType) type;
                TypeElement te = (TypeElement) dt.asElement();
                nestedQualifiedName(sb, te);
                sb.append(';');
            }
                break;
            case ARRAY:
            {
                sb.append('[');
                ArrayType at = (ArrayType) type;
                fieldType(sb, at.getComponentType());
            }
                break;
            case TYPEVAR:
            {
                sb.append('L');
                TypeVariable tv = (TypeVariable) type;
                DeclaredType upperBound = (DeclaredType) tv.getUpperBound();
                List<? extends TypeMirror> directSupertypes = Typ.directSupertypes(upperBound);
                TypeElement te = null;
                if (!directSupertypes.isEmpty())
                {
                    TypeMirror st = directSupertypes.get(0);
                    te = (TypeElement) Typ.asElement(st);
                }
                else
                {
                    te = El.getTypeElement(Object.class.getName());
                }
                sb.append(El.getBinaryName(te).toString().replace('.', '/'));
                sb.append(';');
            }
                break;
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
    }

    private static String getMethodDesriptor(ExecutableElement eel) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (VariableElement ve : eel.getParameters())
        {
            fieldType(sb, ve.asType());
        }
        sb.append(')');
        fieldType(sb, eel.getReturnType());
        return sb.toString();
    }

    protected static void nestedSimpleName(Appendable sb, TypeElement te) throws IOException
    {
        if (NestingKind.TOP_LEVEL != te.getNestingKind())
        {
            nestedSimpleName(sb, (TypeElement)te.getEnclosingElement());
            sb.append('$');
        }
        sb.append(te.getSimpleName());
    }
    protected static void nestedQualifiedName(Appendable sb, TypeElement te) throws IOException
    {
        PackageElement pkg = El.getPackageOf(te);
        if (!pkg.isUnnamed())
        {
            sb.append(pkg.getQualifiedName().toString().replace('.', '/'));
            sb.append('/');
        }
        nestedSimpleName(sb, te);
    }
    /**
     * Return fully qualified form from internal form. Eg. Ljava/lang/String returns
     * java.lang.String
     * @param fieldDecriptor
     * @return 
     */
    public static String getFullyQualifiedForm(String fieldDecriptor)
    {
        if (fieldDecriptor.length() == 1)
        {
            char cc = fieldDecriptor.charAt(0);
            switch (cc)
            {
                case 'Z':
                    return "boolean";
                case 'B':
                    return "byte";
                case 'C':
                    return "char";
                case 'S':
                    return "short";
                case 'I':
                    return "int";
                case 'J':
                    return "long";
                case 'F':
                    return "float";
                case 'D':
                    return "double";
                case 'V':
                    return "void";
                default:
                    return fieldDecriptor;
            }
        }
        else
        {
            if (fieldDecriptor.startsWith("L"))
            {
                fieldDecriptor = fieldDecriptor.substring(1, fieldDecriptor.length()-1);
            }
            if (fieldDecriptor.startsWith("["))
            {
                return getFullyQualifiedForm(fieldDecriptor.substring(1))+"[]";
            }
            return fieldDecriptor.replace('/', '.');  //.replace('$', '.');
        }
    }
}
