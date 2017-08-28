package com.suisrc.common.exception;

/**
 * 暂时没有支持，禁止访问或者使用
 * 
 * @author Y13
 *
 */
public class NoSupportException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoSupportException(String message) {
        super(message);
    }

}
