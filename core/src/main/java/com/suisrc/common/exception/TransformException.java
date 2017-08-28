package com.suisrc.common.exception;

/**
 * 类型转换异常
 * 
 * @author Y13
 *
 */
public class TransformException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public static TransformException create(Throwable t) {
        return new TransformException(t);
    }

    private TransformException(Throwable t) {
        super(t);
    }

}
