package com.suisrc.jaxrsapi.test.bean;

import javax.ws.rs.DefaultValue;

import com.suisrc.jaxrsapi.core.annotation.Reviser;
import com.suisrc.jaxrsapi.core.annotation.TfDefaultValue;
import com.suisrc.jaxrsapi.core.annotation.ThreadValue;
import com.suisrc.jaxrsapi.core.annotation.Value;

/**
 * 测试接口
 * @author Y13
 *
 */
public class TestBean {
    
    @Reviser(TReviseHandler.class)
    @Value("t1")
    private String name;

    @ThreadValue("t2")
    private String age;

    @Value("t3")
    @TfDefaultValue(T4Str.class)
    @DefaultValue("t3")
    private String other;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
    
    

}
