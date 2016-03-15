package io.pivotal.logreggator.awscw.service;

import cf.dropsonde.firehose.Firehose;
import cf.dropsonde.firehose.FirehoseBuilder;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.pivotal.logreggator.awscw.config.AmazonConfig;
import io.pivotal.logreggator.awscw.model.Metric;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class DopplerService
{
//    @Value("${dopplerURI}")
    String dopplerURI="";

//    @Value("${cf.host}")
    String host="";

//    @Value("${cf.username}")
    String username="";

//    @Value("${cf.password}")
    String password="";

//    @Value("${cf.organization}")
    String organization="";

//    @Value("${cf.space}")
    String space="";

//    @Value("${aws.access-key}")
    String awsKey="";

//    @Value("${aws.secret-key}")
    String awsSecKey="";

    private CloudFoundryClient cloudFoundryClient;

    public void tapNozzle()
    {
        Mono<String> myToken = getAuth();
        Firehose firehose = FirehoseBuilder.create(dopplerURI, myToken.get())
                .skipTlsValidation(true)
                .build();

        firehose
                .open().filter(e->e.eventType.getValue()==6)
                .toBlocking()
//                .forEach(e->pointNozzle(e));
                .forEach(e->System.out.println(e));
    }

    public void pointNozzle(events.Envelope e)
    {
        System.out.println(e);
        System.out.println("*****Calling CloudWatch*****");
        //CloudWatch
        String awsAccessKey = awsKey;
        String awsSecretKey = awsSecKey;

        // Test Metric
        Metric metric = new Metric();
        metric.setOrigin("my-linux");
        metric.setEventType("Metric");
        metric.setTimestamp("1457625691773037608");
        Metric.ValueMetric m = metric.getValueMetric();
        m.setName("memoryStats.numBytesAllocatedHeap");
        m.setValue(7993912.0);
        m.setUnit("count");
        metric.setValueMetric(m);

        AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
        new CloudWatchService(new AmazonConfig(), new AmazonCloudWatchClient(awsCredentials), executor, metric).start();
        System.out.println("*****Finished Calling CloudWatch*****");
    }

    private Mono<String> getAuth()
    {
        cloudFoundryClient=
                SpringCloudFoundryClient.builder()
                .host(this.host)
                .username(this.username)
                .password(this.password)
                .skipSslValidation(true)
                .build();

        return cloudFoundryClient.getAccessToken();
    }
}
