package com.suisrc.jaxrsapi.test;

import com.suisrc.common.jdejst.JdeJst;
import com.suisrc.jaxrsapi.core.factory.NSCF;
import com.suisrc.jaxrsapi.test.bean.TestServerActivator;

/**
 * 测试入口
 * @author ICG-DL-Y13
 *
 */
public class TMain {
    
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        System.setProperty(JdeJst.class.getName() + ".show_src", "true");
//        NSCF.build("D:/test/target/", TestServerActivator.class);
        NSCF.build("D:/test/target/", TestServerActivator.class);
        System.out.println("OK");
    }

}
