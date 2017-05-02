package com.hpe.caf.auditing.elastic;

public class RetryElasticsearchOperation
{
    public static final int DEFAULT_RETRIES = 5;
    public static final long DEFAULT_WAIT_TIME_MS = 1000;

    private int numberOfRetries;
    private int numberOfTriesLeft;
    private long timeToWait;

    public RetryElasticsearchOperation()
    {
        this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_MS);
    }

    public RetryElasticsearchOperation(int numberOfRetries, long timeToWait)
    {
        this.numberOfRetries = numberOfRetries;
        numberOfTriesLeft = numberOfRetries;
        this.timeToWait = timeToWait;
    }

    public boolean shouldRetry()
    {
        return numberOfTriesLeft > 0;
    }

    public void retryNeeded() throws Exception
    {
        numberOfTriesLeft--;
        if (!shouldRetry()) {
            throw new Exception("Retry Failed: Total " + numberOfRetries
                    + " attempts made at interval " + getTimeToWait()
                    + "ms");
        }
        waitUntilNextTry();
    }

    public long getTimeToWait()
    {
        return timeToWait;
    }

    private void waitUntilNextTry()
    {
        try {
            Thread.sleep(getTimeToWait());
        } catch (InterruptedException ignored) {
        }
    }
}