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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.vesalainen.bcc.model.E;
import org.vesalainen.bcc.model.ElementFactory;

/**
 *
 * @author tkv
 */
public abstract class FieldInitializer
{

    protected VariableElement field;

    protected FieldInitializer(Field field)
    {
        this(ElementFactory.get(field));
    }
    protected FieldInitializer(VariableElement field)
    {
        this.field = field;
    }

    public abstract void init(MethodCompiler c) throws IOException;

    public static FieldInitializer getInstance(Field field, boolean value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Field field, byte value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Field field, char value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Field field, short value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Field field, int value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Field field, long value)
    {
        return new LongInit(field, value);
    }
    public static FieldInitializer getInstance(Field field, float value)
    {
        return new FloatInit(field, value);
    }
    public static FieldInitializer getInstance(Field field, double value)
    {
        return new DoubleInit(field, value);
    }
    public static FieldInitializer getInstance(Field field, String value)
    {
        return new StringInit(field, value);
    }
    public static FieldInitializer getObjectInstance(Field field, Class<?> cls)
    {
        return new ObjectInit(field, cls);
    }
    public static class IntInit extends FieldInitializer
    {
        int value;
        public IntInit(Field field, boolean value)
        {
            super(field);
            if (value)
            {
                this.value = 1;
            }
        }

        public IntInit(Field field, byte value)
        {
            super(field);
            this.value = value;
        }

        public IntInit(Field field, char value)
        {
            super(field);
            this.value = value;
        }

        public IntInit(Field field, short value)
        {
            super(field);
            this.value = value;
        }

        public IntInit(Field field, int value)
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
                c.putstatic(field);
            }
            else
            {
                c.aload(0);
                c.iconst(value);
                c.putfield(field);
            }
        }
    }
    public static class FloatInit extends FieldInitializer
    {
        private float value;
        public FloatInit(Field field, float value)
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
                c.putstatic(field);
            }
            else
            {
                c.aload(0);
                c.fconst(value);
                c.putfield(field);
            }
        }

    }
    public static class DoubleInit extends FieldInitializer
    {
        private double value;
        public DoubleInit(Field field, double value)
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
                c.putstatic(field);
            }
            else
            {
                c.aload(0);
                c.dconst(value);
                c.putfield(field);
            }
        }

    }
    public static class LongInit extends FieldInitializer
    {
        private long value;
        public LongInit(Field field, long value)
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
                c.putstatic(field);
            }
            else
            {
                c.aload(0);
                c.lconst(value);
                c.putfield(field);
            }
        }

    }
    public static class StringInit extends FieldInitializer
    {
        private String value;
        public StringInit(Field field, String value)
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
                c.putstatic(field);
            }
            else
            {
                c.aload(0);
                c.ldc(value);
                c.putfield(field);
            }
        }

    }

    public static class ObjectInit extends FieldInitializer
    {
        private TypeElement cls;
        private ExecutableElement constructor;
        public ObjectInit(Field field, Class<?> cls)
        {
            this(ElementFactory.get(field), ElementFactory.get(cls));
        }
        public ObjectInit(VariableElement field, TypeElement cls)
        {
            super(field);
            this.cls = cls;
            constructor = E.findConstructor(cls);
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (field.getModifiers().contains(Modifier.STATIC))
            {
                c.anew(cls);
                c.dup();
                c.invokespecial(constructor);
                c.putstatic(field);
            }
            else
            {
                c.aload(0);
                c.anew(cls);
                c.dup();
                c.invokespecial(constructor);
                c.putfield(field);
            }
        }
    }

}
