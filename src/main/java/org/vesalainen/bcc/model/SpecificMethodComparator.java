/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.bcc.model;

import java.util.Comparator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Sorts methods so that the most specific method is first.
 * @author Timo Vesalainen
 * @see 15.12.2.5. Choosing the Most Specific Method
 */
class SpecificMethodComparator implements Comparator<ExecutableElement>
{

    @Override
    public int compare(ExecutableElement m1, ExecutableElement m2)
    {
        List<? extends VariableElement> parameters1 = m1.getParameters();
        List<? extends VariableElement> parameters2 = m2.getParameters();
        if (parameters1.size() != parameters2.size())
        {
            throw new IllegalArgumentException(m1+" parameter count differs from "+m2);
        }
        // One applicable method m1 is more specific than another applicable 
        // method m2, for an invocation with argument expressions e1, ..., ek, 
        // if any of the following are true:
        //  m2 is not generic, m1 and m2 are applicable by strict or loose 
        //  invocation, and where m1 has formal parameter types S1, ..., Sn and 
        //  m2 has formal parameter types T1, ..., Tn, the type Si is more 
        //  specific than Ti for argument ei for all i (1 ≤ i ≤ n, n = k).
        int count = parameters1.size();
        for (int ii=0;ii<count;ii++)
        {
            if (Typ.isSubtype(parameters1.get(ii).asType(), parameters2.get(ii).asType()))
            {
                return -1;
            }
            else
            {
                if (Typ.isSubtype(parameters2.get(ii).asType(), parameters1.get(ii).asType()))
                {
                    return 1;
                }
            }
        }
        return 0;
    }
    
}
