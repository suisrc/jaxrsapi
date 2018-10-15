package com.suisrc.jaxrsapi.soap.provider;

import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLStreamWriter2;

import com.ctc.wstx.api.WriterConfig;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.ctc.wstx.sw.SimpleNsStreamWriter;
import com.ctc.wstx.sw.XmlWriter;

/**
 * 
 * 对soap特殊的格式化内容支持
 * 
 * @author Y13
 *
 */
public class WstxOutputFactory2 extends WstxOutputFactory {

    @Override
    protected XMLStreamWriter2 createSW(String enc, WriterConfig cfg, XmlWriter xw) {
        if (cfg.willSupportNamespaces()) {
            return new RepairingNsStreamWriter2(xw, enc, cfg);
        }
        return super.createSW(enc, cfg, xw);
    }
    
    /**
     * 
     */
    private static class RepairingNsStreamWriter2 extends SimpleNsStreamWriter {

        /**
         * prefix.
         */
        protected HashMap<String, String> mSuggestedPrefixes = null;

        /**
         * 
         * @param xw
         * @param enc
         * @param cfg
         */
        public RepairingNsStreamWriter2(XmlWriter xw, String enc, WriterConfig cfg) {
            super(xw, enc, cfg);
        }

        /**
         * 
         */
        public void doSetPrefix(String prefix, String uri) throws XMLStreamException {
            if (uri == null || uri.length() == 0) {
                if (mSuggestedPrefixes != null) {
                    mSuggestedPrefixes.entrySet().removeIf(e -> e.getValue().equals(prefix));
                }
            } else {
                if (mSuggestedPrefixes == null) {
                    mSuggestedPrefixes = new HashMap<>(4);
                }
                mSuggestedPrefixes.put(uri, prefix);
            }
        }


        /**
         * 
         */
        protected void writeStartOrEmpty(String localName, String nsURI) throws XMLStreamException {
            String prefix = mCurrElem.getPrefix();
            if (prefix == null) {
                prefix = mSuggestedPrefixes == null || nsURI == null || nsURI.isEmpty() ? null : mSuggestedPrefixes.get(nsURI);
                if (prefix != null) {
                    mCurrElem.addPrefix(prefix, nsURI);
                }
            } else if (!prefix.isEmpty()) {
                mCurrElem.setDefaultNsUri(nsURI);
            }
            super.writeStartOrEmpty(localName, nsURI);
            if (!mCurrElem.getPrefix().isEmpty()) {
                // writeNamespace(mCurrElem.getPrefix(), nsURI);
                doWriteNamespace(prefix, nsURI);
            }
        }

    }

}
