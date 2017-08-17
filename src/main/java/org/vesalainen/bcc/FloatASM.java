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
public class FloatASM extends Assembler implements TypeASM
{

    FloatASM(CodeDataOutput out, Map<String, Label> labels)
    {
        super(out, labels);
    }
    public void tadd() throws IOException
    {
        out.writeByte(FADD);
    }
    public void taload() throws IOException
    {
        out.writeByte(FALOAD);
    }
    public void tastore() throws IOException
    {
        out.writeByte(FASTORE);
    }
    public void tcmpg() throws IOException
    {
        out.writeByte(FCMPG);
    }
    public void tcmpl() throws IOException
    {
        out.writeByte(FCMPL);
    }
    public void tconst(int i) throws IOException
    {
        switch (i)
        {
            case 0:
                fconst_0();
                break;
            case 1:
                fconst_1();
                break;
            case 2:
                fconst_2();
                break;
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public void fconst_0() throws IOException
    {
        out.writeByte(FCONST_0);
    }
    public void fconst_1() throws IOException
    {
        out.writeByte(FCONST_1);
    }
    public void fconst_2() throws IOException
    {
        out.writeByte(FCONST_2);
    }
    public void tdiv() throws IOException
    {
        out.writeByte(FDIV);
    }
    public void tload(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(FLOAD_0);
                break;
            case 1:
                out.writeByte(FLOAD_1);
                break;
            case 2:
                out.writeByte(FLOAD_2);
                break;
            case 3:
                out.writeByte(FLOAD_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(FLOAD);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(FLOAD);
                    out.writeShort(index);
                }
                break;
        }
    }
    public void tmul() throws IOException
    {
        out.writeByte(FMUL);
    }
    public void tneg() throws IOException
    {
        out.writeByte(FNEG);
    }
    public void trem() throws IOException
    {
        out.writeByte(FREM);
    }
    public void treturn() throws IOException
    {
        out.writeByte(FRETURN);
    }
    public void tstore(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(FSTORE_0);
                break;
            case 1:
                out.writeByte(FSTORE_1);
                break;
            case 2:
                out.writeByte(FSTORE_2);
                break;
            case 3:
                out.writeByte(FSTORE_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(FSTORE);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(FSTORE);
                    out.writeShort(index);
                }
                break;
        }
    }
    public void tsub() throws IOException
    {
        out.writeByte(FSUB);
    }
    public void d2t() throws IOException
    {
        out.writeByte(D2F);
    }
    public void i2t() throws IOException
    {
        out.writeByte(I2F);
    }
    public void l2t() throws IOException
    {
        out.writeByte(L2F);
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

    public void f2t() throws IOException
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
