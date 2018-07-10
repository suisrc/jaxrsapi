package com.suisrc.jaxrsapi.test.bean;

import com.suisrc.jaxrsapi.core.ApiActivator;
import com.suisrc.jaxrsapi.core.runtime.RetryPredicate;

public class RetryPredicateImpl implements RetryPredicate<String> {

    public RetryPredicateImpl() {
    }
    
    public RetryPredicateImpl(ApiActivator activator) {
    }

    @Override
    public boolean test(int count, int time, String result, Exception e) {
        return false;
    }

}
