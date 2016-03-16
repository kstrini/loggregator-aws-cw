package io.pivotal.logreggator.awscw.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.pivotal.logreggator.awscw.config.AmazonConfig;
import io.pivotal.logreggator.awscw.model.ContainerMetric;
import io.pivotal.logreggator.awscw.model.Metric;
import io.pivotal.logreggator.awscw.model.ValueMetric;
import io.pivotal.logreggator.awscw.units.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import cf.dropsonde.firehose.Firehose;
import cf.dropsonde.firehose.FirehoseBuilder;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import reactor.core.publisher.Mono;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DopplerService
{
    @Value("${doppler.URI}")
    String dopplerURI;
    @Value("${cf.host}")
    String host;

    @Value("${doppler.User}")
    String username;
    @Value("${doppler.Password}")
    String password;

    @Value("${aws.access-key}")
    String awsKey;
    @Value("${aws.secret-key}")
    String awsSecKey;

    public void tapNozzle()
    {
        Mono<String> myToken = getAuth();
        Firehose firehose = FirehoseBuilder.create(dopplerURI, myToken.get())
            .skipTlsValidation(true)
            .build();

        firehose
            .open().filter(e->e.eventType.getValue()==6||e.eventType.getValue()==9)
            .toBlocking()
            .forEach(e->pointNozzle(e));
    }

    public void pointNozzle(events.Envelope e)
    {
        System.out.println(e);
        //CloudWatch
        String awsAccessKey = awsKey;
        String awsSecretKey = awsSecKey;

        AmazonConfig awsConfig = new AmazonConfig()
		.setCloudWatchUpdateTime(new Duration(5, TimeUnit.SECONDS))
		.setAwsAccessKey(awsAccessKey)
		.setAwsSecretKey(awsSecretKey)
		.setAlertingEnabled(true);

        Metric metric=null;
        AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
        try
        {
            String cleanString = e.toString().replaceAll("Envelope", "\n").trim().replaceAll("[{}]", "\n").trim();
            String propertiesFormat = cleanString.replaceAll(",", "\n").trim();
            Properties properties = new Properties();
            properties.load(new StringReader(propertiesFormat));
            Map<String, String> map = new HashMap(properties);

            for (String key : map.keySet()) {System.out.println("KEY: " + key + " " + "VALUE: " + map.get(key));}
            if (map.get("eventType").contains("ValueMetric")) {
                metric = new ValueMetric();
                metric.setEventType(map.get("eventType"));
                metric.setTimestamp(map.get("timestamp"));
                metric.setOrigin(map.get("origin"));
                ((ValueMetric) metric).setValue(map.get("value"));
                ((ValueMetric) metric).setUnit(map.get("unit"));
                ((ValueMetric) metric).setName(map.get("name"));
                System.out.println(((ValueMetric) metric).toString());
                new CloudWatchService(awsConfig, new AmazonCloudWatchClient(awsCredentials), executor, metric).start();
            }
            else if (map.get("eventType").contains("ContainerMetric"))
            {
                metric = new ContainerMetric();
                metric.setEventType(map.get("eventType"));
                metric.setTimestamp(map.get("timestamp"));
                metric.setOrigin(map.get("origin"));
                ((ContainerMetric) metric).setApplicationId(map.get("applicationId"));
                ((ContainerMetric) metric).setInstanceIndex(Integer.valueOf(map.get("instanceIndex")));
                ((ContainerMetric) metric).setCpuPercentage(Double.valueOf(map.get("cpuPercentage")));
                ((ContainerMetric) metric).setMemoryBytes(Long.valueOf(map.get("memoryBytes")));
                ((ContainerMetric) metric).setDiskBytes(Long.valueOf(map.get("diskBytes")));
                System.out.println(((ContainerMetric) metric).toString());
                new CloudWatchService(awsConfig, new AmazonCloudWatchClient(awsCredentials), executor, metric).start();
            }
            else
            {
                for (String key : map.keySet()) {System.out.println("KEY: " + key + " " + "VALUE: " + map.get(key));}
            }
        }
        catch(Exception io){io.printStackTrace();}
    }

    private Mono<String> getAuth()
    {
        CloudFoundryClient cloudFoundryClient=SpringCloudFoundryClient.builder()
            .host(this.host)
            .username(this.username)
            .password(this.password)
            .skipSslValidation(true)
            .build();
        return cloudFoundryClient.getAccessToken();
    }
}