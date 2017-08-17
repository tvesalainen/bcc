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
public class CmdComp
{
    private static String type(String cmd)
    {
        if (cmd.startsWith("b"))
        {
            return "byte";
        }
        if (cmd.startsWith("s"))
        {
            return "short";
        }
        if (cmd.startsWith("i"))
        {
            return "int";
        }
        if (cmd.startsWith("l"))
        {
            return "long";
        }
        if (cmd.startsWith("f"))
        {
            return "float";
        }
        if (cmd.startsWith("d"))
        {
            return "double";
        }
        if (cmd.startsWith("c"))
        {
            return "char";
        }
        if (cmd.startsWith("a"))
        {
            return "Object";
        }
        return "???";
    }

    private static String pr(String cmd)
    {
        return cmd.substring(0, 1);
    }
    private static void print(String cmd)
    {
        Pattern p = Pattern.compile("([a-z])ipush");
        Matcher m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Push "+type(cmd)+" constant", "...", "..., i", "value int value");
            System.err.println("public void "+cmd+"("+type(cmd)+" value) throws IOException");
            System.err.println("{");
            System.err.println("    "+m.group(1)+".t"+cmd.substring(1)+"(value);");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])const_0");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Push "+type(cmd)+" constant", "...", "..., i", "value int value");
            System.err.println("public void "+cmd.substring(0, 6)+"(int value) throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tconst(value);");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])load");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Load "+type(cmd)+" from local variable", "...", "..., value", "index");
            System.err.println("public void "+cmd+"(int index) throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tload(index);");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])store");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Store "+type(cmd)+" into local variable", "..., value", "...", "index");
            System.err.println("public void "+cmd+"(int index) throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tstore(index);");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])inc");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc2("Increment local variable by constant", "index into local variable", "con constant value");
            System.err.println("public void "+cmd+"(int index, int con) throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tinc(index, con);");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])aload");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Load "+type(cmd)+" from array", "..., arrayref, index", "..., value");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".taload();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])astore");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Store into "+type(cmd)+" array", "..., arrayref, index, value", "...");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tastore();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])add");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Add "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tadd();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])sub");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Subtract "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tsub();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])mul");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Multiply "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tmul();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])div");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Divide "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tdiv();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])rem");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Remainder "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".trem();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])neg");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Negate "+type(cmd)+"", "..., value", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tneg();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])shl");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Shift left "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tshl();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])shr");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Arithmetic shift right "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tshr();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])ushr");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Logical shift right "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tushr();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])and");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Boolean AND "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tand();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])or");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Boolean OR "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".tor();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])xor");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Boolean XOR "+type(cmd)+"", "..., value1, value2", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+".txor();");
            System.err.println("}");
            return;
        }
        p = Pattern.compile("([a-z])2([a-z])");
        m = p.matcher(cmd);
        if (m.matches())
        {
            doc1("Convert "+type(m.group(1))+" to "+type(m.group(2)), "..., value", "..., result");
            System.err.println("public void "+cmd+"() throws IOException");
            System.err.println("{");
            System.err.println("    "+cmd.substring(0, 1)+"."+cmd+"();");
            System.err.println("}");
            return;
        }
    }

    private static void doc1(String msg, String before, String after, String... params)
    {
        System.err.println("/**");
        System.err.println(" * "+msg);
        System.err.println(" * <p>Stack: "+before+"=&gt;"+after);
        for (String param : params)
        {
            System.err.println(" * @param "+param);
        }
        System.err.println(" * @throws IOException");
        System.err.println(" */");
    }
    private static void doc2(String msg, String... params)
    {
        System.err.println("/**");
        System.err.println(" * "+msg);
        System.err.println(" * <p>Stack: No change");
        for (String param : params)
        {
            System.err.println(" * @param "+param);
        }
        System.err.println(" * @throws IOException");
        System.err.println(" */");
    }
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
                    print(mm.group(2));
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
