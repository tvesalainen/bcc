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

import org.vesalainen.bcc.type.ConstructorWrapper;
import java.io.IOException;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 *
 * @author tkv
 */
public abstract class FieldInitializer
{

    protected Member field;

    protected FieldInitializer(Member field)
    {
        this.field = field;
    }

    public abstract void init(MethodCompiler c) throws IOException;

    public static FieldInitializer getInstance(Member field, boolean value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Member field, byte value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Member field, char value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Member field, short value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Member field, int value)
    {
        return new IntInit(field, value);
    }
    public static FieldInitializer getInstance(Member field, long value)
    {
        return new LongInit(field, value);
    }
    public static FieldInitializer getInstance(Member field, float value)
    {
        return new FloatInit(field, value);
    }
    public static FieldInitializer getInstance(Member field, double value)
    {
        return new DoubleInit(field, value);
    }
    public static FieldInitializer getInstance(Member field, String value)
    {
        return new StringInit(field, value);
    }
    public static FieldInitializer getObjectInstance(Member field, Type cls)
    {
        return new ObjectInit(field, cls);
    }
    public static class IntInit extends FieldInitializer
    {
        int value;
        public IntInit(Member field, boolean value)
        {
            super(field);
            if (value)
            {
                this.value = 1;
            }
        }

        public IntInit(Member field, byte value)
        {
            super(field);
            this.value = value;
        }

        public IntInit(Member field, char value)
        {
            super(field);
            this.value = value;
        }

        public IntInit(Member field, short value)
        {
            super(field);
            this.value = value;
        }

        public IntInit(Member field, int value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (Modifier.isStatic(field.getModifiers()))
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
        public FloatInit(Member field, float value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (Modifier.isStatic(field.getModifiers()))
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
        public DoubleInit(Member field, double value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (Modifier.isStatic(field.getModifiers()))
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
        public LongInit(Member field, long value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (Modifier.isStatic(field.getModifiers()))
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
        public StringInit(Member field, String value)
        {
            super(field);
            this.value = value;
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (Modifier.isStatic(field.getModifiers()))
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
        private Type cls;
        private Member constructor;
        public ObjectInit(Member field, Type cls)
        {
            super(field);
            this.cls = cls;
            constructor = new ConstructorWrapper(cls);
        }

        @Override
        public void init(MethodCompiler c) throws IOException
        {
            if (Modifier.isStatic(field.getModifiers()))
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
