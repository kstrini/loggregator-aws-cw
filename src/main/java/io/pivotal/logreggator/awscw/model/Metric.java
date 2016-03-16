package io.pivotal.logreggator.awscw.model;

public interface Metric
{
    void setOrigin(String origin);
    void setEventType(String eventType);
    void setTimestamp(String timestamp);
    String getOrigin();
    String getEventType();
    String getTimeStamp();
}
