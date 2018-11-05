package com.suisrc.jaxrsapi.tools.dto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.jdeparser.FormatPreferences;
import org.jboss.jdeparser.JAnnotation;
import org.jboss.jdeparser.JClassDef;
import org.jboss.jdeparser.JDeparser;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JFiler;
import org.jboss.jdeparser.JMethodDef;
import org.jboss.jdeparser.JMod;
import org.jboss.jdeparser.JParamDeclaration;
import org.jboss.jdeparser.JSourceFile;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JVarDeclaration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.suisrc.jaxrsapi.tools.Utils;

/**
 * 将JSON格式转换为class
 * @author Y13
 *
 */
public class JsonNode2Class {

    /**
     * JSON的根
     */
    private JsonNode root;
    
    /**
     * 类型名称
     */
    private String name;
    
    /**
     * 说明
     */
    private Map<String, FieldDesc> commnets;
    
    
    
    /**
     * 构造方法
     * @param root
     * @param name
     */
    public JsonNode2Class(JsonNode root, String name, Map<String, FieldDesc> commnets ) {
        this.root = root;
        this.name = name;
        this.commnets = commnets;
    }

    public String toClassString() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        try {
            FormatPreferences format = new FormatPreferences();
            JSources sources = JDeparser.createSources(new JFiler() {
                public OutputStream openStream(String packageName, String fileName) throws IOException {
                    return output;
                }
            }, format);

            JSourceFile srcfile = sources.createSourceFile("pkg", name);
            Set<String> imports = new TreeSet<>();
            buildClass(name, root, srcfile, imports, JMod.PUBLIC, "");
            imports.forEach(srcfile::_import);
            sources.writeSources();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return output.toString();
    }

    private JClassDef buildClass(String className, JsonNode root, JSourceFile srcfile, Collection<String> imports, int mod, String model) {
        
        
        JClassDef jclass = srcfile._class(mod, className);
        imports.add("io.swagger.annotations.ApiModel");
        JAnnotation janno = jclass.annotate("io.swagger.annotations.ApiModel");
        janno.value(model + className);
        
        Map<String, JVarDeclaration> jvars = new LinkedHashMap<>();
        
        String model1 = model + className + ".";
        root.fields().forEachRemaining(e -> {
            String fieldName = Utils.line2camel(e.getKey());
            JsonNodeType type = e.getValue().getNodeType();
            String fieldClass = getFieldClass(srcfile, imports, mod, e.getValue(), fieldName, type, model1);
            if (fieldClass == null) {
                System.err.println(e.getKey());
                return;
            }
            
            JVarDeclaration jvar = jclass.field(JMod.PRIVATE, fieldClass, fieldName);
            FieldDesc desc = commnets.get(e.getKey()); // 获取配置描述
            if (desc != null) {
                imports.add("io.swagger.annotations.ApiModelProperty");
                imports.add("com.fasterxml.jackson.annotation.JsonProperty");
                JAnnotation jannoVar = jvar.annotate("io.swagger.annotations.ApiModelProperty");
                jannoVar.value(desc.getDescription());
                if (desc.isMust()) {
                    imports.add("com.suisrc.jaxrsapi.core.annotation.NotNull");
                    jannoVar = jvar.annotate("com.suisrc.jaxrsapi.core.annotation.NotNull");
                    jannoVar.value(e.getKey() + "属性为空");
                }
            }
            JAnnotation jannoVar = jvar.annotate("com.fasterxml.jackson.annotation.JsonProperty");
            jannoVar.value(e.getKey());
            jvars.put(e.getKey(), jvar);
        });
        
        jvars.forEach((k, jvar) -> {
            String fieldName = jvar.name();
            String fn = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            
            JMethodDef jget = jclass.method(JMod.PUBLIC, jvar.type(), "get" + fn);
            jget.body()._return(JExprs.$v(jvar));

            JMethodDef jset = jclass.method(JMod.PUBLIC, void.class, "set" + fn);
            JParamDeclaration param = jset.param(jvar.type(), fieldName);
            jset.body().assign(JExprs.$v("this").field(fieldName), JExprs.$v(param));

            FieldDesc desc = commnets.get(k); // 获取配置描述
            if (desc != null) {
                jget.docComment().text(desc.getDescription());
                jset.docComment().text(desc.getDescription());
            }
        });
        return jclass;
    }

    /**
     * 
     * @param srcFile
     * @param imports
     * @param mod
     * @param e
     * @param fieldName
     * @param type
     * @return
     */
    private String getFieldClass(JSourceFile srcfile, Collection<String> imports, int mod, JsonNode root, String fieldName, JsonNodeType type, String model) {
        boolean array = false;
        JsonNode child = root;
        if (type == JsonNodeType.ARRAY) {
            ArrayNode node = (ArrayNode) root;
            child = node.get(0);
            if (child == null) {
                return null;
            }
            type = child.getNodeType();
            array = true;
        }
        String className = null;
        if (type == JsonNodeType.NUMBER) {
            className = Integer.class.getName();
        }
        if (type == JsonNodeType.BOOLEAN) {
            className = Boolean.class.getName();
        }
        if (type == JsonNodeType.OBJECT) {
            String className2 = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            buildClass(className2, child, srcfile, imports, mod | JMod.STATIC, model);
            className = className2;
        }
        if (type == JsonNodeType.POJO) {
            String className2 = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            buildClass(className2, child, srcfile, imports, mod | JMod.STATIC, model);
            className = className2;
        }
        if (type == JsonNodeType.STRING) {
            className = String.class.getName();
        }
        return className == null ? null : array ? className + "[]" : className;
    }
}
