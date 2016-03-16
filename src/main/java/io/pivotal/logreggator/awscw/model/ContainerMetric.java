package io.pivotal.logreggator.awscw.model;

public class ContainerMetric implements Metric
{
    // Metric Inheritance
    String origin;
    String eventType;
    String timestamp;

    String applicationId;
    Integer instanceIndex;
    Double cpuPercentage;
    Long memoryBytes;
    Long diskBytes;

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

    public String getApplicationId() {return applicationId;}
    public void setApplicationId(String applicationId) {this.applicationId = applicationId;}

    public Integer getInstanceIndex() {return instanceIndex;}
    public void setInstanceIndex(Integer instanceIndex) {this.instanceIndex = instanceIndex;}

    public Double getCpuPercentage() {return cpuPercentage;}
    public void setCpuPercentage(Double cpuPercentage) {this.cpuPercentage = cpuPercentage;}

    public Long getMemoryBytes() {return memoryBytes;}
    public void setMemoryBytes(Long memoryBytes) {this.memoryBytes = memoryBytes;}

    public Long getDiskBytes() {return diskBytes;}
    public void setDiskBytes(Long diskBytes) {this.diskBytes = diskBytes;}

    @Override
    public String toString() {
        return "ContainerMetric{" +
                "origin='" + origin + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", instanceIndex=" + instanceIndex +
                ", cpuPercentage=" + cpuPercentage +
                ", memoryBytes=" + memoryBytes +
                ", diskBytes=" + diskBytes +
                '}';
    }
}
