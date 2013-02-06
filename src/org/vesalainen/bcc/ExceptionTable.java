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
    private int start_pc;
    private int end_pc;
    private int handler_pc;
    private int catch_type;

    public ExceptionTable(DataInput in) throws IOException
    {
        start_pc = in.readUnsignedShort();
        end_pc = in.readUnsignedShort();
        handler_pc = in.readUnsignedShort();
        catch_type = in.readUnsignedShort();
    }

    public void write(DataOutput out) throws IOException
    {
        out.writeShort(start_pc);
        out.writeShort(end_pc);
        out.writeShort(handler_pc);
        out.writeShort(catch_type);
    }

}
