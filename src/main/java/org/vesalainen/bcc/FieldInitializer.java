/*
 * Copyright (C) 2012 Timo Vesalainen
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
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.vesalainen.bcc.model.El;

/**
 *
 * @author tkv
 */
public abstract class FieldInitializer
{

    protected VariableElement field;

    protected FieldInitializer(VariableElement field)
    {
        this.field = field;
    }

    public abstract void init(MethodCompiler c) throws IOException;

    public static FieldInitializer getInstance(VariableElement field, boolean value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(VariableElement field, byte value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(VariableElement field, char value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(VariableElement field, short value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(VariableElement field, int value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(VariableElement field, long value)
    {
        return new LongInit(field, value);
    }
    public static FieldInitializer getInstance(VariableElement field, float value)
    {
        return new FloatInit(field, value);
    }
    public static FieldInitializer getInstance(VariableElement field, double value)
    {
        return new DoubleInit(field, value);
    }
    public static FieldInitializer getInstance(VariableElement field, String value)
    {
        return new StringInit(field, value);
    }
    public static FieldInitializer getObjectInstance(VariableElement field, Class<?> ocls)
    {
        return new ObjectInit(field, ocls);
    }
    public static FieldInitializer getObjectInstance(VariableElement field, String ocls)
    {
        return new ObjectInit(field, ocls);
    }
    public static FieldInitializer getObjectInstance(VariableElement field, TypeElement ocls)
    {
        return new ObjectInit(field, ocls);
    }
    public static FieldInitializer getArrayInstance(VariableElement field, TypeMirror type, int size)
    {
        return new ArrayInit(field, type, size);
    }
    public static class IntInit extends FieldInitializer
    {
        int value;
        private IntInit(VariableElement field, boolean value)
        {
            super(field);
            if (value)
            {
                this.value = 1;
            }
        }

        private IntInit(VariableElement field, byte value)
        {
            super(field);
            this.value = value;
        }

        private IntInit(VariableElement field, char value)
        {
            super(field);
            this.value = value;
        }

        private IntInit(VariableElement field, short value)
        {
            super(field);
            this.value = value;
        }

        private IntInit(VariableElement field, int value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                c.iconst(value);
                c.putStaticField(field);
            }
            else
            {
                c.aload(0);
                c.iconst(value);
                c.putField(field);
            }
        }
    }
    public static class FloatInit extends FieldInitializer
    {
        private float value;
        public FloatInit(VariableElement field, float value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                c.fconst(value);
                c.putStaticField(field);
            }
            else
            {
                c.aload(0);
                c.fconst(value);
                c.putField(field);
            }
        }

    }
    public static class DoubleInit extends FieldInitializer
    {
        private double value;
        public DoubleInit(VariableElement field, double value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                c.dconst(value);
                c.putStaticField(field);
            }
            else
            {
                c.aload(0);
                c.dconst(value);
                c.putField(field);
            }
        }

    }
    public static class LongInit extends FieldInitializer
    {
        private long value;
        public LongInit(VariableElement field, long value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                c.lconst(value);
                c.putStaticField(field);
            }
            else
            {
                c.aload(0);
                c.lconst(value);
                c.putField(field);
            }
        }

    }
    public static class StringInit extends FieldInitializer
    {
        private String value;
        public StringInit(VariableElement field, String value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                c.ldc(value);
                c.putStaticField(field);
            }
            else
            {
                c.aload(0);
                c.ldc(value);
                c.putField(field);
            }
        }

    }

    public static class ObjectInit extends FieldInitializer
    {
        private TypeElement cls;
        private ExecutableElement constructor;
        public ObjectInit(VariableElement field, Class<?> cls)
        {
            this(field, cls.getCanonicalName());
        }
        public ObjectInit(VariableElement field, String cls)
        {
            this(field, El.getTypeElement(cls));
        }
        public ObjectInit(VariableElement field, TypeElement cls)
        {
            super(field);
            this.cls = cls;
            constructor = El.getConstructor(cls);
            if (constructor == null)
            {
                OutputStreamWriter sw = new OutputStreamWriter(System.err);
                El.printElements(sw, cls);
                for (Element e : cls.getEnclosedElements())
                {
                    El.printElements(sw, e);
                }
                try
                {
                    sw.flush();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(FieldInitializer.class.getName()).log(Level.SEVERE, null, ex);
                }
                throw new IllegalArgumentException("no constructor for "+cls);
            }
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                c.anew(cls);
                c.dup();
                c.invokespecial(constructor);
                c.putStaticField(field);
            }
            else
            {
                c.aload(0);
                c.anew(cls);
                c.dup();
                c.invokespecial(constructor);
                c.putField(field);
            }
        }
    }

    public static class ArrayInit extends FieldInitializer
    {
        private final TypeMirror type;
        private final int size;
        public ArrayInit(VariableElement field, TypeMirror type, int size)
        {
            super(field);
            this.type = type;
            this.size = size;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                c.newarray(type, size);
                c.putStaticField(field);
            }
            else
            {
                c.aload(0);
                c.newarray(type, size);
                c.putField(field);
            }
        }
    }

}
