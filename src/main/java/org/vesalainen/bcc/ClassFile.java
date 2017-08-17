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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import org.vesalainen.bcc.ConstantInfo.Clazz;
import org.vesalainen.bcc.ConstantInfo.ConstantDouble;
import org.vesalainen.bcc.ConstantInfo.ConstantFloat;
import org.vesalainen.bcc.ConstantInfo.ConstantInteger;
import org.vesalainen.bcc.ConstantInfo.ConstantLong;
import org.vesalainen.bcc.ConstantInfo.ConstantString;
import org.vesalainen.bcc.ConstantInfo.Fieldref;
import org.vesalainen.bcc.ConstantInfo.Methodref;
import org.vesalainen.bcc.ConstantInfo.NameAndType;
import org.vesalainen.bcc.ConstantInfo.Ref;
import org.vesalainen.bcc.ConstantInfo.Utf8;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.ElementFilter;
import org.vesalainen.bcc.AccessFlags.ClassFlags;
import org.vesalainen.bcc.ClassFile.ClassType;
import org.vesalainen.bcc.ConstantInfo.Filler;
import org.vesalainen.bcc.ConstantInfo.InterfaceMethodref;
import org.vesalainen.bcc.annotation.ModelUtil;
import org.vesalainen.bcc.model.El;
import org.vesalainen.bcc.model.Typ;

/**
 * ClassFile wraps java classfile
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * TODO make thread safe
 */
public class ClassFile implements Writable, TypeElement
{
    protected int magic;
    protected int minor_version;
    protected int major_version;
    protected List<ConstantInfo> constant_pool = new ArrayList<>();
    protected int this_class;
    protected int super_class;
    protected List<Short> interfaces = new ArrayList<>();
    protected List<FieldInfo> fields = new ArrayList<>();
    protected List<MethodInfo> methods = new ArrayList<>();
    protected List<AttributeInfo> attributes = new ArrayList<>();

    protected Map<Integer,Object> indexedElementMap = new HashMap<>();
    protected Map<Class<? extends ConstantInfo>,List<ConstantInfo>> constantPoolMap = new HashMap<>();
    protected Map<ConstantInfo,Integer> constantPoolIndexMap = new HashMap<>();
    protected Map<String, TypeParameterElement> typeParameterMap = new HashMap<>();
    
    protected TypeElement superClass;
    protected Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    protected ClassType type = new ClassType();
    protected Name qualifiedName;
    protected PackageElement packageElement;
    protected List<Element> enclosedElements = new ArrayList<>();
    private Name simpleName;
    private boolean synthetic = true;

    protected ClassFile(TypeElement superClass, String qualifiedName, Modifier... modifiers)
    {
        this.superClass = superClass;
        this.qualifiedName = El.getName(qualifiedName);
        this.modifiers.addAll(Arrays.asList(modifiers));
        int idx = qualifiedName.lastIndexOf('.');
        if (idx != -1)
        {
            packageElement = El.getPackageElement(qualifiedName.substring(0, idx));
            simpleName = El.getName(qualifiedName.substring(idx+1));
        }
        else
        {
            packageElement = El.getPackageElement("");
            simpleName = El.getName(qualifiedName);
        }
        if (packageElement == null)
        {
            throw new IllegalArgumentException("packageElement == null for "+qualifiedName);
        }
    }
    public ClassFile(Class<?> cls) throws IOException
    {
        this(cls.getClassLoader().getResourceAsStream(cls.getName().replace('.', '/')+".class"));
    }

    public ClassFile(byte[] bytes) throws IOException
    {
        this(new ByteArrayInputStream(bytes));
    }

    public ClassFile(InputStream in) throws IOException
    {
        this(new DataInputStream(new BufferedInputStream(in)));
    }

    public ClassFile(File file) throws IOException
    {
        this(new DataInputStream(new BufferedInputStream(new FileInputStream(file))));
    }

