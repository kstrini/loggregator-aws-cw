package io.pivotal.logreggator.awscw.model;

public class ValueMetric implements Metric
{
    // Metric Inheritance
    String origin;
    String eventType;
    String timestamp;

    String name;
    String value;
    String unit;

    @Override
    public void setOrigin(String origin) {this.origin=origin;}
    @Override
    public String getOrigin() {return this.origin;}

    @Override
    public void setEventType(String eventType) {this.eventType=eventType;}
    @Override
    public String getEventType() {return this.eventType;}

    @Override
    public void setTimestamp(String timestamp) {this.timestamp=timestamp;}
    @Override
    public String getTimeStamp() {return this.timestamp;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getValue() {return value;}
    public void setValue(String value) {this.value = value;}

    public String getUnit() {return unit;}
    public void setUnit(String unit) {this.unit = unit;}

    @Override
    public String toString() {
        return "ValueMetric{" +
                "origin='" + origin + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
