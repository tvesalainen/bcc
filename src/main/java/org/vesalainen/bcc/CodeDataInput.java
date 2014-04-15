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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

/**
 *
 * @author tkv
 */
public class CodeDataInput extends DataInputStream
{
    private byte[] code;
    private MyByteArrayInputStream is;

    public CodeDataInput(byte[] code)
    {
        super(new MyByteArrayInputStream(code));
        this.code = code;
        is = (MyByteArrayInputStream) in;
    }

    public CodeDataInput(byte[] code, int offset)
    {
        super(new MyByteArrayInputStream(code, offset));
        this.code = code;
        is = (MyByteArrayInputStream) in;
    }

    public CodeDataInput branch(int pc)
    {
        return new CodeDataInput(code, pc);
    }

    public int pc()
    {
        return is.position();
    }

    private static class MyByteArrayInputStream extends ByteArrayInputStream
    {

        public MyByteArrayInputStream(byte[] buf, int offset)
        {
            super(buf, offset, buf.length - offset);
        }

        public MyByteArrayInputStream(byte[] buf)
        {
            super(buf);
        }

        public int position()
        {
            return pos;
        }
    }
}