    private ClassFile(DataInputStream oin) throws IOException
    {
        magic = oin.readInt();
        if (magic != 0xcafebabe)
        {
            throw new ClassFormatError();
        }
        minor_version = oin.readUnsignedShort();
        major_version = oin.readUnsignedShort();
        int constant_pool_count = oin.readUnsignedShort();
        for (int ii = 0; ii < constant_pool_count-1; ii++)
        {
            ConstantInfo ci = ConstantInfo.read(oin);
            addConstantInfo(ci);
            if (ci instanceof ConstantInfo.ConstantDouble || ci instanceof ConstantInfo.ConstantLong)
            {
                ii++;
            }
        }
        int access_flags = oin.readUnsignedShort();
        ClassFlags.setModifiers(modifiers, access_flags);
        synthetic = ClassFlags.isSynthetic(access_flags);
        this_class = oin.readUnsignedShort();
        super_class = oin.readUnsignedShort();
        int interfaces_count = oin.readUnsignedShort();
        for (int ii = 0; ii < interfaces_count; ii++)
        {
            interfaces.add((short) oin.readUnsignedShort());
        }
        int fields_count = oin.readUnsignedShort();
        for (int ii = 0; ii < fields_count; ii++)
        {
            FieldInfo fieldInfo = new FieldInfo(oin);
            fields.add(fieldInfo);
            enclosedElements.add(fieldInfo);
        }
        int methods_count = oin.readUnsignedShort();
        for (int ii = 0; ii < methods_count; ii++)
        {
            MethodInfo methodInfo = new MethodInfo(this, oin);
            methods.add(methodInfo);
            enclosedElements.add(methodInfo);
        }
        int attributes_count = oin.readUnsignedShort();
        for (int ii = 0; ii < attributes_count; ii++)
        {
            attributes.add(AttributeInfo.getInstance(this, oin));
        }
        Clazz superClazz = (Clazz) getConstantInfo(super_class);
        superClass = El.fromDescriptor(getString(superClazz.getName_index()));
        Clazz clazz = (Clazz) getConstantInfo(this_class);
        qualifiedName = El.getName(Descriptor.getFullyQualifiedForm(getString(clazz.getName_index())));
    }

