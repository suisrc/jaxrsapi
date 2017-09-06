package org.jboss.jdeparser;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.suisrc.common.jdejst.JdeJst;
import com.suisrc.jaxrsapi.core.util.JaxrsapiUtils;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

/**
 * 类型的定义
 * 
 * 在类型的定义，属性的定义，方法的定义，需要进行特使的代理处理，用于应对两个体系间的兼容
 * 
 * @author Y13
 *
 */
public class JJClass extends JJAnnotation {
    
    /**
     * 创建一个类
     * 
     * 该类所在的包为接口所在的包中的impl包中
     * @param jdeJst
     * @param name
     * @return
     */
    public static JJClass create(JdeJst jdeJst, String name) {
        return new JJClass(jdeJst, name, "impl");
    }
    
    /**
     * 代码文件
     */
    private JSourceFile srcFile;
    
    /**
     * 代码类型
     */
    private JClassDef classDef;
    
    /**
     * 类的包名
     */
    private String packageName;
    
    /**
     * 类名
     */
    private String className;
    
    /**
     * 所有导入的类型
     */
    private Set<JType> imports = new HashSet<>();
    
    /**
     * 一次性访问接口
     */
    private boolean isOneTimeProxy = false;
    
    /**
     * 多接口模式
     */
    private boolean isMulitMode = false;
    
    /**
     * jdeparser和javassist控制器
     */
    private JdeJst jdeJst;
    
    /**
     * 属性
     */
    private List<JJField> fields = new ArrayList<>();
    
    /**
     * 方法
     */
    private List<JJMethod> methods = new ArrayList<>();
    
    /**
     * 方法-GET
     */
    private Map<String, JJField> methodGets = new LinkedHashMap<>();
    
    /**
     * 方法-SET
     */
    private Map<String, JJField> methodSets = new LinkedHashMap<>();
    
    /**
     * 接口
     */
    private List<String> _implements = new ArrayList<>();
    
    /**
     * 构造方法
     * @param jdeJst
     * @param name
     */
    private JJClass(JdeJst jdeJst, String name, String pkgName) {
        this.jdeJst = jdeJst;
        
        int offset = name.lastIndexOf('.');
        packageName = (offset < 0 ? "" : name.substring(0, offset + 1)) + pkgName;
        className = offset < 0 ? name : name.substring(offset + 1);
        srcFile = jdeJst.getJdeSrc().createSourceFile(packageName, className);
        classDef = srcFile._class(JMod.PUBLIC, className);
    }

    /**
     * 获取类型所有者
     * @return
     */
    public JdeJst getJdeJst() {
        return jdeJst;
    }
    
    /**
     * 获取代码文件
     */
    public JSourceFile getSrcFile() {
        return srcFile;
    }
    
    /**
     * 获取javassist控制对象
     * @return
     */
    public CtClass getCtClass() {
        ClassPool ctPool = jdeJst.getJstPool();
        checkCtClass(ctPool);
        CtClass ctClass = ctPool.makeClass(getCanonicalName());
        createCtClassInfo(ctClass, ctPool);
        fields.forEach(f -> f.createCtFieldInfo(ctClass));
        createMethodGetInfo(ctClass);
        createMethodSetInfo(ctClass);
        methods.forEach(m -> m.createCtMethodInfo(ctClass));
        createAnnotationInfo(ctClass, ctPool);
        return ctClass;
    }

    /**
     * 解决注解问题
     * @param pool
     */
    private void createAnnotationInfo(CtClass ctClass, ClassPool ctPool) {
        // 基本信息
        ClassFile ctFile = ctClass.getClassFile();
        ConstPool constPool = ctFile.getConstPool();
        optAnnotate(constPool, ctFile::addAttribute);
        fields.forEach(f -> f.optAnnotate(constPool, f.getCtField().getFieldInfo()::addAttribute));
        methods.forEach(m-> m.optAnnotate(constPool, m.getCtMethod().getMethodInfo()::addAttribute));
    }

    /**
     * 对文件中的类型记性check
     * @param pool
     */
    private void checkCtClass(ClassPool pool) {
        try {
            for (JType type : imports) {
                JaxrsapiUtils.getCtClass(pool, ((AbstractJType)type).qualifiedName());
            }
        } catch (ClassNotFoundException | NotFoundException e) {
            throw new RuntimeException(e);
        }
        
    }

