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

import java.util.Set;
import java.util.Map;
import java.io.Serializable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.annotation.dump.AssertDescriptor;
import org.vesalainen.annotation.dump.AssertSignature;
import org.vesalainen.bcc.model.ElementFactory;
import org.vesalainen.bcc.model.DeclaredTypeBuilder;
import org.vesalainen.bcc.model.ExecutableElementImpl.MethodBuilder;
import org.vesalainen.bcc.model.TypeElementImpl.ClassBuilder;
import org.vesalainen.bcc.model.VariableElementImpl.VariableBuilder;

/**
 *
 * @author Timo Vesalainen
 */
public class SignatureDescriptorTests
{
    
    public SignatureDescriptorTests()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Test
    public void testAll()
    {
        testFromClass(T0.class);
        testFromClass(T1.class);
        testFromClass(T2.class);
        testFromClass(T3.class);
        testFromClass(T4.class);
        testFromClass(T5.class);
        testFromClass(T6.class);
        testFromClass(T7.class);
        testFromClass(T8.class);
    }
    private void testFromClass(Class<?> aClass)
    {
        testIt(aClass);
        for (Field field : aClass.getDeclaredFields())
        {
            testIt(field);
        }
        for (Constructor constructor : aClass.getDeclaredConstructors())
        {
            testIt(constructor);
        }
        for (Method method : aClass.getDeclaredMethods())
        {
            testIt(method);
        }
    }