    int getConstantPoolSize()
    {
        return constant_pool.size();
    }
    /**
     * Returns the constant map index to field
     * @param declaringClass
     * @param name
     * @param type
     * @return 
     */
    int getFieldIndex(TypeElement declaringClass, String name, TypeMirror type)
    {
        return getFieldIndex(declaringClass, name, Descriptor.getFieldDesriptor(type));
    }
    /**
     * Returns the constant map index to field
     * @param declaringClass
     * @param name
     * @param descriptor
     * @return
     */
    int getFieldIndex(TypeElement declaringClass, String name, String descriptor)
    {
        String fullyQualifiedForm = declaringClass.getQualifiedName().toString();
        return getRefIndex(Fieldref.class, fullyQualifiedForm, name, descriptor);
    }
    /**
     * Returns the constant map index to method
     * @param method
     * @return
     */
    int getMethodIndex(ExecutableElement method)
    {
        TypeElement declaringClass = (TypeElement) method.getEnclosingElement();
        String fullyQualifiedname = declaringClass.getQualifiedName().toString();
        return getRefIndex(Methodref.class, fullyQualifiedname, method.getSimpleName().toString(), Descriptor.getDesriptor(method));
    }
    /**
     * Returns the constant map index to reference
     * @param refType
     * @param fullyQualifiedname
     * @param name
     * @param descriptor
     * @return
     */
    protected int getRefIndex(Class<? extends Ref> refType, String fullyQualifiedname, String name, String descriptor)
    {
        String internalForm = fullyQualifiedname.replace('.', '/');
        for (ConstantInfo ci : listConstantInfo(refType))
        {
            Ref mr = (Ref) ci;
            int classIndex = mr.getClass_index();
            Clazz clazz = (Clazz) getConstantInfo(classIndex);
            String cn = getString(clazz.getName_index());
            if (internalForm.equals(cn))
            {
                int nameAndTypeIndex = mr.getName_and_type_index();
                NameAndType nat = (NameAndType) getConstantInfo(nameAndTypeIndex);
                int nameIndex = nat.getName_index();
                String str = getString(nameIndex);
                if (name.equals(str))
                {
                    int descriptorIndex = nat.getDescriptor_index();
                    String descr = getString(descriptorIndex);
                    if (descriptor.equals(descr))
                    {
                        return constantPoolIndexMap.get(ci);
                    }
                }
            }
        }
        return -1;
    }
    /**
     * Returns the constant map index to name
     * @param name
     * @return
     */
    public int getNameIndex(CharSequence name)
    {
        for (ConstantInfo ci : listConstantInfo(Utf8.class))
        {
            Utf8 utf8 = (Utf8) ci;
            String str = utf8.getString();
            if (str.contentEquals(name))
            {
                return constantPoolIndexMap.get(ci);
            }
        }
        return -1;
    }
    /**
     * Returns the constant map index to name and type
     * @param name
     * @param descriptor
     * @return
     */
    public int getNameAndTypeIndex(String name, String descriptor)
    {
        for (ConstantInfo ci : listConstantInfo(NameAndType.class))
        {
            NameAndType nat = (NameAndType) ci;
            int nameIndex = nat.getName_index();
            String str = getString(nameIndex);
            if (name.equals(str))
            {
                int descriptorIndex = nat.getDescriptor_index();
                String descr = getString(descriptorIndex);
                if (descriptor.equals(descr))
                {
                    return constantPoolIndexMap.get(ci);
                }
            }
        }
        return -1;
    }
    /**
     * Returns the constant map index to integer constant
     * @param constant
     * @return
     */
    public final int getConstantIndex(int constant)
    {
        for (ConstantInfo ci : listConstantInfo(ConstantInteger.class))
        {
            ConstantInteger ic = (ConstantInteger) ci;
            if (constant == ic.getConstant())
            {
                return constantPoolIndexMap.get(ci);
            }
        }
        return -1;
    }
    /**
     * Returns the constant map index to constant
     * @param constant
     * @return
     */
    public final int getConstantIndex(float constant)
    {
        for (ConstantInfo ci : listConstantInfo(ConstantFloat.class))
        {
            ConstantFloat ic = (ConstantFloat) ci;
            if (Float.compare(constant, ic.getConstant()) == 0)
            {
                return constantPoolIndexMap.get(ci);
            }
        }
        return -1;
    }
    /**
     * Returns the constant map index to constant
     * @param constant
     * @return
     */
    public final int getConstantIndex(double constant)
    {
        for (ConstantInfo ci : listConstantInfo(ConstantDouble.class))
        {
            ConstantDouble ic = (ConstantDouble) ci;
            if (Double.compare(constant, ic.getConstant()) == 0)
            {
                return constantPoolIndexMap.get(ci);
            }
        }
        return -1;
    }
    /**
     * Returns the constant map index to constant
     * @param constant
     * @return
     */
    public final int getConstantIndex(long constant)
    {
        for (ConstantInfo ci : listConstantInfo(ConstantLong.class))
        {
            ConstantLong ic = (ConstantLong) ci;
            if (constant == ic.getConstant())
            {
                return constantPoolIndexMap.get(ci);
            }
        }
        return -1;
    }
    /**
     * Returns the constant map index to constant
     * @param constant
     * @return
     */
    public final int getConstantIndex(String constant)
    {
        int nameIndex = getNameIndex(constant);
        for (ConstantInfo ci : listConstantInfo(ConstantString.class))
        {
            ConstantString ic = (ConstantString) ci;
            if (nameIndex == ic.getString_index())
            {
                return constantPoolIndexMap.get(ci);
            }
        }
        return -1;
    }

    protected void addMethodInfo(MethodInfo methodInfo)
    {
        if (methods.contains(methodInfo))
        {
            throw new IllegalArgumentException("method "+methodInfo.getName_index()+" "+methodInfo.getDescriptor_index()+" exists already");
        }
        methods.add(methodInfo);
        enclosedElements.add(methodInfo);
    }

    protected void addFieldInfo(FieldInfo fieldInfo)
    {
        if (fields.contains(fieldInfo))
        {
            throw new IllegalArgumentException("field "+fieldInfo+" exists already");
        }
        fields.add(fieldInfo);
        enclosedElements.add(fieldInfo);
    }

    protected List<FieldInfo> getFields()
    {
        return fields;
    }

    protected List<MethodInfo> getMethodInfos()
    {
        return methods;
    }
    
    /**
     * Returns all classnames (in internal form) that this class references
     * @return
     */
    public SortedSet<String> getReferencedClassnames()
    {
        SortedSet<String> set = new TreeSet<String>();
        List<ConstantInfo> list = constantPoolMap.get(ConstantInfo.Clazz.class);
        if (list != null)
        {
            for (ConstantInfo ci : list)
            {
                ConstantInfo.Clazz cls = (Clazz) ci;
                int ni = cls.getName_index();
                String name = getString(ni);
                if (name.startsWith("["))
                {
                    for (int ii=1;ii<name.length();ii++)
                    {
                        if (name.charAt(ii) == 'L')
                        {
                            name = name.substring(ii+1, name.length()-1);
                            set.add(name);
                            break;
                        }
                    }
                }
                else
                {
                    set.add(name);
                }
            }
        }
        return set;
    }

