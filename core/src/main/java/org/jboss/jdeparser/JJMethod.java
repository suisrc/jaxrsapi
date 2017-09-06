package org.jboss.jdeparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

/**
 * 方法
 * @author Y13
 *
 */
public class JJMethod extends JJAnnotation {

    public static JJMethod create(JJClass jjClass, int mod, Class<?> returnType, String methodName) {
        JJMethod jjm = new JJMethod();
        jjm.methodDef = jjClass.getJClassDef().method(mod, returnType, methodName);
        jjm.jjClass = jjClass;
        return jjm;
    }

    public static JJMethod create(JJClass jjClass, int mod, String returnType, String methodName) {
        JJMethod jjm = new JJMethod();
        jjm.methodDef = jjClass.getJClassDef().method(mod, returnType, methodName);
        jjm.jjClass = jjClass;
        return jjm;
    }
    
    /**
     * 方法定义
     */
    private JMethodDef methodDef;
    
    /**
     * javassist
     */
    private CtMethod ctMethod;
    
    /**
     * 类型
     */
    private JJClass jjClass;
    
    /**
     * 构造方法
     */
    private JJMethod() {}

    public JMethodDef getJMethodDef() {
        return methodDef;
    }
    
    public CtMethod getCtMethod() {
        return ctMethod;
    }
    
    /**
     * 获取所属类型
     * @return
     */
    public JJClass getJJClass() {
        return jjClass;
    }
    
    /**
     * 增加类的注释注释
     * @param anno
     * @return
     */
    public JAnnotation annotate(Class<? extends Annotation> anno) {
        JAnnotation jAnno = methodDef.annotate(anno);
        newAnnotate(jAnno, anno.getName());
        return jAnno;
    }

    /**
     * 构建方法的内容
     * @param ctClass
     * @return
     */
    public void createCtMethodInfo(CtClass ctClass) {
        if (methodDef instanceof Writable ) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ExSourceFileWriter writer = new ExSourceFileWriter(jjClass.getJdeJst().getFormat(), out);
            writer.setClassFile((ImplJSourceFile) jjClass.getSrcFile());
            writer.setOutputStream(out);
            try {
                ((Writable)methodDef).write(writer);
                writer.nl();
                writer.close();
                String source = new String(out.toByteArray(), "UTF-8");
                ctMethod = CtNewMethod.make(source, ctClass);
                ctClass.addMethod(ctMethod);
            } catch (IOException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
