package com.loglens.parser.elasticsearch;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class IndexNameResolver {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public String resolve(String tenantId) {
        return "logs-" + tenantId + "-" + LocalDate.now().format(FMT);
    }
}
