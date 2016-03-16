package io.pivotal.logreggator.awscw;

import io.pivotal.logreggator.awscw.service.DopplerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringReactorExample
{
	public static void main(String[] args) throws Exception
	{
		SpringApplication.run(SpringReactorExample.class);

		// Firehose
		new DopplerService().tapNozzle();
	}
}
