package com.suisrc.jaxrsapi.test.bean;

import com.suisrc.jaxrsapi.core.runtime.ReviseHandler;

public class TReviseHandler implements ReviseHandler<String> {

    @Override
    public String accept(String value) {
        return value;
    }

}
