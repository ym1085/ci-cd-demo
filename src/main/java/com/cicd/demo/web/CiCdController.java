package com.cicd.demo.web;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class CiCdController {

    @Value("${app.version:1.0.0}")
    private String version;

    @GetMapping("/")
    public ResponseEntity<Response> init(
            @RequestHeader Map<String, String> headerMap,
            @RequestHeader("host") String host) {

        log.info("Hello CICD! version = {}", version);
        log.info("headerMap = {}, host = {}", headerMap, host);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Response.builder()
                        .version(version)
                        .host(host)
                        .build()
                );
    }

    @Data
    @Builder
    public static class Response {
        private final String version;
        private final String host;
    }
}