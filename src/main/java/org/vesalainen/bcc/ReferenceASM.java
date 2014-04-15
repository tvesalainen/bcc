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
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author tkv
 */
public class ReferenceASM extends Assembler implements TypeASM
{

    ReferenceASM(CodeDataOutput out, Map<String, Label> labels)
    {
        super(out, labels);
    }
    public void taload() throws IOException
    {
        out.writeByte(AALOAD);
    }
    public void tastore() throws IOException
    {
        out.writeByte(AASTORE);
    }
    public void tconst_null() throws IOException
    {
        out.writeByte(ACONST_NULL);
    }
    public void tload(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(ALOAD_0);
                break;
            case 1:
                out.writeByte(ALOAD_1);
                break;
            case 2:
                out.writeByte(ALOAD_2);
                break;
            case 3:
                out.writeByte(ALOAD_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(ALOAD);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(ALOAD);
                    out.writeShort(index);
                }
                break;
        }
    }
    public void treturn() throws IOException
    {
        out.writeByte(ARETURN);
    }
    public void tstore(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(ASTORE_0);
                break;
            case 1:
                out.writeByte(ASTORE_1);
                break;
            case 2:
                out.writeByte(ASTORE_2);
                break;
            case 3:
                out.writeByte(ASTORE_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(ASTORE);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(ASTORE);
                    out.writeShort(index);
                }
                break;
        }
    }
    public void if_tcmpeq(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IF_ACMPEQ);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeByte(IF_ACMPEQ);
            out.writeShort(branch);
        }
    }
    public void if_tcmpne(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IF_ACMPNE);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeByte(IF_ACMPNE);
            out.writeShort(branch);
        }
    }

    public void tipush(int b) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tconst(int i) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tinc(int index, int con) throws IOException
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

    public void i2t() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void l2t() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void f2t() throws IOException
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

    public void tcmpl() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void tcmpg() throws IOException
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
