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

import org.vesalainen.bcc.Label.Branch;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CodeDataOutput implements DataOutput
{
    private Assembler asm;
    private ByteArrayOutputStream baos;
    private DataOutput out;
    private int lastOpCode;

    public CodeDataOutput(Assembler asm)
    {
        this.asm = asm;
        baos = new ByteArrayOutputStream();
        this.out = new DataOutputStream(baos);
    }

    public byte[] getCode()
    {
        return baos.toByteArray();
    }

    public int position() throws IOException
    {
        baos.flush();
        return baos.size();
    }

    public int getLastOpCode()
    {
        return lastOpCode;
    }

    public void writeUTF(String s) throws IOException
    {
        out.writeUTF(s);
    }

    public void writeShort(Branch branch) throws IOException
    {
        branch.setReference(position());
        writeShort(0);
    }

    public void writeShort(int v) throws IOException
    {
        if (v < Short.MIN_VALUE || v > 0xffff)
        {
            throw new IllegalArgumentException(String.valueOf(v));
        }
        out.writeShort(v);
    }

    public void writeLong(long v) throws IOException
    {
        out.writeLong(v);
    }

    public void writeInt(Branch branch) throws IOException
    {
        branch.setReference(position());
        branch.setWide(true);
        out.writeInt(0);
    }

    public void writeInt(int v) throws IOException
    {
        out.writeInt(v);
    }

    public void writeFloat(float v) throws IOException
    {
        out.writeFloat(v);
    }

    public void writeDouble(double v) throws IOException
    {
        out.writeDouble(v);
    }

    public void writeChars(String s) throws IOException
    {
        out.writeChars(s);
    }

    public void writeChar(int v) throws IOException
    {
        if (v < Character.MIN_VALUE || v > 0xffff)
        {
            throw new IllegalArgumentException(String.valueOf(v));
        }
        out.writeChar(v);
    }

    public void writeBytes(String s) throws IOException
    {
        out.writeBytes(s);
    }

    public void writeOpCode(int opCode) throws IOException
    {
        writeByte(opCode);
        lastOpCode = opCode;
    }

    public void writeByte(int v) throws IOException
    {
        if (v < Byte.MIN_VALUE || v > 0xff)
        {
            throw new IllegalArgumentException(String.valueOf(v));
        }
        out.writeByte(v);
    }

    public void writeBoolean(boolean v) throws IOException
    {
        out.writeBoolean(v);
    }

    public void write(byte[] b, int off, int len) throws IOException
    {
        out.write(b, off, len);
    }

    public void write(byte[] b) throws IOException
    {
        out.write(b);
    }

    public void write(int b) throws IOException
    {
        out.write(b);
    }
}
