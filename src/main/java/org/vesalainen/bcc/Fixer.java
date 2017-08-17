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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Fixer
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        try
        {
            String[] files = new String[]
            {
                "Assembler.java",
                "BooleanASM.java",
                "ByteASM.java",
                "CharASM.java",
                "DoubleASM.java",
                "FloatASM.java",
                "IntASM.java",
                "LongASM.java",
                "ReferenceASM.java",
                "ShortASM.java",
                "VoidASM.java"
            };
            File dir = new File("C:\\Users\\tkv\\Documents\\NetBeansProjects\\JVM\\src\\fi\\sw_nets\\jvm\\");
            for (String file : files)
            {
                File f = new File(dir, file);
                FileInputStream fis = new FileInputStream(f);
                byte[] buf = new byte[(int) f.length()];
                fis.read(buf);
                fis.close();
                String str = new String(buf);
                str = str.replace("out.writeByte(0);", "out.writeByte(NOP);");
                str = str.replace("out.writeByte(1);", "out.writeByte(ACONST_NULL);");
                str = str.replace("out.writeByte(2);", "out.writeByte(ICONST_M1);");
                str = str.replace("out.writeByte(3);", "out.writeByte(ICONST_0);");
                str = str.replace("out.writeByte(4);", "out.writeByte(ICONST_1);");
                str = str.replace("out.writeByte(5);", "out.writeByte(ICONST_2);");
                str = str.replace("out.writeByte(6);", "out.writeByte(ICONST_3);");
                str = str.replace("out.writeByte(7);", "out.writeByte(ICONST_4);");
                str = str.replace("out.writeByte(8);", "out.writeByte(ICONST_5);");
                str = str.replace("out.writeByte(9);", "out.writeByte(LCONST_0);");
                str = str.replace("out.writeByte(10);", "out.writeByte(LCONST_1);");
                str = str.replace("out.writeByte(11);", "out.writeByte(FCONST_0);");
                str = str.replace("out.writeByte(12);", "out.writeByte(FCONST_1);");
                str = str.replace("out.writeByte(13);", "out.writeByte(FCONST_2);");
                str = str.replace("out.writeByte(14);", "out.writeByte(DCONST_0);");
                str = str.replace("out.writeByte(15);", "out.writeByte(DCONST_1);");
                str = str.replace("out.writeByte(16);", "out.writeByte(BIPUSH);");
                str = str.replace("out.writeByte(17);", "out.writeByte(SIPUSH);");
                str = str.replace("out.writeByte(18);", "out.writeByte(LDC);");
                str = str.replace("out.writeByte(19);", "out.writeByte(LDC_W);");
                str = str.replace("out.writeByte(20);", "out.writeByte(LDC2_W);");
                str = str.replace("out.writeByte(21);", "out.writeByte(ILOAD);");
                str = str.replace("out.writeByte(22);", "out.writeByte(LLOAD);");
                str = str.replace("out.writeByte(23);", "out.writeByte(FLOAD);");
                str = str.replace("out.writeByte(24);", "out.writeByte(DLOAD);");
                str = str.replace("out.writeByte(25);", "out.writeByte(ALOAD);");
                str = str.replace("out.writeByte(26);", "out.writeByte(ILOAD_0);");
                str = str.replace("out.writeByte(27);", "out.writeByte(ILOAD_1);");
                str = str.replace("out.writeByte(28);", "out.writeByte(ILOAD_2);");
                str = str.replace("out.writeByte(29);", "out.writeByte(ILOAD_3);");
                str = str.replace("out.writeByte(30);", "out.writeByte(LLOAD_0);");
                str = str.replace("out.writeByte(31);", "out.writeByte(LLOAD_1);");
                str = str.replace("out.writeByte(32);", "out.writeByte(LLOAD_2);");
                str = str.replace("out.writeByte(33);", "out.writeByte(LLOAD_3);");
                str = str.replace("out.writeByte(34);", "out.writeByte(FLOAD_0);");
                str = str.replace("out.writeByte(35);", "out.writeByte(FLOAD_1);");
                str = str.replace("out.writeByte(36);", "out.writeByte(FLOAD_2);");
                str = str.replace("out.writeByte(37);", "out.writeByte(FLOAD_3);");
                str = str.replace("out.writeByte(38);", "out.writeByte(DLOAD_0);");
                str = str.replace("out.writeByte(39);", "out.writeByte(DLOAD_1);");
                str = str.replace("out.writeByte(40);", "out.writeByte(DLOAD_2);");
                str = str.replace("out.writeByte(41);", "out.writeByte(DLOAD_3);");
                str = str.replace("out.writeByte(42);", "out.writeByte(ALOAD_0);");
                str = str.replace("out.writeByte(43);", "out.writeByte(ALOAD_1);");
                str = str.replace("out.writeByte(44);", "out.writeByte(ALOAD_2);");
                str = str.replace("out.writeByte(45);", "out.writeByte(ALOAD_3);");
                str = str.replace("out.writeByte(46);", "out.writeByte(IALOAD);");
                str = str.replace("out.writeByte(47);", "out.writeByte(LALOAD);");
                str = str.replace("out.writeByte(48);", "out.writeByte(FALOAD);");
                str = str.replace("out.writeByte(49);", "out.writeByte(DALOAD);");
                str = str.replace("out.writeByte(50);", "out.writeByte(AALOAD);");
                str = str.replace("out.writeByte(51);", "out.writeByte(BALOAD);");
                str = str.replace("out.writeByte(52);", "out.writeByte(CALOAD);");
                str = str.replace("out.writeByte(53);", "out.writeByte(SALOAD);");
                str = str.replace("out.writeByte(54);", "out.writeByte(ISTORE);");
                str = str.replace("out.writeByte(55);", "out.writeByte(LSTORE);");
                str = str.replace("out.writeByte(56);", "out.writeByte(FSTORE);");
                str = str.replace("out.writeByte(57);", "out.writeByte(DSTORE);");
                str = str.replace("out.writeByte(58);", "out.writeByte(ASTORE);");
                str = str.replace("out.writeByte(59);", "out.writeByte(ISTORE_0);");
                str = str.replace("out.writeByte(60);", "out.writeByte(ISTORE_1);");
                str = str.replace("out.writeByte(61);", "out.writeByte(ISTORE_2);");
                str = str.replace("out.writeByte(62);", "out.writeByte(ISTORE_3);");
                str = str.replace("out.writeByte(63);", "out.writeByte(LSTORE_0);");
                str = str.replace("out.writeByte(64);", "out.writeByte(LSTORE_1);");
                str = str.replace("out.writeByte(65);", "out.writeByte(LSTORE_2);");
                str = str.replace("out.writeByte(66);", "out.writeByte(LSTORE_3);");
                str = str.replace("out.writeByte(67);", "out.writeByte(FSTORE_0);");
                str = str.replace("out.writeByte(68);", "out.writeByte(FSTORE_1);");
                str = str.replace("out.writeByte(69);", "out.writeByte(FSTORE_2);");
                str = str.replace("out.writeByte(70);", "out.writeByte(FSTORE_3);");
                str = str.replace("out.writeByte(71);", "out.writeByte(DSTORE_0);");
                str = str.replace("out.writeByte(72);", "out.writeByte(DSTORE_1);");
                str = str.replace("out.writeByte(73);", "out.writeByte(DSTORE_2);");
                str = str.replace("out.writeByte(74);", "out.writeByte(DSTORE_3);");
                str = str.replace("out.writeByte(75);", "out.writeByte(ASTORE_0);");
                str = str.replace("out.writeByte(76);", "out.writeByte(ASTORE_1);");
                str = str.replace("out.writeByte(77);", "out.writeByte(ASTORE_2);");
                str = str.replace("out.writeByte(78);", "out.writeByte(ASTORE_3);");
                str = str.replace("out.writeByte(79);", "out.writeByte(IASTORE);");
                str = str.replace("out.writeByte(80);", "out.writeByte(LASTORE);");
                str = str.replace("out.writeByte(81);", "out.writeByte(FASTORE);");
                str = str.replace("out.writeByte(82);", "out.writeByte(DASTORE);");
                str = str.replace("out.writeByte(83);", "out.writeByte(AASTORE);");
                str = str.replace("out.writeByte(84);", "out.writeByte(BASTORE);");
                str = str.replace("out.writeByte(85);", "out.writeByte(CASTORE);");
                str = str.replace("out.writeByte(86);", "out.writeByte(SASTORE);");
                str = str.replace("out.writeByte(87);", "out.writeByte(POP);");
                str = str.replace("out.writeByte(88);", "out.writeByte(POP2);");
                str = str.replace("out.writeByte(89);", "out.writeByte(DUP);");
                str = str.replace("out.writeByte(90);", "out.writeByte(DUP_X1);");
                str = str.replace("out.writeByte(91);", "out.writeByte(DUP_X2);");
                str = str.replace("out.writeByte(92);", "out.writeByte(DUP2);");
                str = str.replace("out.writeByte(93);", "out.writeByte(DUP2_X1);");
                str = str.replace("out.writeByte(94);", "out.writeByte(DUP2_X2);");
                str = str.replace("out.writeByte(95);", "out.writeByte(SWAP);");
                str = str.replace("out.writeByte(96);", "out.writeByte(IADD);");
                str = str.replace("out.writeByte(97);", "out.writeByte(LADD);");
                str = str.replace("out.writeByte(98);", "out.writeByte(FADD);");
                str = str.replace("out.writeByte(99);", "out.writeByte(DADD);");
                str = str.replace("out.writeByte(100);", "out.writeByte(ISUB);");
                str = str.replace("out.writeByte(101);", "out.writeByte(LSUB);");
                str = str.replace("out.writeByte(102);", "out.writeByte(FSUB);");
                str = str.replace("out.writeByte(103);", "out.writeByte(DSUB);");
                str = str.replace("out.writeByte(104);", "out.writeByte(IMUL);");
                str = str.replace("out.writeByte(105);", "out.writeByte(LMUL);");
                str = str.replace("out.writeByte(106);", "out.writeByte(FMUL);");
                str = str.replace("out.writeByte(107);", "out.writeByte(DMUL);");
                str = str.replace("out.writeByte(108);", "out.writeByte(IDIV);");
                str = str.replace("out.writeByte(109);", "out.writeByte(LDIV);");
                str = str.replace("out.writeByte(110);", "out.writeByte(FDIV);");
                str = str.replace("out.writeByte(111);", "out.writeByte(DDIV);");
                str = str.replace("out.writeByte(112);", "out.writeByte(IREM);");
                str = str.replace("out.writeByte(113);", "out.writeByte(LREM);");
                str = str.replace("out.writeByte(114);", "out.writeByte(FREM);");
                str = str.replace("out.writeByte(117);", "out.writeByte(LNEG);");
                str = str.replace("out.writeByte(118);", "out.writeByte(FNEG);");
                str = str.replace("out.writeByte(119);", "out.writeByte(DNEG);");
                str = str.replace("out.writeByte(120);", "out.writeByte(ISHL);");
                str = str.replace("out.writeByte(121);", "out.writeByte(LSHL);");
                str = str.replace("out.writeByte(122);", "out.writeByte(ISHR);");
                str = str.replace("out.writeByte(123);", "out.writeByte(LSHR);");
                str = str.replace("out.writeByte(124);", "out.writeByte(IUSHR);");
                str = str.replace("out.writeByte(125);", "out.writeByte(LUSHR);");
                str = str.replace("out.writeByte(126);", "out.writeByte(IAND);");
                str = str.replace("out.writeByte(127);", "out.writeByte(LAND);");
                str = str.replace("out.writeByte(128);", "out.writeByte(IOR);");
                str = str.replace("out.writeByte(129);", "out.writeByte(LOR);");
                str = str.replace("out.writeByte(130);", "out.writeByte(IXOR);");
                str = str.replace("out.writeByte(131);", "out.writeByte(LXOR);");
                str = str.replace("out.writeByte(132);", "out.writeByte(IINC);");
                str = str.replace("out.writeByte(133);", "out.writeByte(I2L);");
                str = str.replace("out.writeByte(134);", "out.writeByte(I2F);");
                str = str.replace("out.writeByte(135);", "out.writeByte(I2D);");
                str = str.replace("out.writeByte(136);", "out.writeByte(L2I);");
                str = str.replace("out.writeByte(137);", "out.writeByte(L2F);");
                str = str.replace("out.writeByte(138);", "out.writeByte(L2D);");
                str = str.replace("out.writeByte(139);", "out.writeByte(F2I);");
                str = str.replace("out.writeByte(140);", "out.writeByte(F2L);");
                str = str.replace("out.writeByte(141);", "out.writeByte(F2D);");
                str = str.replace("out.writeByte(142);", "out.writeByte(D2I);");
                str = str.replace("out.writeByte(143);", "out.writeByte(D2L);");
                str = str.replace("out.writeByte(144);", "out.writeByte(D2F);");
                str = str.replace("out.writeByte(145);", "out.writeByte(I2B);");
                str = str.replace("out.writeByte(146);", "out.writeByte(I2C);");
                str = str.replace("out.writeByte(147);", "out.writeByte(I2S);");
                str = str.replace("out.writeByte(148);", "out.writeByte(LCMP);");
                str = str.replace("out.writeByte(149);", "out.writeByte(FCMPL);");
                str = str.replace("out.writeByte(150);", "out.writeByte(FCMPG);");
                str = str.replace("out.writeByte(151);", "out.writeByte(DCMPL);");
                str = str.replace("out.writeByte(152);", "out.writeByte(DCMPG);");
                str = str.replace("out.writeByte(153);", "out.writeByte(IFEQ);");
                str = str.replace("out.writeByte(154);", "out.writeByte(IFNE);");
                str = str.replace("out.writeByte(155);", "out.writeByte(IFLT);");
                str = str.replace("out.writeByte(156);", "out.writeByte(IFGE);");
                str = str.replace("out.writeByte(157);", "out.writeByte(IFGT);");
                str = str.replace("out.writeByte(158);", "out.writeByte(IFLE);");
                str = str.replace("out.writeByte(159);", "out.writeByte(IF_ICMPEQ);");
                str = str.replace("out.writeByte(160);", "out.writeByte(IF_ICMPNE);");
                str = str.replace("out.writeByte(161);", "out.writeByte(IF_ICMPLT);");
                str = str.replace("out.writeByte(162);", "out.writeByte(IF_ICMPGE);");
                str = str.replace("out.writeByte(163);", "out.writeByte(IF_ICMPGT);");
                str = str.replace("out.writeByte(164);", "out.writeByte(IF_ICMPLE);");
                str = str.replace("out.writeByte(165);", "out.writeByte(IF_ACMPEQ);");
                str = str.replace("out.writeByte(166);", "out.writeByte(IF_ACMPNE);");
                str = str.replace("out.writeByte(167);", "out.writeByte(GOTO );");
                str = str.replace("out.writeByte(168);", "out.writeByte(JSR);");
                str = str.replace("out.writeByte(169);", "out.writeByte(RET);");
                str = str.replace("out.writeByte(170);", "out.writeByte(TABLESWITCH);");
                str = str.replace("out.writeByte(171);", "out.writeByte(LOOKUPSWITCH);");
                str = str.replace("out.writeByte(172);", "out.writeByte(IRETURN);");
                str = str.replace("out.writeByte(173);", "out.writeByte(LRETURN);");
                str = str.replace("out.writeByte(174);", "out.writeByte(FRETURN);");
                str = str.replace("out.writeByte(175);", "out.writeByte(DRETURN);");
                str = str.replace("out.writeByte(176);", "out.writeByte(ARETURN);");
                str = str.replace("out.writeByte(177);", "out.writeByte(RETURN);");
                str = str.replace("out.writeByte(178);", "out.writeByte(GETSTATIC);");
                str = str.replace("out.writeByte(179);", "out.writeByte(PUTSTATIC);");
                str = str.replace("out.writeByte(180);", "out.writeByte(GETFIELD);");
                str = str.replace("out.writeByte(181);", "out.writeByte(PUTFIELD);");
                str = str.replace("out.writeByte(182);", "out.writeByte(INVOKEVIRTUAL);");
                str = str.replace("out.writeByte(183);", "out.writeByte(INVOKESPECIAL);");
                str = str.replace("out.writeByte(184);", "out.writeByte(INVOKESTATIC);");
                str = str.replace("out.writeByte(185);", "out.writeByte(INVOKEINTERFACE);");
                str = str.replace("out.writeByte(187);", "out.writeByte(NEW);");
                str = str.replace("out.writeByte(188);", "out.writeByte(NEWARRAY);");
                str = str.replace("out.writeByte(189);", "out.writeByte(ANEWARRAY);");
                str = str.replace("out.writeByte(190);", "out.writeByte(ARRAYLENGTH);");
                str = str.replace("out.writeByte(191);", "out.writeByte(ATHROW);");
                str = str.replace("out.writeByte(192);", "out.writeByte(CHECKCAST);");
                str = str.replace("out.writeByte(193);", "out.writeByte(INSTANCEOF);");
                str = str.replace("out.writeByte(194);", "out.writeByte(MONITORENTER);");
                str = str.replace("out.writeByte(195);", "out.writeByte(MONITOREXIT);");
                str = str.replace("out.writeByte(196);", "out.writeByte(WIDE);");
                str = str.replace("out.writeByte(197);", "out.writeByte(MULTIANEWARRAY);");
                str = str.replace("out.writeByte(198);", "out.writeByte(IFNULL);");
                str = str.replace("out.writeByte(199);", "out.writeByte(IFNONNULL);");
                str = str.replace("out.writeByte(200);", "out.writeByte(GOTO_W);");
                str = str.replace("out.writeByte(201);", "out.writeByte(JSR_W);");
                str = str.replace("out.writeByte(202);", "out.writeByte(BREAKPOINT);");
                str = str.replace("out.writeByte(254);", "out.writeByte(IMPDEP1);");
                str = str.replace("out.writeByte(255);", "out.writeByte(IMPDEP2);");
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(str.getBytes());
                fos.close();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
