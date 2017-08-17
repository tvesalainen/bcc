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
public class LongASM extends Assembler implements TypeASM
{

    LongASM(CodeDataOutput out, Map<String, Label> labels)
    {
        super(out, labels);
    }

    public void tadd() throws IOException
    {
        out.writeByte(LADD);
    }

    public void taload() throws IOException
    {
        out.writeByte(LALOAD);
    }

    public void tand() throws IOException
    {
        out.writeByte(LAND);
    }

    public void tastore() throws IOException
    {
        out.writeByte(LASTORE);
    }

    public void tcmp() throws IOException
    {
        out.writeByte(LCMP);
    }

    public void tconst(int i) throws IOException
    {
        switch (i)
        {
            case 0:
                tconst_0();
                break;
            case 1:
                tconst_1();
                break;
            default:
                throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    public void tconst_0() throws IOException
    {
        out.writeByte(LCONST_0);
    }

    public void tconst_1() throws IOException
    {
        out.writeByte(LCONST_1);
    }

    public void tdiv() throws IOException
    {
        out.writeByte(LDIV);
    }

    public void tload(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(LLOAD_0);
                break;
            case 1:
                out.writeByte(LLOAD_1);
                break;
            case 2:
                out.writeByte(LLOAD_2);
                break;
            case 3:
                out.writeByte(LLOAD_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(LLOAD);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(LLOAD);
                    out.writeShort(index);
                }
                break;
        }
    }

    public void tmul() throws IOException
    {
        out.writeByte(LMUL);
    }

    public void tneg() throws IOException
    {
        out.writeByte(LNEG);
    }

    public void tor() throws IOException
    {
        out.writeByte(LOR);
    }

    public void trem() throws IOException
    {
        out.writeByte(LREM);
    }

    public void treturn() throws IOException
    {
        out.writeByte(LRETURN);
    }

    public void tshl() throws IOException
    {
        out.writeByte(LSHL);
    }

    public void tshr() throws IOException
    {
        out.writeByte(LSHR);
    }

    public void tstore(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(LSTORE_0);
                break;
            case 1:
                out.writeByte(LSTORE_1);
                break;
            case 2:
                out.writeByte(LSTORE_2);
                break;
            case 3:
                out.writeByte(LSTORE_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(LSTORE);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(LSTORE);
                    out.writeShort(index);
                }
                break;
        }
    }

    public void tsub() throws IOException
    {
        out.writeByte(LSUB);
    }

    public void tushr() throws IOException
    {
        out.writeByte(LUSHR);
    }

    public void txor() throws IOException
    {
        out.writeByte(LXOR);
    }

    public void i2t() throws IOException
    {
        out.writeByte(I2L);
    }

    public void f2t() throws IOException
    {
        out.writeByte(F2L);
    }

    public void d2t() throws IOException
    {
        out.writeByte(D2L);
    }

    public void tipush(int b) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tinc(int index, int con) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void l2t() throws IOException
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

    public void tcmpl() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tcmpg() throws IOException
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
