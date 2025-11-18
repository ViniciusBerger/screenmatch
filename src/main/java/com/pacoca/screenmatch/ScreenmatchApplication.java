/*
 * Main file. 
 * 
 * App works based on main method below.
 */
package com.pacoca.screenmatch;

import com.pacoca.screenmatch.services.Service;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {


	// run to start the app
	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}


	// run method works as engine for this app
	@Override
	public void run(String... args) throws Exception {
		Service controller = new Service();

		controller.menu();
	}
}
