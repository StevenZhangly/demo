package com.example.demo;

import com.example.demo.service.ListService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(DemoApplication.class, args);

		ListService listService = context.getBean(ListService.class);
		System.out.println(context.getEnvironment().getProperty("os.name") + ":" + listService.showListCmd());
	}

}
