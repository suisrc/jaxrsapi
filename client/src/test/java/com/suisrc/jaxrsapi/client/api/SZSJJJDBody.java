package com.suisrc.jaxrsapi.client.api;

import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;

@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class SZSJJJDBody {
    
    private String licensekey;
    
    
    public String getLicensekey() {
        return licensekey;
    }

    public void setLicensekey(String licensekey) {
        this.licensekey = licensekey;
    }

}
