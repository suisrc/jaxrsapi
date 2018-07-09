package com.suisrc.jaxrsapi.core.simple;

import com.suisrc.jaxrsapi.core.token.Token;

/**
 * 获取访问令牌接口
 * 
 * @author Y13
 *
 */
public interface TokenProduce {

    /**
     * 获取访问令牌
     * @param appId 
     * @param appSecret 
     * @return
     */
    Token getToken(String appId, String appSecret);

}
