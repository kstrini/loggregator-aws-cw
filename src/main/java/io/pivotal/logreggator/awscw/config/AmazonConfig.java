package io.pivotal.logreggator.awscw.config;

import java.util.concurrent.TimeUnit;
import io.pivotal.logreggator.awscw.units.Duration;

public class AmazonConfig
{
    private boolean alertingEnabled = true;
    private Duration cloudWatchUpdateTime = new Duration(10, TimeUnit.SECONDS);
    private String fromAddress;
    private String toAddress;
    private String awsAccessKey;
    private String awsSecretKey;

    public Duration getCloudWatchUpdateTime()
    {
        return cloudWatchUpdateTime;
    }

    public AmazonConfig setCloudWatchUpdateTime(Duration cloudWatchUpdateTime)
    {
        this.cloudWatchUpdateTime = cloudWatchUpdateTime;
        return this;
    }

    public String getFromAddress()
    {
        return fromAddress;
    }
    public AmazonConfig setFromAddress(String fromAddress)
    {
        this.fromAddress = fromAddress;
        return this;
    }

    public String getToAddress()
    {
        return toAddress;
    }
    public AmazonConfig setToAddress(String toAddress)
    {
        this.toAddress = toAddress;
        return this;
    }

    public String getAwsAccessKey()
    {
        return awsAccessKey;
    }
    public AmazonConfig setAwsAccessKey(String awsAccessKey)
    {
        this.awsAccessKey = awsAccessKey;
        return this;
    }

    public String getAwsSecretKey()
    {
        return awsSecretKey;
    }
    public AmazonConfig setAwsSecretKey(String awsSecretKey)
    {
        this.awsSecretKey = awsSecretKey;
        return this;
    }

    public boolean isAlertingEnabled()
    {
        return alertingEnabled;
    }

    public AmazonConfig setAlertingEnabled(boolean alertingEnabled)
    {
        this.alertingEnabled = alertingEnabled;
        return this;
    }

    public boolean isValid()
    {
        return !alertingEnabled || (fromAddress != null && toAddress != null && awsAccessKey != null && awsSecretKey != null);
    }
}
