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
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class IntASM extends Assembler implements TypeASM
{

    IntASM(CodeDataOutput out, Map<String, Label> labels)
    {
        super(out, labels);
    }

    public void tadd() throws IOException
    {
        out.writeByte(IADD);
    }
    public void taload() throws IOException
    {
        out.writeByte(IALOAD);
    }
    public void tand() throws IOException
    {
        out.writeByte(IAND);
    }
    public void tastore() throws IOException
    {
        out.writeByte(IASTORE);
    }

    public void tconst(int i) throws IOException
    {   // actually bipush
        switch (i)
        {
            case -1:
                out.writeByte(ICONST_M1);
                break;
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
                out.writeByte(i);
                break;
        }
    }
    public void tdiv() throws IOException
    {
        out.writeByte(IDIV);
    }
    public void tinc(int index, int con) throws IOException
    {
        if (index < 256 && con > Byte.MIN_VALUE && con < 256)
        {
            out.writeByte(IINC);
            out.writeByte(index);
            out.writeByte(con);
        }
        else
        {
            out.writeByte(WIDE);
            out.writeByte(IINC);
            out.writeShort(index);
            out.writeShort(con);
        }
    }
    public void tload(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(ILOAD_0);
                break;
            case 1:
                out.writeByte(ILOAD_1);
                break;
            case 2:
                out.writeByte(ILOAD_2);
                break;
            case 3:
                out.writeByte(ILOAD_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(ILOAD);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(ILOAD);
                    out.writeShort(index);
                }
                break;
        }
    }
    public void tmul() throws IOException
    {
        out.writeByte(IMUL);
    }
    public void tneg() throws IOException
    {
        out.writeByte(116);
    }
    public void tor() throws IOException
    {
        out.writeByte(IOR);
    }
    public void trem() throws IOException
    {
        out.writeByte(IREM);
    }
    public void treturn() throws IOException
    {
        out.writeByte(IRETURN);
    }
    public void tshl() throws IOException
    {
        out.writeByte(ISHL);
    }
    public void tshr() throws IOException
    {
        out.writeByte(ISHR);
    }
    public void tstore(int index) throws IOException
    {
        switch (index)
        {
            case 0:
                out.writeByte(ISTORE_0);
                break;
            case 1:
                out.writeByte(ISTORE_1);
                break;
            case 2:
                out.writeByte(ISTORE_2);
                break;
            case 3:
                out.writeByte(ISTORE_3);
                break;
            default:
                if (index < 256)
                {
                    out.writeByte(ISTORE);
                    out.writeByte(index);
                }
                else
                {
                    out.writeByte(WIDE);
                    out.writeByte(ISTORE);
                    out.writeShort(index);
                }
                break;
        }
    }
    public void tsub() throws IOException
    {
        out.writeByte(ISUB);
    }
    public void tushr() throws IOException
    {
        out.writeByte(IUSHR);
    }
    public void txor() throws IOException
    {
        out.writeByte(IXOR);
    }
    public void if_tcmpeq(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IF_ICMPEQ);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeByte(IF_ICMPEQ);
            out.writeShort(branch);
        }
    }
    public void if_tcmpne(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IF_ICMPNE);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeByte(IF_ICMPNE);
            out.writeShort(branch);
        }
    }
    public void if_tcmplt(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IF_ICMPLT);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeByte(IF_ICMPLT);
            out.writeShort(branch);
        }
    }
    public void if_tcmpge(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IF_ICMPGE);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeByte(IF_ICMPGE);
            out.writeShort(branch);
        }
    }
    public void if_tcmpgt(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IF_ICMPGT);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeByte(IF_ICMPGT);
            out.writeShort(branch);
        }
    }
    public void if_tcmple(String target) throws IOException
    {
        if (wideIndex)
        {
            out.writeByte(NOT_IF_ICMPLE);
            out.writeShort(WIDEFIXOFFSET);
            Branch branch = createBranch(target);
            out.writeByte(GOTO_W);
            out.writeInt(branch);
        }
        else
        {
            Branch branch = createBranch(target);
            out.writeByte(IF_ICMPLE);
            out.writeShort(branch);
        }
    }

    public void tipush(int b) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void i2t() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void l2t() throws IOException
    {
        out.writeByte(L2I);
    }

    public void f2t() throws IOException
    {
        out.writeByte(F2I);
    }

    public void d2t() throws IOException
    {
        out.writeByte(D2I);
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

    public void tconst_null() throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
