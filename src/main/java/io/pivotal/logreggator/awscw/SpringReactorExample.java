package io.pivotal.logreggator.awscw;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.pivotal.logreggator.awscw.config.AmazonConfig;
import io.pivotal.logreggator.awscw.model.Metric;
import io.pivotal.logreggator.awscw.service.CloudWatchService;
import io.pivotal.logreggator.awscw.service.DopplerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
public class SpringReactorExample
{
	//CloudWatch
//	@Value("${aws.access-key}")
	static String awsAccessKey="";

//	@Value("${aws.secret-key}")
	static String awsSecretKey="";

	public static void main(String[] args) throws Exception
	{
		SpringApplication.run(SpringReactorExample.class);

		// Firehose
		new DopplerService().tapNozzle();

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

	}
}
