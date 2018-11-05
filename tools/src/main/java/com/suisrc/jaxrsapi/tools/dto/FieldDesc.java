package com.suisrc.jaxrsapi.tools.dto;

/**
 * 属性描述说明
 * 
 * @author Y13
 *
 */
public class FieldDesc {
    
    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数是否必要
     */
    private boolean isMust = false;
    
    /**
     * 参数描述
     */
    private String description;

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public boolean isMust() {
        return isMust;
    }

    public void setMust(boolean isMust) {
        this.isMust = isMust;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        StringBuilder sbir = new StringBuilder();
        sbir.append("{");
        sbir.append("name").append(":").append(name).append(',');
        sbir.append("isMust").append(":").append(isMust).append(',');
        sbir.append("description").append(":").append(description);
        sbir.append("}");
        return sbir.toString();
    }
    
    public String toPrintString(String prefix, boolean ism) {
        StringBuilder sbir = new StringBuilder(prefix);
        if (name.equals("参数")) {
            sbir.append(name + ".");
            for (int i = name.length(); i < 18; i++) {
                sbir.append(' ');
            }
            if (ism) {
                sbir.append("必须.");
                for (int i = 0; i < 7; i++) {
                    sbir.append(' ');
                }
            }
        } else {
            sbir.append(name);
            for (int i = name.length(); i < 20; i++) {
                sbir.append(' ');
            }
            sbir.append(' ');
            if (ism) {
                sbir.append(isMust ? "是." : "否.");
                for (int i = 0; i < 8; i++) {
                    sbir.append(' ');
                }
            }
        }
        sbir.append(description);
        return sbir.toString();
    }
}
