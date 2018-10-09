package com.suisrc.jaxrsapi.client.api;

/**
 * {"GoodsList":[{"amount":"79.00","num":"1","skuId":"xxxxx"}],"PayWay":[{"amount":"79.00","payCode":"01"}],"dpdm":"xxxx","saleMoney":"79.00","saleno":"186","saleno_old":"-1","saletime":"2016-08-29 17:51:50","shopCode":"xxxx","sktno_old":"xxxx"}
 * @author Y13
 *
 */
public class ZYHBody {
    
    private String shopCode;

    public String getShopCode() {
        return shopCode;
    }

    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }
    

}
