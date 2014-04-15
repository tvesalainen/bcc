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

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author tkv
 */
public class LookupList extends ArrayList<LookupPair>
{
    public void addLookup(int number, String target)
    {
        add(new LookupPair(number, target));
    }

    public boolean isContiguous()
    {
        Collections.sort(this);
        boolean first = true;
        int prev = 0;
        for (LookupPair lp : this)
        {
            if (first)
            {
                first = false;
                prev = lp.getMatch();
            }
            else
            {
                int next = lp.getMatch();
                if (next - prev != 1)
                {
                    return false;
                }
                prev = next;
            }
        }
        return true;
    }

    public boolean canOptimize()
    {
        int optSize = calcOpt();
        int size = 0;
        if (isContiguous())
        {
            size = calcTableSize();
        }
        else
        {
            size = calcLookupSize();
        }
        return optSize < size;
    }

    private int calcOpt()
    {
        int size = 0;
        for (LookupPair lp : this)
        {
            size++;     // dup
            int match = lp.getMatch();
            if (match >= -1 && match <= 5)
            {
                size++;     // iconst_x
            }
            else
            {
                size+=2;     // bipush or ldc
            }
            size+=3;     // if_icmpne
            size++;     // pop
            size+=3;     // goto_n
        }
        return size + 3 + 1;    // last goto_n and pop
    }

    private int calcTableSize()
    {
        return 16 + 4 * size();
    }

    private int calcLookupSize()
    {
        return 12 + 8 * size();
    }
}
