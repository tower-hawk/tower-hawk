package org.towerhawk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.towerhawk.plugin.TowerhawkPluginManager;

@SpringBootApplication
public class Towerhawk {

	public static void main(String[] args) {
		SpringApplication.run(Towerhawk.class, args);
	}
}