    /**
     * 构建set方法的内容
     * @param ctClass
     */
    private void createMethodSetInfo(CtClass ctClass) {
        methodGets.entrySet().forEach(et -> {
            try {
                ctClass.addMethod(CtNewMethod.setter(et.getKey(), et.getValue().getCtField()));
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 构建get方法的内容
     * @param ctClass
     */
    private void createMethodGetInfo(CtClass ctClass) {
        methodGets.entrySet().forEach(et -> {
            try {
                ctClass.addMethod(CtNewMethod.getter(et.getKey(), et.getValue().getCtField()));
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 使用javassist构建类型的内容
     * @param ctClass
     * @param ctPool 
     */
    private void createCtClassInfo(CtClass ctClass, ClassPool ctPool) {
        for (String ifaceStr : _implements) {
            try {
                CtClass iface = ctPool.get(ifaceStr);
                ctClass.addInterface(iface);
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 获取JDeparser控制对象
     * @return
     */
    public JClassDef getJClassDef() {
        return classDef;
    }

    /**
     * 获取类名
     * @return
     */
    public String getSimpleName() {
        return className;
    }

    /**
     * 获取java中类的规范命名
     * @return
     */
    public String getCanonicalName() {
        return packageName + "." + className;
    }
    
    /**
     * 增加类的注释注释
     * @param anno
     * @return
     */
    public JAnnotation annotate(Class<? extends Annotation> anno) {
        JAnnotation jAnno = classDef.annotate(anno);
        newAnnotate(jAnno, anno.getName());
        return jAnno;
    }

    /**
     * 增加类的接口定义
     * @param iface
     */
    public void _implements(String iface) {
        _implements.add(iface);
        classDef._implements(iface);
    }

    /**
     * 增加类的接口定义
     * @param iface
     */
    public void _implements(Class<?> iface) {
        _implements.add(iface.getCanonicalName());
        classDef._implements(iface);
    }

    /**
     * 增加类的属性
     * @param mod
     * @param type
     * @param name
     * @return
     */
    public JJField field(int mod, String type, String name) {
        JJField feild = JJField.create(this, mod, type, name);
        fields.add(feild);
        return feild;
    }

    /**
     * 增加类的属性
     * @param mod
     * @param type
     * @param name
     * @return
     */
    public JJField field(int mod, Class<?> type, String name) {
        JJField feild = JJField.create(this, mod, type, name);
        fields.add(feild);
        return feild;
    }

    /**
     * 配置属性的get方法
     * @param field
     * @param getter
     */
    public JMethodDef getter(JJField field, String getter, String comment) {
        methodGets.put(getter, field);
        
        JMethodDef methodDef = classDef.method(JMod.PUBLIC, field.getJVarDeclaration().type(), getter);
        methodDef.body()._return(JExprs.$v(field.getJVarDeclaration()));
        methodDef.docComment().text(comment);
        return methodDef;
    }

    /**
     * 配置属性的set方法
     * @param field
     * @param setter
     */
    public JMethodDef setter(JJField field, String setter, String comment) {
        methodSets.put(setter, field);
        
        JMethodDef methodDef = classDef.method(JMod.PUBLIC, void.class, setter);
        JParamDeclaration param = methodDef.param(field.getJVarDeclaration().type(), "pm");
        methodDef.body().assign(JExprs.$v(field.getJVarDeclaration()), JExprs.$v(param));
        methodDef.docComment().text(comment);
        return methodDef;
    }

    /**
     * 增加类的方法
     * @param mod
     * @param returnType
     * @param methodName
     * @return
     */
    public JJMethod method(int mod, Class<?> returnType, String methodName) {
        JJMethod method = JJMethod.create(this, mod, returnType, methodName);
        methods.add(method);
        return method;
    }

    /**
     * 增加类的方法
     * @param mod
     * @param returnType
     * @param methodName
     * @return
     */
    public JJMethod method(int mod, String returnType, String methodName) {
        JJMethod method = JJMethod.create(this, mod, returnType, methodName);
        methods.add(method);
        return method;
    }
    
    /**
     * 执行import内容导入
     */
    public void imports() {
        imports.forEach(srcFile::_import);
    }
    
    /**
     * 导入内容
     * @param type
     * @return
     */
    public JJClass _import(final String type) {
        return _import(JTypes.typeNamed(type));
    }

    /**
     * 导入内容
     * @param type
     * @return
     */
    public JJClass _import(final JType type) {
        imports.add(type);
        return this;
    }

    /**
     * 导入内容
     * @param type
     * @return
     */
    public JJClass _import(final Class<?> type) {
        return _import(JTypes.typeOf(type));
    }

    /**
     * 设定一次性访问接口
     * @param otp
     */
    public void setOneTimeProxy(boolean otp) {
        isOneTimeProxy = otp;
    }
    
    /**
     * 判断是否为一次性访问接口
     * @return
     */
    public boolean isOneTimeProxy() {
        return isOneTimeProxy;
    }
    
    /**
     * 判断是否为多接口模式
     * @return
     */
    public boolean isMulitMode() {
        return isMulitMode;
    }
    
    /**
     * 设定是否为多接口
     * @param isMulitMode
     */
    public void setMulitMode(boolean isMulitMode) {
        this.isMulitMode = isMulitMode;
    }

}
