package com.loglens.contracts;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String RAW_LOGS = "raw-logs";
    public static final String PARSED_LOGS = "parsed-logs";
    public static final String EMBEDDING_RESULTS = "embedding-results";
    public static final String ALERT_EVENTS = "alert-events";

    public static String deadLetter(String topic) {
        return topic + ".DLT";
    }
}
