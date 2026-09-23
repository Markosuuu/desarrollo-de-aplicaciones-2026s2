package com.prontaentrega;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Punto de entrada de la aplicacion Spring Boot.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class App {
    /**
     * Carga variables locales y levanta el backend.
     */
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry ->
            System.setProperty(entry.getKey(), entry.getValue())
        );
        SpringApplication.run(App.class, args);
    }
}
