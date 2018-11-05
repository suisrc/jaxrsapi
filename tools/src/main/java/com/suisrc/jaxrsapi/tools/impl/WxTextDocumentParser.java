package com.suisrc.jaxrsapi.tools.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.suisrc.core.reference.RefVal;
import com.suisrc.jaxrsapi.tools.BulkParser;
import com.suisrc.jaxrsapi.tools.DocParser;
import com.suisrc.jaxrsapi.tools.dto.Document;
import com.suisrc.jaxrsapi.tools.dto.FieldDesc;

/**
 * 普通文档解析工具
 * 
 * @author Y13
 *
 */
public class WxTextDocumentParser implements DocParser {

    @Override
    public Document parser(InputStream inputstream) {
        Document doc = null;
        try {
            doc = buildDocument(inputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 返回文档信息
        return doc;
    }

    private Document buildDocument(InputStream inputstream) throws IOException {
        Document doc = new Document();
        // 读取信息
        InputStreamReader isr = new InputStreamReader(inputstream);
        BufferedReader reader = new BufferedReader(isr);
        // 读取内容时候，使用的缓存控制器
        RefVal<String> buffer = new RefVal<>();
        // 缓冲器
        Map<String, String> cache = new HashMap<>();
        // 获取解析器
        BulkParser[] parsers = getParsers();
        // 读取名称内容
        String name = readNextBulk(buffer, reader, true);
        doc.setName(name);
        // 读取解析的内容
        jumpSpaceLine(buffer, reader);
        while(buffer.get() != null) {
            for (BulkParser parser : parsers) {
                if (parser.process(cache, buffer, doc, reader)) {
                    break;
                }
            }
            jumpSpaceLine(buffer, reader);
        }
        reader.close();
        return doc;
    }
    
    /**
     * 解析SQL文本块的解析器
     * @return
     */
    private BulkParser[] getParsers() {
        return new BulkParser[]{
                this::buildDescriptionBulk,
                this::buildRequestMethodBulk,
                this::buildRequestUrlBulk,
                this::buildRequestBodyBulk,
                this::buildNodeBulk,
                this::buildResultBulk,
                this::jumpThisLine // 强制读取下一行,行内容无法处理
        };
    }

    /**
     * 
     */
    private boolean buildResultBulk(Map<String, String> cache, RefVal<String> buffer, Document doc, BufferedReader reader) throws IOException {
        return buildInfoBulk(buffer, reader, "返回结果：", content -> {
            String key = "参数说明：\n";
            int offset = content.indexOf(key);
            String json = content.substring(0, offset);
            if (!json.isEmpty()) {
                doc.setResultJson(json);
            }
            String desc = content.substring(offset + key.length());
            String[] lines = desc.split("\\n");
            for (String line : lines) {
                String[] strs = line.split("\\s+");
                FieldDesc fd = new FieldDesc();
                fd.setName(strs[0]);
                fd.setDescription(strs[1]);
                doc.getResultMap().put(fd.getName(), fd);
            }
        }, true);
    }

    /**
     * 
     */
    private boolean buildNodeBulk(Map<String, String> cache, RefVal<String> buffer, Document doc, BufferedReader reader) throws IOException {
        return buildInfoBulk(buffer, reader, "说明：", doc::setNote, true);
    }

    /**
     * 
     */
    private boolean buildRequestBodyBulk(Map<String, String> cache, RefVal<String> buffer, Document doc, BufferedReader reader) throws IOException {
        return buildInfoBulk(buffer, reader, "请求包体：", content -> {
            String key = "参数说明：\n";
            int offset = content.indexOf(key);
            String json = content.substring(0, offset);
            if (!json.isEmpty()) {
                doc.setRequestJson(json);
            }
            String desc = content.substring(offset + key.length());
            String[] lines = desc.split("\\n");
            for (String line : lines) {
                String[] strs = line.split("\\s+");
                FieldDesc fd = new FieldDesc();
                fd.setName(strs[0]);
                fd.setMust("是".equals(strs[1]) ? true : false);
                fd.setDescription(strs[2]);
                doc.getParamMap().put(fd.getName(), fd);
            }
        }, true);
    }
    
    /**
     * 
     */
    private boolean buildRequestUrlBulk(Map<String, String> cache, RefVal<String> buffer, Document doc, BufferedReader reader) throws IOException {
        return buildInfoBulkInLine(buffer, reader, "请求地址：", doc::setRequestUrl);
    }

    /**
     * 
     */
    private boolean buildRequestMethodBulk(Map<String, String> cache, RefVal<String> buffer, Document doc, BufferedReader reader) throws IOException {
        return buildInfoBulkInLine(buffer, reader, "请求方式：", doc::setRequestMethod);
    }

    /**
     * 
     */
    private boolean buildDescriptionBulk(Map<String, String> cache, RefVal<String> buffer, Document doc, BufferedReader reader) throws IOException {
        return buildInfoBulk(buffer, reader, "请求描述：", doc::setDescription, true);
    }

    /**
     * 
     * @param buffer
     * @param info
     * @param reader
     * @return
     * @throws IOException 
     */
    private boolean buildInfoBulkInLine(RefVal<String> buffer, BufferedReader reader, String key, Consumer<String> consumer) throws IOException {
        if (!buffer.get().startsWith(key)) {
            return false;
        }
        String statement = buffer.get().substring(key.length());
        if (statement != null && !statement.isEmpty()) {
            consumer.accept(statement);
        }
        readNextLine(buffer, reader);
        return true;
    }
    

    /**
     * 
     * @param buffer
     * @param info
     * @param reader
     * @return
     * @throws IOException 
     */
    private boolean buildInfoBulk(RefVal<String> buffer, BufferedReader reader, String key, Consumer<String> consumer, boolean sp) throws IOException {
        if (!buffer.get().startsWith(key)) {
            return false;
        }
        String statement = readNextBulk(buffer, reader, sp);
        if (statement != null && !statement.isEmpty()) {
            consumer.accept(statement);
        }
        return true;
    }
    
    /**
     * 读取下一行
     * @param buffer
     * @param info
     * @param reader
     * @return
     */
    private boolean jumpThisLine(Map<String, String> cache, RefVal<String> buffer, Document doc, BufferedReader reader) throws IOException {
        readNextLine(buffer, reader); // 改行内容无法处理，跳过
        return true;
    }
    
    /**
     * 跳过空行
     * @param buffer
     * @param reader
     * @throws IOException
     */
    private void jumpSpaceLine(RefVal<String> buffer, BufferedReader reader) throws IOException {
        while (buffer.get() != null && buffer.get().trim().isEmpty()) {
            readNextLine(buffer, reader); // 跳过空行
        }
    }

    /**
     * 读取下一行
     * @param buffer
     * @param info
     * @param reader
     * @return
     */
    private void readNextLine(RefVal<String> buffer, BufferedReader reader) throws IOException {
        buffer.set(reader.readLine());
    }

    /**
     * 读取下一模块
     * @param buffer
     * @param reader
     * @return
     * @throws IOException
     */
    private String readNextBulk(RefVal<String> buffer, BufferedReader reader, boolean sp) throws IOException {
        StringBuilder sbir = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null && !line.isEmpty()) {
            sbir.append(line).append(sp ? '\n' : ' ');
        }
        buffer.set(line);
        return sbir.length() > 0 ? sbir.toString().trim() : null;
    }
}