    private int addConstantInfo(ConstantInfo ci)
    {
        return addConstantInfo(ci, constant_pool.size());
    }
    protected int addConstantInfo(ConstantInfo ci, int fromIndex)
    {
        List<ConstantInfo> rest = constant_pool.subList(fromIndex, constant_pool.size());
        int index = rest.indexOf(ci);
        if (index != -1)
        {
            return fromIndex+index+1;
        }
        constant_pool.add(ci);
        index = constant_pool.size();
        if (ci instanceof ConstantInfo.ConstantDouble || ci instanceof ConstantInfo.ConstantLong)
        {
            constant_pool.add(new Filler());
        }
        List<ConstantInfo> list = constantPoolMap.get(ci.getClass());
        if (list == null)
        {
            list = new ArrayList<>();
            constantPoolMap.put(ci.getClass(), list);
        }
        constantPoolIndexMap.put(ci, index);
        list.add(ci);
        return index;
    }
    
    protected void addIndexedElement(int index, Object type)
    {
        assert (type instanceof Element) || (type instanceof ArrayType);
        indexedElementMap.put(index, type);
    }

    private List<ConstantInfo> listConstantInfo(Class<? extends ConstantInfo> cls)
    {
        List<ConstantInfo> list = constantPoolMap.get(cls);
        if (list == null)
        {
            list = new ArrayList<ConstantInfo>();
            constantPoolMap.put(cls, list);
        }
        return list;
    }
    public void addInterface(short intf)
    {
        interfaces.add(intf);
    }
    /**
     * Adds a new attribute
     * @param ai
     */
    public void addAttribute(AttributeInfo ai)
    {
        attributes.add(ai);
    }
    /**
     * Returns a element from constant map
     * @param index
     * @return
     */
    Object getIndexedType(int index)
    {
        Object ae = indexedElementMap.get(index);
        if (ae == null)
        {
            throw new VerifyError("constant pool at "+index+" not proper type");
        }
        return ae;
    }
    /**
     * Returns a descriptor to fields at index.
     * @param index
     * @return
     */
    public String getFieldDescription(int index)
    {
        ConstantInfo constantInfo = getConstantInfo(index);
        if (constantInfo instanceof Fieldref)
        {
            Fieldref fr = (Fieldref) getConstantInfo(index);
            int nt = fr.getName_and_type_index();
            NameAndType nat = (NameAndType) getConstantInfo(nt);
            int ni = nat.getName_index();
            int di = nat.getDescriptor_index();
            return getString(ni)+" "+getString(di);
        }
        return "unknown "+constantInfo;
    }
    /**
     * Returns a descriptor to method at index.
     * @param index
     * @return
     */
    public String getMethodDescription(int index)
    {
        ConstantInfo constantInfo = getConstantInfo(index);
        if (constantInfo instanceof Methodref)
        {
            Methodref mr = (Methodref) getConstantInfo(index);
            int nt = mr.getName_and_type_index();
            NameAndType nat = (NameAndType) getConstantInfo(nt);
            int ni = nat.getName_index();
            int di = nat.getDescriptor_index();
            return getString(ni)+" "+getString(di);
        }
        if (constantInfo instanceof InterfaceMethodref)
        {
            InterfaceMethodref mr = (InterfaceMethodref) getConstantInfo(index);
            int nt = mr.getName_and_type_index();
            NameAndType nat = (NameAndType) getConstantInfo(nt);
            int ni = nat.getName_index();
            int di = nat.getDescriptor_index();
            return getString(ni)+" "+getString(di);
        }
        return "unknown "+constantInfo;
    }
    /**
     * Returns a descriptor to class at index.
     * @param index
     * @return
     */
    public String getClassDescription(int index)
    {
        Clazz cz = (Clazz) getConstantInfo(index);
        int ni = cz.getName_index();
        return getString(ni);
    }
    public boolean isImplemented(ExecutableElement method)
    {
        ExecutableType mt = (ExecutableType) method.asType();
        List<? extends TypeMirror> mpt = mt.getParameterTypes();
        int count = mpt.size();
        for (ExecutableElement exe : ElementFilter.methodsIn(enclosedElements))
        {
            ExecutableType et = (ExecutableType) exe.asType();
            List<? extends TypeMirror> ept = et.getParameterTypes();
            if (
                    exe.getSimpleName().contentEquals(method.getSimpleName()) &&
                    count == ept.size()
                    )
            {
                boolean is = true;
                for (int ii=0;ii<count;ii++)
                {
                    if (!Typ.isSameType(ept.get(ii), mpt.get(ii)))
                    {
                        is = false;
                    }
                }
                if (is)
                {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Return true if class contains method ref to given method
     * @param method
     * @return 
     */
    public boolean referencesMethod(ExecutableElement method)
    {
        TypeElement declaringClass = (TypeElement) method.getEnclosingElement();
        String fullyQualifiedname = declaringClass.getQualifiedName().toString();
        String name = method.getSimpleName().toString();
        assert name.indexOf('.') == -1;
        String descriptor = Descriptor.getDesriptor(method);
        return getRefIndex(Methodref.class, fullyQualifiedname, name, descriptor) != -1;
    }
    public boolean referencesClass(TypeElement type)
    {
        return getClassIndex(type) != -1;
    }
    protected final int getClassIndex(TypeElement type)
    {
        return getClassIndex(El.getInternalForm(type));
    }
    protected final int getClassIndex(ArrayType at)
    {
        return getClassIndex(Descriptor.getFieldDesriptor(at));
    }
    private final int getClassIndex(String name)
    {
        int index = -1;
        for (ConstantInfo ci : listConstantInfo(Clazz.class))
        {
            Clazz cls = (Clazz) ci;
            int nameIndex = cls.getName_index();
            String cn = getString(nameIndex);
            if (name.equals(cn))
            {
                index = constantPoolIndexMap.get(ci);
                break;
            }
        }
        return index;
    }
    /**
     * Return constantInfo at index.
     * @param index
     * @return
     */
    public final ConstantInfo getConstantInfo(int index)
    {
        return constant_pool.get(index-1);
    }
    /**
     * Return a constant string at index.
     * @param index
     * @return
     */
    public final String getString(int index)
    {
        Utf8 utf8 = (Utf8) getConstantInfo(index);
        return utf8.getString();
    }
    public boolean isSynthetic()
    {
        return synthetic;
    }
    
    @Override
    public Name getQualifiedName()
    {
        return qualifiedName;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors()
    {
        return ModelUtil.getAnnotationMirrors(attributes);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType)
    {
        return ModelUtil.getAnnotation(attributes, annotationType);
    }

    @Override
    public TypeMirror getSuperclass()
    {
        return superClass.asType();
    }

    @Override
    public List<? extends Element> getEnclosedElements()
    {
        return enclosedElements;
    }

    @Override
    public NestingKind getNestingKind()
    {
        return NestingKind.TOP_LEVEL;
    }

    @Override
    public Name getSimpleName()
    {
        return simpleName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<? extends TypeMirror> getInterfaces()
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters()
    {
        return superClass.getTypeParameters();
    }

    @Override
    public Element getEnclosingElement()
    {
        return packageElement;
    }

    @Override
    public TypeMirror asType()
    {
        return type;
    }

    @Override
    public ElementKind getKind()
    {
        return ElementKind.CLASS;
    }

    @Override
    public Set<Modifier> getModifiers()
    {
        return modifiers;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p)
    {
        return v.visitType(this, p);
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /*
    public static void main(String... args)
    {
        try
        {
            ClassFile cf = new ClassFile(Dumper.class);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    */
    public class ClassType implements DeclaredType
    {

        @Override
        public Element asElement()
        {
            return ClassFile.this;
        }

        @Override
        public TypeMirror getEnclosingType()
        {
            return packageElement.asType();
        }

        @Override
        public List<? extends TypeMirror> getTypeArguments()
        {
            DeclaredType dt = (DeclaredType) superClass.asType();
            return dt.getTypeArguments();
        }

        @Override
        public TypeKind getKind()
        {
            return TypeKind.DECLARED;
        }

        @Override
        public <R, P> R accept(TypeVisitor<R, P> v, P p)
        {
            return v.visitDeclared(this, p);
        }

        @Override
        public List<? extends AnnotationMirror> getAnnotationMirrors()
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> annotationType)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType)
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}
