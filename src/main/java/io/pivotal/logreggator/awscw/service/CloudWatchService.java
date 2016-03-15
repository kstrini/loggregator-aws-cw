package io.pivotal.logreggator.awscw.service;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import io.pivotal.logreggator.awscw.config.AmazonConfig;
import io.pivotal.logreggator.awscw.model.Metric;
import io.pivotal.logreggator.awscw.units.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public CloudWatchService(AmazonConfig config, AmazonCloudWatch cloudWatch, @MonitorExecutorService ScheduledExecutorService executorService, Metric mToWrite)
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
        System.out.println("*****Putting metric data in");
        if (future == null) {
            future = executorService.scheduleAtFixedRate(new Runnable()
            {
                @Override
                public void run()
                {
                    try {
                        updateCloudWatch();
                        System.out.println("*****Finished metric data put *****");
                    }
                    catch (Exception e) {
                        log.error("CloudWatch update failed: " + e.getMessage());
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

        MetricDatum datum = new MetricDatum()
                .withMetricName(mToWrite.getValueMetric().getName())
                .withUnit(mToWrite.getValueMetric().getUnit())
                .withValue(mToWrite.getValueMetric().getValue())
                .withDimensions(
                        new Dimension().withName("Origin").withValue(mToWrite.getOrigin()),
                        new Dimension().withName("EventType").withValue(mToWrite.getEventType()),
                        new Dimension().withName("TimeStamp").withValue(mToWrite.getTimestamp())
                );


        cloudWatch.putMetricData(new PutMetricDataRequest()
                .withNamespace("CloudFoundry")
                .withMetricData(datum)
        );
    }
}