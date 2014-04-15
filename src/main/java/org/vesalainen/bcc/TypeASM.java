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

/**
 *
 * @author tkv
 */
public interface TypeASM extends OpCode
{
    void tipush(int b) throws IOException;
    void tconst(int i) throws IOException;
    void tconst_null() throws IOException;
    void tload(int index) throws IOException;
    void tstore(int index) throws IOException;
    void tinc(int index, int con) throws IOException;
    void taload() throws IOException;
    void tastore() throws IOException;
    void tadd() throws IOException;
    void tsub() throws IOException;
    void tmul() throws IOException;
    void tdiv() throws IOException;
    void trem() throws IOException;
    void tneg() throws IOException;
    void tshl() throws IOException;
    void tshr() throws IOException;
    void tushr() throws IOException;
    void tand() throws IOException;
    void tor() throws IOException;
    void txor() throws IOException;
    void i2t() throws IOException;
    void l2t() throws IOException;
    void f2t() throws IOException;
    void d2t() throws IOException;
    void tcmp() throws IOException;
    void tcmpl() throws IOException;
    void tcmpg() throws IOException;
    void if_tcmpeq(String target) throws IOException;
    void if_tcmpne(String target) throws IOException;
    void if_tcmplt(String target) throws IOException;
    void if_tcmpge(String target) throws IOException;
    void if_tcmpgt(String target) throws IOException;
    void if_tcmple(String target) throws IOException;
    void treturn() throws IOException;
}
