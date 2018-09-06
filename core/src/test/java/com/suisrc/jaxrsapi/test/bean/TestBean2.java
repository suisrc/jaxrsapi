package com.suisrc.jaxrsapi.test.bean;

import com.suisrc.jaxrsapi.core.annotation.Reviser;
import com.suisrc.jaxrsapi.core.annotation.Value;

/**
 * 测试接口
 * @author Y13
 *
 */
public class TestBean2 {
    
    @Reviser(TReviseHandler.class)
    @Value("TestBean2")
    private String name1;

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }


}
