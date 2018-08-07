package com.suisrc.jaxrsapi.test.bean;

import javax.ws.rs.DefaultValue;

import com.suisrc.jaxrsapi.core.annotation.NotNull;
import com.suisrc.jaxrsapi.core.annotation.Reviser;
import com.suisrc.jaxrsapi.core.annotation.TfDefaultValue;
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

    @NotNull("年龄为空")
    @Value("T:t2.toString")
    private String age;

    @Value("t3")
    @TfDefaultValue(T4Str.class)
    @DefaultValue("t3")
    private String other;

    @Value("t4")
    @DefaultValue("t4")
    private String other2;

    public String getOther2() {
        return other2;
    }

    public void setOther2(String other2) {
        this.other2 = other2;
    }

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
