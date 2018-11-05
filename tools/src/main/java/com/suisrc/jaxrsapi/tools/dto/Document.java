package com.suisrc.jaxrsapi.tools.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suisrc.jaxrsapi.tools.Utils;

/**
 * 数据文档
 * 
 * 用于生成API接口所需要的所有数据
 * 
 * @author Y13
 *
 */
public class Document {
    
    /**
     * 请求的名称
     */
    private String name;
    
    /**
     * 请求的描述
     */
    private String description;
    
    /**
     * 请求方式
     */
    private String requestMethod;
    
    /**
     * 请求地址
     */
    private String requestUrl;
    
    /**
     * 请求包体
     */
    private String requestJson;
    
    /**
     * 请求包体的JSON描述
     */
    private JsonNode requestNode;
    
    /**
     * 参数说明
     */
    private Map<String, FieldDesc> paramMap = new LinkedHashMap<>();
    
    /**
     * 特别说明
     */
    private String note;
    
    /**
     * 返回结果
     */
    private String resultJson;
    
    /**
     * 返回结果的JSON描述
     */
    private JsonNode resultNode;
    
    /**
     * 返回结果描述
     */
    private Map<String, FieldDesc> resultMap = new LinkedHashMap<>();
    
    /**
     * 结果类型的名称
     */
    private String resultClassName;
    
    /**
     * 数据体类型的名称
     */
    private String bodyClassName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public JsonNode getRequestNode() {
        return requestNode;
    }

    public void setRequestNode(JsonNode requestNode) {
        this.requestNode = requestNode;
    }

    public Map<String, FieldDesc> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, FieldDesc> paramMap) {
        this.paramMap = paramMap;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public JsonNode getResultNode() {
        return resultNode;
    }

    public void setResultNode(JsonNode resultNode) {
        this.resultNode = resultNode;
    }

    public Map<String, FieldDesc> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, FieldDesc> resultMap) {
        this.resultMap = resultMap;
    }

    private String toPrintFieldMapString(String prefix, Map<String, FieldDesc> map, boolean ism) {
        StringBuilder sbir = new StringBuilder();
        map.forEach((k, v) -> {
            sbir.append(v.toPrintString(prefix, ism)).append('\n');
        });
        if (sbir.length() == 0) {
            return prefix + "\n";
        }
        return sbir.toString();
    }

    private String toPrintTextString(String prefix, String json) {
        if (json == null) {
            return prefix + "\n";
        }
        StringBuilder sbir = new StringBuilder();
        String[] lines = json.split("\\n");
        for (String line : lines) {
            sbir.append(prefix).append(line).append('\n');
        }
        return sbir.toString();
    }

    public String toPrintComment() {
        String prefix = "    * ";
        
        StringBuilder sbir = new StringBuilder("    /**\n");
        sbir.append(toPrintTextString(prefix, name));
        sbir.append(prefix).append('\n');
        sbir.append(prefix).append("请求描述：").append(description).append('\n');
        sbir.append(prefix).append('\n');
        sbir.append(prefix).append("请求方式：").append(requestMethod).append('\n');
        sbir.append(prefix).append('\n');
        sbir.append(prefix).append("请求地址：").append(requestUrl).append('\n');
        sbir.append(prefix).append('\n');
        sbir.append(prefix).append("请求包体：").append('\n');
        sbir.append(toPrintTextString(prefix, requestJson));
        sbir.append(prefix).append('\n');
        sbir.append(prefix).append("参数说明：").append('\n');
        sbir.append(toPrintFieldMapString(prefix, paramMap, true));
        sbir.append(prefix).append("说明：").append('\n');
        sbir.append(toPrintTextString(prefix, note));
        sbir.append(prefix).append('\n');
        sbir.append(prefix).append("返回结果：").append('\n');
        sbir.append(toPrintTextString(prefix, resultJson));
        sbir.append(prefix).append('\n');
        sbir.append(prefix).append("参数说明：").append('\n');
        sbir.append(toPrintFieldMapString(prefix, resultMap, false));
        sbir.append(prefix).append('\n');;
        
        sbir.append("    */");
        return sbir.toString();
    }
    
    /**
     * 
     * @return
     */
    public String toPrintMethod() {
        String prefix = "    ";
        StringBuilder sbir = new StringBuilder();
        sbir.append(prefix);
        int offset = requestMethod.indexOf('(');
        if (offset < 0) {
            offset = requestMethod.indexOf('（');
        }
        String method = offset > 0 ? requestMethod.substring(0, offset) : requestMethod;
        method = method.trim();
        sbir.append("@" + method);
        sbir.append('\n');
        
        offset = requestUrl.indexOf('?');
        String url = requestUrl.substring(0, offset);
        String param = requestUrl.substring(offset + 1);
        int off1 = url.lastIndexOf('/');
        String path = url.substring(off1 + 1);
        sbir.append(prefix);
        sbir.append("@Path(\"" + path + "\")");
        sbir.append('\n');
        
        sbir.append(prefix).append("@Produces(MediaType.APPLICATION_JSON)");
        sbir.append('\n');
        sbir.append(prefix).append("@Consumes(MediaType.APPLICATION_JSON)");
        sbir.append('\n');

        offset = name.lastIndexOf("-:");
        String name1 = offset > 0 ? name.substring(offset + 2) : path;
        String name2 = name1.substring(0, 1).toUpperCase() + name1.substring(1);
        String resultClass = resultMap.size() > 3 ? name2 + "Result" : "WxErrCode";
        String bodyClass = name2 + "Body";
        
        // (@QueryParam("suite_access_token")@Value(QyConsts.SUITE_ACCESS_TOKEN)@NotNull("应用的访问令牌为空") String satoken, UserDetail3rdBody body)
        StringBuilder paramDesc = new StringBuilder();
        paramDesc.append('(');
        String[] paramValues = param.split("&");
        for (String pValue : paramValues) {
            offset = pValue.indexOf('=');
            String key = pValue.substring(0, offset);
            
            paramDesc.append("@QueryParam(\"" + key + "\")");
            FieldDesc fd = paramMap.get(key);
            if (fd!= null) {
                paramDesc.append("@NotNull(\"内容为空\")");
            }
            paramDesc.append(" String ").append(Utils.line2camel(key));
            paramDesc.append(", ");
        }
        if (requestJson != null) {
            paramDesc.append(bodyClass).append(" body, ");
        }
        if (paramDesc.length() > 1) {
            paramDesc.setLength(paramDesc.length() - 2);
        }
        paramDesc.append(')');
        sbir.append(prefix).append(resultClass).append(' ').append(name1).append(paramDesc).append(';');
        
        if (resultMap.size() > 3) {
            resultClassName = resultClass;
        }
        if (requestJson != null) {
            bodyClassName = bodyClass;
        }
        return sbir.toString();
    }
    
    /**
     * 
     * @return
     */
    public String toPrintResultBean() {
        if (resultClassName == null) {
            return "none";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            resultNode = mapper.readTree(resultJson);
            return toPrintBean(resultClassName, resultNode, resultMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    /**
     * 
     * @return
     */
    public String toPrintBodyBean() {
        if (bodyClassName == null) {
            return "none";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            requestNode = mapper.readTree(requestJson);
            return toPrintBean(bodyClassName, requestNode, paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String toPrintBean(String className, JsonNode root, Map<String, FieldDesc> comments) {
        return new JsonNode2Class(root, className, comments).toClassString();
    }
}
