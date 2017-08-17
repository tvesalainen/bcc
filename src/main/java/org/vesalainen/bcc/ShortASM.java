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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ShortASM extends IntASM implements TypeASM
{

    ShortASM(CodeDataOutput out, Map<String, Label> labels)
    {
        super(out, labels);
    }

    public void tipush(int value) throws IOException
    {
        out.writeByte(SIPUSH);
        out.writeByte(value);
    }
    public void taload() throws IOException
    {
        out.writeByte(SALOAD);
    }
    public void tastore() throws IOException
    {
        out.writeByte(SASTORE);
    }
    public void i2t() throws IOException
    {
        out.writeByte(I2S);
    }
}
