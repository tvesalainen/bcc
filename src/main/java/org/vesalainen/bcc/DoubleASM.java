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
public class DoubleASM extends Assembler implements TypeASM
{

    DoubleASM(CodeDataOutput out, Map<String, Label> labels)
    {
        super(out, labels);
    }
    public void tadd() throws IOException
    {
        out.writeByte(DADD);
    }
    public void taload() throws IOException
    {
        out.writeByte(DALOAD);
    }
    public void tastore() throws IOException
    {
        out.writeByte(DASTORE);
    }
    public void tcmpg() throws IOException
    {
        out.writeByte(DCMPG);
    }
    public void tcmpl() throws IOException
    {
        out.writeByte(DCMPL);
    }
    public void tconst(int i) throws IOException
    {
        switch (i)
        {
            case 0:
                dconst_0();
                break;
            case 1:
                dconst_1();
                break;
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public void dconst_0() throws IOException
    {
        out.writeByte(DCONST_0);
    }
    public void dconst_1() throws IOException
    {
        out.writeByte(DCONST_1);
    }
    public void tdiv() throws IOException
    {
        out.writeByte(DDIV);
    }
    public void tload(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(DLOAD_0);
                break;
            case 1:
                out.writeByte(DLOAD_1);
                break;
            case 2:
                out.writeByte(DLOAD_2);
                break;
            case 3:
                out.writeByte(DLOAD_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(DLOAD);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(DLOAD);
                    out.writeShort(index);
                }
                break;
        }
    }
    public void tmul() throws IOException
    {
        out.writeByte(DMUL);
    }
    public void tneg() throws IOException
    {
        out.writeByte(DNEG);
    }
    public void trem() throws IOException
    {
        out.writeByte(115);
    }
    public void treturn() throws IOException
    {
        out.writeByte(DRETURN);
    }
    public void tstore(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(DSTORE_0);
                break;
            case 1:
                out.writeByte(DSTORE_1);
                break;
            case 2:
                out.writeByte(DSTORE_2);
                break;
            case 3:
                out.writeByte(DSTORE_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(DSTORE);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(DSTORE);
                    out.writeShort(index);
                }
                break;
        }
    }
    public void tsub() throws IOException
    {
        out.writeByte(DSUB);
    }
    public void f2t() throws IOException
    {
        out.writeByte(F2D);
    }
    public void i2t() throws IOException
    {
        out.writeByte(I2D);
    }
    public void l2t() throws IOException
    {
        out.writeByte(L2D);
    }

    public void tipush(int b) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tinc(int index, int con) throws IOException
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

    public void d2t() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tcmp() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmpeq(Object target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmpne(Object target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmplt(Object target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmpge(Object target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmpgt(Object target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void if_tcmple(Object target) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tconst_null() throws IOException
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
