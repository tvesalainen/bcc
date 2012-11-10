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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 *
 * @author tkv
 */
public class LineNumberPrintStream extends BufferedOutputStream
{
    private PrintStream ps;
    int linenumber = 1;

    public LineNumberPrintStream(OutputStream os)
    {
        super(os);
        this.ps = new PrintStream(os);
    }

    public int getLinenumber()
    {
        return linenumber;
    }

    public void println(Object x)
    {
        ps.println(x.toString());
        linenumber++;
    }

    public void println(String x)
    {
        ps.println(x);
        countLines(x);
        linenumber++;
    }

    public void println(char[] x)
    {
        for (char cc : x)
        {
            if (cc == '\n')
            {
                linenumber++;
            }
        }
        ps.println(x);
        linenumber++;
    }

    public void println(double x)
    {
        ps.println(x);
        linenumber++;
    }

    public void println(float x)
    {
        ps.println(x);
        linenumber++;
    }

    public void println(long x)
    {
        ps.println(x);
        linenumber++;
    }

    public void println(int x)
    {
        ps.println(x);
        linenumber++;
    }

    public void println(char x)
    {
        ps.println(x);
        linenumber++;
    }

    public void println(boolean x)
    {
        ps.println(x);
        linenumber++;
    }

    public void println()
    {
        ps.println();
        linenumber++;
    }

    public PrintStream printf(Locale l, String format, Object... args)
    {
        String s = String.format(l, format, args);
        countLines(s);
        return ps.printf(l, format, args);
    }

    public PrintStream printf(String format, Object... args)
    {
        String s = String.format(format, args);
        countLines(s);
        return ps.printf(format, args);
    }

    public void print(Object obj)
    {
        ps.print(obj.toString());
    }

    public void print(String s)
    {
        countLines(s);
        ps.print(s);
    }

    public void print(char[] s)
    {
        for (char cc : s)
        {
            if (cc == '\n')
            {
                linenumber++;
            }
        }
        ps.print(s);
    }

    public void print(double d)
    {
        ps.print(d);
    }

    public void print(float f)
    {
        ps.print(f);
    }

    public void print(long l)
    {
        ps.print(l);
    }

    public void print(int i)
    {
        ps.print(i);
    }

    public void print(char c)
    {
        if (c == '\n')
        {
            linenumber++;
        }
        ps.print(c);
    }

    public void print(boolean b)
    {
        ps.print(b);
    }

    public PrintStream format(Locale l, String format, Object... args)
    {
        String s = String.format(l, format, args);
        countLines(s);
        return ps.format(l, format, args);
    }

    public PrintStream format(String format, Object... args)
    {
        String s = String.format(format, args);
        countLines(s);
        return ps.format(format, args);
    }

    public void flush()
    {
        ps.flush();
    }

    public void close() throws IOException
    {
        ps.flush();
        super.close();
    }

    public boolean checkError()
    {
        return ps.checkError();
    }

    public PrintStream append(char c)
    {
        if (c == '\n')
        {
            linenumber++;
        }
        return ps.append(c);
    }

    public PrintStream append(CharSequence csq, int start, int end)
    {
        countLines(csq);
        return ps.append(csq, start, end);
    }

    public PrintStream append(CharSequence csq)
    {
        countLines(csq);
        return ps.append(csq);
    }

    private void countLines(CharSequence str)
    {
        int len = str.length();
        for (int ii=0;ii<len;ii++)
        {
            if (str.charAt(ii) == '\n')
            {
                linenumber++;
            }
        }
    }
}
