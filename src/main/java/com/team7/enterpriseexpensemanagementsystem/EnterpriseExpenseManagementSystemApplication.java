package com.team7.enterpriseexpensemanagementsystem;

import com.team7.enterpriseexpensemanagementsystem.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class EnterpriseExpenseManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EnterpriseExpenseManagementSystemApplication.class, args);
	}

}
