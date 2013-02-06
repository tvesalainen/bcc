/*
 * Copyright (C) 2013 Timo Vesalainen
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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Timo Vesalainen
 */
public class ExceptionTable implements Writable 
{
    private Block block;
    private Label label;
    private int catch_type;

    public ExceptionTable(Block block, Label label, int catch_type)
    {
        this.block = block;
        this.label = label;
        this.catch_type = catch_type;
    }

    public ExceptionTable(DataInput in) throws IOException
    {
        block = new Block(in.readUnsignedShort());
        block.setEnd(in.readUnsignedShort());
        label = new Label("");
        label.setAddress(in.readUnsignedShort());
        catch_type = in.readUnsignedShort();
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        if (block.getEnd() == -1)
        {
            throw new IllegalArgumentException("block end not set");
        }
        if (label.getAddress() == -1)
        {
            throw new IllegalArgumentException("label address not set");
        }
        out.writeShort(block.getStart());
        out.writeShort(block.getEnd());
        out.writeShort(label.getAddress());
        out.writeShort(catch_type);
    }

    int getHandler()
    {
        return label.getAddress();
    }
}
