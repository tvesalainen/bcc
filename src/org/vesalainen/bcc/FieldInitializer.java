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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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

    protected FieldInitializer(VariableElement field)
    {
        this.field = field;
    }

    public abstract void init(MethodCompiler c) throws IOException;

    public static FieldInitializer getInstance(Class<?> cls, String name, boolean value)
    {
        return new IntInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getInstance(Class<?> cls, String name, byte value)
    {
        return new IntInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getInstance(Class<?> cls, String name, char value)
    {
        return new IntInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getInstance(Class<?> cls, String name, short value)
    {
        return new IntInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getInstance(Class<?> cls, String name, int value)
    {
        return new IntInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getInstance(Class<?> cls, String name, long value)
    {
        return new LongInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getInstance(Class<?> cls, String name, float value)
    {
        return new FloatInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getInstance(Class<?> cls, String name, double value)
    {
        return new DoubleInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getInstance(Class<?> cls, String name, String value)
    {
        return new StringInit(E.getField(cls, name), value);
    }
    public static FieldInitializer getObjectInstance(Class<?> cls, String name, Class<?> ocls)
    {
        return new ObjectInit(E.getField(cls, name), ocls);
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
            this(field, ElementFactory.get(cls));
        }
        public ObjectInit(VariableElement field, TypeElement cls)
        {
            super(field);
            this.cls = cls;
            constructor = E.getConstructor(cls);
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

}