    private void testIt(AnnotatedElement ae)
    {
        AssertDescriptor assertDescriptor = ae.getAnnotation(AssertDescriptor.class);
        if (assertDescriptor != null)
        {
            System.err.println("test descriptor: "+ae);
            String exp = assertDescriptor.value();
            String got = Descriptor.getDesriptor(ElementFactory.get(ae));
            if (!exp.equals(got))
            {
                fail("differs\n"+exp+"\n"+got);
            }
        }
        AssertSignature assertSignature = ae.getAnnotation(AssertSignature.class);
        if (assertSignature != null)
        {
            System.err.println("test signature: "+ae);
            String exp = assertSignature.value();
            String got = Signature.getSignature(ElementFactory.get(ae));
            if (!exp.equals(got))
            {
                fail("differs\n"+exp+"\n"+got);
            }
        }
    }
/*
    @Test
    public void testDescriptor()
    {
        try
        {
            String exp = "Lorg/vesalainen/bcc/T0;";
            String got = ODescriptor.getFieldDesriptor(T0.class);
            assertEquals(exp, got);
            
            exp = "Ljava/lang/Integer;";
            got = ODescriptor.getFieldDesriptor(T1.class.getField("field1"));
            assertEquals(exp, got);

            exp = "[Ljava/lang/Integer;";
            got = ODescriptor.getFieldDesriptor(T1.class.getField("field2"));
            assertEquals(exp, got);

            exp = "(Ljava/lang/Integer;)V";
            got = ODescriptor.getMethodDesriptor(T1.class.getDeclaredMethod("method0", Integer.class));
            assertEquals(exp, got);

            exp = "(Ljava/lang/Integer;)V";
            got = ODescriptor.getMethodDesriptor(T1.class.getDeclaredMethod("method1", Integer.class));
            assertEquals(exp, got);

            exp = "(Ljava/lang/Integer;I)Ljava/util/List;";
            got = ODescriptor.getMethodDesriptor(T1.class.getDeclaredMethod("method2", Integer.class, int.class));
            assertEquals(exp, got);

            exp = "(Ljava/util/List;)Ljava/lang/Integer;";
            got = ODescriptor.getMethodDesriptor(T1.class.getDeclaredMethod("method3", List.class));
            assertEquals(exp, got);

            exp = "(Ljava/util/List;)Ljava/lang/Integer;";
            got = ODescriptor.getMethodDesriptor(T1.class.getDeclaredMethod("method4", List.class));
            assertEquals(exp, got);

            exp = "(Ljava/lang/Object;)V";
            got = ODescriptor.getMethodDesriptor(T1.class.getDeclaredMethod("method5", Object.class));
            assertEquals(exp, got);

            exp = "(Ljava/util/List;)V";
            got = ODescriptor.getMethodDesriptor(T1.class.getDeclaredMethod("method6", List.class));
            assertEquals(exp, got);
        }
        catch (NoSuchMethodException | NoSuchFieldException | SecurityException ex)
        {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetClassSignature0()
    {
        String exp = "Ljava/lang/Object;";
        String got = OSignature.getClassSignature(T0.class);
        assertEquals(exp, got);
    }

    @Test
    public void testGetClassSignature1()
    {
        try
        {
            String exp = "<T:Ljava/lang/Integer;:Ljava/io/Serializable;>Ljava/lang/Object;";
            String got = OSignature.getClassSignature(T1.class);
            assertEquals(exp, got);
            
            exp = "Ljava/lang/Object;Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;";
            got = OSignature.getClassSignature(T5.class);
            assertEquals(exp, got);

            exp = "<T:Ljava/lang/Number;>Ljava/lang/Object;Ljava/util/Map<Ljava/lang/String;TT;>;";
            got = OSignature.getClassSignature(T6.class);
            assertEquals(exp, got);
            
            exp = "TT;";
            got = OSignature.getFieldSignature(T1.class.getField("field1"));
            assertEquals(exp, got);
            
            exp = "[TT;";
            got = OSignature.getFieldSignature(T1.class.getField("field2"));
            assertEquals(exp, got);

            exp = "(TT;)V";
            got = OSignature.getMethodSignature(T1.class.getDeclaredMethod("method0", Integer.class));
            assertEquals(exp, got);

            exp = "(TT;)V";
            got = OSignature.getMethodSignature(T1.class.getDeclaredMethod("method1", Integer.class));
            assertEquals(exp, got);

            exp = "(TT;I)Ljava/util/List<TT;>;";
            got = OSignature.getMethodSignature(T1.class.getDeclaredMethod("method2", Integer.class, int.class));
            assertEquals(exp, got);

            exp = "(Ljava/util/List<+Ljava/lang/Number;>;)TT;";
            got = OSignature.getMethodSignature(T1.class.getDeclaredMethod("method3", List.class));
            assertEquals(exp, got);

            exp = "(Ljava/util/List<-Ljava/lang/Number;>;)TT;";
            got = OSignature.getMethodSignature(T1.class.getDeclaredMethod("method4", List.class));
            assertEquals(exp, got);

            exp = "<U:Ljava/lang/Object;>(TU;)V";
            got = OSignature.getMethodSignature(T1.class.getDeclaredMethod("method5", Object.class));
            assertEquals(exp, got);

            exp = "<U:Ljava/lang/Object;>(Ljava/util/List<Ljava/util/Set<TU;>;>;)V";
            got = OSignature.getMethodSignature(T1.class.getDeclaredMethod("method6", List.class));
            assertEquals(exp, got);
        }
        catch (NoSuchMethodException | NoSuchFieldException | SecurityException ex)
        {
            ex.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetClassSignature2()
    {
        String exp = "<T::Ljava/util/Set<Ljava/lang/Integer;>;:Ljava/io/Serializable;>Ljava/lang/Object;";
        String got = OSignature.getClassSignature(T2.class);
        assertEquals(exp, got);
    }

    @Test
    public void testGetClassSignature3()
    {
        String exp = "<T::Ljava/util/Set<Ljava/lang/Integer;>;:Ljava/io/Serializable;U::Ljava/util/Map$Entry<Ljava/lang/Boolean;Ljava/lang/String;>;>Ljava/lang/Object;";
        String got = OSignature.getClassSignature(T3.class);
        assertEquals(exp, got);
    }

    @Test
    public void testGetClassSignature4()
    {
        String exp = "<U:Ljava/lang/Long;>Ljava/lang/Object;Ljava/lang/Cloneable;";
        String got = OSignature.getClassSignature(T4.Inner.class);
        assertEquals(exp, got);
            
    }

*/
    @Test
    public void testGetClassWrapperSignature1()
    {
        try
        {
            // public class T1<T extends Integer & Serializable>
            String exp = "<T:Ljava/lang/Integer;:Ljava/io/Serializable;>Ljava/lang/Object;";
            ClassBuilder b1 = ElementFactory.classBuilder()
                    .addModifier(javax.lang.model.element.Modifier.PUBLIC)
                    .setQualifiedName("org.vesalainen.bcc.T1")
                    .setSuperclass(Object.class)
                    .addTypeParameter("T", Integer.class, Serializable.class);
                    b1.addField("field1", "T");
                    b1.addField("field2", "[T");
                    b1.addField("field4", "[[T");
                    // public void method1(T t)
                    MethodBuilder mb = b1.addMethod("method1");
                    VariableBuilder vb = mb.addParameter("t");
                    vb.setType("T");
                    //     public List<T> method2(T t, int i)
                    mb = b1.addMethod("method2");
                    mb.setReturnType(List.class, "T");
                    vb = mb.addParameter("t");
                    vb.setType("T");
                    vb = mb.addParameter("i");
                    vb.setType(int.class);
                    // public T method3(List<? extends Number> l)
                    mb = b1.addMethod("method3");
                    mb.setReturnType("T");
                    vb = mb.addParameter("l");
                    DeclaredTypeBuilder dtb = vb.setComplexType(List.class);
                    dtb.addExtends(Number.class);
                    // public T method3(List<? super Number> l)
                    mb = b1.addMethod("method4");
                    mb.setReturnType("T");
                    vb = mb.addParameter("l");
                    dtb = vb.setComplexType(List.class);
                    dtb.addSuper(Number.class);
                    // public <U> void method5(U u)
                    mb = b1.addMethod("method5");
                    mb.addTypeParameter("U", Object.class);
                    vb = mb.addParameter("u");
                    vb.setType("U");
                    // public static <U> void method6(java.util.List<Set<U>> boxes)
                    mb = b1.addMethod("method6");
                    mb.addTypeParameter("U", Object.class);
                    vb = mb.addParameter("boxes");
                    dtb = vb.setComplexType(List.class);
                    dtb = dtb.addNested(Set.class);
                    dtb.addType("U");
                    TypeElement e1 = b1.getTypeElement();
            
            String got = Signature.getSignature(e1);
            assertEquals(exp, got);
            
            exp = "Lorg/vesalainen/bcc/T1;";
            got = Descriptor.getDesriptor(e1);
            System.err.println(exp);
            System.err.println(got);
            assertEquals(exp, got);
            
            // public abstract class T5 implements Map<String,Integer>
            TypeElement e5 = ElementFactory.classBuilder()
                    .addModifier(javax.lang.model.element.Modifier.PUBLIC)
                    .addModifier(javax.lang.model.element.Modifier.ABSTRACT)
                    .setQualifiedName("org.vesalainen.bcc.T5")
                    .setSuperclass(Object.class)
                    .addInterface(Map.class, String.class, Integer.class)
                    .getTypeElement();
            exp = "Ljava/lang/Object;Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;";
            got = Signature.getSignature(e5);
            assertEquals(exp, got);
            
            exp = "Lorg/vesalainen/bcc/T5;";
            got = Descriptor.getDesriptor(e5);
            System.err.println(exp);
            System.err.println(got);
            assertEquals(exp, got);
            
            // public abstract class T6<T extends Number> implements Map<String,T>
            TypeElement e6 = ElementFactory.classBuilder()
                    .addModifier(javax.lang.model.element.Modifier.PUBLIC)
                    .addModifier(javax.lang.model.element.Modifier.ABSTRACT)
                    .setQualifiedName("org.vesalainen.bcc.T6")
                    .setSuperclass(Object.class)
                    .addTypeParameter("T", Number.class)
                    .addInterface(Map.class, String.class.getCanonicalName(), "T")
                    .getTypeElement();
            exp = "<T:Ljava/lang/Number;>Ljava/lang/Object;Ljava/util/Map<Ljava/lang/String;TT;>;";
            got = Signature.getSignature(e6);
            assertEquals(exp, got);
            
            // public T field1;

            VariableElement f1 = null;
            VariableElement f2 = null;
            for (VariableElement ve : ElementFilter.fieldsIn(e1.getEnclosedElements()))
            {
                if ("field1".contentEquals(ve.getSimpleName()))
                {
                    f1 = ve;
                }
                if ("field2".contentEquals(ve.getSimpleName()))
                {
                    f2 = ve;
                }
            }
            assertNotNull("field1 not found", f1);
            assertNotNull("field2 not found", f2);
            exp = "TT;";
            got = Signature.getSignature(f1);
            assertEquals(exp, got);
            exp = "Ljava/lang/Integer;";
            got = Descriptor.getDesriptor(f1);
            assertEquals(exp, got);

            exp = "[TT;";
            got = Signature.getSignature(f2);
            assertEquals(exp, got);
            exp = "[Ljava/lang/Integer;";
            got = Descriptor.getDesriptor(f2);
            assertEquals(exp, got);

            ExecutableElement m1 = null;
            ExecutableElement m2 = null;
            ExecutableElement m3 = null;
            ExecutableElement m4 = null;
            ExecutableElement m5 = null;
            ExecutableElement m6 = null;
            for (ExecutableElement ee : ElementFilter.methodsIn(e1.getEnclosedElements()))
            {
                if ("method1".contentEquals(ee.getSimpleName()))
                {
                    m1 = ee;
                }
                if ("method2".contentEquals(ee.getSimpleName()))
                {
                    m2 = ee;
                }
                if ("method3".contentEquals(ee.getSimpleName()))
                {
                    m3 = ee;
                }
                if ("method4".contentEquals(ee.getSimpleName()))
                {
                    m4 = ee;
                }
                if ("method5".contentEquals(ee.getSimpleName()))
                {
                    m5 = ee;
                }
                if ("method6".contentEquals(ee.getSimpleName()))
                {
                    m6 = ee;
                }
            }
            assertNotNull("method1 not found", m1);
            assertNotNull("method2 not found", m2);
            assertNotNull("method3 not found", m3);
            assertNotNull("method4 not found", m4);
            assertNotNull("method5 not found", m5);
            assertNotNull("method6 not found", m6);
            exp = "(TT;)V";
            got = Signature.getSignature(m1);
            assertEquals(exp, got);
            exp = "(Ljava/lang/Integer;)V";
            got = Descriptor.getDesriptor(m1);
            assertEquals(exp, got);


            // public List<T> method2(T t, int i)
            exp = "(TT;I)Ljava/util/List<TT;>;";
            got = Signature.getSignature(m2);
            assertEquals(exp, got);
            exp = "(Ljava/lang/Integer;I)Ljava/util/List;";
            got = Descriptor.getDesriptor(m2);
            assertEquals(exp, got);

            // public T method3(List<? extends Number> l)
            exp = "(Ljava/util/List<+Ljava/lang/Number;>;)TT;";
            got = Signature.getSignature(m3);
            assertEquals(exp, got);
            exp = "(Ljava/util/List;)Ljava/lang/Integer;";
            got = Descriptor.getDesriptor(m3);
            assertEquals(exp, got);



            // public T method4(List<? super Number> l)
            exp = "(Ljava/util/List<-Ljava/lang/Number;>;)TT;";
            got = Signature.getSignature(m4);
            assertEquals(exp, got);
            exp = "(Ljava/util/List;)Ljava/lang/Integer;";
            got = Descriptor.getDesriptor(m4);
            assertEquals(exp, got);


            // public <U> void method5(U u)
            exp = "<U:Ljava/lang/Object;>(TU;)V";
            got = Signature.getSignature(m5);
            assertEquals(exp, got);
            exp = "(Ljava/lang/Object;)V";
            got = Descriptor.getDesriptor(m5);
            assertEquals(exp, got);


            // public static <U> void method6(java.util.List<Set<U>> boxes)
            exp = "<U:Ljava/lang/Object;>(Ljava/util/List<Ljava/util/Set<TU;>;>;)V";
            got = Signature.getSignature(m6);
            assertEquals(exp, got);
            exp = "(Ljava/util/List;)V";
            got = Descriptor.getDesriptor(m6);
            assertEquals(exp, got);
        }
        catch (SecurityException ex)
        {
            ex.printStackTrace();
            fail();
        }
    }

}
