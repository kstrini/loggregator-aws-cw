package io.pivotal.logreggator.awscw.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import io.pivotal.logreggator.awscw.config.AmazonConfig;
import io.pivotal.logreggator.awscw.model.ContainerMetric;
import io.pivotal.logreggator.awscw.model.Metric;
import io.pivotal.logreggator.awscw.model.ValueMetric;
import io.pivotal.logreggator.awscw.units.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class CloudWatchService
{
    private static final Logger log = LoggerFactory.getLogger(CloudWatchService.class);
    private final boolean enabled;
    private final Duration updateTime;
    private final AmazonCloudWatch cloudWatch;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> future;

    private Metric mToWrite;

    public CloudWatchService(AmazonConfig config, AmazonCloudWatch cloudWatch, ScheduledExecutorService executorService, Metric mToWrite)
    {
        this.enabled = config.isAlertingEnabled();
        this.updateTime = config.getCloudWatchUpdateTime();
        this.cloudWatch = cloudWatch;
        this.executorService = executorService;
        this.mToWrite = mToWrite;
    }
    public CloudWatchService()
    {
        this.enabled = false;
        this.updateTime = null;
        this.cloudWatch = null;
        this.executorService = null;
        this.mToWrite = null;
    }

    public synchronized void start()
    {
        if (future == null)
        {
            future = executorService.scheduleAtFixedRate(new Runnable()
            {
                @Override
                public void run()
                {
                    if(mToWrite == null){System.out.println("Found a Null Metric Object");}
                    else
                    {
                        try {updateCloudWatch();}
                        catch (Exception e) {log.error("CloudWatch update failed: " + e.getMessage());}
                    }
                }
            }, (long) updateTime.toMillis(), (long) updateTime.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void stop()
    {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
    }

    public void updateCloudWatch()
    {
        if (!enabled) {
            log.info("Skipping CloudWatch update (disabled by configuration)");
            return;
        }

        MetricDatum datum = new MetricDatum();
        datum.withDimensions(
                new Dimension().withName("Origin").withValue(mToWrite.getOrigin()),
                new Dimension().withName("EventType").withValue(mToWrite.getEventType()),
                new Dimension().withName("TimeStamp").withValue(mToWrite.getTimeStamp())
        );
        if(mToWrite.getEventType().contains("ValueMetric"))
        {
            ValueMetric vMetric = (ValueMetric)mToWrite;
            System.out.println("Attempting to write ValueMetric: " + vMetric.toString());
            datum.withMetricName(vMetric.getName());
            datum.withValue(Double.valueOf(vMetric.getValue()));
            // Map CF Metric Units to AWS CW Units
            String unit = vMetric.getUnit();
            switch (unit)
            {
                case "ms":
                    datum.withUnit(StandardUnit.Milliseconds);
                    break;
                case "count":
                    datum.withUnit(StandardUnit.Count);
                    break;
                case "MiB":
                    datum.withUnit(StandardUnit.Megabytes);
                    break;
                case "Metric":
                    datum.withUnit(StandardUnit.Count);
                    break;
                case "drains":
                    datum.withUnit(StandardUnit.Count);
                    break;
                default:
                    datum.withUnit(StandardUnit.None);
             }
        }
        else if(mToWrite.getEventType().contains("ContainerMetric"))
        {
            ContainerMetric cMetric = (ContainerMetric)mToWrite;
            System.out.println("Attempting to write ContainerMetric: " + cMetric.toString());
            datum.withMetricName("ContainerMetric");
            datum.withUnit(StandardUnit.None);
            datum.withValue(1.0d);
            List<Dimension> addMoreDimensions = datum.getDimensions();
            addMoreDimensions.add(new Dimension().withName("applicationId").withValue(cMetric.getApplicationId()));
            addMoreDimensions.add(new Dimension().withName("instanceIndex").withValue(cMetric.getInstanceIndex().toString()));
            addMoreDimensions.add(new Dimension().withName("cpuPercentage").withValue(cMetric.getCpuPercentage().toString()));
            addMoreDimensions.add(new Dimension().withName("memoryBytes").withValue(cMetric.getMemoryBytes().toString()));
            addMoreDimensions.add(new Dimension().withName("diskBytes").withValue(cMetric.getDiskBytes().toString()));
            datum.setDimensions(addMoreDimensions);
        }

        cloudWatch.putMetricData(new PutMetricDataRequest()
                .withNamespace("CloudFoundry")
                .withMetricData(datum)
        );
    }
}