package com.suisrc.jaxrsapi.test;

import com.suisrc.jaxrsapi.core.factory.NSCF;
import com.suisrc.jaxrsapi.test.bean.TestServerActivator;

/**
 * 生成接口的实现内容
 * 
 * @author Y13
 *
 */
class ModuleBuildSources {
    
    /**
     * 构建调用代码
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
//        NSCF.buildSources(null, TestServerActivator.class);

        String path = ClassLoader.getSystemResource("").getPath();
        path = path.substring(1, path.length() - "target/test-classes/".length()) + "src/test/java";
        NSCF.buildFile(path, null, false, TestServerActivator.class);
        System.out.println("Build Completed!");
    }

}
