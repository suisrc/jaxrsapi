package com.suisrc.jaxrsapi.core.token;

/**
 * 凭据状态
 * 
 * @author Y13
 *
 */
public enum TokenStatus {

  NONE("凭证无效"), VALID("凭证有效"), WILL_EXPIRE("凭证将要过期"), EXPIRED("凭证已经过期");

  public final String message;

  private TokenStatus(String message) {
    this.message = message;
  }
}
