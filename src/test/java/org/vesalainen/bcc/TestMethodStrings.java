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

import javax.lang.model.element.ExecutableElement;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.bcc.model.El;
/**
 * @author Timo Vesalainen
 */
public class TestMethodStrings 
{
    @Test
    public void test1() throws NoSuchMethodException
    {
        String exp = "org.vesalainen.bcc.T0 m1(java.lang.String,int,[long)";
        String got = El.getExecutableString(T0.class.getDeclaredMethod("m1", String.class, int.class, long[].class));
        System.err.println(exp);
        System.err.println(got);
        assertEquals(exp, got);
        
        ExecutableElement executableElement = El.getExecutableElement(got);
        got = El.getExecutableString(executableElement);
        System.err.println(exp);
        System.err.println(got);
        assertEquals(exp, got);
        
    }
}
