package org.jboss.jdeparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;

/**
 * 属性
 * @author Y13
 *
 */
public class JJField extends JJAnnotation {

    public static JJField create(JJClass jjClass, int mod, String type, String name) {
        JJField jjf = new JJField();
        jjf.fieldDef = jjClass.getJClassDef().field(mod, type, name);
        jjf.jjClass = jjClass;
        return jjf;
    }

    public static JJField create(JJClass jjClass, int mod, Class<?> type, String name) {
        JJField jjf = new JJField();
        jjf.fieldDef = jjClass.getJClassDef().field(mod, type, name);
        jjf.jjClass = jjClass;
        return jjf;
    }
    
    /**
     * 属性的定义
     */
    private JVarDeclaration fieldDef;
    
    /**
     * javassist
     */
    private CtField ctField;
    
    /**
     * 类型
     */
    private JJClass jjClass;
    
    
    /**
     * 禁止使用接口定义
     */
    private JJField() {}

    public JVarDeclaration getJVarDeclaration() {
        return fieldDef;
    }
    
    public CtField getCtField() {
        return ctField;
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
        JAnnotation jAnno = fieldDef.annotate(anno);
        newAnnotate(jAnno, anno.getName());
        return jAnno;
    }

    /**
     * 构建属性的内容
     * @param ctClass
     * @return
     */
    public void createCtFieldInfo(CtClass ctClass) {
        if (fieldDef instanceof Writable ) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ExSourceFileWriter writer = new ExSourceFileWriter(jjClass.getJdeJst().getFormat(), out);
            writer.setClassFile((ImplJSourceFile) jjClass.getSrcFile());
            writer.setOutputStream(out);
            try {
                ((Writable)fieldDef).write(writer);
                writer.nl();
                writer.close();
                String source = new String(out.toByteArray(), "UTF-8");
                ctField = CtField.make(source, ctClass);
                ctClass.addField(ctField);
            } catch (IOException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
