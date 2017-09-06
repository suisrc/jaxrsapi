package com.suisrc.common.jdejst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.jdeparser.FormatPreferences;
import org.jboss.jdeparser.JDeparser;
import org.jboss.jdeparser.JFiler;
import org.jboss.jdeparser.JJClass;
import org.jboss.jdeparser.JSources;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

/**
 * jdeparser和 javassist适配器，用于使这两个工具兼容。
 * 
 * jdeparser在生成代码的时候具有很大的优势，但是无法对代码进行动态编译，而javaCompile使用起来有点生涩
 * 所以使用适配器，让jdeparser和javassist兼容，取jdeparser生成代码的优势和javassist编译优势。
 * 同时jdeparser生成代码后，可以看到最初的源代码，这个在调试和测试的时候非常有效。
 * 
 * 子处理过程中，由于时间的原因，目前支持文件中单类的情况
 * 
 * @author Y13
 *
 */
public class JdeJst {
    
    /**
     * 是否显示代码
     */
    private boolean show_src = Boolean.valueOf(System.getProperty(JdeJst.class.getName() + ".show_src", "false"));
    
    /**
     * 内部文件写出流的提供者
     *
     */
    private class InternalFitler extends JFiler {
        @Override
        public OutputStream openStream(String packageName, String fileName) throws IOException {
            if (target == null) {
                return System.out; // 输出到标准输入流中
            }
            final File dir = new File(target, packageName.replace('.', File.separatorChar));
            dir.mkdirs();
            return new FileOutputStream(new File(dir, fileName + ".java"));
        }
        
    }
    
    /**
     * jdeparser代码控制器
     * 
     * 用于代码内容生成
     */
    private JSources jdeSrc;
    
    /**
     * javassist代码控制器
     * 
     * 用于代码生成后注入
     */
    private ClassPool jstPool;
    
    /**
     * 代码最终写出的位置
     */
    private File target;
    
    /**
     * 代码格式化方式
     */
    private FormatPreferences format;
    
    /**
     * 创建的所有class
     */
    private Map<Object, JJClass> classMap;

    //-----------------------------------------------------------ZERO 构建
    
    /**
     * 构造方法
     */
    public JdeJst() {
        initialize();
    }
    
    //-----------------------------------------------------------ZERO get_set
    
    public JSources getJdeSrc() {
        return jdeSrc;
    }

    public void setJdeSrc(JSources jdeSrc) {
        this.jdeSrc = jdeSrc;
    }

    public ClassPool getJstPool() {
        return jstPool;
    }

    public void setJstPool(ClassPool jstPool) {
        this.jstPool = jstPool;
    }

    public File getTarget() {
        return target;
    }

    public void setTarget(File target) {
        this.target = target;
    }

    public FormatPreferences getFormat() {
        return format;
    }

    public void setFormat(FormatPreferences format) {
        this.format = format;
    }

    //-----------------------------------------------------------ZERO 计算逻辑
    
    /**
     * 判断是否显示代码
     * @return
     */
    public boolean isShowSrc() {
        return show_src;
    }
    
    /**
     * 配置是否强制显示代码
     * @param show
     */
    public void setShowSrc(boolean show) {
        show_src = show;
    }
    
    /**
     * 初始化
     */
    protected void initialize() {
        format = new FormatPreferences();
        target = null;
        classMap = new LinkedHashMap<>();
        
        jdeSrc = JDeparser.createSources(new InternalFitler(), format);
        jstPool = new ClassPool(true); // 不使用默认的，这样尽可能在使用后丢弃，用以节省内存
    }

    /**
     * 释放
     */
    public void destory() {
        format = null;
        target = null;
        jdeSrc = null;
        jstPool = null;
    }
    
    /**
     * 创建class
     */
    public JJClass createClass(Object key, String name) {
        JJClass jjc = JJClass.create(this, name);
        add(key != null ? key : name, jjc);
        return jjc;
        
    }

    /**
     * 增加到类型集合中
     * @param jjc
     */
    protected void add(Object key, JJClass jjc) {
        classMap.put(key, jjc);
    }
    
    /**
     * 写出代码
     * @return
     */
    public Map<Object, CtClass> writeSource() {
        Map<Object, CtClass> ctClasses = new LinkedHashMap<>();
        classMap.entrySet().forEach(v -> ctClasses.put(v.getKey(), v.getValue().getCtClass()));
        // 是否出力生成结果
        if (isShowSrc()) {
            try {
                jdeSrc.writeSources();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ctClasses;
    }
    
    /**
     * 写出代码到classloader中
     */
    public void writeSource2ClassLoader(ClassLoader cl) {
        Collection<CtClass> srcs = writeSource().values();
        try {
            for (CtClass src : srcs) {
                src.toClass(cl, null);
            }
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 获取代码字节
     */
    public Map<Object, byte[]> writeSource4Byte() {
        Map<Object, CtClass> srcs = writeSource();
        Map<Object, byte[]> bytes = new LinkedHashMap<>();
        try {
            for (Entry<Object, CtClass> src : srcs.entrySet()) {
                bytes.put(src.getKey(), src.getValue().toBytecode());
            }
        } catch (IOException | CannotCompileException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }

    /**
     * 写出class文件
     */
    public void writeSource4File() {
        try {
            classMap.values().forEach(cm -> cm.imports());
            jdeSrc.writeSources();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
