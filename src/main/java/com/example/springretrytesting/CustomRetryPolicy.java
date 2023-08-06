package com.example.springretrytesting;

import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryPolicy;

@FunctionalInterface
interface RetryCondition<T> {
    boolean evaluate(T result, String errorMessage);
}

public class CustomRetryPolicy implements RetryPolicy {

    private RetryCondition retryCondition;
    private int maxAttempts;

    public CustomRetryPolicy(int maxAttempts,RetryCondition retryCondition) {
        this.retryCondition = retryCondition;
        this.maxAttempts  = maxAttempts;
    }

    @Override
    public boolean canRetry(RetryContext context) {
        CustomRetryContext customRetryContext = (CustomRetryContext) context;
        System.out.println("Retry Can retry start");
        System.out.println("*************************" + context);
        System.out.println("*************************" + customRetryContext.getRetryCount() + "/" + maxAttempts + "/" + customRetryContext.shouldRetry() + "/" + customRetryContext.getStatusCode() + "/" + customRetryContext.getErrorMessage());
        System.out.println("*********************************************DATA");
        System.out.println(customRetryContext.getRetryCount() <= maxAttempts);
        System.out.println(customRetryContext.shouldRetry());
        System.out.println(customRetryContext.getStatusCode());
        System.out.println(customRetryContext.getErrorMessage());
        System.out.println("evlauate" + retryCondition.evaluate(customRetryContext.getStatusCode(), customRetryContext.getErrorMessage()));
        System.out.println("*********************************************DATA");
        System.out.println("Retry Can retry start end");
        return customRetryContext.getRetryCount() <= maxAttempts && customRetryContext.shouldRetry()/*  && retryCondition.evaluate(customRetryContext.getStatusCode(), customRetryContext.getErrorMessage()) */;
    }

    @Override
    public RetryContext open(RetryContext context) {
        return new CustomRetryContext(context);
    }

    @Override
    public void close(RetryContext context) {

    }

    @Override
    public void registerThrowable(RetryContext context, Throwable throwable) {

    }
}
