package com.suisrc.jaxrsapi.soap.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.annotation.Priority;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.client.jaxrs.internal.proxy.ClientInvoker;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import com.suisrc.core.Global;
import com.suisrc.core.cache.ScopedCache;
import com.suisrc.core.utils.FileUtils;

/**
 * 用户解析SOAP12的内容
 * 
 * 注意： 原本soap1.2内容应该使用@WebService内容解析， 推荐通过axis2构建WebServiceClient内容进行访问。
 * 由于WebService可以通过http和rcp等各种内容访问。
 * 
 * 所以这里吧WebService访问形式通过Restful web service进行兼容和模仿
 * 
 * 注意，只能给Client使用，不能用户服务器解析, 仅仅作为临时使用内容
 * 再次特别强调，该内容只能给Client使用
 * 
 * @author Y13
 *
 */
@Provider
@Consumes(SoapConsts.APPLICATION_SOAP12_XML)
@Produces(SoapConsts.APPLICATION_SOAP12_XML)
@Priority(10)
public class JacksonClientSoap12Provider extends JacksonXMLProvider {
    private static final Logger logger = Logger.getLogger(JacksonClientSoap12Provider.class.getName());
    private static final boolean debug = Boolean.getBoolean(JacksonClientSoap12Provider.class.getName());
    
    private static ThreadLocal<Map<Object, Object>> thread_cache = null;
    public static Map<Object, Object> getThreadCache() {
        if (thread_cache == null) {
            thread_cache = new ThreadLocal<>();
        }
        Map<Object, Object> cache = thread_cache.get();
        if (cache == null) {
            thread_cache.set(cache = new ConcurrentHashMap<>());
        }
        return cache;
    }
    
    private String prefix = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" 
            + "<soap12:Envelope "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
            + "xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">"
            + "<soap12:Body>";
    
    private String suffix = "</soap12:Body>"
            + "</soap12:Envelope>";

    public JacksonClientSoap12Provider() {
    }
    
    public JacksonClientSoap12Provider(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }
    
    /**
     * 验证是否需要使用该解析器
     */
    protected boolean hasMatchingMediaType(MediaType mediaType) {
        if (mediaType != null) {
            String subtype = mediaType.getSubtype();
            return "soap+xml".equalsIgnoreCase(subtype);
        }
        return false;
    }
    
    @Override
    public XmlMapper _locateMapperViaProvider(Class<?> type, MediaType mediaType) {
        return new XmlMapper();
    }

    /**
     * Method that JAX-RS container calls to deserialize given value.
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        XmlMapper mapper = locateMapper(type, mediaType);
        // 配置
        String content = FileUtils.getContent(entityStream);
        if (debug) {
            logger.info("read soap12: " + content);
        }
        Soap12XmlMethod wma = type.getAnnotation(Soap12XmlMethod.class);
        if (wma == null) {
            wma = findSoap12XmlMethod();
        }
        if (wma == null) {
            throw new RuntimeException("no found 'Soap12XmlMethod' Annotation");
        }
        String nodeName = !wma.operationResponse().isEmpty() ? wma.operationResponse() : wma.operationName() + "Response";
        int offset0 = content.indexOf(nodeName);
        offset0 = content.indexOf('>', offset0) + 1;
        int offset1 = content.lastIndexOf(nodeName);
        offset1 = content.lastIndexOf('<', offset1);
        JsonParser parser = mapper.getFactory().createParser(content.substring(offset0, offset1));
        // 基本root
        JsonNode root = mapper.readTree(parser);
        // 构建对象
        return mapper.treeToValue(root, type);
    }

    /**
     * 使用特殊方法查找调用的内容
     * @return
     */
    private Soap12XmlMethod findSoap12XmlMethod() {
        ScopedCache cache = Global.getThreadCache();
        ClientInvoker invoker;
        if (cache != null) {
            invoker = (ClientInvoker) cache.get(SoapConsts.CLIENT_INVOKER);
        } else {
            invoker = (ClientInvoker) getThreadCache().get(SoapConsts.CLIENT_INVOKER);
        }
        if (invoker == null) {
            return null;
        }
        return invoker.getMethod().getAnnotation(Soap12XmlMethod.class);
    }

    /**
     * Method that JAX-RS container calls to serialize given value.
     */
    @Override
    public void writeTo(Object value, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        XmlMapper mapper = locateMapper(type, mediaType);
        // mapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
        // 基本body
        JsonNode body = mapper.valueToTree(value);
        // 写出
        ObjectNode root = ((ObjectNode)body).objectNode();
        JacksonXmlRootElement re = type.getAnnotation(JacksonXmlRootElement.class);
        root.set(re.localName(), body);

        Soap12XmlMethod wma = type.getAnnotation(Soap12XmlMethod.class);
        if (wma == null) {
            wma = findSoap12XmlMethod();
        }
        if (wma == null) {
            throw new RuntimeException("no found 'Soap12XmlMethod' Annotation");
        }
        // 配置
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        JsonEncoding enc = findEncoding(mediaType, httpHeaders);
        JsonGenerator g = mapper.getFactory().createGenerator(tmp, enc);
        g.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        // 执行
        // mapper.writeTree(g, root);
        writeTree(mapper, g, root, wma.operationName(), wma.namespace());
        
        entityStream.write(prefix.getBytes());
        entityStream.write(tmp.toByteArray());
        entityStream.write(suffix.getBytes());
        
        if (debug) {
            logger.info("write soap12: " + prefix + tmp.toString() + suffix);
        }
    }

    /**
     *  
     * @param mapper
     * @param jgen
     * @param rootNode
     * @param rootname
     * @param namespace
     * @throws IOException
     * @throws JsonProcessingException
     */
    public void writeTree(XmlMapper mapper, JsonGenerator jgen, JsonNode rootNode, 
            String ln, String ns) throws IOException, JsonProcessingException {
        SerializationConfig config0 = mapper.getSerializationConfig();
         SerializationConfig config = config0.withRootName(PropertyName.construct(ln, ns));
        DefaultSerializerProvider provider0 = (DefaultSerializerProvider) mapper.getSerializerProvider();
        DefaultSerializerProvider provider = provider0.createInstance(config, mapper.getSerializerFactory());
        provider.serializeValue(jgen, rootNode);
        if (config.isEnabled(SerializationFeature.FLUSH_AFTER_WRITE_VALUE)) {
            jgen.flush();
        }
    }
}
