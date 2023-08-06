package com.example.springretrytesting;
import org.springframework.retry.context.RetryContextSupport;
import org.springframework.retry.RetryContext;

public class CustomRetryContext extends RetryContextSupport {

    private boolean shouldRetry = true;
    private int statusCode;
    private String errorMessage;
    private Exception lastException;


    private int retryCount = 0;

    public CustomRetryContext(RetryContext parent) {
        super(parent);
    }

    public void setShouldRetry(boolean shouldRetry) {
        this.shouldRetry = shouldRetry;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean shouldRetry() {
        return shouldRetry;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public Exception getLastException() {
        return lastException;
    }

    public void setLastException(Exception lastException) {
        this.lastException = lastException;
    }
}