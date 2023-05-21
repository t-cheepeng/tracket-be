package com.tcheepeng.tracket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class TracketApplication {

	public static void main(String[] args) {
		SpringApplication.run(TracketApplication.class, args);
	}
}
