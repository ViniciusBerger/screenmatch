package com.pacoca.screenmatch;

import com.pacoca.screenmatch.model.*;
import com.pacoca.screenmatch.services.Controller;
import com.pacoca.screenmatch.services.Converter;
import com.pacoca.screenmatch.services.Service;
import com.sun.tools.javac.Main;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Controller controller = new Controller();

		controller.menu();
	}
}
