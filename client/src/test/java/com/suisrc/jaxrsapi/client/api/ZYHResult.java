package com.suisrc.jaxrsapi.client.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {"Count":null,"Retcode":"00","Retmsg":"保存成功"}
 * 
 * @author Y13
 *
 */
public class ZYHResult {

  @JsonProperty("errcode")
  private String errcode;

  @JsonProperty("errmsg")
  private String errmsg;

  @JsonProperty("Count")
  private String count;

  @JsonProperty("Retcode")
  private String retcode;

  @JsonProperty("Retmsg")
  private String retmsg;

  public String getCount() {
    return count;
  }

  public void setCount(String count) {
    this.count = count;
  }

  public String getRetcode() {
    return retcode;
  }

  public void setRetcode(String retcode) {
    this.retcode = retcode;
  }

  public String getRetmsg() {
    return retmsg;
  }

  public void setRetmsg(String retmsg) {
    this.retmsg = retmsg;
  }

}
