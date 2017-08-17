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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class ConstantInfo implements Writable
{
    public static final int CONSTANT_Class=7;
    public static final int CONSTANT_Fieldref=9;
    public static final int CONSTANT_Methodref=10;
    public static final int CONSTANT_InterfaceMethodref=11;
    public static final int CONSTANT_String=8;
    public static final int CONSTANT_Integer=3;
    public static final int CONSTANT_Float=4;
    public static final int CONSTANT_Long=5;
    public static final int CONSTANT_Double=6;
    public static final int CONSTANT_NameAndType=12;
    public static final int CONSTANT_Utf8=1;

    private int tag;

    public static ConstantInfo read(DataInput in) throws IOException
    {
        int tag = in.readUnsignedByte();
        switch (tag)
        {
            case CONSTANT_Class:
                return new Clazz(in);
            case CONSTANT_Fieldref:
                return new Fieldref(in);
            case CONSTANT_Methodref:
                return new Methodref(in);
            case CONSTANT_InterfaceMethodref:
                return new InterfaceMethodref(in);
            case CONSTANT_String:
                return new ConstantString(in);
            case CONSTANT_Integer:
                return new ConstantInteger(in);
            case CONSTANT_Float:
                return new ConstantFloat(in);
            case CONSTANT_Long:
                return new ConstantLong(in);
            case CONSTANT_Double:
                return new ConstantDouble(in);
            case CONSTANT_NameAndType:
                return new NameAndType(in);
            case CONSTANT_Utf8:
                return new Utf8(in);
            default:
                throw new ClassFormatError("Unknown constant tag "+tag);
        }
    }

    public ConstantInfo(int tag)
    {
        switch (tag)
        {
            case CONSTANT_Class:
            case CONSTANT_Fieldref:
            case CONSTANT_Methodref:
            case CONSTANT_InterfaceMethodref:
            case CONSTANT_String:
            case CONSTANT_Integer:
            case CONSTANT_Float:
            case CONSTANT_Long:
            case CONSTANT_Double:
            case CONSTANT_NameAndType:
            case CONSTANT_Utf8:
                this.tag = tag;
                break;
            default:
                throw new IllegalArgumentException("unknown constant info tag="+tag);
        }
    }

    public ConstantInfo(int tag, DataInput in) throws IOException
    {
        this.tag = tag;
        initialize(in);
    }

    protected abstract void initialize(DataInput in) throws IOException;

    public void write(DataOutput out) throws IOException
    {
        out.writeByte(tag);
    }

    public int getTag()
    {
        return tag;
    }

    public static class Clazz extends ConstantInfo
    {
        private int name_index;

        public Clazz(int name_index)
        {
            super(CONSTANT_Class);
            this.name_index = name_index;
        }

        public Clazz(DataInput in) throws IOException
        {
            super(CONSTANT_Class, in);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            name_index = in.readUnsignedShort();
        }

        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeShort(name_index);
        }

        public int getName_index()
        {
            return name_index;
        }

        @Override
        public java.lang.String toString()
        {
            return "Clazz{" + "name_index=" + name_index + '}';
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
            final Clazz other = (Clazz) obj;
            if (this.name_index != other.name_index)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 47 * hash + this.name_index;
            return hash;
        }

    }
    public static class Ref extends ConstantInfo
    {
        private int class_index;
    	private int name_and_type_index;

        public Ref(int tag, int class_index, int name_and_type_index)
        {
            super(tag);
            this.class_index = class_index;
            this.name_and_type_index = name_and_type_index;
        }

        public Ref(int tag, DataInput in) throws IOException
        {
            super(tag, in);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            class_index = in.readUnsignedShort();
            name_and_type_index = in.readUnsignedShort();
        }
        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeShort(class_index);
            out.writeShort(name_and_type_index);
        }

        public int getClass_index()
        {
            return class_index;
        }

        public int getName_and_type_index()
        {
            return name_and_type_index;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (!(obj instanceof Ref))
            {
                return false;
            }
            final Ref other = (Ref) obj;
            if (this.class_index != other.class_index)
            {
                return false;
            }
            if (this.name_and_type_index != other.name_and_type_index)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 97 * hash + this.class_index;
            hash = 97 * hash + this.name_and_type_index;
            return hash;
        }


    }
    public static class Fieldref extends Ref
    {

        public Fieldref(int class_index, int name_and_type_index)
        {
            super(CONSTANT_Fieldref, class_index, name_and_type_index);
        }
        public Fieldref(DataInput in) throws IOException
        {
            super(CONSTANT_Fieldref, in);
        }

        @Override
        public java.lang.String toString()
        {
            return "Fieldref{" + '}';
        }

    }
    public static class Methodref extends Ref
    {

        public Methodref(int class_index, int name_and_type_index)
        {
            super(CONSTANT_Methodref, class_index, name_and_type_index);
        }
        public Methodref(DataInput in) throws IOException
        {
            super(CONSTANT_Methodref, in);
        }

        @Override
        public java.lang.String toString()
        {
            return "Methodref{" + '}';
        }

    }
    public static class InterfaceMethodref extends Ref
    {
        public InterfaceMethodref(int class_index, int name_and_type_index)
        {
            super(CONSTANT_InterfaceMethodref, class_index, name_and_type_index);
        }
        public InterfaceMethodref(DataInput in) throws IOException
        {
            super(CONSTANT_InterfaceMethodref, in);
        }

        @Override
        public java.lang.String toString()
        {
            return "InterfaceMethodref{" + '}';
        }

    }
    public static class ConstantString extends ConstantInfo
    {
        private int string_index;

        public ConstantString(int string_index)
        {
            super(CONSTANT_String);
            this.string_index = string_index;
        }

        public ConstantString(DataInput in) throws IOException
        {
            super(CONSTANT_String, in);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            string_index = in.readUnsignedShort();
        }

        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeShort(string_index);
        }

        public int getString_index()
        {
            return string_index;
        }

        @Override
        public String toString()
        {
            return "String{" + "string_index=" + string_index + '}';
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
            final ConstantString other = (ConstantString) obj;
            if (this.string_index != other.string_index)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 43 * hash + this.string_index;
            return hash;
        }

    }
    public static class ConstantInteger extends ConstantInfo
    {
        private int constant;

        public ConstantInteger(int bytes)
        {
            super(CONSTANT_Integer);
            this.constant = bytes;
        }

        public ConstantInteger(DataInput in) throws IOException
        {
            super(CONSTANT_Integer, in);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            constant = in.readInt();
        }

        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeInt(constant);
        }

        public int getConstant()
        {
            return constant;
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
            final ConstantInteger other = (ConstantInteger) obj;
            if (this.constant != other.constant)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 53 * hash + this.constant;
            return hash;
        }

    }
    public static class ConstantFloat extends ConstantInfo
    {
        private int constant;

        public ConstantFloat(int bytes)
        {
            super(CONSTANT_Float);
            this.constant = bytes;
        }

        public ConstantFloat(DataInput in) throws IOException
        {
            super(CONSTANT_Float, in);
        }

        ConstantFloat(float constant)
        {
            super(CONSTANT_Float);
            this.constant = Float.floatToRawIntBits(constant);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            constant = in.readInt();
        }

        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeInt(constant);
        }

        public float getConstant()
        {
            return Float.intBitsToFloat(constant);
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
            final ConstantFloat other = (ConstantFloat) obj;
            if (this.constant != other.constant)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 71 * hash + this.constant;
            return hash;
        }

    }
    public static class ConstantLong extends ConstantInfo
    {
        private int high_bytes;
    	private int low_bytes;

        public ConstantLong(int high_bytes, int low_bytes)
        {
            super(CONSTANT_Long);
            this.high_bytes = high_bytes;
            this.low_bytes = low_bytes;
        }

        public ConstantLong(DataInput in) throws IOException
        {
            super(CONSTANT_Long, in);
        }

        public ConstantLong(long constant)
        {
            super(CONSTANT_Long);
            high_bytes = (int) (constant >> 32);
            low_bytes = (int) (constant & 0xffffffff);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            high_bytes = in.readInt();
            low_bytes = in.readInt();
        }

        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeInt(high_bytes);
            out.writeInt(low_bytes);
        }

        public long getConstant()
        {
            return ((long) high_bytes << 32) + low_bytes;
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
            final ConstantLong other = (ConstantLong) obj;
            if (this.high_bytes != other.high_bytes)
            {
                return false;
            }
            if (this.low_bytes != other.low_bytes)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 97 * hash + this.high_bytes;
            hash = 97 * hash + this.low_bytes;
            return hash;
        }


    }
    public static class ConstantDouble extends ConstantInfo
    {
        private int high_bytes;
    	private int low_bytes;

        public ConstantDouble(int high_bytes, int low_bytes)
        {
            super(CONSTANT_Double);
            this.high_bytes = high_bytes;
            this.low_bytes = low_bytes;
        }

        public ConstantDouble(DataInput in) throws IOException
        {
            super(CONSTANT_Double, in);
        }

        public ConstantDouble(double constant)
        {
            super(CONSTANT_Double);
            long l = Double.doubleToRawLongBits(constant);
            high_bytes = (int) (l >> 32);
            low_bytes = (int) (l & 0xffffffff);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            high_bytes = in.readInt();
            low_bytes = in.readInt();
        }

        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeInt(high_bytes);
            out.writeInt(low_bytes);
        }

        public double getConstant()
        {
            return Double.longBitsToDouble(((long) high_bytes << 32) + low_bytes);
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
            final ConstantDouble other = (ConstantDouble) obj;
            if (this.high_bytes != other.high_bytes)
            {
                return false;
            }
            if (this.low_bytes != other.low_bytes)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 83 * hash + this.high_bytes;
            hash = 83 * hash + this.low_bytes;
            return hash;
        }

    }
    public static class NameAndType extends ConstantInfo
    {
        private int name_index;
    	private int descriptor_index;

        public NameAndType(int name_index, int descriptor_index)
        {
            super(CONSTANT_NameAndType);
            this.name_index = name_index;
            this.descriptor_index = descriptor_index;
        }

        public NameAndType(DataInput in) throws IOException
        {
            super(CONSTANT_NameAndType, in);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            name_index = in.readUnsignedShort();
            descriptor_index = in.readUnsignedShort();
        }

        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeShort(name_index);
            out.writeShort(descriptor_index);
        }

        public int getDescriptor_index()
        {
            return descriptor_index;
        }

        public int getName_index()
        {
            return name_index;
        }

        @Override
        public java.lang.String toString()
        {
            return "NameAndType{" + "name_index=" + name_index + "descriptor_index=" + descriptor_index + '}';
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
            final NameAndType other = (NameAndType) obj;
            if (this.name_index != other.name_index)
            {
                return false;
            }
            if (this.descriptor_index != other.descriptor_index)
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 97 * hash + this.name_index;
            hash = 97 * hash + this.descriptor_index;
            return hash;
        }

    }
    public static class Utf8 extends ConstantInfo
    {
    	private CharSequence string;

        public Utf8(CharSequence string)
        {
            super(CONSTANT_Utf8);
            this.string = string;
        }

        public Utf8(DataInput in) throws IOException
        {
            super(CONSTANT_Utf8, in);
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
            string = in.readUTF();
        }

        @Override
        public void write(DataOutput out) throws IOException
        {
            super.write(out);
            out.writeUTF(string.toString());
        }

        public java.lang.String getString()
        {
            return string.toString();
        }

        @Override
        public java.lang.String toString()
        {
            return "Utf8{" + "string=" + string + '}';
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
            final Utf8 other = (Utf8) obj;
            if ((this.string == null) ? (other.string != null) : !this.string.equals(other.string))
            {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 41 * hash + (this.string != null ? this.string.hashCode() : 0);
            return hash;
        }

    }
    static class Filler extends ConstantInfo
    {

        public Filler()
        {
            super(1); // must be valid but is considered unusable.
        }

        @Override
        protected void initialize(DataInput in) throws IOException
        {
        }

        @Override
        public void write(DataOutput out) throws IOException
        {
        }
        
    }

}
