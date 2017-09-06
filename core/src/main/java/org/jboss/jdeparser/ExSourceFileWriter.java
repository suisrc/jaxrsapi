package org.jboss.jdeparser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.jboss.jdeparser.FormatPreferences.Space;

/**
 * 用户扩展原有的JDEparser组件的内容
 * @author Y13
 *
 */
public class ExSourceFileWriter extends SourceFileWriter {
    
    /**
     * 输出流
     */
    private ByteArrayOutputStream out;

    /**
     * 构造方法
     * @param format
     * @param writer
     */
    public ExSourceFileWriter(ByteArrayOutputStream out) {
        this(new FormatPreferences(), out);
    }


    /**
     * 构造方法
     * @param format
     * @param writer
     */
    public ExSourceFileWriter(FormatPreferences format, ByteArrayOutputStream out) {
        this(format, new OutputStreamWriter(out));
    }
    /**
     * 构造方法
     * @param format
     * @param writer
     */
    public ExSourceFileWriter(FormatPreferences format, Writer writer) {
        super(format, writer);
    }
    
    /**
     * 加入注解监听
     * @param rule
     * @throws IOException
     */
    void write(FormatPreferences.Space rule) throws IOException {
        super.write(rule);
        if (rule == Space.AFTER_ANNOTATION && out != null) {
            // 清空注解，javassist无法正确解析注解的内容
            flush();
            out.reset();
        }
        
    }

    /**
     * 暂时解决
     * @param out
     */
    void setOutputStream(ByteArrayOutputStream out) {
        this.out = out;
    }

}
