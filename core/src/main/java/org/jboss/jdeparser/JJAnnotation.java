package org.jboss.jdeparser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

/**
 * 解决类型上的注解问题
 * 
 * 在jdeparser想javassist转换过程中，注解在javassist中的解析是无法识别的，这里增加特殊的方法进行解决
 * 
 * @author Y13
 *
 */
public class JJAnnotation {
    
    /**
     * 注解信息
     */
    protected static class AnnoInfo {
        
        /**
         * 注解类型
         */
        protected final String annoType;
        
        /**
         * 构建注解后执行的操作
         */
        protected final List<BiConsumer<ConstPool, Annotation>> opts = new ArrayList<>();
        
        AnnoInfo(String type) {
            this.annoType = type;
        }
    }
    

    /**
     * 注解信息
     */
    protected Map<JAnnotation, AnnoInfo> _annotations = new LinkedHashMap<>(4);
    
    /**
     * 判断是否有注解
     */
    public boolean hasAnnotate() {
        return !_annotations.isEmpty();
    }
    
    /**
     * 处理注解
     */
    public void optAnnotate(ConstPool constPool, Consumer<AnnotationsAttribute> handler) {
        if (hasAnnotate()) {
            AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            for (AnnoInfo anno : _annotations.values()) {
                javassist.bytecode.annotation.Annotation annotation = 
                        new javassist.bytecode.annotation.Annotation(anno.annoType, constPool);
                anno.opts.forEach(opt -> opt.accept(constPool, annotation));
                attribute.addAnnotation(annotation);
            }
            handler.accept(attribute);
        }
    }

    /**
     * 增加一个注释
     * @param jAnno
     * @param type
     */
    protected void newAnnotate(JAnnotation jAnno, String type) {
        _annotations.put(jAnno, new AnnoInfo(type));
    }

    /**
     * 增加一个注释的值
     * @param anno
     * @param name
     * @param literal
     */
    public void annotateValue(JAnnotation anno, String name, String literal) {
        anno.value(name, literal);
        // javassist回调使用，回调后执行注解解析其中的值
        // Annotation.createMemberValue
        AnnoInfo annoInfo = _annotations.get(anno);
        annoInfo.opts.add((cp, an) -> an.addMemberValue(name, new StringMemberValue(literal, cp)));
    }
}
