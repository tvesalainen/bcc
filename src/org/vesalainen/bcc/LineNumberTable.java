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

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tkv
 */
public class LineNumberTable extends AttributeInfo
{
    private List<LineNumber> lineNumbers = new ArrayList<LineNumber>();

    LineNumberTable(SubClass subClass)
    {
        super(subClass, "LineNumberTable");
    }

    void addLineNumber(int pc, int line)
    {
        lineNumbers.add(new LineNumber(pc, line));
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeShort(attribute_name_index );
        out.writeInt(lineNumbers.size()*4+2);
        out.writeShort(lineNumbers.size());
        for (LineNumber ln : lineNumbers)
        {
            ln.write(out);
        }
    }

    public int getAttributeSize()
    {
        return lineNumbers.size()*4+2+6;
    }


    private class LineNumber implements Writable
    {
        int start_pc;
        int line_number;

        public LineNumber(int start_pc, int line_number)
        {
            this.start_pc = start_pc;
            this.line_number = line_number;
        }

        public void write(DataOutput out) throws IOException
        {
            out.writeShort(start_pc);
            out.writeShort(line_number);
        }

    }
}
