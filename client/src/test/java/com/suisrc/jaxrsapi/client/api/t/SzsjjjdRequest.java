package com.suisrc.jaxrsapi.client.api.t;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "astr_request")
public class SzsjjjdRequest {
    
    private String licensekey;
    
    public String getLicensekey() {
        return licensekey;
    }

    public void setLicensekey(String licensekey) {
        this.licensekey = licensekey;
    }

}
