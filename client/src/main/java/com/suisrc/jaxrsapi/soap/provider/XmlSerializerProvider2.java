package com.suisrc.jaxrsapi.soap.provider;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.XmlSerializerProvider;
import com.fasterxml.jackson.dataformat.xml.util.StaxUtil;

/**
 * 
 * @author Y13
 *
 */
public class XmlSerializerProvider2 extends XmlSerializerProvider {
    private static final long serialVersionUID = 1L;

    public static XmlSerializerProvider2 createInstance0(SerializerProvider src, SerializationConfig config,
            SerializerFactory jsf) {
        return new XmlSerializerProvider2((XmlSerializerProvider) src, config, jsf);
    }

    private String rootNamePrefix = "";
    
    public XmlSerializerProvider2(XmlSerializerProvider src, SerializationConfig config, SerializerFactory f) {
        super(src, config, f);
    }

    @Override
    public DefaultSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
        return new XmlSerializerProvider(this, config, jsf);
    }

    public String getRootNamePrefix() {
        return rootNamePrefix;
    }

    public void setRootNamePrefix(String rootNamePrefix) {
        this.rootNamePrefix = rootNamePrefix;
    }

    protected QName _rootNameFromConfig() {
        PropertyName name = _config.getFullRootName();
        if (name == null) {
            return null;
        }
        String ns = name.getNamespace();
        if (ns == null || ns.isEmpty()) {
        }
        return new QName(ns, name.getSimpleName(), getRootNamePrefix());
    }

    protected void _initWithRootName(ToXmlGenerator xgen, QName rootName) throws IOException {
        /*
         * 28-Nov-2012, tatu: We should only initialize the root name if no name has been set, as
         * per [Issue#42], to allow for custom serializers to work.
         */
        if (!xgen.setNextNameIfMissing(rootName)) {
            // however, if we are root, we... insist
            if (xgen.getOutputContext().inRoot()) {
                xgen.setNextName(rootName);
            }
        }
        xgen.initGenerator();
        String ns = rootName.getNamespaceURI();
        /*
         * 解决ns名称异常情况
         */
        if (ns != null && ns.length() > 0) {
            try {
                String np = rootName.getPrefix();
                if (np != null && !np.isEmpty()) {
                    xgen.getStaxWriter().setPrefix(np, ns);
                } else {
                    xgen.getStaxWriter().setDefaultNamespace(ns);
                }
            } catch (XMLStreamException e) {
                StaxUtil.throwXmlAsIOException(e);
            }
        }
    }
}
