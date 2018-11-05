package com.suisrc.jaxrsapi.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.suisrc.jaxrsapi.tools.dto.Document;
import com.suisrc.jaxrsapi.tools.impl.WxTextDocumentParser;

public class Utils {

    /**
     * 下划线转驼峰
     * @param key
     * @return
     */
    public static String line2camel(String content) {
        StringBuilder sbir = new StringBuilder();
        int offset = 0;
        int index;
        while ((index = content.indexOf('_', offset)) > 0) {
            String str = content.substring(offset, index);
            if (sbir.length() > 0) {
                str = str.substring(0, 1).toUpperCase() + str.substring(1);
            }
            sbir.append(str);
            offset = index + 1;
        }
        String str = content.substring(offset);
        if (sbir.length() > 0) {
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
        }
        sbir.append(str);
        return sbir.toString();
    }
    
    /**
     * 输出生成的内容到控制台
     * @param file
     */
    public static void build2Console(String file) {
        try {
            WxTextDocumentParser pp = new WxTextDocumentParser();
            FileInputStream fis = new FileInputStream(file);
            Document doc = pp.parser(fis);

            System.out.println(doc.toPrintComment());
            System.out.println(doc.toPrintMethod());
            System.out.println(doc.toPrintBodyBean());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void build2File(String inFile, String outFile) {
        try {
            WxTextDocumentParser pp = new WxTextDocumentParser();
            FileInputStream fis = new FileInputStream(inFile);
            Document doc = pp.parser(fis);

            FileOutputStream fos = new FileOutputStream(outFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(doc.toPrintComment());
            osw.write('\n');
            osw.write(doc.toPrintMethod());
            osw.write("\n\n\n\n\n");
            osw.write("-----------------------------讨厌的分割线，你在这里做什么呢?----------------------");
            osw.write("-----------------------------就算我讨厌，你能拿我怎么办---------------------------");
            osw.write("\n\n\n\n\n");
            osw.write(doc.toPrintBodyBean());
            osw.write("\n\n\n\n\n");
            osw.write("-----------------------------爱的那舍难分，爱的奋不顾身---------------------------");
            osw.write("-----------------------------对不起，我是分割线，不要抄歌词-----------------------");
            osw.write("\n\n\n\n\n");
            osw.write(doc.toPrintResultBean());
            osw.write("\n\n\n\n\n");
            osw.write("-----------------------------over over over over over-------------------------");
            osw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
