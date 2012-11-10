package org.vesalainen.bcc;

public class LookupPair implements Comparable<LookupPair>
{

    private int match;
    private String target;

    public LookupPair(int match, String target)
    {
        this.match = match;
        this.target = target;
    }

    public int compareTo(LookupPair o)
    {
        int rc = match - o.match;
        if (rc == 0)
        {
            throw new IllegalArgumentException("More than one lookup match for "+match+" targets: "+this.target+" "+o.target);
        }
        return rc;
    }

    public int getMatch()
    {
        return match;
    }

    public String getTarget()
    {
        return target;
    }

    @Override
    public String toString()
    {
        return match + "->" + target;
    }

}
