package com.suisrc.jaxrsapi.core.token;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p> Token 执行过程中访问凭证暂存 {"access_token":"ACCESS_TOKEN","expires_in":7200}
 * 
 * @author Y13
 *
 */
@JsonInclude(Include.NON_NULL)
public class Token implements Serializable {
  private static final long serialVersionUID = 8709719312919900103L;

  /**
   * <p> 获取到的凭证
   */
  @JsonProperty("access_token")
  private String accessToken;

  /**
   * <p> 凭证有效时间，单位：秒
   */
  @JsonProperty("expires_in")
  private long expiresIn = -1;

  /**
   * <p> 凭证创建时间, 单位：毫秒
   */
  @JsonIgnore
  private long createTime = System.currentTimeMillis();
  
  /**
   * <p> 令牌名称
   */
  @JsonIgnore
  private String nameKey;


  public Token() {}

  public Token(String accessToken, long expiresIn) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn;
  }

  public Token(String accessToken, long expiresIn, long createTime) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn;
    this.createTime = createTime;
  }

  public String getAccessToken() {
    return this.accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public long getExpiresIn() {
    return this.expiresIn;
  }

  public void setExpiresIn(long expiresIn) {
    this.expiresIn = expiresIn;
  }
  
  public String getNameKey() {
    return nameKey;
  }

  public void setNameKey(String nameKey) {
    this.nameKey = nameKey;
  }

  public long getCreateTime() {
    return createTime;
  }
  
  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  }

  /**
   * <p> 通过时间判定令牌是否有效
   * 
   * @param advanceIn 用于判断将要过期的提前时间端，单位是秒, 可以为空，为空不进行判断
   * @return
   */
  public TokenStatus getStatus(Long advanceIn) {
    if (accessToken == null) {
      // 凭证无效
      return TokenStatus.NONE;
    }
    // 取得时间间隔
    long interval = System.currentTimeMillis() - createTime;
    // 转换为秒,获取差距间隔时间
    interval = expiresIn - interval / 1000;
    if (interval <= 0) {
      // 过期
      return TokenStatus.EXPIRED;
    }
    if (advanceIn != null && interval <= advanceIn) {
      // 凭据将要过期
      return TokenStatus.WILL_EXPIRE;
    }
    return TokenStatus.VALID; // 凭据有效
  }

  /**
   * <p> 数据拷贝
   * 
   * @param token
   */
  public void copy(Token token) {
    this.accessToken = token.accessToken;
    this.expiresIn = token.expiresIn;
    this.createTime = token.createTime;
    this.nameKey = token.nameKey;
  }

}
