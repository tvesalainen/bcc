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
import java.util.List;

/**
 *
 * @author tkv
 */
public class Label
{
    private List<String> names = new ArrayList<>();
    private int address = -1;
    private List<Branch> branches = new ArrayList<>();

    public Label(String label)
    {
        names.add(label);
    }

    public Label(Label l1, Label l2)
    {
        names.addAll(l1.names);
        names.addAll(l2.names);
        branches.addAll(l1.branches);
        branches.addAll(l2.branches);
    }

    public void addName(String name)
    {
        names.add(name);
    }
    
    public List<String> getNames()
    {
        return names;
    }

    public int getAddress()
    {
        if (address == -1)
        {
            throw new IllegalArgumentException("address not set");
        }
        return address;
    }

    public void setAddress(int address)
    {
        if (this.address != -1)
        {
            throw new IllegalArgumentException(names+" address has already been fixed");
        }
        this.address = address;
    }

    public void resetAddress()
    {
        this.address = -1;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Label other = (Label) obj;
        if (this.names != other.names && (this.names == null || !this.names.equals(other.names)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 53 * hash + (this.names != null ? this.names.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString()
    {
        return names.toString();
    }

    public Branch createBranch(int offset)
    {
        Branch branch = new Branch(this, offset);
        branches.add(branch);
        return branch;
    }

    public void fixCode(byte[] code)
    {
        try
        {
            for (Branch branch : branches)
            {
                branch.fixCode(code, address);
            }
        }
        catch (IllegalArgumentException ex)
        {
            throw new IllegalArgumentException("problem with label "+this, ex);
        }
    }

    public class Branch
    {
        private Label label;
        private int offset;
        private int reference;
        private boolean wide;

        public Branch(Label label, int offset)
        {
            this.label = label;
            this.offset = offset;
        }

        public boolean isWide()
        {
            return wide;
        }

        public void setWide(boolean wide)
        {
            this.wide = wide;
        }

        public Label getLabel()
        {
            return label;
        }

        public int getOffset()
        {
            return offset;
        }

        public int getReference()
        {
            return reference;
        }

        public void setReference(int reference)
        {
            this.reference = reference;
        }

        private void fixCode(byte[] code, int address)
        {
            if (address == -1)
            {
                throw new BranchException(label + " is not set");
            }
            int a = address-offset;
            if (wide)
            {
                int a1 = (a >> 24) & 0xff;
                int a2 = (a >> 16) & 0xff;
                int a3 = (a >> 8) & 0xff;
                int a4 = a & 0xff;
                code[reference] = (byte) a1;
                code[reference + 1] = (byte) a2;
                code[reference + 2] = (byte) a3;
                code[reference + 3] = (byte) a4;
            }
            else
            {
                if (a < Short.MIN_VALUE || a > Short.MAX_VALUE)
                {
                    throw new BranchException("too long jump! use wide index jump at (or set wideIndex option) "+address);
                }
                int a3 = (a >> 8) & 0xff;
                int a4 = a & 0xff;
                code[reference] = (byte) a3;
                code[reference + 1] = (byte) a4;
            }
        }

    }
}
