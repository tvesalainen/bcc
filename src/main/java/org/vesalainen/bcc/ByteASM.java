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
import java.util.Map;

/**
 *
 * @author tkv
 */
public class ByteASM extends IntASM implements TypeASM
{

    ByteASM(CodeDataOutput out, Map<String, Label> labels)
    {
        super(out, labels);
    }

    @Override
    public void tipush(int b) throws IOException
    {
        switch (b)
        {
            case 0:
                out.writeByte(ICONST_0);
                break;
            case 1:
                out.writeByte(ICONST_1);
                break;
            case 2:
                out.writeByte(ICONST_2);
                break;
            case 3:
                out.writeByte(ICONST_3);
                break;
            case 4:
                out.writeByte(ICONST_4);
                break;
            case 5:
                out.writeByte(ICONST_5);
                break;
            default:
                out.writeByte(BIPUSH);
                out.writeByte(b);
                break;
        }
    }
    public void taload() throws IOException
    {
        out.writeByte(BALOAD);
    }
    public void tastore() throws IOException
    {
        out.writeByte(BASTORE);
    }
    public void i2t() throws IOException
    {
        out.writeByte(I2B);
    }
}
