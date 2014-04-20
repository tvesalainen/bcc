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
public class VoidASM extends Assembler implements TypeASM
{

    VoidASM(CodeDataOutput out, Map<String, Label> labels)
    {
        super(out, labels);
    }
    public void treturn() throws IOException
    {
        out.writeByte(RETURN);
    }

    public void tipush(int b) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tconst(int i) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tconst_null() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tload(int index) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tstore(int index) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tinc(int index, int con) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void taload() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tastore() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tadd() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tsub() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tmul() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tdiv() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void trem() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tneg() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tshl() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tshr() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tushr() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tand() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tor() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void txor() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tcmp() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tcmpg() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmpeq(String target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmpne(String target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmplt(String target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmpge(String target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmpgt(String target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmple(String target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
