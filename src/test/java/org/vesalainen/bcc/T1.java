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
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * @author Timo Vesalainen
 */
@AssertDescriptor("Lorg/vesalainen/bcc/T1;")
@AssertSignature("<T:Ljava/lang/Integer;:Ljava/io/Serializable;>Ljava/lang/Object;")
public class T1<T extends Integer & Serializable>
{
    @AssertDescriptor("I")
    public int field0;
    @AssertDescriptor("Ljava/lang/Integer;")
    @AssertSignature("TT;")
    public T field1;
    @AssertDescriptor("[Ljava/lang/Integer;")
    @AssertSignature("[TT;")
    public T[] field2;
    @AssertDescriptor("Ljava/lang/String;")
    public String field3;
    @AssertDescriptor("[[Ljava/lang/Integer;")
    @AssertSignature("[[TT;")
    public T[][] field4;
    
    public enum E {A, B};
    @AssertDescriptor("Lorg/vesalainen/bcc/T1$E;")
    public E e;

    @AssertDescriptor("()V")
    public T1()
    {
    }
    
    @AssertDescriptor("([[Ljava/lang/Integer;)V")
    @AssertSignature("([[TT;)V")
    public T1(T[][] field4)
    {
        this.field4 = field4;
    }
    
    @AssertDescriptor("(Ljava/lang/Integer;)V")
    @AssertSignature("(TT;)V")
    public void method0(T t) throws IOException
    {
    }
    @AssertDescriptor("(Ljava/lang/Integer;)V")
    @AssertSignature("(TT;)V")
    public void method1(T t)
    {
    }
    @AssertDescriptor("(Ljava/lang/Integer;I)Ljava/util/List;")
    @AssertSignature("(TT;I)Ljava/util/List<TT;>;")
    public List<T> method2(T t, int i)
    {
        return null;
    }
    @AssertDescriptor("(Ljava/util/List;)Ljava/lang/Integer;")
    @AssertSignature("(Ljava/util/List<+Ljava/lang/Number;>;)TT;")
    public T method3(List<? extends Number> l)
    {
        return null;
    }
    @AssertDescriptor("(Ljava/util/List;)Ljava/lang/Integer;")
    @AssertSignature("(Ljava/util/List<-Ljava/lang/Number;>;)TT;")
    public T method4(List<? super Number> l)
    {
        return null;
    }
    @AssertDescriptor("(Ljava/lang/Object;)V")
    @AssertSignature("<U:Ljava/lang/Object;>(TU;)V")
    public <U> void method5(U u)
    {
    }
    @AssertDescriptor("(Ljava/util/List;)V")
    @AssertSignature("<U:Ljava/lang/Object;>(Ljava/util/List<Ljava/util/Set<TU;>;>;)V")
    public static <U> void method6(java.util.List<Set<U>> boxes)
    {
        
    }
}
