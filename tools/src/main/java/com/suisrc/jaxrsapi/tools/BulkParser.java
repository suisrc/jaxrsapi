package com.suisrc.jaxrsapi.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import com.suisrc.core.reference.RefVal;
import com.suisrc.jaxrsapi.tools.dto.Document;

/**
 * 区块处理
 * 
 * @author Y13
 *
 */
public interface BulkParser {

    /**
     * 
     * @param cache
     * @param current
     * @param doc
     * @param reader
     * @return
     * @throws IOException
     */
    boolean process(Map<String, String> cache, RefVal<String> current, Document doc, BufferedReader reader) throws IOException;

}
