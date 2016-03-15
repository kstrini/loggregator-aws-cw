package io.pivotal.logreggator.awscw.model;

public class Metric
{
    String origin;
    String eventType;
    String timestamp;
    ValueMetric valueMetric;

    public class ValueMetric
    {
        String name;
        Double value;
        String unit;

        public String getName() {
            return name;
        }
        public void setName(String name) {this.name = name;}

        public Double getValue() {return value;}
        public void setValue(Double value) {this.value = value;}

        public String getUnit() {
            return unit;
        }
        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

    public String getOrigin() {
        return origin;
    }
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getEventType() {
        return eventType;
    }
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ValueMetric getValueMetric() {return valueMetric;}
    public void setValueMetric(ValueMetric valueMetric) {this.valueMetric = valueMetric;}
}
