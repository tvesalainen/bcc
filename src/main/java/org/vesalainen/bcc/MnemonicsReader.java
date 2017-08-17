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

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MnemonicsReader
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            URL url = new URL("http://java.sun.com/docs/books/jvms/second_edition/html/Mnemonics.doc.html");
            LineNumberReader r = new LineNumberReader(new InputStreamReader(url.openStream()));

            Pattern p = Pattern.compile(" [0-9]{2,3} \\((0x[0-9a-fA-F]{2})\\)	 <i>([^<]*)</i><p>");
            String line = r.readLine();
            while (line != null)
            {
                Matcher mm = p.matcher(line);
                if (mm.matches())
                {
                    //System.err.println("public static final int "+mm.group(2).toUpperCase()+" = "+mm.group(1)+";");
                    //System.err.println("case "+mm.group(2).toUpperCase()+":");
                    //System.err.println("return \""+mm.group(2)+"\";");
                    int num = Integer.parseInt(mm.group(1).substring(2), 16);
                    System.err.println("str = str.replaceAll(\"out.writeByte("+num+");\", \"out.writeByte("+mm.group(2).toUpperCase()+");\");");
                }
                line = r.readLine();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
