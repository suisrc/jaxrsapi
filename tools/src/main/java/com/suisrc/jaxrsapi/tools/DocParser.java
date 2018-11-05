package com.suisrc.jaxrsapi.tools;

import java.io.InputStream;

import com.suisrc.jaxrsapi.tools.dto.Document;

/**
 * 
 * 文档分析
 * 
 * document
 * 
 * @author Y13
 *
 */
public interface DocParser {
    
    /**
     * 解析内容到文档中
     * 
     * @param inputstream
     * @return
     */
    Document parser(InputStream inputstream);

}
